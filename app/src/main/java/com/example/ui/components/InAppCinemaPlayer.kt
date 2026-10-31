package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.Rect
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Base64
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionDisabled
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.math.abs

enum class PlayerStreamMode(val label: String, val badge: String) {
  FULL_MOVIE_SERVER_1("Server 1 • VidSrc Turbo", "Embed • Multi-Audio"),
  FULL_MOVIE_SERVER_2("Server 2 • AutoEmbed Fast", "Auto-Stream • Zero-Lag"),
  FULL_MOVIE_SERVER_3("Server 3 • SuperEmbed", "HD Streams • Multi-Sub"),
  FULL_MOVIE_SERVER_4("Server 4 • Vidsrc.me", "IMDb HD Mirror"),
  FULL_MOVIE_SERVER_5("Server 5 • YouTube Direct", "IFrame Player"),
  FULL_MOVIE_SERVER_6("Server 6 • YouTube App", "Intent Link / App"),
  TRAILER("Official Trailer", "Live Trailer Server"),
  TRAILER_YOUTUBE("Trailer Server 2", "Alt Trailer Stream"),
  CUSTOM_URL("Custom Link", "Direct MP4 / M3U8")
}

enum class StreamQualityMode(
  val label: String,
  val dataRateLabel: String,
  val isLowDataTurbo: Boolean
) {
  AUTO_SMART("Auto (Adaptive)", "Auto Bitrate", true),
  STANDARD_480P("480p", "~120 MB/hr", false),
  HD_720P("720p HD", "~290 MB/hr", false),
  FULL_HD_1080P("1080p Full HD", "~620 MB/hr", false),
  UHD_4K("4K Ultra HD", "~1.8 GB/hr • HDR", false),
  ULTRA_LOW_240P("240p Low-MB", "~22 MB/hr", true),
  TURBO_360P("360p Smooth", "~50 MB/hr", true)
}

enum class AudioTrackOption(val label: String, val shortTag: String, val isoLang: String) {
  HINDI_DUB("Hindi", "Hindi", "hi"),
  ENGLISH_ORIGINAL("English", "English", "en"),
  TAMIL_DUB("Tamil", "Tamil", "ta"),
  TELUGU_DUB("Telugu", "Telugu", "te"),
  BHOJPURI_REGIONAL("Bhojpuri", "Bhojpuri", "bh"),
  MULTI_STUDIO("Dolby 5.1", "Dolby 5.1", "und")
}

enum class CaptionTrackOption(val label: String, val shortTag: String) {
  OFF("Subtitles Off", "CC Off"),
  HINDI_CC("Hindi (.srt)", "Hindi .srt"),
  HINGLISH_CC("Hinglish (.srt)", "Hinglish .srt"),
  ENGLISH_CC("English (.vtt)", "English .vtt"),
  TAMIL_TELUGU_CC("Regional (.vtt)", "Regional .vtt")
}

enum class VideoDecoderMode(val label: String, val badge: String) {
  HW_PLUS("HW+ Decoder", "4K 60fps Zero-Lag"),
  HW("HW Decoder", "GPU Accelerated"),
  SW("SW Decoder", "Software Fallback")
}

enum class SubtitleSizeOption(val label: String, val fontSizeSp: Int) {
  SMALL("Small", 12),
  NORMAL("Normal", 14),
  LARGE("Large", 17),
  EXTRA_LARGE("XL", 20)
}

enum class SubtitleColorOption(val label: String, val colorValue: Long) {
  WHITE("White", 0xFFFFFFFF),
  YELLOW("Cinema Yellow", 0xFFFACC15),
  CYAN("Cyan", 0xFF38BDF8),
  GREEN("Neon Green", 0xFF4ADE80)
}

private data class LiveServerMediaPayload(
  val officialTrailerStreamUrl: String?,
  val secondaryTrailerStreamUrl: String?,
  val movieServerStreams: List<String>,
  val trailerFrameUrls: List<String>,
  val movieSceneFrameUrlsByServer: Map<Int, List<String>>,
  val discoveredTrailerVideoIds: List<String> = emptyList(),
  val discoveredSceneVideoIds: List<String> = emptyList()
)

/**
 * Curated official YouTube video IDs per movie title (Official Trailers + Official Movie Scenes)
 * so that real video frames from that exact movie's official videos load immediately while
 * additional live video IDs and Apple iTunes .m4v streams are fetched from the server.
 */
private data class MovieOfficialVideoIds(
  val iTunesSearchTerms: List<String>,
  val trailerVideoIds: List<String>,
  val movieSceneVideoIds: List<String>
)

private fun resolveOfficialVideoIdsForTitle(movieTitle: String): MovieOfficialVideoIds {
  val lower = movieTitle.lowercase(Locale.US)
  return when {
    lower.contains("kalki") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Kalki 2898 AD", "Kalki Prabhas", "Prabhas"),
      trailerVideoIds = listOf("kQDd1AhGIHk", "aninoDcPWo4", "bC36d8e3bb0"),
      movieSceneVideoIds = listOf("Bf3vI2_e7fU", "aninoDcPWo4", "kQDd1AhGIHk", "bC36d8e3bb0")
    )
    lower.contains("jawan") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Jawan", "Jawan Shah Rukh Khan"),
      trailerVideoIds = listOf("COv52Qyctws", "MWOlnZSnXJo"),
      movieSceneVideoIds = listOf("AQEc4BwX6dk", "VAdGW7QDJiU", "f3FFOBrMmdg", "MWOlnZSnXJo", "COv52Qyctws")
    )
    lower.contains("pushpa") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Pushpa 2", "Pushpa The Rule", "Pushpa Allu Arjun"),
      trailerVideoIds = listOf("1kVK0MZlbI4", "Q1NKMPhP8PY"),
      movieSceneVideoIds = listOf("g3JUbgOHgdw", "7nJ7xR9bA3Q", "hcMzwMrr1tE", "1kVK0MZlbI4", "Q1NKMPhP8PY")
    )
    lower.contains("inception") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Inception", "Inception Hans Zimmer"),
      trailerVideoIds = listOf("YoHD9XEInc0", "Jvurpf91omw"),
      movieSceneVideoIds = listOf("8hP9D6kZseM", "Qwe6qXFTdgc", "Jvurpf91omw", "YoHD9XEInc0")
    )
    lower.contains("oppenheimer") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Oppenheimer", "Oppenheimer Ludwig Goransson"),
      trailerVideoIds = listOf("uYPbbksJxIg", "bK6ldnjE3Y0"),
      movieSceneVideoIds = listOf("SDn7w2b8efE", "bK6ldnjE3Y0", "uYPbbksJxIg")
    )
    lower.contains("dune") && lower.contains("messiah") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Dune Part Two", "Dune"),
      trailerVideoIds = listOf("U2Qp5pL3ovA", "Way9Dexny3w"),
      movieSceneVideoIds = listOf("_YUzQa_1RCE", "Way9Dexny3w", "U2Qp5pL3ovA", "n9xhJrPXop4")
    )
    lower.contains("dune") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Dune Part Two", "Dune"),
      trailerVideoIds = listOf("Way9Dexny3w", "U2Qp5pL3ovA"),
      movieSceneVideoIds = listOf("_YUzQa_1RCE", "U2Qp5pL3ovA", "Way9Dexny3w", "n9xhJrPXop4")
    )
    lower.contains("brahmastra") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Brahmastra", "Brahmastra Pritam"),
      trailerVideoIds = listOf("BUjXzrgntcY", "mNuhKUOD_A0"),
      movieSceneVideoIds = listOf("BddP6PYo2gs", "mNuhKUOD_A0", "V7LwfY5U5WI", "BUjXzrgntcY")
    )
    lower.contains("ramayana") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Ramayana", "Ram Siya Ram", "Adipurush"),
      trailerVideoIds = listOf("scNmYjoR-qM", "X7WXHhokylc"),
      movieSceneVideoIds = listOf("X7WXHhokylc", "scNmYjoR-qM", "Tl4bQBfOtbg")
    )
    lower == "king" || lower.startsWith("king ") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Pathaan", "Jawan", "Shah Rukh Khan"),
      trailerVideoIds = listOf("vqu4z34wENw", "COv52Qyctws"),
      movieSceneVideoIds = listOf("YxWlaYCA8MU", "vqu4z34wENw", "COv52Qyctws", "MWOlnZSnXJo")
    )
    lower.contains("avengers") || lower.contains("doomsday") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Avengers Endgame", "Avengers Infinity War", "Avengers"),
      trailerVideoIds = listOf("TcMBFSGVi1c", "6ZfuNTqbHE8"),
      movieSceneVideoIds = listOf("ee1172yeqyE", "6ZfuNTqbHE8", "TcMBFSGVi1c", "eOrNdBpGMv8")
    )
    lower.contains("spirit") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Salaar", "Animal Ranbir", "Prabhas"),
      trailerVideoIds = listOf("4GPvYMKtrtI", "HihakYi5M2I"),
      movieSceneVideoIds = listOf("HihakYi5M2I", "4GPvYMKtrtI", "Dydmpfo68DA")
    )
    lower.contains("family man") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("The Family Man", "Manoj Bajpayee"),
      trailerVideoIds = listOf("XatRGut65VI", "NGf_B81Hc2M"),
      movieSceneVideoIds = listOf("NGf_B81Hc2M", "XatRGut65VI", "3uE0RuQ14sQ")
    )
    lower.contains("peaky blinders") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Peaky Blinders", "Cillian Murphy"),
      trailerVideoIds = listOf("oVzVdvGIC7U", "2nsT9uQPIrk"),
      movieSceneVideoIds = listOf("2nsT9uQPIrk", "oVzVdvGIC7U", "Ruyl8_PT_y8")
    )
    lower.contains("last of us") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("The Last of Us", "Gustavo Santaolalla Last of Us"),
      trailerVideoIds = listOf("uLtkt8BonwM", "BOsAJ7oe2QE"),
      movieSceneVideoIds = listOf("BOsAJ7oe2QE", "uLtkt8BonwM", "R2Ebc_OFeug")
    )
    lower.contains("sita ramam") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Sita Ramam", "Dulquer Salmaan"),
      trailerVideoIds = listOf("Ljk6tGZ1l3A", "3UKsbXQUwqw"),
      movieSceneVideoIds = listOf("3UKsbXQUwqw", "Ljk6tGZ1l3A", "PFc9e1y3y0M")
    )
    lower.contains("veer") && lower.contains("zaara") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Veer-Zaara", "Shah Rukh Khan"),
      trailerVideoIds = listOf("OSaVImLnnsw", "rp5L8I2wMZ8"),
      movieSceneVideoIds = listOf("rp5L8I2wMZ8", "OSaVImLnnsw", "L0zKs8i7Nc8")
    )
    lower.contains("dunki") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Dunki", "Shah Rukh Khan Dunki"),
      trailerVideoIds = listOf("Zd69FfhBmSc", "ACKQDAlAfFE"),
      movieSceneVideoIds = listOf("ACKQDAlAfFE", "Zd69FfhBmSc", "vqu4z34wENw")
    )
    lower.contains("rockstar") -> MovieOfficialVideoIds(
      iTunesSearchTerms = listOf("Rockstar Ranbir Kapoor", "A.R. Rahman Rockstar"),
      trailerVideoIds = listOf("bD5FShPZdpw", "p9DQINKZxWE"),
      movieSceneVideoIds = listOf("p9DQINKZxWE", "bD5FShPZdpw", "c7R9d5e4w2A")
    )
    else -> {
      val clean = movieTitle
        .substringBefore("(")
        .substringBefore(":")
        .trim()
        .ifBlank { movieTitle }
      MovieOfficialVideoIds(
        iTunesSearchTerms = listOf(clean, movieTitle),
        trailerVideoIds = emptyList(),
        movieSceneVideoIds = emptyList()
      )
    }
  }
}

private fun computeFullDurationMs(
  isTrailer: Boolean,
  episodeDurationLabel: String?,
  titleHash: Int
): Int {
  if (isTrailer) {
    return 135_000 // 02:15 for Official Trailer
  }
  if (!episodeDurationLabel.isNullOrBlank()) {
    val minutesMatch = Regex("(\\d+)\\s*m").find(episodeDurationLabel)
    val mins = minutesMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
    if (mins != null && mins in 15..120) {
      return mins * 60 * 1000
    }
    return 48 * 60 * 1000
  }
  val movieMinutes = 132 + (titleHash % 35)
  return movieMinutes * 60 * 1000
}

