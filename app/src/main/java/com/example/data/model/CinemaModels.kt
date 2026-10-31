package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlin.math.abs

enum class MediaType(val label: String) {
  MOVIE("Movie"),
  SERIES("Series")
}

enum class ReleaseStatus(val label: String) {
  RELEASED("Released"),
  UPCOMING("Coming Soon")
}

enum class WatchStatus(val label: String, val hindiLabel: String) {
  WANT_TO_WATCH("Want to Watch", "Dekhna Hai (Want to Watch)"),
  WATCHED("Watched", "Dekh Chuke (Watched)")
}

data class CastMember(
  val id: String,
  val name: String,
  val characterName: String,
  val profileUrl: String,
  val bio: String = "",
  val knownForDepartment: String = "Acting"
)

data class SeriesEpisode(
  val seasonNumber: Int,
  val episodeNumber: Int,
  val title: String,
  val duration: String,
  val overview: String,
  val streamUrl: String,
  val lowMbStreamUrl: String
) {
  val code: String
    get() = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"
}

data class SeriesSeason(
  val seasonNumber: Int,
  val title: String,
  val episodes: List<SeriesEpisode>
)

data class UserProfile(
  val id: String,
  val name: String,
  val avatarEmoji: String,
  val badgeLabel: String,
  val isKids: Boolean = false,
  val isPinProtected: Boolean = false,
  val pinCode: String = ""
) {
  companion object {
    val DEFAULT_PROFILES = listOf(
      UserProfile("main", "Main Profile", "🎬", "Personal • 4K HDR", isKids = false, isPinProtected = true, pinCode = "1234"),
      UserProfile("family", "Family Lounge", "🍿", "Living Room TV", isKids = false),
      UserProfile("action_fan", "Action & Sci-Fi", "⚡", "Thriller & IMAX", isKids = false),
      UserProfile("kids", "Kids & Family", "🌟", "U/A 7+ Safe Mode", isKids = true)
    )
  }
}

data class WatchHistoryEntry(
  val mediaId: String,
  val title: String,
  val mediaType: MediaType,
  val posterUrl: String,
  val backdropUrl: String,
  val releaseYear: String,
  val rating: Double,
  val positionMs: Int,
  val durationMs: Int,
  val seasonNumber: Int = 1,
  val episodeNumber: Int = 1,
  val lastWatchedAt: Long = System.currentTimeMillis(),
  val profileId: String = "main"
) {
  val progressFraction: Float
    get() = if (durationMs > 0) {
      (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0.05f, 0.98f)
    } else {
      0.25f
    }

  val formattedTimestamp: String
    get() {
      val posSec = (positionMs / 1000).coerceAtLeast(0)
      val durSec = (durationMs / 1000).coerceAtLeast(1)
      val pMin = posSec / 60
      val pRem = posSec % 60
      val dMin = durSec / 60
      val dRem = durSec % 60
      return String.format(java.util.Locale.US, "%02d:%02d / %02d:%02d", pMin, pRem, dMin, dRem)
    }

  val resumeBadgeText: String
    get() = if (mediaType == MediaType.SERIES) {
      "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')} • $formattedTimestamp"
    } else {
      "Resume • $formattedTimestamp"
    }

  fun toMediaItem(): MediaItem = MediaItem(
    id = mediaId,
    title = title,
    mediaType = mediaType,
    releaseStatus = ReleaseStatus.RELEASED,
    releaseYear = releaseYear,
    releaseDateFull = releaseYear,
    rating = rating,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl.ifBlank { posterUrl },
    overview = "Resume watching $title from $formattedTimestamp.",
    genres = listOf("Drama", "Action"),
    durationOrSeasons = if (mediaType == MediaType.SERIES) "3 Seasons" else "2h 18m"
  )
}

