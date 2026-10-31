package com.example

import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ReleaseStatus
import com.example.data.model.SmartDownloadQuality
import com.example.data.model.SmartDownloadState
import com.example.data.model.SmartDownloadTask
import com.example.data.model.SmartSearchMatcher
import com.example.data.model.WatchlistItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun watchlistMetadataEncoding_preservesTagsStorageAndTraktState() {
    val encoded = WatchlistItem.encodeCastSummary(
      cast = listOf(
        CastMember(
          id = "c1",
          name = "Prabhas",
          characterName = "Bhairava",
          profileUrl = ""
        )
      ),
      progress = WatchlistItem.CloudPlaybackProgress(
        positionMs = 2_400_000,
        durationMs = 8_280_000,
        seasonNumber = 1,
        episodeNumber = 1,
        lastWatchedAt = 1700000000L
      ),
      director = "Nag Ashwin",
      profileId = "family",
      isFavorite = true,
      customTags = listOf("Weekend Watch", "Sci-Fi Vault"),
      storageSource = "Google Drive Stream",
      isTraktSynced = true
    )

    val item = WatchlistItem(
      id = "family__kalki",
      mediaId = "family__kalki",
      title = "Kalki 2898 AD",
      castSummary = encoded
    )

    assertEquals("family", item.profileId)
    assertTrue(item.isFavorite)
    assertTrue(item.customTags.contains("Weekend Watch"))
    assertTrue(item.customTags.contains("Sci-Fi Vault"))
    assertTrue(item.customTags.contains("Favorites"))
    assertEquals("Google Drive Stream", item.storageSource)
    assertTrue(item.isTraktSynced)
    assertEquals(2_400_000, item.toWatchHistoryEntry()?.positionMs)
  }

  @Test
  fun smartSearchAndDownloadTask_calculatesCorrectly() {
    val sampleMovie = MediaItem(
      id = "movie_veer_zaara",
      title = "Veer-Zaara",
      mediaType = MediaType.MOVIE,
      releaseStatus = ReleaseStatus.RELEASED,
      releaseYear = "2004",
      rating = 8.5,
      posterUrl = "",
      backdropUrl = "",
      overview = "An epic romantic drama starring Shah Rukh Khan.",
      genres = listOf("Romance", "Drama"),
      trailerUrl = "",
      fullMovieUrl = "",
      director = "Yash Chopra"
    )
    val eval = SmartSearchMatcher.evaluate(sampleMovie, "romantic dramas")
    assertTrue(eval.score > 0)

    val task = SmartDownloadTask(
      media = sampleMovie,
      quality = SmartDownloadQuality.FULL_HD_1080P,
      progressFraction = 0.5f,
      state = SmartDownloadState.PAUSED
    )
    assertEquals(710, task.downloadedMb)
  }
}
