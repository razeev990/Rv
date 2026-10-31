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

  @Test
  fun tmdbIdExtraction_andServerUrls_handlePrefixesAndCuratedIds() {
    // 1. Prefix stripping
    assertEquals("1596275", com.example.ui.components.extractNumericTmdbId("tmdb_movie_1596275"))
    assertEquals("1022789", com.example.ui.components.extractNumericTmdbId("tmdb_tv_1022789"))
    assertEquals("533535", com.example.ui.components.extractNumericTmdbId("movie_533535"))
    assertEquals("1596275", com.example.ui.components.extractNumericTmdbId("1596275"))
    // IMDb IDs should not be treated as numeric TMDB ID
    assertEquals("", com.example.ui.components.extractNumericTmdbId("tt15398776"))

    // 2. resolveTmdbId resolves prefixes and curated titles like Dunki
    assertEquals("1596275", com.example.ui.components.resolveTmdbId("tmdb_movie_1596275", "Custom Film"))
    assertEquals("1072790", com.example.ui.components.resolveTmdbId("curated_dunki", "Dunki"))
    assertEquals("872906", com.example.ui.components.resolveTmdbId("curated_jawan", "Jawan"))
    assertEquals("838209", com.example.ui.components.resolveTmdbId("curated_pushpa_2", "Pushpa 2: The Rule"))

    // 3. Test all 6 servers outputting clean URLs
    val s1 = com.example.ui.components.buildStreamingServerUrl(
      server = com.example.ui.components.PlayerStreamMode.FULL_MOVIE_SERVER_1,
      tmdbId = "tmdb_movie_1596275",
      imdbId = "",
      isSeries = false,
      season = 1,
      episode = 1
    )
    assertEquals("https://vidsrc.mov/embed/movie/1596275", s1)

    val s2 = com.example.ui.components.buildStreamingServerUrl(
      server = com.example.ui.components.PlayerStreamMode.FULL_MOVIE_SERVER_2,
      tmdbId = "tmdb_movie_1596275",
      imdbId = "",
      isSeries = false,
      season = 1,
      episode = 1
    )
    assertEquals("https://autoembed.cc/embed/movie/1596275", s2)

    val s3 = com.example.ui.components.buildStreamingServerUrl(
      server = com.example.ui.components.PlayerStreamMode.FULL_MOVIE_SERVER_3,
      tmdbId = "tmdb_movie_1596275",
      imdbId = "",
      isSeries = false,
      season = 1,
      episode = 1
    )
    assertEquals("https://superembed.stream/embed/movie/?id=1596275", s3)

    val s4 = com.example.ui.components.buildStreamingServerUrl(
      server = com.example.ui.components.PlayerStreamMode.FULL_MOVIE_SERVER_4,
      tmdbId = "tmdb_movie_1596275",
      imdbId = "",
      isSeries = false,
      season = 1,
      episode = 1
    )
    assertEquals("https://vidsrc.me/embed/movie?tmdb=1596275", s4)

    val s5 = com.example.ui.components.buildStreamingServerUrl(
      server = com.example.ui.components.PlayerStreamMode.FULL_MOVIE_SERVER_5,
      tmdbId = "1596275",
      imdbId = "",
      isSeries = false,
      season = 1,
      episode = 1,
      youtubeVideoId = "ACKQDAlAfFE"
    )
    assertEquals("https://www.youtube.com/embed/ACKQDAlAfFE", s5)

    val s6 = com.example.ui.components.buildStreamingServerUrl(
      server = com.example.ui.components.PlayerStreamMode.FULL_MOVIE_SERVER_6,
      tmdbId = "1596275",
      imdbId = "",
      isSeries = false,
      season = 1,
      episode = 1,
      youtubeVideoId = "ACKQDAlAfFE"
    )
    assertEquals("https://youtu.be/ACKQDAlAfFE", s6)
  }
}
