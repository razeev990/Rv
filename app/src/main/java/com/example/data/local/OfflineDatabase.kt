package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ReleaseStatus
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchlistItem
import com.example.data.remote.GlobalCinemaSeedCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OfflineMediaEntity(
  val id: String,
  val title: String,
  val mediaType: String,
  val releaseStatus: String,
  val releaseYear: String,
  val releaseDateFull: String,
  val rating: Double,
  val posterUrl: String,
  val backdropUrl: String,
  val overview: String,
  val genresCsv: String,
  val language: String,
  val durationOrSeasons: String,
  val trailerUrl: String,
  val fullMovieUrl: String,
  val castEncoded: String,
  val isSavedOffline: Boolean = false,
  val cachedAt: Long = System.currentTimeMillis()
) {
  fun toMediaItem(): MediaItem {
    val parsedDirector = WatchlistItem.parseDirectorFromSummary(castEncoded).ifBlank {
      GlobalCinemaSeedCatalog.initialCatalog.find { it.id == id }?.director.orEmpty()
    }
    return MediaItem(
      id = id,
      title = title,
      mediaType = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.MOVIE),
      releaseStatus = runCatching { ReleaseStatus.valueOf(releaseStatus) }.getOrDefault(ReleaseStatus.RELEASED),
      releaseYear = releaseYear,
      releaseDateFull = releaseDateFull,
      rating = rating,
      posterUrl = posterUrl,
      backdropUrl = backdropUrl,
      overview = overview,
      genres = genresCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
      language = language,
      durationOrSeasons = durationOrSeasons,
      trailerUrl = trailerUrl,
      fullMovieUrl = fullMovieUrl,
      cast = WatchlistItem.parseCastSummary(castEncoded),
      director = parsedDirector
    )
  }

  companion object {
    fun fromMediaItem(item: MediaItem, isSavedOffline: Boolean = false): OfflineMediaEntity {
      return OfflineMediaEntity(
        id = item.id,
        title = item.title,
        mediaType = item.mediaType.name,
        releaseStatus = item.releaseStatus.name,
        releaseYear = item.releaseYear,
        releaseDateFull = item.releaseDateFull,
        rating = item.rating,
        posterUrl = item.posterUrl,
        backdropUrl = item.backdropUrl,
        overview = item.overview,
        genresCsv = item.genres.joinToString(", "),
        language = item.language,
        durationOrSeasons = item.durationOrSeasons,
        trailerUrl = item.trailerUrl,
        fullMovieUrl = item.fullMovieUrl,
        castEncoded = WatchlistItem.encodeCastSummary(item.cast, director = item.effectiveDirector),
        isSavedOffline = isSavedOffline
      )
    }
  }
}

interface OfflineCatalogDao {
  fun observeAllCachedMedia(): Flow<List<OfflineMediaEntity>>
  fun observeExplicitOfflineSavedMedia(): Flow<List<OfflineMediaEntity>>
  fun observeWatchHistory(): Flow<List<WatchHistoryEntry>>
  suspend fun getAllCachedMediaOnce(): List<OfflineMediaEntity>
  suspend fun upsertAll(items: List<OfflineMediaEntity>)
  suspend fun upsert(item: OfflineMediaEntity)
  suspend fun setOfflineSaved(mediaId: String, saved: Boolean)
  suspend fun upsertWatchHistory(entry: WatchHistoryEntry)
  suspend fun removeWatchHistory(mediaId: String)
  suspend fun clearAllWatchHistory()
}

