package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ReleaseStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class CinemaNetworkClient(
  private val httpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(10, TimeUnit.SECONDS)
    .readTimeout(12, TimeUnit.SECONDS)
    .build()
) {
  private val tmdbApiKey: String
    get() = runCatching { BuildConfig.TMDB_API_KEY }.getOrDefault("")

  val isTmdbConfigured: Boolean
    get() = tmdbApiKey.isNotBlank() && tmdbApiKey != "MY_TMDB_API_KEY"

  private fun executeGet(url: String): String? {
    val request = Request.Builder()
      .url(url)
      .header("Accept", "application/json")
      .header("User-Agent", "CineVault-Android/1.0")
      .build()
    return runCatching {
      httpClient.newCall(request).execute().use { response ->
        if (response.isSuccessful) response.body?.string() else null
      }
    }.getOrNull()
  }

  /**
   * Fetches live movies with direct playable MP4 trailers from the public iTunes Movie API
   * and live global TV series from the public TVMaze API, plus TMDB when configured.
   */
  suspend fun fetchLiveGlobalFeed(): List<MediaItem> = withContext(Dispatchers.IO) {
    val combined = mutableListOf<MediaItem>()

    if (isTmdbConfigured) {
      combined.addAll(fetchTmdbList("movie/now_playing", MediaType.MOVIE, ReleaseStatus.RELEASED))
      combined.addAll(fetchTmdbList("movie/upcoming", MediaType.MOVIE, ReleaseStatus.UPCOMING))
      combined.addAll(fetchTmdbList("tv/popular", MediaType.SERIES, ReleaseStatus.RELEASED))
    }

    // Fetch live movies with playable MP4 trailers from iTunes Search API
    combined.addAll(fetchItunesMovies("action 2025"))
    // Fetch live global TV series with embedded cast from TVMaze API
    combined.addAll(fetchTvMazeSeries())

    combined.distinctBy { it.id }
  }

  suspend fun searchGlobalCatalog(query: String): List<MediaItem> = withContext(Dispatchers.IO) {
    val trimmed = query.trim()
    if (trimmed.isBlank()) return@withContext emptyList()

    val results = mutableListOf<MediaItem>()
    if (isTmdbConfigured) {
      results.addAll(searchTmdb(trimmed))
    }
    results.addAll(fetchItunesMovies(trimmed))
    results.addAll(searchTvMazeSeries(trimmed))
    results.distinctBy { it.id }
  }

  suspend fun fetchRichDetailsAndCast(item: MediaItem): MediaItem = withContext(Dispatchers.IO) {
    val baseEnriched = when {
      item.id.startsWith("tmdb_") && isTmdbConfigured -> enrichFromTmdb(item)
      item.id.startsWith("tvmaze_") -> enrichFromTvMaze(item)
      else -> item
    }

    // Resolve live official studio trailer & stream from iTunes Movie/Video Server
    val liveStreams = if (!baseEnriched.trailerUrl.contains("itunes.apple.com")) {
      fetchLiveItunesMovieStreams(baseEnriched.title)
    } else {
      listOf(baseEnriched.trailerUrl)
    }

    val resolvedTrailer = liveStreams.firstOrNull().orEmpty().ifBlank { baseEnriched.trailerUrl }
    val resolvedMovieStream = liveStreams.getOrNull(1)
      ?: liveStreams.firstOrNull()
      ?: baseEnriched.fullMovieUrl

    baseEnriched.copy(
      trailerUrl = resolvedTrailer,
      fullMovieUrl = resolvedMovieStream
    )
  }

  fun fetchLiveItunesTrailerUrl(movieTitle: String): String? {
    return fetchLiveItunesMovieStreams(movieTitle).firstOrNull()
  }

  fun fetchLiveItunesMovieStreams(movieTitle: String): List<String> {
    val cleanTitle = movieTitle
      .substringBefore("(")
      .substringBefore(":")
      .trim()
      .ifBlank { movieTitle }
    val encoded = URLEncoder.encode(cleanTitle, "UTF-8")
    val endpoints = listOf(
      "https://itunes.apple.com/search?term=$encoded&country=IN&media=movie&entity=movie&limit=6",
      "https://itunes.apple.com/search?term=$encoded&country=US&media=movie&entity=movie&limit=6",
      "https://itunes.apple.com/search?term=$encoded&country=IN&media=musicVideo&entity=musicVideo&limit=8"
    )
    val found = mutableListOf<String>()
    for (url in endpoints) {
      val body = executeGet(url) ?: continue
      runCatching {
        val root = JSONObject(body)
        val results = root.optJSONArray("results") ?: return@runCatching
        for (i in 0 until results.length()) {
          val obj = results.getJSONObject(i)
          val preview = obj.optString("previewUrl", "")
          if (preview.startsWith("http") && (preview.contains(".m4v") || preview.contains(".mp4"))) {
            found.add(preview)
          }
        }
      }
      if (found.distinct().size >= 4) break
    }
    return found.distinct()
  }

  suspend fun fetchActorFilmographyFromNetwork(actor: CastMember): List<MediaItem> = withContext(Dispatchers.IO) {
    val results = mutableListOf<MediaItem>()

    if (actor.id.startsWith("tmdb_person_") && isTmdbConfigured) {
      val personId = actor.id.removePrefix("tmdb_person_")
      val url = "https://api.themoviedb.org/3/person/$personId/combined_credits?api_key=$tmdbApiKey"
      val body = executeGet(url)
      if (!body.isNullOrBlank()) {
        runCatching {
          val json = JSONObject(body)
          val castArr = json.optJSONArray("cast") ?: JSONArray()
          for (i in 0 until minOf(castArr.length(), 25)) {
            val obj = castArr.getJSONObject(i)
            val mediaTypeStr = obj.optString("media_type", "movie")
            val mediaType = if (mediaTypeStr == "tv") MediaType.SERIES else MediaType.MOVIE
            val idNum = obj.optInt("id")
            val title = obj.optString("title").ifBlank { obj.optString("name", "Untitled") }
            val releaseDate = obj.optString("release_date").ifBlank { obj.optString("first_air_date", "2024") }
            val year = releaseDate.take(4).ifBlank { "2024" }
            val posterPath = obj.optString("poster_path", "")
            val posterUrl = if (posterPath.isNotBlank() && posterPath != "null") {
              "https://image.tmdb.org/t/p/w500$posterPath"
            } else {
              "https://i.ytimg.com/vi/COv52Qyctws/hqdefault.jpg"
            }
            results.add(
              MediaItem(
                id = "tmdb_${mediaTypeStr}_$idNum",
                title = title,
                mediaType = mediaType,
                releaseStatus = if (year >= "2026") ReleaseStatus.UPCOMING else ReleaseStatus.RELEASED,
                releaseYear = year,
                releaseDateFull = releaseDate.ifBlank { year },
                rating = obj.optDouble("vote_average", 7.8),
                posterUrl = posterUrl,
                backdropUrl = posterUrl,
                overview = obj.optString("overview").ifBlank { "Starring ${actor.name} as ${obj.optString("character", "Lead")}." },
                genres = listOf("Cinema", mediaType.label),
                trailerUrl = "",
                fullMovieUrl = "",
                cast = listOf(actor.copy(characterName = obj.optString("character", actor.characterName)))
              )
            )
          }
        }
      }
    }

    if (actor.id.startsWith("tvmaze_person_")) {
      val personId = actor.id.removePrefix("tvmaze_person_")
      val url = "https://api.tvmaze.com/people/$personId/castcredits?embed=show"
      val body = executeGet(url)
      if (!body.isNullOrBlank()) {
        runCatching {
          val arr = JSONArray(body)
          for (i in 0 until minOf(arr.length(), 20)) {
            val credit = arr.getJSONObject(i)
            val embedded = credit.optJSONObject("_embedded")
            val show = embedded?.optJSONObject("show") ?: continue
            parseTvMazeShow(show, listOf(actor))?.let { results.add(it) }
          }
        }
      }
    }

    // Also search iTunes movies by actor name so any clicked actor gets live movies
    results.addAll(fetchItunesMovies(actor.name, requiredActor = actor))
    results.distinctBy { it.id }
  }

  private fun fetchItunesMovies(term: String, requiredActor: CastMember? = null): List<MediaItem> {
    val encoded = URLEncoder.encode(term, "UTF-8")
    val url = "https://itunes.apple.com/search?term=$encoded&media=movie&entity=movie&limit=15"
    val body = executeGet(url) ?: return emptyList()
    val items = mutableListOf<MediaItem>()
    runCatching {
      val root = JSONObject(body)
      val results = root.optJSONArray("results") ?: JSONArray()
      for (i in 0 until results.length()) {
        val obj = results.getJSONObject(i)
        val trackId = obj.optLong("trackId", 0L)
        if (trackId == 0L) continue
        val title = obj.optString("trackName", "").ifBlank { continue }
        val directorOrArtist = obj.optString("artistName", "Featured Cast")
        val releaseDate = obj.optString("releaseDate", "2024-01-01")
        val year = releaseDate.take(4).ifBlank { "2024" }
        val rawArtwork = obj.optString("artworkUrl100", "")
        val hiResPoster = rawArtwork.replace("100x100bb", "600x900bb")
        val backdrop = rawArtwork.replace("100x100bb", "1000x600bb")
        val previewTrailer = obj.optString("previewUrl", "")
        val rawGenre = obj.optString("primaryGenreName", "Cinema")
        val enrichedGenres = buildList {
          add(rawGenre)
          if (rawGenre.contains("Romance", ignoreCase = true) && !rawGenre.contains("Drama", ignoreCase = true)) {
            add("Drama")
          }
          add("Movie")
        }.distinct()
        val description = obj.optString("longDescription")
          .ifBlank { obj.optString("shortDescription", "Global feature film directed by $directorOrArtist.") }

        val castList = mutableListOf<CastMember>()
        if (requiredActor != null) {
          castList.add(requiredActor)
        }
        castList.add(
          CastMember(
            id = "itunes_artist_${directorOrArtist.replace(Regex("[^a-zA-Z0-9]"), "_").lowercase()}",
            name = directorOrArtist,
            characterName = "Director / Lead",
            profileUrl = hiResPoster,
            knownForDepartment = "Directing"
          )
        )

        items.add(
          MediaItem(
            id = "itunes_movie_$trackId",
            title = title,
            mediaType = MediaType.MOVIE,
            releaseStatus = if (year >= "2026") ReleaseStatus.UPCOMING else ReleaseStatus.RELEASED,
            releaseYear = year,
            releaseDateFull = releaseDate.take(10),
            rating = 8.1,
            posterUrl = hiResPoster,
            backdropUrl = backdrop,
            overview = description,
            genres = enrichedGenres,
            language = "English / Multi-Audio",
            durationOrSeasons = "${(obj.optLong("trackTimeMillis", 7200000L) / 60000L).coerceAtLeast(90)} min",
            trailerUrl = previewTrailer,
            fullMovieUrl = previewTrailer,
            cast = castList.distinctBy { it.id },
            director = directorOrArtist
          )
        )
      }
    }
    return items
  }

  private fun fetchTvMazeSeries(): List<MediaItem> {
    val body = executeGet("https://api.tvmaze.com/shows?page=0") ?: return emptyList()
    val items = mutableListOf<MediaItem>()
    runCatching {
      val arr = JSONArray(body)
      for (i in 0 until minOf(arr.length(), 16)) {
        val show = arr.getJSONObject(i)
        parseTvMazeShow(show)?.let { items.add(it) }
      }
    }
    return items
  }

  private fun searchTvMazeSeries(query: String): List<MediaItem> {
    val encoded = URLEncoder.encode(query, "UTF-8")
    val body = executeGet("https://api.tvmaze.com/search/shows?q=$encoded") ?: return emptyList()
    val items = mutableListOf<MediaItem>()
    runCatching {
      val arr = JSONArray(body)
      for (i in 0 until minOf(arr.length(), 12)) {
        val show = arr.getJSONObject(i).optJSONObject("show") ?: continue
        parseTvMazeShow(show)?.let { items.add(it) }
      }
    }
    return items
  }

  private fun parseTvMazeShow(show: JSONObject, fallbackCast: List<CastMember> = emptyList()): MediaItem? {
    val id = show.optInt("id", 0)
    if (id == 0) return null
    val name = show.optString("name", "").ifBlank { return null }
    val premiered = show.optString("premiered", "2024-01-01")
    val year = premiered.take(4).ifBlank { "2024" }
    val statusStr = show.optString("status", "Running")
    val ratingObj = show.optJSONObject("rating")
    val avgRating = ratingObj?.optDouble("average", 8.0)?.let { if (it.isNaN()) 8.0 else it } ?: 8.0
    val imageObj = show.optJSONObject("image")
    val poster = imageObj?.optString("original")?.ifBlank { imageObj.optString("medium") }
      ?: "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=600&auto=format&fit=crop&q=80"
    val rawSummary = show.optString("summary", "")
      .replace(Regex("<[^>]*>"), "")
      .trim()
      .ifBlank { "Acclaimed television series." }

    val genresArr = show.optJSONArray("genres") ?: JSONArray()
    val genres = mutableListOf<String>()
    for (g in 0 until genresArr.length()) {
      genres.add(genresArr.getString(g))
    }
    if (genres.isEmpty()) genres.add("Drama")

    return MediaItem(
      id = "tvmaze_$id",
      title = name,
      mediaType = MediaType.SERIES,
      releaseStatus = if (statusStr.equals("In Development", ignoreCase = true) || year >= "2026") {
        ReleaseStatus.UPCOMING
      } else {
        ReleaseStatus.RELEASED
      },
      releaseYear = year,
      releaseDateFull = "$year • $statusStr",
      rating = avgRating,
      posterUrl = poster,
      backdropUrl = poster,
      overview = rawSummary,
      genres = genres,
      language = show.optString("language", "English"),
      durationOrSeasons = "${show.optInt("runtime", 50)}m / ep",
      trailerUrl = "",
      fullMovieUrl = "",
      cast = fallbackCast
    )
  }

  private fun enrichFromTvMaze(item: MediaItem): MediaItem {
    val rawId = item.id.removePrefix("tvmaze_")
    val body = executeGet("https://api.tvmaze.com/shows/$rawId?embed=cast") ?: return item
    return runCatching {
      val json = JSONObject(body)
      val embedded = json.optJSONObject("_embedded")
      val castArr = embedded?.optJSONArray("cast") ?: JSONArray()
      val castMembers = mutableListOf<CastMember>()
      for (i in 0 until minOf(castArr.length(), 15)) {
        val entry = castArr.getJSONObject(i)
        val person = entry.optJSONObject("person") ?: continue
        val character = entry.optJSONObject("character")
        val personId = person.optInt("id", i + 1)
        val personName = person.optString("name", "Cast Member")
        val charName = character?.optString("name", "Main Role") ?: "Main Role"
        val personImg = person.optJSONObject("image")?.optString("medium").orEmpty()
        castMembers.add(
          CastMember(
            id = "tvmaze_person_$personId",
            name = personName,
            characterName = charName,
            profileUrl = personImg,
            bio = "Cast member of ${item.title} (${person.optString("country")})."
          )
        )
      }
      if (castMembers.isNotEmpty()) item.copy(cast = castMembers) else item
    }.getOrDefault(item)
  }

  private fun fetchTmdbList(endpoint: String, mediaType: MediaType, defaultReleaseStatus: ReleaseStatus): List<MediaItem> {
    val url = "https://api.themoviedb.org/3/$endpoint?api_key=$tmdbApiKey&language=en-US&page=1"
    val body = executeGet(url) ?: return emptyList()
    val items = mutableListOf<MediaItem>()
    runCatching {
      val root = JSONObject(body)
      val results = root.optJSONArray("results") ?: JSONArray()
      for (i in 0 until minOf(results.length(), 15)) {
        val obj = results.getJSONObject(i)
        val id = obj.optInt("id")
        val title = obj.optString("title").ifBlank { obj.optString("name", "Untitled") }
        val dateStr = obj.optString("release_date").ifBlank { obj.optString("first_air_date", "2025") }
        val year = dateStr.take(4).ifBlank { "2025" }
        val posterPath = obj.optString("poster_path", "")
        val backdropPath = obj.optString("backdrop_path", "")
        val posterUrl = if (posterPath.isNotBlank() && posterPath != "null") {
          "https://image.tmdb.org/t/p/w500$posterPath"
        } else {
          "https://i.ytimg.com/vi/COv52Qyctws/hqdefault.jpg"
        }
        val backdropUrl = if (backdropPath.isNotBlank() && backdropPath != "null") {
          "https://image.tmdb.org/t/p/w780$backdropPath"
        } else posterUrl
        val typePrefix = if (mediaType == MediaType.SERIES) "tv" else "movie"

        items.add(
          MediaItem(
            id = "tmdb_${typePrefix}_$id",
            title = title,
            mediaType = mediaType,
            releaseStatus = defaultReleaseStatus,
            releaseYear = year,
            releaseDateFull = dateStr.ifBlank { year },
            rating = obj.optDouble("vote_average", 7.8),
            posterUrl = posterUrl,
            backdropUrl = backdropUrl,
            overview = obj.optString("overview", "Global cinema release."),
            genres = listOf(mediaType.label, defaultReleaseStatus.label),
            trailerUrl = "",
            fullMovieUrl = ""
          )
        )
      }
    }
    return items
  }

  private fun searchTmdb(query: String): List<MediaItem> {
    val encoded = URLEncoder.encode(query, "UTF-8")
    val url = "https://api.themoviedb.org/3/search/multi?api_key=$tmdbApiKey&query=$encoded"
    val body = executeGet(url) ?: return emptyList()
    val items = mutableListOf<MediaItem>()
    runCatching {
      val root = JSONObject(body)
      val results = root.optJSONArray("results") ?: JSONArray()
      for (i in 0 until minOf(results.length(), 15)) {
        val obj = results.getJSONObject(i)
        val mediaTypeStr = obj.optString("media_type", "movie")
        if (mediaTypeStr != "movie" && mediaTypeStr != "tv") continue
        val mediaType = if (mediaTypeStr == "tv") MediaType.SERIES else MediaType.MOVIE
        val id = obj.optInt("id")
        val title = obj.optString("title").ifBlank { obj.optString("name", "Untitled") }
        val dateStr = obj.optString("release_date").ifBlank { obj.optString("first_air_date", "2025") }
        val year = dateStr.take(4).ifBlank { "2025" }
        val posterPath = obj.optString("poster_path", "")
        val posterUrl = if (posterPath.isNotBlank() && posterPath != "null") {
          "https://image.tmdb.org/t/p/w500$posterPath"
        } else {
          "https://i.ytimg.com/vi/COv52Qyctws/hqdefault.jpg"
        }
        items.add(
          MediaItem(
            id = "tmdb_${mediaTypeStr}_$id",
            title = title,
            mediaType = mediaType,
            releaseStatus = if (year >= "2026") ReleaseStatus.UPCOMING else ReleaseStatus.RELEASED,
            releaseYear = year,
            releaseDateFull = dateStr.ifBlank { year },
            rating = obj.optDouble("vote_average", 7.8),
            posterUrl = posterUrl,
            backdropUrl = posterUrl,
            overview = obj.optString("overview", "Global cinema release."),
            genres = listOf(mediaType.label),
            trailerUrl = "",
            fullMovieUrl = ""
          )
        )
      }
    }
    return items
  }

  private fun enrichFromTmdb(item: MediaItem): MediaItem {
    val parts = item.id.split("_")
    if (parts.size < 3) return item
    val typePrefix = parts[1]
    val rawId = parts[2]
    val url = "https://api.themoviedb.org/3/$typePrefix/$rawId?api_key=$tmdbApiKey&append_to_response=credits,videos"
    val body = executeGet(url) ?: return item
    return runCatching {
      val json = JSONObject(body)
      val credits = json.optJSONObject("credits")
      val castArr = credits?.optJSONArray("cast") ?: JSONArray()
      val castList = mutableListOf<CastMember>()
      for (i in 0 until minOf(castArr.length(), 15)) {
        val c = castArr.getJSONObject(i)
        val pId = c.optInt("id")
        val profilePath = c.optString("profile_path", "")
        val profileUrl = if (profilePath.isNotBlank() && profilePath != "null") {
          "https://image.tmdb.org/t/p/w185$profilePath"
        } else ""
        castList.add(
          CastMember(
            id = "tmdb_person_$pId",
            name = c.optString("name", "Actor"),
            characterName = c.optString("character", "Lead Role"),
            profileUrl = profileUrl
          )
        )
      }

      var resolvedTrailer = item.trailerUrl
      val videos = json.optJSONObject("videos")?.optJSONArray("results") ?: JSONArray()
      for (v in 0 until videos.length()) {
        val vid = videos.getJSONObject(v)
        if (vid.optString("site").equals("YouTube", ignoreCase = true)) {
          val key = vid.optString("key")
          if (key.isNotBlank()) {
            resolvedTrailer = "https://www.youtube.com/embed/$key?autoplay=1"
            break
          }
        }
      }

      item.copy(
        cast = if (castList.isNotEmpty()) castList else item.cast,
        trailerUrl = resolvedTrailer
      )
    }.getOrDefault(item)
  }
}