private val EPISODE_SUBTITLES_POOL = listOf(
  "The Beginning of the Storm" to "A mysterious signal sets off a chain of events that changes everyone's destiny.",
  "Shadows in the Dark" to "Hidden alliances come to light as the team investigates a high-stakes conspiracy.",
  "Point of No Return" to "Cornered from every side, a daring plan is launched before midnight.",
  "Crossroads & Betrayal" to "An unexpected revelation from an insider shakes the foundation of trust.",
  "Into the Fire" to "With time running out, the heroes risk everything in an intense confrontation.",
  "The Final Reckoning" to "All secrets collide in an explosive season finale that sets up the next chapter.",
  "Echoes of Tomorrow" to "After the fallout, a new clue emerges across the border.",
  "Rise of the Legend" to "The ultimate showdown determines who controls the future."
)

data class MediaItem(
  val id: String,
  val title: String,
  val mediaType: MediaType,
  val releaseStatus: ReleaseStatus,
  val releaseYear: String,
  val releaseDateFull: String = releaseYear,
  val rating: Double,
  val posterUrl: String,
  val backdropUrl: String = posterUrl,
  val overview: String,
  val genres: List<String>,
  val language: String = "Hindi / English",
  val durationOrSeasons: String = "2h 18m",
  val trailerUrl: String = "",
  val fullMovieUrl: String = "",
  val cast: List<CastMember> = emptyList(),
  val director: String = ""
) {
  val effectiveDirector: String
    get() = director.ifBlank {
      cast.firstOrNull {
        it.knownForDepartment.equals("Directing", ignoreCase = true) ||
          it.characterName.contains("Director", ignoreCase = true)
      }?.name.orEmpty()
    }

  fun buildSeasons(): List<SeriesSeason> {
    if (mediaType != MediaType.SERIES) return emptyList()
    val parsedSeasonCount = Regex("(\\d+)").find(durationOrSeasons)?.groupValues?.getOrNull(1)?.toIntOrNull()
      ?.coerceIn(1, 6) ?: 3
    val baseHash = abs(id.hashCode())

    return (1..parsedSeasonCount).map { sNum ->
      val episodeCount = 6 + ((baseHash + sNum) % 3) // 6 to 8 episodes per season
      val episodes = (1..episodeCount).map { eNum ->
        val templateIdx = (baseHash + (sNum * 3) + eNum) % EPISODE_SUBTITLES_POOL.size
        val (epName, epDesc) = EPISODE_SUBTITLES_POOL[templateIdx]
        val durationMin = 41 + ((baseHash + sNum + eNum * 5) % 18)

        SeriesEpisode(
          seasonNumber = sNum,
          episodeNumber = eNum,
          title = "Ep $eNum: $epName",
          duration = "${durationMin}m",
          overview = "[$title • Season $sNum, Episode $eNum] $epDesc",
          streamUrl = fullMovieUrl,
          lowMbStreamUrl = trailerUrl
        )
      }
      SeriesSeason(
        seasonNumber = sNum,
        title = "Season $sNum",
        episodes = episodes
      )
    }
  }
}

/**
 * Smart Search Engine matcher supporting:
 * - Movie / Series Title
 * - Actor / Cast Name & Character Name (including aliases like SRK, Big B, RDJ)
 * - Director Name (e.g., Christopher Nolan, Rajkumar Hirani, Atlee, Nag Ashwin, Hanu Raghavapudi)
 * - Specific & Compound Genres (e.g., "romantic dramas", "sci-fi thriller", "action comedy", "crime drama")
 */
object SmartSearchMatcher {
  private val ALIAS_MAP = mapOf(
    "srk" to "shah rukh khan",
    "shahrukh" to "shah rukh khan",
    "king khan" to "shah rukh khan",
    "big b" to "amitabh bachchan",
    "amitabh" to "amitabh bachchan",
    "rdj" to "robert downey jr",
    "leo" to "leonardo dicaprio",
    "nolan" to "christopher nolan",
    "hirani" to "rajkumar hirani",
    "raju hirani" to "rajkumar hirani",
    "vanga" to "sandeep reddy vanga",
    "villeneuve" to "denis villeneuve",
    "bhansali" to "sanjay leela bhansali",
    "slb" to "sanjay leela bhansali",
    "chopra" to "yash chopra",
    "imtiaz" to "imtiaz ali",
    "sukumar" to "sukumar",
    "atlee" to "atlee",
    "nag ashwin" to "nag ashwin"
  )