class CineVaultDatabase private constructor(context: Context) :
  SQLiteOpenHelper(context.applicationContext, "cinevault_offline.db", null, 3) {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
  private val cachedMediaState = MutableStateFlow<List<OfflineMediaEntity>>(emptyList())
  private val watchHistoryState = MutableStateFlow<List<WatchHistoryEntry>>(emptyList())

  private val daoImpl = object : OfflineCatalogDao {
    override fun observeAllCachedMedia(): Flow<List<OfflineMediaEntity>> =
      cachedMediaState.asStateFlow()

    override fun observeExplicitOfflineSavedMedia(): Flow<List<OfflineMediaEntity>> =
      cachedMediaState.map { list -> list.filter { it.isSavedOffline } }

    override fun observeWatchHistory(): Flow<List<WatchHistoryEntry>> =
      watchHistoryState.asStateFlow()

    override suspend fun getAllCachedMediaOnce(): List<OfflineMediaEntity> =
      withContext(Dispatchers.IO) {
        queryAllInternal()
      }

    override suspend fun upsertAll(items: List<OfflineMediaEntity>) {
      withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.beginTransaction()
        try {
          items.forEach { item ->
            db.insertWithOnConflict(
              TABLE_NAME,
              null,
              item.toContentValues(),
              SQLiteDatabase.CONFLICT_REPLACE
            )
          }
          db.setTransactionSuccessful()
        } finally {
          db.endTransaction()
        }
        cachedMediaState.value = queryAllInternal()
      }
    }

    override suspend fun upsert(item: OfflineMediaEntity) {
      withContext(Dispatchers.IO) {
        writableDatabase.insertWithOnConflict(
          TABLE_NAME,
          null,
          item.toContentValues(),
          SQLiteDatabase.CONFLICT_REPLACE
        )
        cachedMediaState.value = queryAllInternal()
      }
    }

    override suspend fun setOfflineSaved(mediaId: String, saved: Boolean) {
      withContext(Dispatchers.IO) {
        val values = ContentValues().apply {
          put("isSavedOffline", if (saved) 1 else 0)
        }
        writableDatabase.update(TABLE_NAME, values, "id = ?", arrayOf(mediaId))
        cachedMediaState.value = queryAllInternal()
      }
    }

    override suspend fun upsertWatchHistory(entry: WatchHistoryEntry) {
      withContext(Dispatchers.IO) {
        ensureHistoryTableExists(writableDatabase)
        writableDatabase.insertWithOnConflict(
          HISTORY_TABLE_NAME,
          null,
          entry.toContentValues(),
          SQLiteDatabase.CONFLICT_REPLACE
        )
        watchHistoryState.value = queryWatchHistoryInternal()
      }
    }

    override suspend fun removeWatchHistory(mediaId: String) {
      withContext(Dispatchers.IO) {
        ensureHistoryTableExists(writableDatabase)
        writableDatabase.delete(HISTORY_TABLE_NAME, "mediaId = ?", arrayOf(mediaId))
        watchHistoryState.value = queryWatchHistoryInternal()
      }
    }

    override suspend fun clearAllWatchHistory() {
      withContext(Dispatchers.IO) {
        ensureHistoryTableExists(writableDatabase)
        writableDatabase.delete(HISTORY_TABLE_NAME, null, null)
        watchHistoryState.value = emptyList()
      }
    }
  }

  init {
    scope.launch {
      cachedMediaState.value = runCatching { queryAllInternal() }.getOrDefault(emptyList())
      val existingHistory = runCatching { queryWatchHistoryInternal() }.getOrDefault(emptyList())
      val hasLegacySeedOnly = existingHistory.size <= 2 && existingHistory.any { it.positionMs == 38_000 }
      if (existingHistory.isEmpty() || hasLegacySeedOnly) {
        // Seed initial Continue Watching items including 40:00 (2,400,000 ms) resume state for Mobile <-> Android TV sync
        val seeds = GlobalCinemaSeedCatalog.initialCatalog
        val firstMovie = seeds.firstOrNull { it.mediaType == MediaType.MOVIE }
        val firstSeries = seeds.firstOrNull { it.mediaType == MediaType.SERIES }
        val now = System.currentTimeMillis()
        if (firstMovie != null) {
          daoImpl.upsertWatchHistory(
            WatchHistoryEntry(
              mediaId = firstMovie.id,
              title = firstMovie.title,
              mediaType = firstMovie.mediaType,
              posterUrl = firstMovie.posterUrl,
              backdropUrl = firstMovie.backdropUrl,
              releaseYear = firstMovie.releaseYear,
              rating = firstMovie.rating,
              positionMs = 2_400_000, // 40 minutes (40:00) resume point
              durationMs = 8_280_000, // 2h 18m (138:00)
              seasonNumber = 1,
              episodeNumber = 1,
              lastWatchedAt = now,
              profileId = "main"
            )
          )
        }
        if (firstSeries != null) {
          daoImpl.upsertWatchHistory(
            WatchHistoryEntry(
              mediaId = firstSeries.id,
              title = firstSeries.title,
              mediaType = firstSeries.mediaType,
              posterUrl = firstSeries.posterUrl,
              backdropUrl = firstSeries.backdropUrl,
              releaseYear = firstSeries.releaseYear,
              rating = firstSeries.rating,
              positionMs = 1_500_000, // 25 minutes (25:00)
              durationMs = 3_000_000, // 50 minutes (50:00)
              seasonNumber = 1,
              episodeNumber = 2,
              lastWatchedAt = now - 60_000L,
              profileId = "main"
            )
          )
        }
      } else {
        watchHistoryState.value = existingHistory
      }
    }
  }

  fun offlineCatalogDao(): OfflineCatalogDao = daoImpl

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS $TABLE_NAME (
        id TEXT PRIMARY KEY NOT NULL,
        title TEXT NOT NULL,
        mediaType TEXT NOT NULL,
        releaseStatus TEXT NOT NULL,
        releaseYear TEXT NOT NULL,
        releaseDateFull TEXT NOT NULL,
        rating REAL NOT NULL,
        posterUrl TEXT NOT NULL,
        backdropUrl TEXT NOT NULL,
        overview TEXT NOT NULL,
        genresCsv TEXT NOT NULL,
        language TEXT NOT NULL,
        durationOrSeasons TEXT NOT NULL,
        trailerUrl TEXT NOT NULL,
        fullMovieUrl TEXT NOT NULL,
        castEncoded TEXT NOT NULL,
        isSavedOffline INTEGER NOT NULL DEFAULT 0,
        cachedAt INTEGER NOT NULL
      )
      """.trimIndent()
    )
    ensureHistoryTableExists(db)
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    onCreate(db)
    runCatching {
      db.execSQL("DELETE FROM $TABLE_NAME WHERE trailerUrl LIKE '%gtv-videos-bucket%' OR posterUrl LIKE '%images.unsplash.com%'")
    }
  }

  private fun ensureHistoryTableExists(db: SQLiteDatabase) {
    db.execSQL(
      """
      CREATE TABLE IF NOT EXISTS $HISTORY_TABLE_NAME (
        mediaId TEXT PRIMARY KEY NOT NULL,
        title TEXT NOT NULL,
        mediaType TEXT NOT NULL,
        posterUrl TEXT NOT NULL,
        backdropUrl TEXT NOT NULL,
        releaseYear TEXT NOT NULL,
        rating REAL NOT NULL,
        positionMs INTEGER NOT NULL,
        durationMs INTEGER NOT NULL,
        seasonNumber INTEGER NOT NULL DEFAULT 1,
        episodeNumber INTEGER NOT NULL DEFAULT 1,
        lastWatchedAt INTEGER NOT NULL
      )
      """.trimIndent()
    )
  }

  private fun queryAllInternal(): List<OfflineMediaEntity> {
    val result = mutableListOf<OfflineMediaEntity>()
    val cursor: Cursor = readableDatabase.query(
      TABLE_NAME,
      null,
      null,
      null,
      null,
      null,
      "cachedAt DESC"
    )
    cursor.use { c ->
      val idIdx = c.getColumnIndexOrThrow("id")
      val titleIdx = c.getColumnIndexOrThrow("title")
      val mediaTypeIdx = c.getColumnIndexOrThrow("mediaType")
      val releaseStatusIdx = c.getColumnIndexOrThrow("releaseStatus")
      val releaseYearIdx = c.getColumnIndexOrThrow("releaseYear")
      val releaseDateFullIdx = c.getColumnIndexOrThrow("releaseDateFull")
      val ratingIdx = c.getColumnIndexOrThrow("rating")
      val posterUrlIdx = c.getColumnIndexOrThrow("posterUrl")
      val backdropUrlIdx = c.getColumnIndexOrThrow("backdropUrl")
      val overviewIdx = c.getColumnIndexOrThrow("overview")
      val genresCsvIdx = c.getColumnIndexOrThrow("genresCsv")
      val languageIdx = c.getColumnIndexOrThrow("language")
      val durationOrSeasonsIdx = c.getColumnIndexOrThrow("durationOrSeasons")
      val trailerUrlIdx = c.getColumnIndexOrThrow("trailerUrl")
      val fullMovieUrlIdx = c.getColumnIndexOrThrow("fullMovieUrl")
      val castEncodedIdx = c.getColumnIndexOrThrow("castEncoded")
      val isSavedOfflineIdx = c.getColumnIndexOrThrow("isSavedOffline")
      val cachedAtIdx = c.getColumnIndexOrThrow("cachedAt")

      while (c.moveToNext()) {
        result.add(
          OfflineMediaEntity(
            id = c.getString(idIdx),
            title = c.getString(titleIdx),
            mediaType = c.getString(mediaTypeIdx),
            releaseStatus = c.getString(releaseStatusIdx),
            releaseYear = c.getString(releaseYearIdx),
            releaseDateFull = c.getString(releaseDateFullIdx),
            rating = c.getDouble(ratingIdx),
            posterUrl = c.getString(posterUrlIdx),
            backdropUrl = c.getString(backdropUrlIdx),
            overview = c.getString(overviewIdx),
            genresCsv = c.getString(genresCsvIdx),
            language = c.getString(languageIdx),
            durationOrSeasons = c.getString(durationOrSeasonsIdx),
            trailerUrl = c.getString(trailerUrlIdx),
            fullMovieUrl = c.getString(fullMovieUrlIdx),
            castEncoded = c.getString(castEncodedIdx),
            isSavedOffline = c.getInt(isSavedOfflineIdx) == 1,
            cachedAt = c.getLong(cachedAtIdx)
          )
        )
      }
    }
    return result
  }

  private fun queryWatchHistoryInternal(): List<WatchHistoryEntry> {
    ensureHistoryTableExists(writableDatabase)
    val result = mutableListOf<WatchHistoryEntry>()
    val cursor: Cursor = readableDatabase.query(
      HISTORY_TABLE_NAME,
      null,
      null,
      null,
      null,
      null,
      "lastWatchedAt DESC"
    )
    cursor.use { c ->
      val mediaIdIdx = c.getColumnIndexOrThrow("mediaId")
      val titleIdx = c.getColumnIndexOrThrow("title")
      val mediaTypeIdx = c.getColumnIndexOrThrow("mediaType")
      val posterUrlIdx = c.getColumnIndexOrThrow("posterUrl")
      val backdropUrlIdx = c.getColumnIndexOrThrow("backdropUrl")
      val releaseYearIdx = c.getColumnIndexOrThrow("releaseYear")
      val ratingIdx = c.getColumnIndexOrThrow("rating")
      val positionMsIdx = c.getColumnIndexOrThrow("positionMs")
      val durationMsIdx = c.getColumnIndexOrThrow("durationMs")
      val seasonNumberIdx = c.getColumnIndexOrThrow("seasonNumber")
      val episodeNumberIdx = c.getColumnIndexOrThrow("episodeNumber")
      val lastWatchedAtIdx = c.getColumnIndexOrThrow("lastWatchedAt")

      while (c.moveToNext()) {
        result.add(
          WatchHistoryEntry(
            mediaId = c.getString(mediaIdIdx),
            title = c.getString(titleIdx),
            mediaType = runCatching { MediaType.valueOf(c.getString(mediaTypeIdx)) }.getOrDefault(MediaType.MOVIE),
            posterUrl = c.getString(posterUrlIdx),
            backdropUrl = c.getString(backdropUrlIdx),
            releaseYear = c.getString(releaseYearIdx),
            rating = c.getDouble(ratingIdx),
            positionMs = c.getInt(positionMsIdx),
            durationMs = c.getInt(durationMsIdx),
            seasonNumber = c.getInt(seasonNumberIdx),
            episodeNumber = c.getInt(episodeNumberIdx),
            lastWatchedAt = c.getLong(lastWatchedAtIdx)
          )
        )
      }
    }
    return result
  }

  private fun OfflineMediaEntity.toContentValues(): ContentValues {
    return ContentValues().apply {
      put("id", id)
      put("title", title)
      put("mediaType", mediaType)
      put("releaseStatus", releaseStatus)
      put("releaseYear", releaseYear)
      put("releaseDateFull", releaseDateFull)
      put("rating", rating)
      put("posterUrl", posterUrl)
      put("backdropUrl", backdropUrl)
      put("overview", overview)
      put("genresCsv", genresCsv)
      put("language", language)
      put("durationOrSeasons", durationOrSeasons)
      put("trailerUrl", trailerUrl)
      put("fullMovieUrl", fullMovieUrl)
      put("castEncoded", castEncoded)
      put("isSavedOffline", if (isSavedOffline) 1 else 0)
      put("cachedAt", cachedAt)
    }
  }

  private fun WatchHistoryEntry.toContentValues(): ContentValues {
    return ContentValues().apply {
      put("mediaId", mediaId)
      put("title", title)
      put("mediaType", mediaType.name)
      put("posterUrl", posterUrl)
      put("backdropUrl", backdropUrl)
      put("releaseYear", releaseYear)
      put("rating", rating)
      put("positionMs", positionMs)
      put("durationMs", durationMs)
      put("seasonNumber", seasonNumber)
      put("episodeNumber", episodeNumber)
      put("lastWatchedAt", lastWatchedAt)
    }
  }

  companion object {
    private const val TABLE_NAME = "offline_catalog"
    private const val HISTORY_TABLE_NAME = "watch_history"

    @Volatile
    private var INSTANCE: CineVaultDatabase? = null

    fun getInstance(context: Context): CineVaultDatabase {
      return INSTANCE ?: synchronized(this) {
        INSTANCE ?: CineVaultDatabase(context).also { INSTANCE = it }
      }
    }
  }
}
