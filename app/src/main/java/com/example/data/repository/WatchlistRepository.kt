package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.MediaItem
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class WatchlistRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    ),
    Firebase.auth
  )

  private fun requireUserId(): String {
    return auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  private fun sanitizeId(rawId: String): String {
    val cleaned = rawId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
    return cleaned.ifBlank { "media_default" }
  }

  fun observeWatchlist(userId: String? = null): Flow<List<WatchlistItem>> = flow {
    val uid = userId ?: auth.currentUser?.uid ?: "unauthenticated_uid"
    val path = "users/$uid/watchlist"
    emitAll(
      db.collection("users")
        .document(uid)
        .collection("watchlist")
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            WatchlistItem::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
          )
        }
        .catch { error ->
          if (error is Exception) {
            handleFirestoreError(error, OperationType.LIST, path)
          }
          throw error
        }
    )
  }

  suspend fun getWatchlistByUser(userId: String = requireUserId()): Result<List<WatchlistItem>> {
    val path = "users/$userId/watchlist"
    return try {
      val snapshot = db.collection("users")
        .document(userId)
        .collection("watchlist")
        .get()
        .await()
      val items = snapshot.toObjects(
        WatchlistItem::class.java,
        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
      )
      Result.success(items)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.LIST, path)
      Result.failure(e)
    }
  }

  suspend fun getWatchlistItemById(
    mediaId: String,
    targetUserId: String = requireUserId()
  ): Result<WatchlistItem?> {
    val safeId = sanitizeId(mediaId)
    val docRef = db.collection("users")
      .document(targetUserId)
      .collection("watchlist")
      .document(safeId)
    return try {
      val snapshot = docRef.get().await()
      val item = snapshot.toObject(
        WatchlistItem::class.java,
        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
      )
      Result.success(item)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.GET, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun saveToWatchlist(
    media: MediaItem,
    status: WatchStatus,
    userNotes: String? = null,
    userRating: Int? = null,
    progress: WatchlistItem.CloudPlaybackProgress? = null,
    profileId: String = "main",
    isFavorite: Boolean = false,
    customTags: List<String> = emptyList(),
    storageSource: String = "CineVault Cloud",
    isTraktSynced: Boolean = true
  ): Result<String> {
    val uid = requireUserId()
    val safeId = sanitizeId(WatchlistItem.buildProfileScopedDocId(media.id, profileId))
    val docRef = db.collection("users")
      .document(uid)
      .collection("watchlist")
      .document(safeId)

    return try {
      val baseMap = mutableMapOf<String, Any>(
        "userId" to uid,
        "mediaId" to safeId,
        "title" to media.title.take(300).ifBlank { "Untitled" },
        "mediaType" to media.mediaType.name,
        "status" to status.name,
        "releaseStatus" to media.releaseStatus.name,
        "releaseYear" to media.releaseYear.take(20).ifBlank { "2025" },
        "rating" to media.rating.coerceIn(0.0, 10.0),
        "posterUrl" to media.posterUrl.take(1000),
        "overview" to media.overview.take(4000),
        "genres" to media.genres.joinToString(", ").take(300),
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )

      if (media.backdropUrl.isNotBlank()) {
        baseMap["backdropUrl"] = media.backdropUrl.take(1000)
      }
      if (media.trailerUrl.isNotBlank()) {
        baseMap["trailerUrl"] = media.trailerUrl.take(1000)
      }
      if (media.fullMovieUrl.isNotBlank()) {
        baseMap["fullMovieUrl"] = media.fullMovieUrl.take(1000)
      }
      val encodedCast = WatchlistItem.encodeCastSummary(
        cast = media.cast,
        progress = progress,
        director = media.effectiveDirector,
        profileId = profileId,
        isFavorite = isFavorite,
        customTags = customTags,
        storageSource = storageSource,
        isTraktSynced = isTraktSynced
      )
      if (encodedCast.isNotBlank()) {
        baseMap["castSummary"] = encodedCast
      }
      if (!userNotes.isNullOrBlank()) {
        baseMap["userNotes"] = userNotes.take(1000)
      }
      if (userRating != null) {
        baseMap["userRating"] = userRating.coerceIn(0, 10)
      }

      docRef.set(baseMap).await()
      Result.success(safeId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateWatchStatus(
    mediaId: String,
    newStatus: WatchStatus,
    userNotes: String? = null,
    userRating: Int? = null,
    castSummaryWithProgress: String? = null
  ): Result<Unit> {
    val uid = requireUserId()
    val safeId = sanitizeId(mediaId)
    val docRef = db.collection("users")
      .document(uid)
      .collection("watchlist")
      .document(safeId)

    return try {
      val updates = mutableMapOf<String, Any>(
        "status" to newStatus.name,
        "updatedAt" to FieldValue.serverTimestamp()
      )
      if (userNotes != null) {
        updates["userNotes"] = userNotes.take(1000)
      }
      if (userRating != null) {
        updates["userRating"] = userRating.coerceIn(0, 10)
      }
      if (!castSummaryWithProgress.isNullOrBlank()) {
        updates["castSummary"] = castSummaryWithProgress.take(4900)
      }
      docRef.update(updates).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun syncPlaybackProgressToCloud(
    media: MediaItem,
    positionMs: Int,
    durationMs: Int,
    seasonNumber: Int,
    episodeNumber: Int,
    existingItem: WatchlistItem? = null,
    profileId: String = "main"
  ): Result<Unit> {
    val uid = requireUserId()
    val safeId = sanitizeId(WatchlistItem.buildProfileScopedDocId(media.id, profileId))
    val progress = WatchlistItem.CloudPlaybackProgress(
      positionMs = positionMs.coerceAtLeast(1000),
      durationMs = durationMs.coerceAtLeast(30_000),
      seasonNumber = seasonNumber.coerceAtLeast(1),
      episodeNumber = episodeNumber.coerceAtLeast(1),
      lastWatchedAt = System.currentTimeMillis()
    )
    val castToEncode = if (media.cast.isNotEmpty()) {
      media.cast
    } else {
      WatchlistItem.parseCastSummary(existingItem?.castSummary)
    }
    val isFav = existingItem?.isFavorite ?: false
    val existingTags = existingItem?.customTags ?: emptyList()
    val existingStorage = existingItem?.storageSource ?: "CineVault Cloud"
    val existingTrakt = existingItem?.isTraktSynced ?: true
    val encodedCastAndProgress = WatchlistItem.encodeCastSummary(
      cast = castToEncode,
      progress = progress,
      director = media.effectiveDirector,
      profileId = profileId,
      isFavorite = isFav,
      customTags = existingTags,
      storageSource = existingStorage,
      isTraktSynced = existingTrakt
    )

    return if (existingItem != null) {
      updateWatchStatus(
        mediaId = safeId,
        newStatus = existingItem.parsedWatchStatus,
        userNotes = existingItem.userNotes,
        userRating = existingItem.userRating?.toInt(),
        castSummaryWithProgress = encodedCastAndProgress
      )
    } else {
      saveToWatchlist(
        media = media,
        status = WatchStatus.WANT_TO_WATCH,
        progress = progress,
        profileId = profileId,
        isFavorite = isFav,
        customTags = existingTags,
        storageSource = existingStorage,
        isTraktSynced = existingTrakt
      ).map { Unit }
    }
  }

  suspend fun removeFromWatchlist(mediaId: String): Result<Unit> {
    val uid = requireUserId()
    val safeId = sanitizeId(mediaId)
    val docRef = db.collection("users")
      .document(uid)
      .collection("watchlist")
      .document(safeId)
    return try {
      docRef.delete().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, docRef.path)
      Result.failure(e)
    }
  }
}