  private val GENRE_SYNONYMS = mapOf(
    "romance" to listOf("romance", "romantic", "love", "rom-com", "romcom", "couple", "heartfelt"),
    "drama" to listOf("drama", "dramas", "dramatic", "emotional", "family"),
    "action" to listOf("action", "actions", "fight", "battle", "combat", "high-octane", "masala"),
    "sci-fi" to listOf("sci-fi", "scifi", "science fiction", "futuristic", "space", "cyberpunk", "dystopian"),
    "thriller" to listOf("thriller", "thrillers", "suspense", "mystery", "psychological", "spy"),
    "crime" to listOf("crime", "crimes", "gangster", "underworld", "mafia", "syndicate", "heist", "police"),
    "comedy" to listOf("comedy", "comedies", "funny", "humor", "humour", "laugh", "lighthearted"),
    "mythology" to listOf("mythology", "mythological", "epic", "avatar", "ramayana", "astraverse", "ancient"),
    "biography" to listOf("biography", "biopic", "biopics", "biographical", "true story", "history", "historical"),
    "adventure" to listOf("adventure", "adventures", "journey", "quest", "exploration"),
    "fantasy" to listOf("fantasy", "magical", "supernatural", "mythic"),
    "superhero" to listOf("superhero", "superheroes", "marvel", "avengers", "multiverse")
  )

  fun normalizeToken(token: String): String {
    val clean = token.trim().lowercase().replace(Regex("[^a-z0-9\\-]"), "")
    return when {
      clean == "dramas" || clean == "dramatic" -> "drama"
      clean == "romances" || clean == "romantic" || clean == "lovestory" -> "romance"
      clean == "thrillers" || clean == "suspense" -> "thriller"
      clean == "comedies" || clean == "funny" -> "comedy"
      clean == "actions" -> "action"
      clean == "biopics" || clean == "biopic" || clean == "biographical" -> "biography"
      clean == "scifi" -> "sci-fi"
      clean == "adventures" -> "adventure"
      clean == "crimes" || clean == "gangster" -> "crime"
      clean == "mythological" -> "mythology"
      else -> clean
    }
  }

  fun expandQueryAliases(rawQuery: String): String {
    var q = rawQuery.trim().lowercase()
      .replace("sci fi", "sci-fi")
      .replace("science fiction", "sci-fi")
      .replace("rom com", "romance comedy")
      .replace("rom-com", "romance comedy")
      .replace("love story", "romance drama")
      .replace("love stories", "romance drama")
    ALIAS_MAP.forEach { (shortKey, fullVal) ->
      if (q == shortKey || q.split(Regex("\\s+")).contains(shortKey)) {
        q = "$q $fullVal"
      }
    }
    return q
  }

  fun extractGenreConcepts(rawQuery: String): List<String> {
    val expanded = expandQueryAliases(rawQuery)
    val tokens = expanded.split(Regex("[\\s,/]+")).map { normalizeToken(it) }.filter { it.isNotEmpty() }
    val matchedCanonicalGenres = mutableListOf<String>()
    for ((canonical, synonyms) in GENRE_SYNONYMS) {
      if (tokens.contains(canonical) || synonyms.any { syn -> expanded.contains(syn) }) {
        matchedCanonicalGenres.add(canonical)
      }
    }
    return matchedCanonicalGenres.distinct()
  }

  private fun itemMatchesCanonicalGenre(item: MediaItem, canonicalGenre: String): Boolean {
    val itemGenresLower = item.genres.map { it.lowercase() }
    val overviewLower = item.overview.lowercase()
    val synonyms = GENRE_SYNONYMS[canonicalGenre] ?: listOf(canonicalGenre)
    return itemGenresLower.any { g ->
      g.contains(canonicalGenre) || synonyms.any { syn -> g.contains(syn) }
    } || synonyms.any { syn -> syn.length >= 5 && overviewLower.contains(syn) }
  }

  data class MatchEvaluation(
    val score: Int,
    val badge: String?
  )

