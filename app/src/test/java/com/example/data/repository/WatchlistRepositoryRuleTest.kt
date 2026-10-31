package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ReleaseStatus
import com.example.data.model.WatchStatus
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class WatchlistRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private fun createSampleMedia(id: String): MediaItem {
    return MediaItem(
      id = id,
      title = "Interstellar",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2014",
      rating = 8.7,
      posterUrl = "https://image.tmdb.org/t/p/w500/interstellar.jpg",
      backdropUrl = "https://image.tmdb.org/t/p/w780/interstellar_bg.jpg",
      overview = "A team of explorers travel through a wormhole in space.",
      genres = listOf("Sci-Fi", "Drama", "Adventure"),
      trailerUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
      fullMovieUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
      cast = listOf(
        CastMember(
          id = "actor_1",
          name = "Matthew McConaughey",
          characterName = "Cooper",
          profileUrl = "https://image.tmdb.org/t/p/w185/matthew.jpg"
        )
      )
    )
  }

  @Test
  fun saveToWatchlist_andUpdateWatchStatus_succeedsForOwner() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = WatchlistRepository(firestore, auth)
    val mediaId = "movie_${UUID.randomUUID().toString().replace("-", "_")}"
    val sampleMedia = createSampleMedia(mediaId)

    val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveToWatchlist(
        media = sampleMedia,
        status = WatchStatus.WANT_TO_WATCH,
        userNotes = "Watch this weekend",
        userRating = 9
      )
    }
    assertTrue("Expected saveToWatchlist to succeed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)

    val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.updateWatchStatus(
        mediaId = mediaId,
        newStatus = WatchStatus.WATCHED,
        userNotes = "Masterpiece!",
        userRating = 10
      )
    }
    assertTrue("Expected updateWatchStatus to succeed: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

    val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.getWatchlistItemById(mediaId, aliceUid).getOrThrow()
    }
    assertNotNull(fetched)
    assertEquals(WatchStatus.WATCHED.name, fetched?.status)
    assertEquals(10L, fetched?.userRating)
  }

  @Test
  fun observeWatchlist_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val repository = WatchlistRepository(firestore, auth)
    val mediaId = "movie_${UUID.randomUUID().toString().replace("-", "_")}"
    val sampleMedia = createSampleMedia(mediaId)

    withTimeout(DEFAULT_TIMEOUT_MS) {
      repository.saveToWatchlist(sampleMedia, WatchStatus.WANT_TO_WATCH).getOrThrow()
    }

    val emittedItems = withTimeout(FLOW_TIMEOUT_MS) {
      repository.observeWatchlist(aliceUid).first { list -> list.any { it.mediaId == mediaId } }
    }
    assertTrue(emittedItems.any { it.mediaId == mediaId })
  }

  @Test
  fun getWatchlistItemById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser(ALICE_EMAIL)
    val aliceRepo = WatchlistRepository(firestore, auth)
    val mediaId = "movie_${UUID.randomUUID().toString().replace("-", "_")}"
    withTimeout(DEFAULT_TIMEOUT_MS) {
      aliceRepo.saveToWatchlist(createSampleMedia(mediaId), WatchStatus.WATCHED).getOrThrow()
    }

    signInTestUser(BOB_EMAIL)
    val bobRepo = WatchlistRepository(firestore, auth)
    val crossUserResult = withTimeout(DEFAULT_TIMEOUT_MS) {
      bobRepo.getWatchlistItemById(mediaId, targetUserId = aliceUid)
    }
    assertTrue(crossUserResult.isFailure)
    val firestoreEx = crossUserResult.exceptionOrNull()?.unwrapFirestoreException()
    assertNotNull("Expected FirebaseFirestoreException", firestoreEx)
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
  }

  @Test
  fun observeWatchlist_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repository = WatchlistRepository(firestore, auth)

    try {
      withTimeout(FLOW_TIMEOUT_MS) {
        repository.observeWatchlist("some_user_123").first()
      }
      fail("Expected unauthenticated observeWatchlist to throw exception")
    } catch (e: Exception) {
      val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
        "Expected nested FirebaseFirestoreException, got $e"
      }
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
    }
  }

  private companion object {
    const val ALICE_EMAIL = "alice@cinevault.test"
    const val BOB_EMAIL = "bob@cinevault.test"
    const val DEFAULT_TIMEOUT_MS = 5000L
    const val FLOW_TIMEOUT_MS = 3000L
  }
}