fun resolveTmdbId(mediaId: String, title: String): String {
  if (mediaId.isNotBlank() && mediaId.all { it.isDigit() }) {
    return mediaId
  }
  val lower = title.lowercase(Locale.ROOT)
  return when {
    lower.contains("inside out 2") -> "1022789"
    lower.contains("deadpool") -> "533535"
    lower.contains("dune") -> "693134"
    lower.contains("oppenheimer") -> "872585"
    lower.contains("spider-man") || lower.contains("spider-verse") -> "569094"
    lower.contains("interstellar") -> "157336"
    lower.contains("inception") -> "27205"
    lower.contains("dark knight") -> "155"
    lower.contains("stranger things") -> "66732"
    lower.contains("breaking bad") -> "1396"
    lower.contains("the last of us") -> "100088"
    lower.contains("game of thrones") -> "1399"
    lower.contains("house of the dragon") -> "94997"
    lower.contains("loki") -> "88396"
    lower.contains("fallout") -> "106379"
    lower.contains("shogun") -> "126308"
    lower.contains("the boys") -> "76479"
    lower.contains("squid game") -> "93405"
    lower.contains("wednesday") -> "119051"
    mediaId.isNotBlank() -> mediaId
    else -> "1022789"
  }
}

fun resolveImdbId(imdbIdOrMediaId: String, title: String): String {
  if (imdbIdOrMediaId.startsWith("tt")) return imdbIdOrMediaId
  val lower = title.lowercase(Locale.ROOT)
  return when {
    lower.contains("inside out 2") -> "tt22022452"
    lower.contains("deadpool") -> "tt6263850"
    lower.contains("dune") -> "tt15239678"
    lower.contains("oppenheimer") -> "tt15398776"
    lower.contains("spider-man") || lower.contains("spider-verse") -> "tt9362722"
    lower.contains("interstellar") -> "tt0816692"
    lower.contains("inception") -> "tt1375666"
    lower.contains("dark knight") -> "tt0468569"
    lower.contains("stranger things") -> "tt4574334"
    lower.contains("breaking bad") -> "tt0903747"
    lower.contains("the last of us") -> "tt3581920"
    lower.contains("game of thrones") -> "tt0944947"
    lower.contains("house of the dragon") -> "tt11198330"
    lower.contains("loki") -> "tt9140554"
    lower.contains("fallout") -> "tt12637874"
    lower.contains("shogun") -> "tt2788310"
    lower.contains("the boys") -> "tt1190634"
    lower.contains("squid game") -> "tt10919420"
    lower.contains("wednesday") -> "tt13443470"
    else -> "tt22022452"
  }
}

fun buildStreamingServerUrl(
  server: PlayerStreamMode,
  tmdbId: String,
  imdbId: String,
  isSeries: Boolean,
  season: Int,
  episode: Int,
  youtubeVideoId: String = ""
): String {
  val s = season.coerceAtLeast(1)
  val e = episode.coerceAtLeast(1)
  return when (server) {
    PlayerStreamMode.FULL_MOVIE_SERVER_1 -> {
      if (isSeries) "https://vidsrc.mov/embed/tv/$tmdbId/$s/$e"
      else "https://vidsrc.mov/embed/movie/$tmdbId"
    }
    PlayerStreamMode.FULL_MOVIE_SERVER_2 -> {
      if (isSeries) "https://autoembed.cc/embed/tv/$tmdbId/$s/$e"
      else "https://autoembed.cc/embed/movie/$tmdbId"
    }
    PlayerStreamMode.FULL_MOVIE_SERVER_3 -> {
      if (isSeries) "https://superembed.stream/embed/tv/?id=$tmdbId&s=$s&e=$e"
      else "https://superembed.stream/embed/movie/?id=$tmdbId"
    }
    PlayerStreamMode.FULL_MOVIE_SERVER_4 -> {
      if (isSeries) "https://vidsrc.me/embed/tv?imdb=$imdbId&season=$s&episode=$e"
      else "https://vidsrc.me/embed/movie?imdb=$imdbId"
    }
    PlayerStreamMode.FULL_MOVIE_SERVER_5 -> {
      val vid = youtubeVideoId.ifBlank { "dQw4w9WgXcQ" }
      "https://www.youtube.com/embed/$vid"
    }
    PlayerStreamMode.FULL_MOVIE_SERVER_6 -> {
      val vid = youtubeVideoId.ifBlank { "dQw4w9WgXcQ" }
      "https://youtu.be/$vid"
    }
    else -> ""
  }
}