  fun evaluate(item: MediaItem, rawQuery: String): MatchEvaluation {
    val trimmed = rawQuery.trim()
    if (trimmed.isBlank()) return MatchEvaluation(0, null)

    val qLower = trimmed.lowercase()
    val expanded = expandQueryAliases(trimmed)
    val titleLower = item.title.lowercase()
    val directorLower = item.effectiveDirector.lowercase()
    val genresLower = item.genres.map { it.lowercase() }
    val overviewLower = item.overview.lowercase()
    val languageLower = item.language.lowercase()

    var score = 0
    var primaryBadge: String? = null

    // 1. Exact or substring Title match
    if (titleLower == qLower) {
      score += 180
      primaryBadge = "Title Match"
    } else if (titleLower.contains(qLower) && qLower.length >= 2) {
      score += 130
      primaryBadge = "Title Match"
    }

    // 2. Director match
    if (directorLower.isNotBlank()) {
      val dirTokens = directorLower.split(Regex("\\s+")).filter { it.length >= 3 }
      if (directorLower.contains(qLower) ||
        expanded.contains(directorLower) ||
        (qLower.length >= 4 && dirTokens.any { it.startsWith(qLower) || qLower.contains(it) })
      ) {
        score += 145
        if (primaryBadge == null) {
          primaryBadge = "Director: ${item.effectiveDirector}"
        }
      }
    }

    // 3. Actor / Cast match
    val matchedActor = item.cast.firstOrNull { member ->
      val nameLower = member.name.lowercase()
      val charLower = member.characterName.lowercase()
      val nameTokens = nameLower.split(Regex("\\s+")).filter { it.length >= 3 }
      nameLower.contains(qLower) ||
        expanded.contains(nameLower) ||
        (qLower.length >= 3 && nameTokens.any { it.startsWith(qLower) || qLower.contains(it) }) ||
        (qLower.length >= 4 && charLower.contains(qLower))
    }
    if (matchedActor != null) {
      score += 140
      if (primaryBadge == null) {
        primaryBadge = "Actor: ${matchedActor.name}"
      }
    }

    // 4. Specific & Compound Genre Match (e.g., "romantic dramas", "sci-fi thriller", "action drama")
    val genreConcepts = extractGenreConcepts(trimmed)
    if (genreConcepts.isNotEmpty()) {
      val matchedConcepts = genreConcepts.filter { concept -> itemMatchesCanonicalGenre(item, concept) }
      if (matchedConcepts.size == genreConcepts.size) {
        // Matched ALL requested genre concepts (e.g., both "romance" AND "drama" for "romantic dramas")
        score += 135 + (matchedConcepts.size * 25)
        if (primaryBadge == null) {
          val label = matchedConcepts.joinToString(" + ") { c ->
            c.replaceFirstChar { it.uppercase() }
          }
          primaryBadge = "Genre: $label"
        }
      } else if (matchedConcepts.isNotEmpty()) {
        // If user searched a compound genre with a specific qualifier like "romantic dramas",
        // only reward partial match if the specific qualifier ("romance") matched, not generic "drama" alone.
        val nonGenericConcepts = genreConcepts.filter { it != "drama" && it != "action" }
        val matchedSpecific = nonGenericConcepts.isEmpty() || nonGenericConcepts.all { it in matchedConcepts }
        if (matchedSpecific) {
          score += 75
          if (primaryBadge == null) {
            primaryBadge = "Genre: ${item.genres.joinToString(", ")}"
          }
        }
      }
    }

    // 5. Direct genre string or language or year match
    if (genresLower.any { it.contains(qLower) }) {
      score += 95
      if (primaryBadge == null) {
        primaryBadge = "Genre: ${item.genres.joinToString(", ")}"
      }
    }
    if (languageLower.contains(qLower) || item.releaseYear == qLower) {
      score += 70
      if (primaryBadge == null) {
        primaryBadge = "${item.language} • ${item.releaseYear}"
      }
    }

    // 6. Multi-word token match across title + director + cast + genres + overview
    val queryTokens = expanded
      .split(Regex("[\\s,]+"))
      .map { normalizeToken(it) }
      .filter { it.length >= 3 && it !in setOf("the", "and", "movie", "movies", "films", "film", "with") }

    if (queryTokens.isNotEmpty() && score == 0) {
      val searchableBlob = buildString {
        append(titleLower).append(" ")
        append(directorLower).append(" ")
        append(genresLower.joinToString(" ")).append(" ")
        append(item.cast.joinToString(" ") { "${it.name.lowercase()} ${it.characterName.lowercase()}" }).append(" ")
        append(overviewLower).append(" ")
        append(languageLower)
      }
      val matchedTokenCount = queryTokens.count { tok -> searchableBlob.contains(tok) }
      if (matchedTokenCount == queryTokens.size) {
        score += 65
        if (primaryBadge == null) primaryBadge = "Smart Match"
      } else if (matchedTokenCount > 0 && queryTokens.size == 1) {
        score += 45
        if (primaryBadge == null) primaryBadge = "Related Match"
      }
    }

    return MatchEvaluation(score = score, badge = primaryBadge)
  }
}