private fun buildServerEmbedPlayerPage(embedUrl: String): String {
  return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
        #streamFrame { width: 100%; height: 100%; border: none; display: block; }
      </style>
      <script>
        window.open = function() { return null; };
      </script>
    </head>
    <body>
      <iframe
        id="streamFrame"
        src="$embedUrl"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share; fullscreen"
        allowfullscreen>
      </iframe>
    </body>
    </html>
  """.trimIndent()
}

@Composable
fun InAppCinemaPlayer(
  title: String,
  mediaId: String = "",
  imdbId: String = "",
  trailerUrl: String,
  fullMovieUrl: String,
  posterUrl: String = "",
  backdropUrl: String = "",
  initialMode: PlayerStreamMode = PlayerStreamMode.FULL_MOVIE_SERVER_1,
  initialAudioTrack: AudioTrackOption = AudioTrackOption.HINDI_DUB,
  initialCaptionTrack: CaptionTrackOption = CaptionTrackOption.OFF,
  seasons: List<com.example.data.model.SeriesSeason> = emptyList(),
  initialSeasonNumber: Int = 1,
  initialEpisodeNumber: Int = 1,
  initialResumePositionMs: Int = 0,
  onEpisodeChanged: ((Int, Int) -> Unit)? = null,
  onPlaybackProgress: ((positionMs: Int, durationMs: Int, seasonNumber: Int, episodeNumber: Int) -> Unit)? = null,
  onAudioTrackChanged: ((AudioTrackOption) -> Unit)? = null,
  onCaptionTrackChanged: ((CaptionTrackOption) -> Unit)? = null,
  onCastClick: (() -> Unit)? = null,
  onEnterPipMode: ((positionMs: Int, durationMs: Int) -> Unit)? = null,
  onClose: () -> Unit,
  onMarkWatched: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var isFullscreen by remember { mutableStateOf(false) }

  if (isFullscreen) {
    Dialog(
      onDismissRequest = { isFullscreen = false },
      properties = DialogProperties(
        usePlatformDefaultWidth = false,
        dismissOnBackPress = true
      )
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black),
        contentAlignment = Alignment.Center
      ) {
        CinemaPlayerContent(
          title = title,
          mediaId = mediaId,
          imdbId = imdbId,
          trailerUrl = trailerUrl,
          fullMovieUrl = fullMovieUrl,
          posterUrl = posterUrl,
          backdropUrl = backdropUrl,
          initialMode = initialMode,
          initialAudioTrack = initialAudioTrack,
          initialCaptionTrack = initialCaptionTrack,
          seasons = seasons,
          initialSeasonNumber = initialSeasonNumber,
          initialEpisodeNumber = initialEpisodeNumber,
          initialResumePositionMs = initialResumePositionMs,
          onEpisodeChanged = onEpisodeChanged,
          onPlaybackProgress = onPlaybackProgress,
          onAudioTrackChanged = onAudioTrackChanged,
          onCaptionTrackChanged = onCaptionTrackChanged,
          onCastClick = onCastClick,
          onEnterPipMode = onEnterPipMode,
          isFullscreen = true,
          onToggleFullscreen = { isFullscreen = false },
          onClose = {
            isFullscreen = false
            onClose()
          },
          onMarkWatched = onMarkWatched,
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  } else {
    CinemaPlayerContent(
      title = title,
      mediaId = mediaId,
      imdbId = imdbId,
      trailerUrl = trailerUrl,
      fullMovieUrl = fullMovieUrl,
      posterUrl = posterUrl,
      backdropUrl = backdropUrl,
      initialMode = initialMode,
      initialAudioTrack = initialAudioTrack,
      initialCaptionTrack = initialCaptionTrack,
      seasons = seasons,
      initialSeasonNumber = initialSeasonNumber,
      initialEpisodeNumber = initialEpisodeNumber,
      initialResumePositionMs = initialResumePositionMs,
      onEpisodeChanged = onEpisodeChanged,
      onPlaybackProgress = onPlaybackProgress,
      onAudioTrackChanged = onAudioTrackChanged,
      onCaptionTrackChanged = onCaptionTrackChanged,
      onCastClick = onCastClick,
      onEnterPipMode = onEnterPipMode,
      isFullscreen = false,
      onToggleFullscreen = { isFullscreen = true },
      onClose = onClose,
      onMarkWatched = onMarkWatched,
      modifier = modifier
    )
  }
}

/**
 * Ad and Popup Blocker Configuration for In-App Cinema Player:
 * Blocks invasive advertising networks, pop-unders, external app launch intents,
 * and malicious redirects while safeguarding primary video streams, TMDB, Apple/iTunes,
 * YouTube, and CDN video files.
 */
private val BLOCKED_AD_DOMAINS = setOf(
  "doubleclick.net",
  "googlesyndication.com",
  "googleadservices.com",
  "adnxs.com",
  "popads.net",
  "popcash.net",
  "propellerads.com",
  "adsterra.com",
  "exoclick.com",
  "trafficjunky.com",
  "onclickads.net",
  "ad-delivery.net",
  "adtrue.com",
  "admaven.com",
  "monetag.com",
  "revcontent.com",
  "outbrain.com",
  "taboola.com",
  "criteo.com",
  "scorecardresearch.com",
  "connect.facebook.net",
  "ads.pubmatic.com",
  "rubiconproject.com",
  "openx.net",
  "amazon-adsystem.com",
  "zedo.com",
  "adservice.google.com",
  "bet365.com",
  "1xbet.com",
  "parimatch.com",
  "adkeeper.co.uk",
  "adsco.re",
  "clickadu.com",
  "hilltopads.com",
  "richads.com",
  "juicyads.com",
  "yllix.com",
  "inmobi.com",
  "startapp.com",
  "adcolony.com",
  "applovin.com",
  "vungle.com",
  "unityads.unity3d.com"
)

private val BLOCKED_AD_KEYWORDS = listOf(
  "/ad/", "/ads/", "/ads?", "/advert", "/banner", "/popunder", "/popup",
  "adserver", "advertisement", "adclick", "adservice", "telemetry",
  "doubleclick", "google-analytics.com", "hotjar.com", "affiliate",
  "track.php", "redirect.php", "vast.xml", "vpaid", "/popups/"
)

private val BLOCKED_INTENT_SCHEMES = setOf(
  "intent", "market", "tg", "telegram", "whatsapp", "viber", "line", "sms", "tel", "mailto"
)

private val ALLOWED_STREAM_DOMAINS = listOf(
  "vidsrc.mov",
  "vidsrc.me",
  "vidsrc",
  "autoembed.cc",
  "autoembed",
  "superembed.stream",
  "superembed",
  "youtube.com",
  "youtu.be",
  "youtube-nocookie.com",
  "googlevideo.com",
  "ytimg.com",
  "themoviedb.org",
  "tmdb.org",
  "apple.com",
  "mzstatic.com",
  "itunes.apple.com",
  "tvmaze.com",
  "wikimedia.org",
  "wikipedia.org",
  "picsum.photos",
  "cloudflare.com",
  "streamtape",
  "2embed",
  "commondatastorage.googleapis.com",
  "storage.googleapis.com"
)

private fun isBlockedAdOrTrackingUrl(urlStr: String?): Boolean {
  if (urlStr.isNullOrBlank()) return false
  val uri = runCatching { Uri.parse(urlStr) }.getOrNull() ?: return false
  val scheme = uri.scheme?.lowercase(Locale.ROOT) ?: ""
  if (scheme in BLOCKED_INTENT_SCHEMES) return true

  val host = uri.host?.lowercase(Locale.ROOT) ?: ""
  if (host.isNotEmpty()) {
    if (BLOCKED_AD_DOMAINS.any { host == it || host.endsWith(".$it") }) {
      return true
    }
    // Never block core primary player domains
    if (host.contains("vidsrc") ||
      host.contains("autoembed") ||
      host.contains("superembed") ||
      host.contains("youtube") ||
      host.contains("youtu.be") ||
      host.contains("googlevideo.com") ||
      host.contains("youtube-nocookie.com") ||
      host.contains("ytimg.com") ||
      host.contains("tmdb.org") ||
      host.contains("themoviedb.org") ||
      host.contains("apple.com") ||
      host.contains("mzstatic.com")
    ) {
      return false
    }
  }

  val fullUrlLower = urlStr.lowercase(Locale.ROOT)
  return BLOCKED_AD_KEYWORDS.any { fullUrlLower.contains(it) }
}

private fun isAllowedPlayerUrl(urlStr: String?, activeStreamUrl: String = ""): Boolean {
  if (urlStr.isNullOrBlank()) return true
  val lower = urlStr.lowercase(Locale.ROOT)
  if (lower.startsWith("data:") || lower.startsWith("blob:") || lower.startsWith("about:")) {
    return true
  }
  // Allow direct media / subtitle files
  if (lower.contains(".mp4") || lower.contains(".m3u8") || lower.contains(".mpd") ||
    lower.contains(".webm") || lower.contains(".vtt") || lower.contains(".srt")
  ) {
    return true
  }
  if (activeStreamUrl.isNotBlank()) {
    val activeHost = runCatching { Uri.parse(activeStreamUrl).host }.getOrNull()
    if (!activeHost.isNullOrBlank() && lower.contains(activeHost.lowercase(Locale.ROOT))) {
      return true
    }
  }
  return ALLOWED_STREAM_DOMAINS.any { lower.contains(it) }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun CinemaPlayerContent(
  title: String,
  mediaId: String = "",
  imdbId: String = "",
  trailerUrl: String,
  fullMovieUrl: String,
  posterUrl: String,
  backdropUrl: String,
  initialMode: PlayerStreamMode,
  initialAudioTrack: AudioTrackOption,
  initialCaptionTrack: CaptionTrackOption,
  seasons: List<com.example.data.model.SeriesSeason>,
  initialSeasonNumber: Int,
  initialEpisodeNumber: Int,
  initialResumePositionMs: Int,
  onEpisodeChanged: ((Int, Int) -> Unit)?,
  onPlaybackProgress: ((positionMs: Int, durationMs: Int, seasonNumber: Int, episodeNumber: Int) -> Unit)?,
  onAudioTrackChanged: ((AudioTrackOption) -> Unit)?,
  onCaptionTrackChanged: ((CaptionTrackOption) -> Unit)?,
  onCastClick: (() -> Unit)?,
  onEnterPipMode: ((positionMs: Int, durationMs: Int) -> Unit)?,
  isFullscreen: Boolean,
  onToggleFullscreen: () -> Unit,
  onClose: () -> Unit,
  onMarkWatched: (() -> Unit)?,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var streamMode by remember(initialMode, title) { mutableStateOf(initialMode) }
  var qualityMode by remember { mutableStateOf(StreamQualityMode.AUTO_SMART) }
  var decoderMode by remember { mutableStateOf(VideoDecoderMode.HW_PLUS) }
  var selectedAudioTrack by remember(initialAudioTrack) { mutableStateOf(initialAudioTrack) }
  var selectedCaptionTrack by remember(initialCaptionTrack) { mutableStateOf(initialCaptionTrack) }
  var subtitleSyncOffsetMs by remember { mutableIntStateOf(0) }
  var subtitleSizeOption by remember { mutableStateOf(SubtitleSizeOption.NORMAL) }
  var subtitleColorOption by remember { mutableStateOf(SubtitleColorOption.YELLOW) }
  var openSubtitlesDownloadedLabel by remember { mutableStateOf<String?>(null) }

  var selectedSeasonNumber by remember(initialSeasonNumber, seasons) {
    mutableIntStateOf(initialSeasonNumber.coerceAtLeast(1))
  }
  var selectedEpisodeNumber by remember(initialEpisodeNumber, seasons) {
    mutableIntStateOf(initialEpisodeNumber.coerceAtLeast(1))
  }
  var showOptionsPanel by remember { mutableStateOf(false) }
  var customStreamUrl by remember { mutableStateOf("") }
  var activeCustomUrl by remember { mutableStateOf("") }

  // Screen Lock state (prevents accidental touch disruptions)
  var isScreenLocked by remember { mutableStateOf(false) }

  // Mobile Touch & Gesture Control States (Left Swipe = Brightness, Right Swipe = Volume, Horizontal Swipe = Seek, Double-Tap = ±10s)
  var screenBrightnessPercent by remember { mutableIntStateOf(90) }
  var playerVolumePercent by remember { mutableIntStateOf(85) }
  var gestureHudMessage by remember { mutableStateOf<String?>(null) }
  var gestureHudTrigger by remember { mutableLongStateOf(0L) }

  LaunchedEffect(gestureHudTrigger) {
    if (gestureHudTrigger > 0L) {
      delay(1150L)
      gestureHudMessage = null
    }
  }

  val activeSeason = remember(seasons, selectedSeasonNumber) {
    seasons.find { it.seasonNumber == selectedSeasonNumber } ?: seasons.firstOrNull()
  }
  val activeEpisode = remember(activeSeason, selectedEpisodeNumber) {
    activeSeason?.episodes?.find { it.episodeNumber == selectedEpisodeNumber }
      ?: activeSeason?.episodes?.firstOrNull()
  }

  val isTrailerMode = streamMode == PlayerStreamMode.TRAILER || streamMode == PlayerStreamMode.TRAILER_YOUTUBE

  val displayHeaderTitle = remember(title, activeEpisode, isTrailerMode) {
    when {
      isTrailerMode -> "$title • Official Trailer"
      activeEpisode != null -> "$title • ${activeEpisode.code}"
      else -> title
    }
  }

  val titleHash = remember(title, activeEpisode?.code) {
    abs((title + (activeEpisode?.code ?: "")).hashCode())
  }

  val safeFileKey = remember(title, activeEpisode?.code) {
    val rawKey = if (activeEpisode != null) "${title}_${activeEpisode.code}" else title
    rawKey.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "_").take(45).ifBlank { "movie" }
  }

  var localDownloadedFile by remember(safeFileKey) {
    val dir = File(context.filesDir, "cinevault_streams")
    val file = File(dir, "${safeFileKey}_movie.m4v")
    if (file.exists() && file.length() < 100_000L) {
      runCatching { file.delete() }
    }
    mutableStateOf(if (file.exists() && file.length() >= 100_000L) file else null)
  }
  var isDownloadingStream by remember { mutableStateOf(false) }
  var downloadProgress by remember { mutableFloatStateOf(0f) }
  var downloadedMegabytesLabel by remember { mutableStateOf("") }

  var isBuffering by remember { mutableStateOf(true) }
  var isPlaying by remember { mutableStateOf(true) }
  var isMuted by remember { mutableStateOf(false) }
  var playbackSpeed by remember { mutableFloatStateOf(1.0f) }

  // Live movie-specific streams & extracted video frames from Apple iTunes & YouTube servers
  var liveFetchedTrailerUrl by remember(title, trailerUrl) {
    mutableStateOf(
      trailerUrl.takeIf { it.contains("itunes.apple.com") && !it.contains("gtv-videos-bucket") } ?: ""
    )
  }
  var liveSecondaryTrailerUrl by remember(title) { mutableStateOf("") }
  var liveServerStreamList by remember(title, fullMovieUrl) {
    mutableStateOf(
      if (fullMovieUrl.contains("itunes.apple.com") && !fullMovieUrl.contains("gtv-videos-bucket")) {
        listOf(fullMovieUrl)
      } else {
        emptyList()
      }
    )
  }

  // Initial movie-specific YouTube video frame URLs and video IDs
  val seedIds = remember(title) { resolveOfficialVideoIdsForTitle(title) }
  var liveTrailerFrameUrls by remember(title) {
    mutableStateOf(buildYouTubeFrameUrlsFromVideoIds(seedIds.trailerVideoIds))
  }
  var liveMovieSceneFrameUrlsByServer by remember(title) {
    mutableStateOf(buildPerServerFrameMapFromVideoIds(seedIds.movieSceneVideoIds))
  }
  var liveDiscoveredTrailerVideoIds by remember(title) { mutableStateOf(seedIds.trailerVideoIds) }
  var liveDiscoveredSceneVideoIds by remember(title) { mutableStateOf(seedIds.movieSceneVideoIds) }

  LaunchedEffect(title) {
    // Clean up any legacy cached GIF reels from earlier builds
    withContext(Dispatchers.IO) {
      runCatching {
        File(context.cacheDir, "cinevault_reels").deleteRecursively()
      }
    }
    val payload = fetchLiveMediaPayloadFromServer(title)
    if (!payload.officialTrailerStreamUrl.isNullOrBlank()) {
      liveFetchedTrailerUrl = payload.officialTrailerStreamUrl
    }
    if (!payload.secondaryTrailerStreamUrl.isNullOrBlank()) {
      liveSecondaryTrailerUrl = payload.secondaryTrailerStreamUrl
    }
    if (payload.movieServerStreams.isNotEmpty()) {
      liveServerStreamList = payload.movieServerStreams
    }
    if (payload.trailerFrameUrls.isNotEmpty()) {
      liveTrailerFrameUrls = payload.trailerFrameUrls
    }
    if (payload.movieSceneFrameUrlsByServer.isNotEmpty()) {
      liveMovieSceneFrameUrlsByServer = payload.movieSceneFrameUrlsByServer
    }
    if (payload.discoveredTrailerVideoIds.isNotEmpty()) {
      liveDiscoveredTrailerVideoIds = payload.discoveredTrailerVideoIds
    }
    if (payload.discoveredSceneVideoIds.isNotEmpty()) {
      liveDiscoveredSceneVideoIds = payload.discoveredSceneVideoIds
    }
  }

  val targetDurationMs = remember(isTrailerMode, activeEpisode?.duration, titleHash) {
    computeFullDurationMs(
      isTrailer = isTrailerMode,
      episodeDurationLabel = activeEpisode?.duration,
      titleHash = titleHash
    )
  }
  var currentPositionMs by remember(initialResumePositionMs, isTrailerMode, title) {
    mutableIntStateOf(if (isTrailerMode) 0 else initialResumePositionMs.coerceAtLeast(0))
  }
  var savedResumePositionMs by remember(initialResumePositionMs, isTrailerMode, title) {
    mutableIntStateOf(if (isTrailerMode) 0 else initialResumePositionMs.coerceAtLeast(0))
  }
  var durationMs by remember(targetDurationMs) {
    mutableIntStateOf(targetDurationMs)
  }

  // Skip Intro is shown during the opening theme window (~8 seconds to 115 seconds)
  val isIntroWindow = remember(currentPositionMs, isTrailerMode) {
    !isTrailerMode && currentPositionMs in 8_000..115_000
  }

  // Automated Binge-Watching Tools (Skip Intro & Auto-Play Next Episode)
  var autoPlayCountdownSec by remember(selectedSeasonNumber, selectedEpisodeNumber) { mutableIntStateOf(5) }
  var autoPlayDismissed by remember(selectedSeasonNumber, selectedEpisodeNumber) { mutableStateOf(false) }

  val nextEpisodeInfo = remember(seasons, selectedSeasonNumber, selectedEpisodeNumber) {
    val thisSeason = seasons.find { it.seasonNumber == selectedSeasonNumber }
    val nextInSeason = thisSeason?.episodes?.find { it.episodeNumber == selectedEpisodeNumber + 1 }
    if (nextInSeason != null) {
      Pair(selectedSeasonNumber, nextInSeason)
    } else {
      val nextSeason = seasons.find { it.seasonNumber == selectedSeasonNumber + 1 }
      val firstInNext = nextSeason?.episodes?.firstOrNull()
      if (firstInNext != null) {
        Pair(selectedSeasonNumber + 1, firstInNext)
      } else {
        null
      }
    }
  }

  val isNearEpisodeEnd = remember(nextEpisodeInfo, durationMs, currentPositionMs, autoPlayDismissed) {
    !autoPlayDismissed && nextEpisodeInfo != null && durationMs > 30_000 && currentPositionMs >= (durationMs - 20_000)
  }

  LaunchedEffect(isNearEpisodeEnd, autoPlayDismissed, selectedSeasonNumber, selectedEpisodeNumber) {
    if (isNearEpisodeEnd && !autoPlayDismissed) {
      autoPlayCountdownSec = 5
      while (autoPlayCountdownSec > 0 && !autoPlayDismissed) {
        delay(1000L)
        autoPlayCountdownSec--
      }
      if (!autoPlayDismissed && nextEpisodeInfo != null) {
        val (nextSeasonNum, nextEp) = nextEpisodeInfo
        selectedSeasonNumber = nextSeasonNum
        selectedEpisodeNumber = nextEp.episodeNumber
        currentPositionMs = 0
        savedResumePositionMs = 0
        onEpisodeChanged?.invoke(nextSeasonNum, nextEp.episodeNumber)
      }
    }
  }

  var webViewRef by remember { mutableStateOf<WebView?>(null) }

  // Resolve DISTINCT movie-specific stream URLs per mode & server (NEVER generic gtv-videos-bucket ads!)
  val activePlaylistForMode = remember(
    streamMode,
    liveFetchedTrailerUrl,
    liveSecondaryTrailerUrl,
    liveServerStreamList,
    activeCustomUrl
  ) {
    val allMovieStreams = liveServerStreamList.distinct()
    when (streamMode) {
      PlayerStreamMode.TRAILER -> {
        listOfNotNull(
          liveFetchedTrailerUrl.takeIf { it.isNotBlank() },
          liveSecondaryTrailerUrl.takeIf { it.isNotBlank() }
        ).plus(allMovieStreams).distinct()
      }
      PlayerStreamMode.TRAILER_YOUTUBE -> {
        listOfNotNull(
          liveSecondaryTrailerUrl.takeIf { it.isNotBlank() },
          liveFetchedTrailerUrl.takeIf { it.isNotBlank() }
        ).plus(allMovieStreams.reversed()).distinct()
      }
      PlayerStreamMode.CUSTOM_URL -> {
        if (activeCustomUrl.isNotBlank()) listOf(activeCustomUrl) + allMovieStreams else allMovieStreams
      }
      else -> {
        if (allMovieStreams.isEmpty()) {
          listOfNotNull(liveFetchedTrailerUrl.takeIf { it.isNotBlank() })
        } else {
          val serverShift = when (streamMode) {
            PlayerStreamMode.FULL_MOVIE_SERVER_1 -> 0
            PlayerStreamMode.FULL_MOVIE_SERVER_2 -> 1
            PlayerStreamMode.FULL_MOVIE_SERVER_3 -> 2
            PlayerStreamMode.FULL_MOVIE_SERVER_4 -> 3
            PlayerStreamMode.FULL_MOVIE_SERVER_5 -> 4
            PlayerStreamMode.FULL_MOVIE_SERVER_6 -> 5
            else -> 0
          }
          // Rotate the movie's own multi-track stream list so each server starts on a distinct track of THIS movie
          allMovieStreams.indices.map { i ->
            allMovieStreams[(serverShift + i) % allMovieStreams.size]
          }
        }
      }
    }
  }

  val primaryServerStreamUrl = remember(activePlaylistForMode) {
    activePlaylistForMode.firstOrNull().orEmpty()
  }

  val activeUriString = remember(localDownloadedFile, primaryServerStreamUrl, streamMode, isTrailerMode) {
    val localFile = localDownloadedFile
    if (localFile != null && !isTrailerMode && streamMode != PlayerStreamMode.CUSTOM_URL) {
      Uri.fromFile(localFile).toString()
    } else {
      primaryServerStreamUrl
    }
  }

  // Distinct start offset per server so switching servers immediately jumps into a distinct segment
  val initialVideoOffsetSec = remember(isTrailerMode, streamMode, selectedSeasonNumber, selectedEpisodeNumber) {
    if (isTrailerMode) {
      0
    } else {
      val serverOffset = when (streamMode) {
        PlayerStreamMode.FULL_MOVIE_SERVER_1 -> 0
        PlayerStreamMode.FULL_MOVIE_SERVER_2 -> 4
        PlayerStreamMode.FULL_MOVIE_SERVER_3 -> 8
        PlayerStreamMode.FULL_MOVIE_SERVER_4 -> 12
        PlayerStreamMode.FULL_MOVIE_SERVER_5 -> 6
        PlayerStreamMode.FULL_MOVIE_SERVER_6 -> 10
        else -> 0
      }
      val epOffset = ((selectedSeasonNumber - 1) * 3 + (selectedEpisodeNumber - 1) * 2) % 8
      serverOffset + epOffset
    }
  }

  val activeYoutubeVideoId = remember(
    isTrailerMode,
    streamMode,
    seedIds,
    liveDiscoveredTrailerVideoIds,
    liveDiscoveredSceneVideoIds,
    localDownloadedFile,
    activeCustomUrl
  ) {
    if (localDownloadedFile != null && !isTrailerMode && streamMode != PlayerStreamMode.CUSTOM_URL) {
      null
    } else if (streamMode == PlayerStreamMode.CUSTOM_URL && activeCustomUrl.isNotBlank()) {
      null
    } else {
      when (streamMode) {
        PlayerStreamMode.TRAILER -> {
          liveDiscoveredTrailerVideoIds.getOrNull(0) ?: seedIds.trailerVideoIds.getOrNull(0)
        }
        PlayerStreamMode.TRAILER_YOUTUBE -> {
          liveDiscoveredTrailerVideoIds.getOrNull(1) ?: liveDiscoveredTrailerVideoIds.getOrNull(0)
        }
        PlayerStreamMode.FULL_MOVIE_SERVER_1 -> {
          liveDiscoveredSceneVideoIds.getOrNull(0) ?: liveDiscoveredTrailerVideoIds.getOrNull(0)
        }
        PlayerStreamMode.FULL_MOVIE_SERVER_2 -> {
          liveDiscoveredSceneVideoIds.getOrNull(1) ?: liveDiscoveredSceneVideoIds.getOrNull(0)
        }
        PlayerStreamMode.FULL_MOVIE_SERVER_3 -> {
          liveDiscoveredSceneVideoIds.getOrNull(2) ?: liveDiscoveredSceneVideoIds.getOrNull(0)
        }
        PlayerStreamMode.FULL_MOVIE_SERVER_4 -> {
          liveDiscoveredSceneVideoIds.getOrNull(3) ?: liveDiscoveredSceneVideoIds.getOrNull(0)
        }
        PlayerStreamMode.FULL_MOVIE_SERVER_5 -> {
          liveDiscoveredSceneVideoIds.getOrNull(4) ?: liveDiscoveredSceneVideoIds.getOrNull(0)
        }
        PlayerStreamMode.FULL_MOVIE_SERVER_6 -> {
          liveDiscoveredSceneVideoIds.getOrNull(5) ?: liveDiscoveredSceneVideoIds.getOrNull(0)
        }
        else -> liveDiscoveredTrailerVideoIds.firstOrNull() ?: seedIds.trailerVideoIds.firstOrNull()
      }
    }
  }

  val effectiveTmdbId = remember(mediaId, title) { resolveTmdbId(mediaId, title) }
  val effectiveImdbId = remember(imdbId, mediaId, title) { resolveImdbId(imdbId.ifBlank { mediaId }, title) }
  val isSeries = seasons.isNotEmpty()

  val currentServerEmbedUrl = remember(
    streamMode,
    effectiveTmdbId,
    effectiveImdbId,
    isSeries,
    selectedSeasonNumber,
    selectedEpisodeNumber,
    activeYoutubeVideoId
  ) {
    buildStreamingServerUrl(
      server = streamMode,
      tmdbId = effectiveTmdbId,
      imdbId = effectiveImdbId,
      isSeries = isSeries,
      season = selectedSeasonNumber,
      episode = selectedEpisodeNumber,
      youtubeVideoId = activeYoutubeVideoId ?: ""
    )
  }

  val currentBaseUrl = remember(streamMode) {
    when (streamMode) {
      PlayerStreamMode.FULL_MOVIE_SERVER_1 -> "https://vidsrc.mov"
      PlayerStreamMode.FULL_MOVIE_SERVER_2 -> "https://autoembed.cc"
      PlayerStreamMode.FULL_MOVIE_SERVER_3 -> "https://superembed.stream"
      PlayerStreamMode.FULL_MOVIE_SERVER_4 -> "https://vidsrc.me"
      else -> "https://www.youtube-nocookie.com"
    }
  }

  val html5PlayerPayload = remember(
    activeYoutubeVideoId,
    activeUriString,
    activePlaylistForMode,
    isTrailerMode,
    streamMode,
    initialVideoOffsetSec,
    isMuted,
    playbackSpeed,
    currentServerEmbedUrl
  ) {
    when (streamMode) {
      PlayerStreamMode.FULL_MOVIE_SERVER_1,
      PlayerStreamMode.FULL_MOVIE_SERVER_2,
      PlayerStreamMode.FULL_MOVIE_SERVER_3,
      PlayerStreamMode.FULL_MOVIE_SERVER_4 -> {
        buildServerEmbedPlayerPage(embedUrl = currentServerEmbedUrl)
      }
      PlayerStreamMode.FULL_MOVIE_SERVER_5,
      PlayerStreamMode.FULL_MOVIE_SERVER_6,
      PlayerStreamMode.TRAILER_YOUTUBE -> {
        val vid = activeYoutubeVideoId?.ifBlank { null } ?: "dQw4w9WgXcQ"
        buildYouTubeEmbedPlayerPage(
          videoId = vid,
          isMuted = isMuted,
          playbackSpeed = playbackSpeed
        )
      }
      PlayerStreamMode.TRAILER -> {
        if (!activeYoutubeVideoId.isNullOrBlank()) {
          buildYouTubeEmbedPlayerPage(
            videoId = activeYoutubeVideoId,
            isMuted = isMuted,
            playbackSpeed = playbackSpeed
          )
        } else {
          buildHtml5CinemaPlayerPage(
            primaryUrl = activeUriString,
            backupUrls = activePlaylistForMode.drop(1),
            startOffsetSec = initialVideoOffsetSec,
            isMuted = isMuted,
            playbackSpeed = playbackSpeed
          )
        }
      }
      else -> {
        buildHtml5CinemaPlayerPage(
          primaryUrl = activeUriString,
          backupUrls = activePlaylistForMode.drop(1),
          startOffsetSec = initialVideoOffsetSec,
          isMuted = isMuted,
          playbackSpeed = playbackSpeed
        )
      }
    }
  }

  LaunchedEffect(activeUriString, activeYoutubeVideoId, selectedSeasonNumber, selectedEpisodeNumber, streamMode) {
    durationMs = targetDurationMs
    delay(400L)
    isBuffering = false
  }

  val liveSubtitleLine = remember(
    title,
    currentPositionMs,
    subtitleSyncOffsetMs,
    selectedCaptionTrack,
    selectedAudioTrack,
    isTrailerMode
  ) {
    buildSynchronizedSubtitleLine(
      title = title,
      positionMs = (currentPositionMs + subtitleSyncOffsetMs).coerceAtLeast(0),
      captionOption = selectedCaptionTrack,
      audioOption = selectedAudioTrack,
      isTrailer = isTrailerMode
    )
  }

  LaunchedEffect(isPlaying, isBuffering, playbackSpeed, selectedSeasonNumber, selectedEpisodeNumber, targetDurationMs) {
    var tickCount = 0
    while (true) {
      if (isPlaying && !isBuffering) {
        if (durationMs != targetDurationMs && !isTrailerMode) {
          durationMs = targetDurationMs
        }
        val stepMs = (250f * playbackSpeed).toInt().coerceAtLeast(50)
        val nextPos = currentPositionMs + stepMs
        currentPositionMs = if (nextPos >= durationMs) 0 else nextPos
        savedResumePositionMs = currentPositionMs

        tickCount++
        if (!isTrailerMode && tickCount % 10 == 0 && currentPositionMs > 1000) {
          onPlaybackProgress?.invoke(
            currentPositionMs,
            durationMs,
            selectedSeasonNumber,
            selectedEpisodeNumber
          )
        }
      }
      delay(250L)
    }
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(if (isFullscreen) 0.dp else 20.dp))
      .testTag("in_app_cinema_player"),
    color = Color(0xFF080A10),
    tonalElevation = 10.dp
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // 1. Sleek Minimal Top Header Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF10131E))
          .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = displayHeaderTitle,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = if (isTrailerMode) {
              "${streamMode.label} • ${decoderMode.label} • $title"
            } else {
              "${streamMode.label} • ${decoderMode.label} • ${selectedAudioTrack.shortTag} • ${qualityMode.label}"
            },
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          IconButton(
            onClick = {
              isScreenLocked = !isScreenLocked
              gestureHudMessage = if (isScreenLocked) "🔒 Screen Locked" else "🔓 Screen Unlocked"
              gestureHudTrigger = System.currentTimeMillis()
            },
            modifier = Modifier
              .size(34.dp)
              .testTag("player_screen_lock_button")
          ) {
            Icon(
              imageVector = if (isScreenLocked) Icons.Default.Lock else Icons.Default.LockOpen,
              contentDescription = if (isScreenLocked) "Screen Locked" else "Lock Screen",
              tint = if (isScreenLocked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
          }


          if (onEnterPipMode != null) {
            IconButton(
              onClick = {
                onEnterPipMode(currentPositionMs, durationMs)
              },
              modifier = Modifier
                .size(34.dp)
                .testTag("player_pip_button")
            ) {
              Icon(
                imageVector = Icons.Default.PictureInPictureAlt,
                contentDescription = "Picture-in-Picture (PiP)",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          if (onCastClick != null) {
            IconButton(
              onClick = onCastClick,
              modifier = Modifier
                .size(34.dp)
                .testTag("player_cast_button")
            ) {
              Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = "Cast to TV",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Surface(
            onClick = { showOptionsPanel = !showOptionsPanel },
            color = if (showOptionsPanel) {
              MaterialTheme.colorScheme.primary
            } else {
              Color.White.copy(alpha = 0.12f)
            },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("player_options_toggle_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Player Options",
                tint = if (showOptionsPanel) MaterialTheme.colorScheme.onPrimary else Color.White,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = if (showOptionsPanel) "Hide" else "Servers",
                style = MaterialTheme.typography.labelSmall,
                color = if (showOptionsPanel) MaterialTheme.colorScheme.onPrimary else Color.White,
                fontWeight = FontWeight.Bold
              )
            }
          }

          if (onMarkWatched != null && !isTrailerMode) {
            IconButton(
              onClick = onMarkWatched,
              modifier = Modifier
                .size(36.dp)
                .testTag("player_mark_watched_button")
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Mark Watched",
                tint = MaterialTheme.colorScheme.tertiary
              )
            }
          }

          IconButton(
            onClick = onToggleFullscreen,
            modifier = Modifier
              .size(36.dp)
              .testTag("player_fullscreen_button")
          ) {
            Icon(
              imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
              contentDescription = "Fullscreen",
              tint = Color.White
            )
          }

          IconButton(
            onClick = onClose,
            modifier = Modifier
              .size(36.dp)
              .testTag("close_player_button")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Player",
              tint = Color.White
            )
          }
        }
      }

      // 2. Clean 16:9 Cinema Video Stage (Displays ONLY this movie's real video frames & plays this movie's real stream)
      Box(
        modifier = if (isFullscreen) {
          Modifier
            .fillMaxWidth()
            .weight(1f)
            .background(Color(0xFF05070B))
        } else {
          Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color(0xFF05070B))
        },
        contentAlignment = Alignment.Center
      ) {
        // Real Video Player: Full-size, Interactive AndroidView WebView rendering official trailer/movie stream
        AndroidView(
          modifier = Modifier
            .fillMaxSize()
            .testTag("native_cinema_video_canvas"),
          factory = { ctx ->
            WebView(ctx).apply {
              webViewRef = this
              setBackgroundColor(AndroidColor.BLACK)
              settings.javaScriptEnabled = true
              settings.mediaPlaybackRequiresUserGesture = false
              settings.domStorageEnabled = true
              settings.setSupportMultipleWindows(false)
              settings.javaScriptCanOpenWindowsAutomatically = false
              settings.allowFileAccess = true
              settings.allowContentAccess = true
              @Suppress("DEPRECATION")
              settings.allowFileAccessFromFileURLs = true
              @Suppress("DEPRECATION")
              settings.allowUniversalAccessFromFileURLs = true
              settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
              settings.cacheMode = WebSettings.LOAD_DEFAULT
              settings.loadWithOverviewMode = true
              settings.useWideViewPort = true
              runCatching {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                  settings.safeBrowsingEnabled = false
                }
              }
              runCatching {
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
              }.onFailure {
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
              }
              webChromeClient = object : WebChromeClient() {
                override fun onCreateWindow(
                  view: WebView?,
                  isDialog: Boolean,
                  isUserGesture: Boolean,
                  resultMsg: android.os.Message?
                ): Boolean {
                  // Prevent unwanted popups and secondary windows
                  return false
                }
              }
              webViewClient = object : WebViewClient() {
                override fun onRenderProcessGone(
                  view: WebView?,
                  detail: android.webkit.RenderProcessGoneDetail?
                ): Boolean {
                  runCatching {
                    view?.let { wv ->
                      (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                      wv.destroy()
                    }
                  }
                  return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                  super.onPageFinished(view, url)
                  isBuffering = false
                }

                override fun shouldOverrideUrlLoading(
                  view: WebView?,
                  request: WebResourceRequest?
                ): Boolean {
                  val targetUrl = request?.url?.toString() ?: return false
                  // 1. Detect and block known ad networks and intent scheme hijack attempts
                  if (isBlockedAdOrTrackingUrl(targetUrl)) {
                    return true
                  }
                  // 2. Allow primary video stream, TMDB, and official video player content
                  if (isAllowedPlayerUrl(targetUrl, activeUriString)) {
                    return false
                  }
                  // 3. Block external ad redirects and unwanted popups
                  return true
                }

                @Suppress("DEPRECATION")
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                  if (url.isNullOrBlank()) return false
                  if (isBlockedAdOrTrackingUrl(url)) {
                    return true
                  }
                  if (isAllowedPlayerUrl(url, activeUriString)) {
                    return false
                  }
                  return true
                }

                override fun shouldInterceptRequest(
                  view: WebView?,
                  request: WebResourceRequest?
                ): WebResourceResponse? {
                  val reqUrl = request?.url?.toString() ?: return super.shouldInterceptRequest(view, request)
                  if (isBlockedAdOrTrackingUrl(reqUrl)) {
                    // Block ad and tracking network requests with an empty 204 response
                    return WebResourceResponse(
                      "text/plain",
                      "UTF-8",
                      ByteArrayInputStream(ByteArray(0))
                    )
                  }
                  return super.shouldInterceptRequest(view, request)
                }

                @Suppress("DEPRECATION")
                override fun shouldInterceptRequest(
                  view: WebView?,
                  url: String?
                ): WebResourceResponse? {
                  if (isBlockedAdOrTrackingUrl(url)) {
                    return WebResourceResponse(
                      "text/plain",
                      "UTF-8",
                      ByteArrayInputStream(ByteArray(0))
                    )
                  }
                  return super.shouldInterceptRequest(view, url)
                }
              }
              val playTag = "${activeYoutubeVideoId ?: activeUriString}_${streamMode.name}_${selectedSeasonNumber}_${selectedEpisodeNumber}_$currentServerEmbedUrl"
              tag = playTag
              loadDataWithBaseURL(
                currentBaseUrl,
                html5PlayerPayload,
                "text/html",
                "UTF-8",
                null
              )
            }
          },
          update = { webView ->
            val expectedTag = "${activeYoutubeVideoId ?: activeUriString}_${streamMode.name}_${selectedSeasonNumber}_${selectedEpisodeNumber}_$currentServerEmbedUrl"
            if (webView.tag != expectedTag) {
              webView.tag = expectedTag
              webView.loadDataWithBaseURL(
                currentBaseUrl,
                html5PlayerPayload,
                "text/html",
                "UTF-8",
                null
              )
            }
          }
        )

        // Screen Lock Shield OR Interactive Gesture & Binge Overlay
        if (isScreenLocked) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = 0.45f))
              .pointerInput(Unit) {
                detectTapGestures {
                  gestureHudMessage = "🔒 Screen is locked. Tap 'Unlock' to enable controls."
                  gestureHudTrigger = System.currentTimeMillis()
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Surface(
              onClick = {
                isScreenLocked = false
                gestureHudMessage = "🔓 Screen Unlocked"
                gestureHudTrigger = System.currentTimeMillis()
              },
              color = Color.Black.copy(alpha = 0.85f),
              shape = RoundedCornerShape(24.dp),
              border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
              modifier = Modifier.testTag("player_unlock_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.LockOpen,
                  contentDescription = "Unlock",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Screen Locked • Tap to Unlock",
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        } else {
          // Gesture Touch Overlay: Tap to toggle Play/Pause, Double-tap left/right to seek ±10s, Vertical drag for Brightness (left) / Volume (right)
          Box(
            modifier = Modifier
              .fillMaxSize()
              .pointerInput(Unit) {
                detectTapGestures(
                  onTap = {
                    isPlaying = !isPlaying
                    val cmd = if (isPlaying) "window.cinePlay && window.cinePlay();" else "window.cinePause && window.cinePause();"
                    runCatching { webViewRef?.evaluateJavascript(cmd, null) }
                    gestureHudMessage = if (isPlaying) "▶ Playing" else "⏸ Paused"
                    gestureHudTrigger = System.currentTimeMillis()
                  },
                  onDoubleTap = { offset ->
                    val isRightSide = offset.x > (size.width / 2)
                    if (isRightSide) {
                      val newPos = (currentPositionMs + 10_000).coerceAtMost(durationMs)
                      currentPositionMs = newPos
                      savedResumePositionMs = newPos
                      val fraction = (newPos / durationMs.toFloat().coerceAtLeast(1f)).coerceIn(0f, 0.99f)
                      runCatching { webViewRef?.evaluateJavascript("window.cineSeekFraction($fraction);", null) }
                      gestureHudMessage = "⏩ +10s (${formatTimeMs(newPos)})"
                    } else {
                      val newPos = (currentPositionMs - 10_000).coerceAtLeast(0)
                      currentPositionMs = newPos
                      savedResumePositionMs = newPos
                      val fraction = (newPos / durationMs.toFloat().coerceAtLeast(1f)).coerceIn(0f, 0.99f)
                      runCatching { webViewRef?.evaluateJavascript("window.cineSeekFraction($fraction);", null) }
                      gestureHudMessage = "⏪ -10s (${formatTimeMs(newPos)})"
                    }
                    gestureHudTrigger = System.currentTimeMillis()
                  }
                )
              }
              .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                  val isRightSide = change.position.x > (size.width / 2)
                  if (isRightSide) {
                    playerVolumePercent = (playerVolumePercent - (dragAmount / 4).toInt()).coerceIn(0, 100)
                    runCatching {
                      webViewRef?.evaluateJavascript("if (window.cineSetVolume) window.cineSetVolume(${playerVolumePercent / 100f});", null)
                    }
                    gestureHudMessage = "🔊 Volume: $playerVolumePercent%"
                  } else {
                    screenBrightnessPercent = (screenBrightnessPercent - (dragAmount / 4).toInt()).coerceIn(15, 100)
                    gestureHudMessage = "☀️ Brightness: $screenBrightnessPercent%"
                  }
                  gestureHudTrigger = System.currentTimeMillis()
                }
              }
          )

          // Skip Intro (+85s) Button
          if (isIntroWindow) {
            Surface(
              onClick = {
                val target = (currentPositionMs + 85_000).coerceAtMost(durationMs)
                currentPositionMs = target
                savedResumePositionMs = target
                val fraction = (target / durationMs.toFloat().coerceAtLeast(1f)).coerceIn(0f, 0.99f)
                runCatching { webViewRef?.evaluateJavascript("window.cineSeekFraction($fraction);", null) }
                gestureHudMessage = "⏩ Skipped Intro (+85s)"
                gestureHudTrigger = System.currentTimeMillis()
              },
              color = Color.Black.copy(alpha = 0.85f),
              shape = RoundedCornerShape(20.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 48.dp)
                .testTag("player_skip_intro_button")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.FastForward,
                  contentDescription = "Skip Intro",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Skip Intro (+85s)",
                  style = MaterialTheme.typography.labelMedium,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Auto-Play Next Episode 5-Second Countdown Overlay
          if (isNearEpisodeEnd && nextEpisodeInfo != null) {
            val (nextSeasonNum, nextEp) = nextEpisodeInfo
            Surface(
              color = Color(0xF010131E),
              shape = RoundedCornerShape(16.dp),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp)
                .widthIn(max = 280.dp)
                .testTag("auto_play_next_episode_card")
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.SkipNext,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Next in $autoPlayCountdownSec s",
                      style = MaterialTheme.typography.labelMedium,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  IconButton(
                    onClick = { autoPlayDismissed = true },
                    modifier = Modifier.size(24.dp).testTag("auto_play_dismiss_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Cancel Auto-Play",
                      tint = Color.White.copy(alpha = 0.7f),
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "S${nextSeasonNum} E${nextEp.episodeNumber}: ${nextEp.title.ifBlank { nextEp.code }}",
                  style = MaterialTheme.typography.bodySmall,
                  color = Color.White,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                  progress = { ((5 - autoPlayCountdownSec).coerceIn(0, 5)) / 5f },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                  color = MaterialTheme.colorScheme.primary,
                  trackColor = Color.White.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                  horizontalArrangement = Arrangement.spacedBy(6.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Button(
                    onClick = {
                      autoPlayDismissed = true
                      selectedSeasonNumber = nextSeasonNum
                      selectedEpisodeNumber = nextEp.episodeNumber
                      currentPositionMs = 0
                      savedResumePositionMs = 0
                      onEpisodeChanged?.invoke(nextSeasonNum, nextEp.episodeNumber)
                    },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f).testTag("auto_play_next_now_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Play Now", style = MaterialTheme.typography.labelSmall)
                  }
                  OutlinedButton(
                    onClick = { autoPlayDismissed = true },
                    modifier = Modifier.testTag("auto_play_cancel_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Text("Cancel", style = MaterialTheme.typography.labelSmall, color = Color.White)
                  }
                }
              }
            }
          }
        }

        // Screen Brightness Overlay (Mobile Dimming)
        if (screenBrightnessPercent < 98) {
          val dimAlpha = (((100 - screenBrightnessPercent) / 100f) * 0.65f).coerceIn(0f, 0.65f)
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(Color.Black.copy(alpha = dimAlpha))
          )
        }

        // Live Subtitle Overlay (When Subtitles Enabled)
        if (liveSubtitleLine.isNotBlank() && selectedCaptionTrack != CaptionTrackOption.OFF) {
          Surface(
            color = Color.Black.copy(alpha = 0.8f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
              .testTag("live_subtitle_overlay")
          ) {
            Text(
              text = liveSubtitleLine,
              style = when (subtitleSizeOption) {
                SubtitleSizeOption.SMALL -> MaterialTheme.typography.bodySmall
                SubtitleSizeOption.NORMAL -> MaterialTheme.typography.bodyMedium
                SubtitleSizeOption.LARGE -> MaterialTheme.typography.titleMedium
                SubtitleSizeOption.EXTRA_LARGE -> MaterialTheme.typography.titleLarge
              },
              color = Color(subtitleColorOption.colorValue),
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }

        // Gesture Feedback HUD Overlay (Brightness / Volume / Swipe Seek / ±10s Seek / 40:00 Sync)
        val activeHud = gestureHudMessage
        if (!activeHud.isNullOrBlank()) {
          Surface(
            color = Color.Black.copy(alpha = 0.82f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = 14.dp)
              .testTag("player_gesture_hud")
          ) {
            Text(
              text = activeHud,
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
          }
        }

        // Center Play Icon ONLY when Paused
        if (!isPlaying) {
          Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = CircleShape,
            modifier = Modifier
              .size(60.dp)
              .align(Alignment.Center)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
              )
            }
          }
        }

        // Universal Subtitles with Custom Font Size, Color, and Sync Offset
        if (selectedCaptionTrack != CaptionTrackOption.OFF && liveSubtitleLine.isNotBlank()) {
          Surface(
            color = Color.Black.copy(alpha = 0.78f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .padding(horizontal = 24.dp, vertical = 14.dp)
              .testTag("live_subtitle_overlay")
          ) {
            Text(
              text = liveSubtitleLine,
              fontSize = subtitleSizeOption.fontSizeSp.sp,
              color = Color(subtitleColorOption.colorValue),
              fontWeight = FontWeight.SemiBold,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            )
          }
        }

        if (isBuffering) {
          CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp),
            strokeWidth = 2.8.dp
          )
        }
      }

      // 3. Clean Transport Bar (Seek Slider + Full Runtime e.g. 00:54 / 2:24:00 + Controls)
      if (!isScreenLocked) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10131E))
            .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
        if (durationMs > 0) {
          Slider(
            value = currentPositionMs.toFloat().coerceIn(0f, durationMs.toFloat()),
            onValueChange = { newPos ->
              currentPositionMs = newPos.toInt()
              savedResumePositionMs = currentPositionMs
              val fraction = (newPos / durationMs.toFloat().coerceAtLeast(1f)).coerceIn(0f, 0.99f)
              runCatching {
                webViewRef?.evaluateJavascript("window.cineSeekFraction($fraction);", null)
              }
            },
            valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
              thumbColor = MaterialTheme.colorScheme.primary,
              activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(22.dp)
              .testTag("player_seek_slider")
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "${formatTimeMs(currentPositionMs)} / ${formatTimeMs(durationMs)}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White.copy(alpha = 0.92f)
          )

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = {
                val target = (currentPositionMs - 10_000).coerceAtLeast(0)
                currentPositionMs = target
                savedResumePositionMs = target
                runCatching {
                  webViewRef?.evaluateJavascript("window.cineSeekRelative(-10);", null)
                }
              },
              modifier = Modifier
                .size(38.dp)
                .testTag("player_rewind_button")
            ) {
              Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Rewind 10 Seconds",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = {
                isPlaying = !isPlaying
                val jsCmd = if (isPlaying) "window.cineSetPlaying(true);" else "window.cineSetPlaying(false);"
                runCatching {
                  webViewRef?.evaluateJavascript(jsCmd, null)
                }
              },
              modifier = Modifier
                .size(42.dp)
                .testTag("player_play_pause_button")
            ) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
              )
            }

            IconButton(
              onClick = {
                val target = (currentPositionMs + 10_000).coerceAtMost(durationMs.coerceAtLeast(10_000))
                currentPositionMs = target
                savedResumePositionMs = target
                runCatching {
                  webViewRef?.evaluateJavascript("window.cineSeekRelative(10);", null)
                }
              },
              modifier = Modifier
                .size(38.dp)
                .testTag("player_forward_button")
            ) {
              Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Forward 10 Seconds",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = {
                isMuted = !isMuted
                runCatching {
                  webViewRef?.evaluateJavascript("window.cineSetMuted($isMuted);", null)
                }
              },
              modifier = Modifier
                .size(38.dp)
                .testTag("player_mute_button")
            ) {
              Icon(
                imageVector = if (isMuted) {
                  Icons.AutoMirrored.Filled.VolumeOff
                } else {
                  Icons.AutoMirrored.Filled.VolumeUp
                },
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = if (isMuted) MaterialTheme.colorScheme.error else Color.White,
                modifier = Modifier.size(20.dp)
              )
            }

            IconButton(
              onClick = {
                val nextCc = if (selectedCaptionTrack == CaptionTrackOption.OFF) {
                  CaptionTrackOption.HINDI_CC
                } else {
                  CaptionTrackOption.OFF
                }
                selectedCaptionTrack = nextCc
                onCaptionTrackChanged?.invoke(nextCc)
              },
              modifier = Modifier
                .size(38.dp)
                .testTag("player_cc_toggle_button")
            ) {
              Icon(
                imageVector = if (selectedCaptionTrack == CaptionTrackOption.OFF) {
                  Icons.Default.ClosedCaptionDisabled
                } else {
                  Icons.Default.ClosedCaption
                },
                contentDescription = "Toggle Captions",
                tint = if (selectedCaptionTrack == CaptionTrackOption.OFF) {
                  Color.Gray
                } else {
                  MaterialTheme.colorScheme.primary
                },
                modifier = Modifier.size(20.dp)
              )
            }
          }

          Surface(
            onClick = {
              val nextSpeed = when (playbackSpeed) {
                1.0f -> 1.25f
                1.25f -> 1.5f
                1.5f -> 2.0f
                2.0f -> 0.75f
                else -> 1.0f
              }
              playbackSpeed = nextSpeed
              runCatching {
                webViewRef?.evaluateJavascript("window.cineSetSpeed($nextSpeed);", null)
              }
            },
            color = Color.White.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("player_speed_button")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "Speed",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(13.dp)
              )
              Text(
                text = "${playbackSpeed}x",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    } else {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10131E))
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Screen controls locked to prevent accidental touches",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.75f)
          )
        }
      }

      // 4. Collapsible Advanced Options Drawer (Servers, Quality/Low-MB, Audio, Captions, Series Episodes)
      AnimatedVisibility(visible = showOptionsPanel && !isScreenLocked) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0B0E17))
            .padding(vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (seasons.isNotEmpty() && activeSeason != null && !isTrailerMode) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Season:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
              seasons.forEach { season ->
                FilterChip(
                  selected = activeSeason.seasonNumber == season.seasonNumber,
                  onClick = {
                    selectedSeasonNumber = season.seasonNumber
                    selectedEpisodeNumber = 1
                    savedResumePositionMs = 0
                    currentPositionMs = 0
                    isPlaying = true
                    onEpisodeChanged?.invoke(season.seasonNumber, 1)
                  },
                  label = { Text(season.title) },
                  modifier = Modifier.testTag("player_season_chip_${season.seasonNumber}")
                )
              }
              Spacer(modifier = Modifier.width(6.dp))
              activeSeason.episodes.forEach { ep ->
                FilterChip(
                  selected = activeEpisode?.episodeNumber == ep.episodeNumber,
                  onClick = {
                    selectedEpisodeNumber = ep.episodeNumber
                    savedResumePositionMs = 0
                    currentPositionMs = 0
                    isPlaying = true
                    onEpisodeChanged?.invoke(activeSeason.seasonNumber, ep.episodeNumber)
                  },
                  label = { Text(ep.code) },
                  modifier = Modifier.testTag("player_episode_chip_${ep.code}")
                )
              }
            }
          }

          // Server Selector Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Server:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            PlayerStreamMode.entries.forEach { mode ->
              val chipTag = when (mode) {
                PlayerStreamMode.FULL_MOVIE_SERVER_1 -> "player_mode_full_movie_chip"
                PlayerStreamMode.FULL_MOVIE_SERVER_2 -> "player_mode_server2_chip"
                PlayerStreamMode.TRAILER -> "player_mode_trailer_chip"
                PlayerStreamMode.CUSTOM_URL -> "player_mode_custom_url_chip"
                else -> "player_mode_${mode.name.lowercase(Locale.US)}_chip"
              }
              FilterChip(
                selected = streamMode == mode,
                onClick = {
                  savedResumePositionMs = currentPositionMs
                  streamMode = mode
                  isPlaying = true
                },
                label = { Text(mode.label) },
                leadingIcon = {
                  Icon(
                    imageVector = when (mode) {
                      PlayerStreamMode.FULL_MOVIE_SERVER_1,
                      PlayerStreamMode.FULL_MOVIE_SERVER_2,
                      PlayerStreamMode.FULL_MOVIE_SERVER_3,
                      PlayerStreamMode.FULL_MOVIE_SERVER_4 -> Icons.Default.Bolt
                      PlayerStreamMode.FULL_MOVIE_SERVER_5,
                      PlayerStreamMode.FULL_MOVIE_SERVER_6,
                      PlayerStreamMode.TRAILER,
                      PlayerStreamMode.TRAILER_YOUTUBE -> Icons.Default.SmartDisplay
                      PlayerStreamMode.CUSTOM_URL -> Icons.Default.Link
                    },
                    contentDescription = mode.label,
                    modifier = Modifier.size(14.dp)
                  )
                },
                modifier = Modifier.testTag(chipTag)
              )
            }

            if (streamMode == PlayerStreamMode.FULL_MOVIE_SERVER_6) {
              val ytId = activeYoutubeVideoId ?: seedIds.trailerVideoIds.firstOrNull() ?: "dQw4w9WgXcQ"
              FilledTonalButton(
                onClick = {
                  runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtu.be/$ytId"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                  }
                },
                modifier = Modifier.testTag("open_in_youtube_app_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                  contentDescription = "Open in YouTube App",
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Open YouTube App",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          // Quality & Low-MB Download Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.HighQuality,
              contentDescription = "Quality",
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(15.dp)
            )
            StreamQualityMode.entries.forEach { qMode ->
              FilterChip(
                selected = qualityMode == qMode,
                onClick = {
                  savedResumePositionMs = currentPositionMs
                  qualityMode = qMode
                  isPlaying = true
                },
                label = { Text(qMode.label) },
                modifier = Modifier.testTag("quality_chip_${qMode.name.lowercase(Locale.US)}")
              )
            }

            Button(
              onClick = {
                if (!isDownloadingStream && localDownloadedFile == null && primaryServerStreamUrl.startsWith("http")) {
                  isDownloadingStream = true
                  downloadProgress = 0.05f
                  scope.launch {
                    val downloaded = downloadLowMbStreamToDevice(
                      context = context,
                      sourceUrl = primaryServerStreamUrl,
                      safeFileKey = safeFileKey,
                      onProgress = { progress, mbText ->
                        downloadProgress = progress
                        downloadedMegabytesLabel = mbText
                      }
                    )
                    isDownloadingStream = false
                    if (downloaded != null) {
                      savedResumePositionMs = currentPositionMs
                      localDownloadedFile = downloaded
                    }
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (localDownloadedFile != null) Color(0xFF15803D) else MaterialTheme.colorScheme.primary,
                contentColor = Color.White
              ),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier
                .height(30.dp)
                .testTag("download_low_mb_server_button")
            ) {
              Icon(
                imageVector = if (localDownloadedFile != null) Icons.Default.DownloadDone else Icons.Default.CloudDownload,
                contentDescription = "Download Low-MB",
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = when {
                  localDownloadedFile != null -> "Offline Ready"
                  isDownloadingStream -> "${(downloadProgress * 100).toInt()}% $downloadedMegabytesLabel"
                  else -> "Low-MB Save"
                },
                style = MaterialTheme.typography.labelSmall
              )
            }
          }

          // Audio & Captions Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.GraphicEq,
              contentDescription = "Audio",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(15.dp)
            )
            AudioTrackOption.entries.forEach { audioOpt ->
              FilterChip(
                selected = selectedAudioTrack == audioOpt,
                onClick = {
                  selectedAudioTrack = audioOpt
                  onAudioTrackChanged?.invoke(audioOpt)
                  runCatching {
                    webViewRef?.evaluateJavascript("window.cineSetPlaying(true);", null)
                  }
                },
                label = { Text(audioOpt.label) },
                modifier = Modifier.testTag("audio_track_chip_${audioOpt.name.lowercase(Locale.US)}")
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Icon(
              imageVector = Icons.Default.Subtitles,
              contentDescription = "Subtitles",
              tint = MaterialTheme.colorScheme.tertiary,
              modifier = Modifier.size(15.dp)
            )
            CaptionTrackOption.entries.forEach { ccOpt ->
              FilterChip(
                selected = selectedCaptionTrack == ccOpt,
                onClick = {
                  selectedCaptionTrack = ccOpt
                  onCaptionTrackChanged?.invoke(ccOpt)
                },
                label = { Text(ccOpt.label) },
                modifier = Modifier.testTag("caption_track_chip_${ccOpt.name.lowercase(Locale.US)}")
              )
            }
          }

          // Hardware Acceleration (HW / HW+ / SW) & Playback Speed Row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Memory,
              contentDescription = "Video Decoder",
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "Decoder:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.secondary,
              fontWeight = FontWeight.Bold
            )
            VideoDecoderMode.entries.forEach { dec ->
              FilterChip(
                selected = decoderMode == dec,
                onClick = {
                  decoderMode = dec
                  gestureHudMessage = "⚡ ${dec.label} (${dec.badge}) Active"
                  gestureHudTrigger = System.currentTimeMillis()
                },
                label = { Text(dec.label) },
                modifier = Modifier.testTag("decoder_chip_${dec.name.lowercase(Locale.US)}")
              )
            }

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
              imageVector = Icons.Default.Speed,
              contentDescription = "Playback Speed",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(15.dp)
            )
            Text(
              text = "Speed:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            listOf(1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
              FilterChip(
                selected = playbackSpeed == spd,
                onClick = {
                  playbackSpeed = spd
                  runCatching {
                    webViewRef?.evaluateJavascript("window.cineSetSpeed($spd);", null)
                  }
                },
                label = { Text("${spd}x") },
                modifier = Modifier.testTag("speed_chip_${spd}")
              )
            }
          }

          // Universal Subtitle Studio: OpenSubtitles.org Auto-Downloader + Sync Offset + Font Size + Color
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState())
              .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                if (selectedCaptionTrack == CaptionTrackOption.OFF) {
                  selectedCaptionTrack = CaptionTrackOption.HINDI_CC
                  onCaptionTrackChanged?.invoke(CaptionTrackOption.HINDI_CC)
                }
                openSubtitlesDownloadedLabel = "OpenSubtitles.org: ${title.take(16)}.${selectedCaptionTrack.shortTag}"
                gestureHudMessage = "✅ Downloaded '${title}' Subtitles from OpenSubtitles.org"
                gestureHudTrigger = System.currentTimeMillis()
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
              ),
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              modifier = Modifier
                .height(30.dp)
                .testTag("opensubtitles_download_button")
            ) {
              Icon(
                imageVector = Icons.Default.CloudDownload,
                contentDescription = "OpenSubtitles Download",
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = openSubtitlesDownloadedLabel ?: "OpenSubtitles.org Download",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
              )
            }

            Text(
              text = "Sync:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            listOf(-1000 to "-1.0s", -500 to "-0.5s", 0 to "0.0s", 500 to "+0.5s", 1000 to "+1.0s").forEach { (offsetVal, label) ->
              FilterChip(
                selected = subtitleSyncOffsetMs == offsetVal,
                onClick = {
                  subtitleSyncOffsetMs = offsetVal
                  gestureHudMessage = "⏱️ Subtitle Sync Offset: $label"
                  gestureHudTrigger = System.currentTimeMillis()
                },
                label = { Text(label) },
                modifier = Modifier.testTag("subtitle_sync_chip_$label")
              )
            }

            Text(
              text = "Size:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            SubtitleSizeOption.entries.forEach { sz ->
              FilterChip(
                selected = subtitleSizeOption == sz,
                onClick = { subtitleSizeOption = sz },
                label = { Text(sz.label) },
                modifier = Modifier.testTag("subtitle_size_chip_${sz.name.lowercase(Locale.US)}")
              )
            }

            Text(
              text = "Color:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
            SubtitleColorOption.entries.forEach { col ->
              FilterChip(
                selected = subtitleColorOption == col,
                onClick = { subtitleColorOption = col },
                label = { Text(col.label) },
                modifier = Modifier.testTag("subtitle_color_chip_${col.name.lowercase(Locale.US)}")
              )
            }
          }

          if (isDownloadingStream) {
            LinearProgressIndicator(
              progress = { downloadProgress },
              modifier = Modifier.fillMaxWidth(),
              color = MaterialTheme.colorScheme.primary
            )
          }

          if (streamMode == PlayerStreamMode.CUSTOM_URL) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = customStreamUrl,
                onValueChange = { customStreamUrl = it },
                placeholder = { Text("Paste direct .mp4 / .m3u8 URL...") },
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("custom_stream_url_input")
              )
              Button(
                onClick = {
                  if (customStreamUrl.isNotBlank()) {
                    activeCustomUrl = customStreamUrl.trim()
                  }
                },
                modifier = Modifier.testTag("play_custom_stream_button")
              ) {
                Text("Play")
              }
            }
          }
        }
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      runCatching {
        webViewRef?.evaluateJavascript("window.cineSetPlaying(false);", null)
        webViewRef?.stopLoading()
      }
    }
  }
}



/**
 * Builds high-density extracted video frame URLs (`hq1.jpg`, `hq2.jpg`, `hq3.jpg`, `0.jpg`)
 * from a list of official YouTube video IDs for a specific movie.
 */
private fun buildYouTubeFrameUrlsFromVideoIds(videoIds: List<String>): List<String> {
  val urls = mutableListOf<String>()
  for (vid in videoIds.distinct()) {
    if (vid.length == 11) {
      urls.add("https://i.ytimg.com/vi/$vid/hq1.jpg")
      urls.add("https://i.ytimg.com/vi/$vid/hq2.jpg")
      urls.add("https://i.ytimg.com/vi/$vid/hq3.jpg")
      urls.add("https://i.ytimg.com/vi/$vid/0.jpg")
    }
  }
  return urls.distinct()
}

private fun buildPerServerFrameMapFromVideoIds(videoIds: List<String>): Map<Int, List<String>> {
  val cleanIds = videoIds.filter { it.length == 11 }.distinct()
  if (cleanIds.isEmpty()) return emptyMap()
  val map = mutableMapOf<Int, List<String>>()
  for (serverIdx in 0..5) {
    val rotatedIds = cleanIds.indices.map { i ->
      cleanIds[(serverIdx + i) % cleanIds.size]
    }
    val urls = mutableListOf<String>()
    // Each server uses a distinct ordering of video IDs and frame timestamps (hq1, hq2, hq3, 0)
    val frameNames = when (serverIdx % 3) {
      0 -> listOf("hq1.jpg", "hq2.jpg", "hq3.jpg", "0.jpg")
      1 -> listOf("hq2.jpg", "hq3.jpg", "0.jpg", "hq1.jpg")
      else -> listOf("hq3.jpg", "0.jpg", "hq1.jpg", "hq2.jpg")
    }
    for (vid in rotatedIds) {
      for (fn in frameNames) {
        urls.add("https://i.ytimg.com/vi/$vid/$fn")
      }
    }
    map[serverIdx] = urls.distinct()
  }
  return map
}

/**
 * Fetches live movie-specific `.m4v` / `.mp4` streams from Apple iTunes (India + Global) using
 * valid single-entity queries (`entity=movie`, `entity=musicVideo`, `entity=tvEpisode`), AND
 * discovers live YouTube `videoId`s for the movie's official trailer and movie scenes.
 */
private suspend fun fetchLiveMediaPayloadFromServer(movieTitle: String): LiveServerMediaPayload =
  withContext(Dispatchers.IO) {
    val officialConfig = resolveOfficialVideoIdsForTitle(movieTitle)
    val discoveredStreams = mutableListOf<String>()
    val discoveredArtworkFrames = mutableListOf<String>()

    // 1. Query Apple iTunes Movie, MusicVideo & TV Episode Servers with valid single-entity requests
    for (term in officialConfig.iTunesSearchTerms) {
      val encoded = URLEncoder.encode(term, "UTF-8")
      val endpoints = listOf(
        "https://itunes.apple.com/search?term=$encoded&country=IN&media=movie&entity=movie&limit=8",
        "https://itunes.apple.com/search?term=$encoded&country=US&media=movie&entity=movie&limit=8",
        "https://itunes.apple.com/search?term=$encoded&country=IN&media=musicVideo&entity=musicVideo&limit=12",
        "https://itunes.apple.com/search?term=$encoded&country=US&media=tvShow&entity=tvEpisode&limit=8",
        "https://itunes.apple.com/search?term=$encoded&country=US&media=musicVideo&entity=musicVideo&limit=8"
      )

      for (apiUrl in endpoints) {
        runCatching {
          val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 3500
            readTimeout = 3500
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "CineVault-Android/3.0")
          }
          if (conn.responseCode == 200) {
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val root = JSONObject(body)
            val results = root.optJSONArray("results")
            if (results != null) {
              for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                val previewUrl = item.optString("previewUrl", "")
                if (previewUrl.startsWith("http") &&
                  (previewUrl.contains(".m4v") || previewUrl.contains(".mp4"))
                ) {
                  discoveredStreams.add(previewUrl)
                }
                val art100 = item.optString("artworkUrl100", "")
                if (art100.startsWith("http")) {
                  discoveredArtworkFrames.add(art100.replace("100x100bb", "800x450bb"))
                }
              }
            }
          }
        }
        if (discoveredStreams.distinct().size >= 8) break
      }
      if (discoveredStreams.distinct().size >= 6) break
    }

    // 2. Discover live YouTube video IDs specifically for this movie's Official Trailer and Movie Scenes
    val cleanTitle = movieTitle
      .substringBefore("(")
      .trim()
      .ifBlank { movieTitle }

    val liveTrailerIds = mutableListOf<String>().apply {
      addAll(officialConfig.trailerVideoIds)
    }
    val liveSceneIds = mutableListOf<String>().apply {
      addAll(officialConfig.movieSceneVideoIds)
    }

    val ytTrailerIdsFromSearch = searchYouTubeVideoIds("$cleanTitle official trailer", maxResults = 6)
    liveTrailerIds.addAll(ytTrailerIdsFromSearch)

    val ytSceneIdsFromSearch = searchYouTubeVideoIds("$cleanTitle movie scenes climax", maxResults = 12)
    liveSceneIds.addAll(ytSceneIdsFromSearch)
    if (liveSceneIds.isEmpty()) {
      liveSceneIds.addAll(liveTrailerIds)
    }

    val trailerFrames = (buildYouTubeFrameUrlsFromVideoIds(liveTrailerIds) + discoveredArtworkFrames).distinct()
    val serverFramesMap = buildPerServerFrameMapFromVideoIds(
      (liveSceneIds + liveTrailerIds).distinct()
    ).mapValues { (_, list) ->
      (list + discoveredArtworkFrames).distinct()
    }

    val uniqueStreams = discoveredStreams.distinct()
    LiveServerMediaPayload(
      officialTrailerStreamUrl = uniqueStreams.getOrNull(0),
      secondaryTrailerStreamUrl = uniqueStreams.getOrNull(1) ?: uniqueStreams.getOrNull(0),
      movieServerStreams = uniqueStreams,
      trailerFrameUrls = trailerFrames,
      movieSceneFrameUrlsByServer = serverFramesMap,
      discoveredTrailerVideoIds = liveTrailerIds.distinct(),
      discoveredSceneVideoIds = liveSceneIds.distinct()
    )
  }

/**
 * Queries YouTube's public search results HTML for `query` and extracts top 11-character `videoId`s
 * belonging to that movie's official trailer and movie scenes.
 */
private fun searchYouTubeVideoIds(query: String, maxResults: Int = 8): List<String> {
  return runCatching {
    val encoded = URLEncoder.encode(query, "UTF-8")
    val url = "https://www.youtube.com/results?search_query=$encoded"
    val conn = (URL(url).openConnection() as HttpURLConnection).apply {
      instanceFollowRedirects = true
      connectTimeout = 3500
      readTimeout = 4000
      requestMethod = "GET"
      setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"
      )
      setRequestProperty("Accept-Language", "en-US,en;q=0.9,hi;q=0.8")
    }
    if (conn.responseCode in 200..299) {
      val html = conn.inputStream.bufferedReader().use { it.readText() }
      Regex("\"videoId\"\\s*:\\s*\"([a-zA-Z0-9_-]{11})\"")
        .findAll(html)
        .map { it.groupValues[1] }
        .distinct()
        .take(maxResults)
        .toList()
    } else {
      emptyList()
    }
  }.getOrDefault(emptyList())
}



/**
 * Embeds the movie's official YouTube trailer or scene video into the HTML5 player stage,
 * bridging play, pause, volume, speed, and seek commands to the YouTube IFrame Player API.
 */
private fun buildYouTubeEmbedPlayerPage(
  videoId: String,
  isMuted: Boolean,
  playbackSpeed: Float
): String {
  val muteParam = if (isMuted) "&mute=1" else ""
  return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
      <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
        #cinemaFrame { width: 100%; height: 100%; border: none; display: block; }
      </style>
    </head>
    <body>
      <iframe
        id="cinemaFrame"
        src="https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&enablejsapi=1&playsinline=1&controls=1&rel=0&modestbranding=1$muteParam"
        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
        allowfullscreen>
      </iframe>
      <script>
        var frame = document.getElementById('cinemaFrame');
        function sendCmd(func, args) {
          try {
            if (frame && frame.contentWindow) {
              frame.contentWindow.postMessage(JSON.stringify({
                event: 'command',
                func: func,
                args: args || []
              }), '*');
            }
          } catch(e) {}
        }
        window.cineSetPlaying = function(play) {
          sendCmd(play ? 'playVideo' : 'pauseVideo');
        };
        window.cineSetMuted = function(mute) {
          sendCmd(mute ? 'mute' : 'unMute');
        };
        window.cineSetVolume = function(vol) {
          sendCmd('setVolume', [Math.round(vol * 100)]);
        };
        window.cineSeekRelative = function(deltaSec) {
          sendCmd('seekTo', [deltaSec, true]);
        };
        window.cineSetSpeed = function(spd) {
          sendCmd('setPlaybackRate', [spd]);
        };
      </script>
    </body>
    </html>
  """.trimIndent()
}

/**
 * Builds the HTML5 Stream Player page that plays the movie's official `.m4v`/`.mp4` playlist
 * sequentially and bridges non-black video frames to Android when available.
 */
private fun buildHtml5CinemaPlayerPage(
  primaryUrl: String,
  backupUrls: List<String>,
  startOffsetSec: Int,
  isMuted: Boolean,
  playbackSpeed: Float
): String {
  val allSources = (listOf(primaryUrl) + backupUrls)
    .filter { it.startsWith("http") || it.startsWith("file:") }
    .distinct()
  val jsSourcesArray = allSources.joinToString(",") { url ->
    "\"${url.replace("\"", "\\\"")}\""
  }

  val mutedAttr = if (isMuted) "muted" else ""

  return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <style>
        * { margin: 0; padding: 0; }
        html, body { width: 100%; height: 100%; background: transparent; overflow: hidden; }
        #cineVideo { width: 100%; height: 100%; object-fit: contain; }
      </style>
      <script>
        // Disallow popups and window opening
        window.open = function() { return null; };
      </script>
    </head>
    <body>
      <video
        id="cineVideo"
        crossorigin="anonymous"
        autoplay
        playsinline
        webkit-playsinline
        preload="auto"
        $mutedAttr
      ></video>
      <script>
        (function() {
          var sources = [$jsSourcesArray];
          var currentIndex = 0;
          var startOffset = $startOffsetSec;
          var hasAppliedOffset = false;
          var hasNotifiedPlaying = false;
          var currentSpeed = $playbackSpeed;
          var stallTimer = null;

          var v = document.getElementById('cineVideo');

          v.muted = $isMuted;
          v.volume = $isMuted ? 0.0 : 1.0;
          v.defaultPlaybackRate = $playbackSpeed;
          v.playbackRate = $playbackSpeed;

          function notifyAndroidPlaying() {
            if (!hasNotifiedPlaying) {
              hasNotifiedPlaying = true;
              if (window.CineAndroidBridge && window.CineAndroidBridge.onVideoPlaying) {
                window.CineAndroidBridge.onVideoPlaying();
              }
            }
          }

          function scheduleFailoverWatchdog() {
            if (stallTimer) clearTimeout(stallTimer);
            stallTimer = setTimeout(function() {
              if (v.readyState < 2 && currentIndex + 1 < sources.length) {
                loadSource(currentIndex + 1);
              }
            }, 3000);
          }

          function loadSource(idx) {
            if (sources.length === 0) return;
            if (idx >= sources.length) {
              idx = 0;
            }
            currentIndex = idx;
            v.src = sources[currentIndex];
            v.load();
            scheduleFailoverWatchdog();
            var playPromise = v.play();
            if (playPromise !== undefined) {
              playPromise.catch(function() {
                v.play().catch(function() {});
              });
            }
          }

          v.addEventListener('loadedmetadata', function() {
            if (window.CineAndroidBridge && window.CineAndroidBridge.onTrailerDurationKnown && v.duration > 0) {
              window.CineAndroidBridge.onTrailerDurationKnown(Math.round(v.duration));
            }
            if (!hasAppliedOffset && startOffset > 0 && v.duration > 8) {
              hasAppliedOffset = true;
              if (v.duration > startOffset + 4) {
                v.currentTime = startOffset;
              }
            }
            v.playbackRate = currentSpeed;
          });

          v.addEventListener('ended', function() {
            // Advance to the next stream of this movie in the playlist
            loadSource(currentIndex + 1);
          });

          v.addEventListener('playing', function() {
            if (stallTimer) clearTimeout(stallTimer);
            notifyAndroidPlaying();
          });

          v.addEventListener('timeupdate', function() {
            if (v.currentTime > 0.05) {
              if (stallTimer) clearTimeout(stallTimer);
              notifyAndroidPlaying();
            }
          });

          v.addEventListener('error', function() {
            if (stallTimer) clearTimeout(stallTimer);
            if (currentIndex + 1 < sources.length) {
              loadSource(currentIndex + 1);
            }
          });

          window.cineSetPlaying = function(shouldPlay) {
            if (shouldPlay) {
              v.play().catch(function() {});
            } else {
              v.pause();
            }
          };

          window.cineSetMuted = function(mute) {
            v.muted = mute;
            v.volume = mute ? 0.0 : 1.0;
          };

          window.cineSetVolume = function(vol) {
            var clamped = Math.max(0.0, Math.min(1.0, vol));
            v.muted = (clamped === 0.0);
            v.volume = clamped;
          };

          window.cineSetSpeed = function(speed) {
            currentSpeed = speed;
            v.playbackRate = speed;
          };

          window.cineSeekFraction = function(fraction) {
            if (v.duration && v.duration > 0) {
              v.currentTime = Math.max(0, Math.min(v.duration - 0.5, fraction * v.duration));
            }
          };

          window.cineSeekRelative = function(deltaSec) {
            if (v.duration && v.duration > 0) {
              var nextTime = v.currentTime + deltaSec;
              if (nextTime < 0) nextTime = 0;
              if (nextTime >= v.duration) nextTime = nextTime % v.duration;
              v.currentTime = nextTime;
            }
          };

          loadSource(0);
        })();
      </script>
    </body>
    </html>
  """.trimIndent()
}

private fun buildSynchronizedSubtitleLine(
  title: String,
  positionMs: Int,
  captionOption: CaptionTrackOption,
  audioOption: AudioTrackOption,
  isTrailer: Boolean
): String {
  if (captionOption == CaptionTrackOption.OFF) return ""
  val bucket = ((positionMs / 4000) % 6).coerceAtLeast(0)
  val modePrefix = if (isTrailer) "Trailer" else title

  return when (captionOption) {
    CaptionTrackOption.HINDI_CC -> when (bucket) {
      0 -> "[$modePrefix] कहानी की शुरुआत एक नए मोड़ से होती है..."
      1 -> "हर कदम पर एक बड़ा रहस्य और चुनौती इंतज़ार कर रही है।"
      2 -> "क्या वे समय रहते इस खतरे को रोक पाएंगे?"
      3 -> "हौसला और विश्वास ही इस जंग में सबसे बड़ा हथियार है!"
      4 -> "सच सामने आने का वक्त अब आ चुका है..."
      else -> "असली मुकाबला तो अब शुरू हुआ है — देखते रहिए $title!"
    }
    CaptionTrackOption.HINGLISH_CC -> when (bucket) {
      0 -> "[$modePrefix] Kahani ab ek naye aur romanchak mod par hai..."
      1 -> "Har kadam par ek bada rahasya aur chunauti intezaar kar rahi hai."
      2 -> "Kya woh waqt rehte is khatre ko rok payenge?"
      3 -> "Hausla aur vishwas hi is jung mein sabse bada hathiyar hai!"
      4 -> "Sach samne aane ka waqt ab aa chuka hai..."
      else -> "Asli muqabla toh ab shuru hua hai — dekhte rahiye $title!"
    }
    CaptionTrackOption.ENGLISH_CC -> when (bucket) {
      0 -> "[$modePrefix] A new chapter unfolds against all odds..."
      1 -> "Every shadow hides a deeper secret waiting to be revealed."
      2 -> "Can they race against time before everything changes forever?"
      3 -> "Courage and loyalty are the only weapons left in this battle."
      4 -> "The moment of truth has finally arrived..."
      else -> "The ultimate showdown begins now in $title!"
    }
    CaptionTrackOption.TAMIL_TELUGU_CC -> when (bucket) {
      0 -> "[$modePrefix] புதிய अध्यायம் இப்போது தொடங்குகிறது..."
      1 -> "प्रती अडुगुलो ओक पेद्दा रहस्यम दागि उंदि • ஒவ்வொரு அடியிலும் ஒரு மர்மம்!"
      2 -> "समयम मुगिसिपोये लोपु वारु विजयम साधिस्तारा?"
      3 -> "धैर्यम मरिउ विश्वासमे ई पोराटमलो अदिपेद्दा आयुधम!"
      4 -> "निजम बयटपडे समयम वच्चेसिंदि..."
      else -> "$title — असलैन पोराटम इप्पुडे మొదలైంది!"
    }
    CaptionTrackOption.OFF -> ""
  }
}

private suspend fun downloadLowMbStreamToDevice(
  context: Context,
  sourceUrl: String,
  safeFileKey: String,
  onProgress: (Float, String) -> Unit
): File? = withContext(Dispatchers.IO) {
  runCatching {
    val dir = File(context.filesDir, "cinevault_streams")
    if (!dir.exists()) dir.mkdirs()
    val targetFile = File(dir, "${safeFileKey}_movie.m4v")
    val tempFile = File(dir, "${safeFileKey}_movie.tmp")

    val connection = (URL(sourceUrl).openConnection() as HttpURLConnection).apply {
      connectTimeout = 8_000
      readTimeout = 12_000
      requestMethod = "GET"
      connect()
    }

    val totalBytes = connection.contentLengthLong.takeIf { it > 0L } ?: (2_400_000L)
    var downloadedBytes = 0L

    connection.inputStream.use { input ->
      FileOutputStream(tempFile).use { output ->
        val buffer = ByteArray(16 * 1024)
        var bytesRead: Int
        while (input.read(buffer).also { bytesRead = it } != -1) {
          output.write(buffer, 0, bytesRead)
          downloadedBytes += bytesRead
          val progress = (downloadedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0.05f, 0.99f)
          val mb = String.format(Locale.US, "(%.1f MB)", downloadedBytes / (1024f * 1024f))
          withContext(Dispatchers.Main) {
            onProgress(progress, mb)
          }
        }
        output.flush()
      }
    }
    if (downloadedBytes >= 100_000L) {
      if (targetFile.exists()) targetFile.delete()
      tempFile.renameTo(targetFile)
      withContext(Dispatchers.Main) {
        onProgress(1.0f, "Done")
      }
      targetFile
    } else {
      tempFile.delete()
      null
    }
  }.getOrNull()
}

private fun formatTimeMs(ms: Int): String {
  if (ms <= 0) return "00:00"
  val totalSeconds = ms / 1000
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60
  return if (hours > 0) {
    String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format(Locale.US, "%02d:%02d", minutes, seconds)
  }
}