@IgnoreExtraProperties
data class WatchlistItem(
  @DocumentId val id: String = "",
  val userId: String = "",
  val mediaId: String = "",
  val title: String = "",
  val mediaType: String = MediaType.MOVIE.name,
  val status: String = WatchStatus.WANT_TO_WATCH.name,
  val releaseStatus: String = ReleaseStatus.RELEASED.name,
  val releaseYear: String = "2025",
  val rating: Double = 0.0,
  val posterUrl: String = "",
  val backdropUrl: String? = null,
  val overview: String = "",
  val trailerUrl: String? = null,
  val fullMovieUrl: String? = null,
  val genres: String = "",
  val castSummary: String? = null,
  val userNotes: String? = null,
  val userRating: Long? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
) {
  val parsedMediaType: MediaType
    get() = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.MOVIE)

  val parsedWatchStatus: WatchStatus
    get() = runCatching { WatchStatus.valueOf(status) }.getOrDefault(WatchStatus.WANT_TO_WATCH)

  val parsedReleaseStatus: ReleaseStatus
    get() = runCatching { ReleaseStatus.valueOf(releaseStatus) }.getOrDefault(ReleaseStatus.RELEASED)

  val profileId: String
    get() = parseProfileIdFromSummary(castSummary).ifBlank {
      val raw = mediaId.ifBlank { id }
      if (raw.contains("__")) raw.substringBefore("__") else "main"
    }

  val isFavorite: Boolean
    get() = parseFavoriteFromSummary(castSummary)

  val customTags: List<String>
    get() {
      val parsed = parseCustomTagsFromSummary(castSummary)
      return if (isFavorite && parsed.none { it.equals("Favorites", ignoreCase = true) }) {
        listOf("Favorites") + parsed
      } else {
        parsed
      }
    }

  val storageSource: String
    get() = parseStorageSourceFromSummary(castSummary)

  val isTraktSynced: Boolean
    get() = parseTraktSyncedFromSummary(castSummary)

  val canonicalMediaId: String
    get() {
      val raw = mediaId.ifBlank { id }
      return if (raw.contains("__")) raw.substringAfter("__") else raw
    }

  fun toMediaItem(): MediaItem {
    val parsedCast = parseCastSummary(castSummary)
    val parsedDirector = parseDirectorFromSummary(castSummary)
    val parsedGenres = genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    return MediaItem(
      id = canonicalMediaId,
      title = title,
      mediaType = parsedMediaType,
      releaseStatus = parsedReleaseStatus,
      releaseYear = releaseYear,
      releaseDateFull = releaseYear,
      rating = rating,
      posterUrl = posterUrl,
      backdropUrl = backdropUrl ?: posterUrl,
      overview = overview,
      genres = if (parsedGenres.isEmpty()) listOf("Drama") else parsedGenres,
      durationOrSeasons = if (parsedMediaType == MediaType.SERIES) "3 Seasons" else "2h 18m",
      trailerUrl = trailerUrl.orEmpty(),
      fullMovieUrl = fullMovieUrl.orEmpty(),
      cast = parsedCast,
      director = parsedDirector
    )
  }

  fun toWatchHistoryEntry(): WatchHistoryEntry? {
    val progress = parseProgressFromSummary(castSummary) ?: return null
    if (progress.positionMs <= 0) return null
    val fallbackTime = updatedAt?.toDate()?.time ?: createdAt?.toDate()?.time ?: System.currentTimeMillis()
    return WatchHistoryEntry(
      mediaId = canonicalMediaId,
      title = title,
      mediaType = parsedMediaType,
      posterUrl = posterUrl,
      backdropUrl = backdropUrl ?: posterUrl,
      releaseYear = releaseYear,
      rating = rating,
      positionMs = progress.positionMs,
      durationMs = progress.durationMs.coerceAtLeast(30_000),
      seasonNumber = progress.seasonNumber.coerceAtLeast(1),
      episodeNumber = progress.episodeNumber.coerceAtLeast(1),
      lastWatchedAt = if (progress.lastWatchedAt > 0L) progress.lastWatchedAt else fallbackTime,
      profileId = profileId
    )
  }

  data class CloudPlaybackProgress(
    val positionMs: Int,
    val durationMs: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val lastWatchedAt: Long
  )

  companion object {
    private const val PROGRESS_DELIMITER = "##CV_PROGRESS|"
    private const val DIRECTOR_DELIMITER = "##CV_DIRECTOR|"
    private const val META_DELIMITER = "##CV_META|"

    val DEFAULT_PLAYLIST_TAGS = listOf(
      "Weekend Watch",
      "Favorites",
      "Family",
      "Sci-Fi Vault",
      "Masterpieces"
    )

    fun buildProfileScopedDocId(rawMediaId: String, profileId: String = "main"): String {
      val cleanMedia = rawMediaId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(95).ifBlank { "media_default" }
      val cleanProf = profileId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(20).ifBlank { "main" }
      return if (cleanProf == "main") cleanMedia else "${cleanProf}__$cleanMedia"
    }

    fun encodeCastSummary(
      cast: List<CastMember>,
      progress: CloudPlaybackProgress? = null,
      director: String = "",
      profileId: String = "main",
      isFavorite: Boolean = false,
      customTags: List<String> = emptyList(),
      storageSource: String = "CineVault Cloud",
      isTraktSynced: Boolean = true
    ): String {
      val castPart = cast.take(12).joinToString(";;") { member ->
        listOf(
          member.id.replace("|", ""),
          member.name.replace("|", ""),
          member.characterName.replace("|", ""),
          member.profileUrl.replace("|", "")
        ).joinToString("|")
      }.take(3500)

      val directorPart = if (director.isNotBlank()) {
        "$DIRECTOR_DELIMITER${director.replace("|", " ").trim()}"
      } else {
        ""
      }

      val progressPart = if (progress != null && progress.positionMs > 0) {
        "$PROGRESS_DELIMITER${progress.positionMs}|${progress.durationMs}|${progress.seasonNumber}|${progress.episodeNumber}|${progress.lastWatchedAt}"
      } else {
        ""
      }

      val cleanProf = profileId.replace("|", "").ifBlank { "main" }
      val cleanTags = customTags
        .map { it.replace("|", "").replace(",", "").trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .joinToString(",")
      val cleanStorage = storageSource.replace("|", "").trim().ifBlank { "CineVault Cloud" }
      val metaPart = "$META_DELIMITER$cleanProf|${if (isFavorite) "1" else "0"}|$cleanTags|$cleanStorage|${if (isTraktSynced) "1" else "0"}"

      return (castPart + directorPart + progressPart + metaPart).take(4900)
    }

    fun parseCastSummary(summary: String?): List<CastMember> {
      if (summary.isNullOrBlank()) return emptyList()
      val cleanCastPart = summary
        .substringBefore(PROGRESS_DELIMITER)
        .substringBefore(DIRECTOR_DELIMITER)
        .substringBefore(META_DELIMITER)
      if (cleanCastPart.isBlank()) return emptyList()
      return cleanCastPart.split(";;").mapNotNull { entry ->
        val parts = entry.split("|")
        if (parts.size >= 3) {
          CastMember(
            id = parts[0].ifBlank { "actor_${parts[1].hashCode()}" },
            name = parts[1],
            characterName = parts[2],
            profileUrl = parts.getOrNull(3).orEmpty()
          )
        } else null
      }
    }

    fun parseDirectorFromSummary(summary: String?): String {
      if (summary.isNullOrBlank() || !summary.contains(DIRECTOR_DELIMITER)) return ""
      return summary
        .substringAfter(DIRECTOR_DELIMITER, "")
        .substringBefore(PROGRESS_DELIMITER)
        .substringBefore(META_DELIMITER)
        .trim()
    }

    fun parseProgressFromSummary(summary: String?): CloudPlaybackProgress? {
      if (summary.isNullOrBlank() || !summary.contains(PROGRESS_DELIMITER)) return null
      val raw = summary
        .substringAfter(PROGRESS_DELIMITER, "")
        .substringBefore(META_DELIMITER)
      val parts = raw.split("|")
      if (parts.size < 4) return null
      val pos = parts[0].toIntOrNull() ?: return null
      val dur = parts[1].toIntOrNull() ?: 105_000
      val season = parts[2].toIntOrNull() ?: 1
      val episode = parts[3].toIntOrNull() ?: 1
      val ts = parts.getOrNull(4)?.toLongOrNull() ?: 0L
      return CloudPlaybackProgress(
        positionMs = pos,
        durationMs = dur,
        seasonNumber = season,
        episodeNumber = episode,
        lastWatchedAt = ts
      )
    }

    fun parseProfileIdFromSummary(summary: String?): String {
      if (summary.isNullOrBlank() || !summary.contains(META_DELIMITER)) return "main"
      val raw = summary.substringAfter(META_DELIMITER, "")
      return raw.split("|").firstOrNull()?.trim()?.ifBlank { "main" } ?: "main"
    }

    fun parseFavoriteFromSummary(summary: String?): Boolean {
      if (summary.isNullOrBlank() || !summary.contains(META_DELIMITER)) return false
      val raw = summary.substringAfter(META_DELIMITER, "")
      val flag = raw.split("|").getOrNull(1)?.trim()
      return flag == "1" || flag.equals("true", ignoreCase = true)
    }

    fun parseCustomTagsFromSummary(summary: String?): List<String> {
      if (summary.isNullOrBlank() || !summary.contains(META_DELIMITER)) return emptyList()
      val raw = summary.substringAfter(META_DELIMITER, "")
      val tagsCsv = raw.split("|").getOrNull(2)?.trim().orEmpty()
      if (tagsCsv.isBlank()) return emptyList()
      return tagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.distinct()
    }

    fun parseStorageSourceFromSummary(summary: String?): String {
      if (summary.isNullOrBlank() || !summary.contains(META_DELIMITER)) return "CineVault Cloud"
      val raw = summary.substringAfter(META_DELIMITER, "")
      return raw.split("|").getOrNull(3)?.trim()?.ifBlank { "CineVault Cloud" } ?: "CineVault Cloud"
    }

    fun parseTraktSyncedFromSummary(summary: String?): Boolean {
      if (summary.isNullOrBlank() || !summary.contains(META_DELIMITER)) return true
      val raw = summary.substringAfter(META_DELIMITER, "")
      val flag = raw.split("|").getOrNull(4)?.trim() ?: "1"
      return flag != "0"
    }
  }
}

enum class SmartDownloadQuality(
  val label: String,
  val badge: String,
  val totalMb: Int
) {
  DATA_SAVER_480P("480p Data Saver", "~310 MB • Fast", 310),
  HD_720P("720p HD", "~680 MB • Balanced", 680),
  FULL_HD_1080P("1080p Full HD", "~1.42 GB • Cinema", 1420),
  UHD_4K("4K Ultra HD", "~3.10 GB • HDR10+", 3100)
}

enum class SmartDownloadState {
  DOWNLOADING,
  PAUSED,
  COMPLETED
}

data class SmartDownloadTask(
  val media: MediaItem,
  val quality: SmartDownloadQuality,
  val progressFraction: Float,
  val state: SmartDownloadState
) {
  val downloadedMb: Int
    get() = (quality.totalMb * progressFraction).toInt().coerceIn(0, quality.totalMb)
}

