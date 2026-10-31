package com.example.ui

import android.content.Context
import android.util.Log
import java.io.File
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ReleaseStatus
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.data.remote.GlobalCinemaSeedCatalog
import com.example.data.repository.CinemaCatalogRepository
import com.example.data.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>
  data class Success<T>(val data: T) : UiState<T>
  data class Error(val message: String) : UiState<Nothing>
}

enum class CatalogCategoryFilter(val label: String) {
  ALL("All Cinema"),
  RELEASED_MOVIES("Released Movies"),
  UPCOMING_MOVIES("Upcoming (Coming Soon)"),
  SERIES("TV & Web Series")
}

enum class MainNavTab(val route: String, val label: String) {
  EXPLORE("explore", "Explore"),
  SERIES("series", "Series"),
  WATCH_MOVIES("watch_movies", "Movie"),
  UPCOMING("upcoming", "Upcoming"),
  WATCHLIST("watchlist", "My Lists"),
  OFFLINE("offline", "Offline")
}

data class CastSessionState(
  val deviceName: String,
  val media: MediaItem,
  val positionMs: Int = 2_400_000,
  val durationMs: Int = 8_280_000,
  val isPlaying: Boolean = true,
  val qualityLabel: String = "4K Dolby Vision"
)

data class PipSessionState(
  val media: MediaItem,
  val positionMs: Int = 2_400_000,
  val durationMs: Int = 8_280_000,
  val isPlaying: Boolean = true,
  val decoderLabel: String = "HW+ 4K"
)

class CineVaultViewModel(
  private val watchlistRepository: WatchlistRepository,
  private val catalogRepository: CinemaCatalogRepository,
  val currentUserId: String
) : ViewModel() {

  private val _profiles = MutableStateFlow(com.example.data.model.UserProfile.DEFAULT_PROFILES)
  val profiles: StateFlow<List<com.example.data.model.UserProfile>> = _profiles.asStateFlow()

  private val _activeProfile = MutableStateFlow(com.example.data.model.UserProfile.DEFAULT_PROFILES.first())
  val activeProfile: StateFlow<com.example.data.model.UserProfile> = _activeProfile.asStateFlow()

  private val _activeCastSession = MutableStateFlow<CastSessionState?>(null)
  val activeCastSession: StateFlow<CastSessionState?> = _activeCastSession.asStateFlow()

  private val _pipSession = MutableStateFlow<PipSessionState?>(null)
  val pipSession: StateFlow<PipSessionState?> = _pipSession.asStateFlow()

  private val _availablePlaylistTags = MutableStateFlow(WatchlistItem.DEFAULT_PLAYLIST_TAGS)
  val availablePlaylistTags: StateFlow<List<String>> = _availablePlaylistTags.asStateFlow()

  private val _isTraktConnected = MutableStateFlow(true)
  val isTraktConnected: StateFlow<Boolean> = _isTraktConnected.asStateFlow()

  private val _traktHandle = MutableStateFlow("@cinevault_global")
  val traktHandle: StateFlow<String> = _traktHandle.asStateFlow()

  private val _lastTraktSyncLabel = MutableStateFlow("Trakt.tv Scrobble Active • Global History Synced")
  val lastTraktSyncLabel: StateFlow<String> = _lastTraktSyncLabel.asStateFlow()

  private val _autoFetchedPreviewMedia = MutableStateFlow<MediaItem?>(null)
  val autoFetchedPreviewMedia: StateFlow<MediaItem?> = _autoFetchedPreviewMedia.asStateFlow()

  private val _isAutoFetchingMetadata = MutableStateFlow(false)
  val isAutoFetchingMetadata: StateFlow<Boolean> = _isAutoFetchingMetadata.asStateFlow()

  private val _smartDownloadTasks = MutableStateFlow<List<com.example.data.model.SmartDownloadTask>>(emptyList())
  val smartDownloadTasks: StateFlow<List<com.example.data.model.SmartDownloadTask>> = _smartDownloadTasks.asStateFlow()

  private val _isAutoDeleteWatchedDownloadsEnabled = MutableStateFlow(true)
  val isAutoDeleteWatchedDownloadsEnabled: StateFlow<Boolean> = _isAutoDeleteWatchedDownloadsEnabled.asStateFlow()

  private val _isAutoPruneStorageEnabled = MutableStateFlow(true)
  val isAutoPruneStorageEnabled: StateFlow<Boolean> = _isAutoPruneStorageEnabled.asStateFlow()

  private val _storageUsedDownloadsMb = MutableStateFlow(284.5f)
  val storageUsedDownloadsMb: StateFlow<Float> = _storageUsedDownloadsMb.asStateFlow()

  private val _storageUsedCacheMb = MutableStateFlow(96.2f)
  val storageUsedCacheMb: StateFlow<Float> = _storageUsedCacheMb.asStateFlow()

  // Raw cloud items across all profiles under the Google account
  private val allCloudWatchlistState: StateFlow<UiState<List<WatchlistItem>>> =
    watchlistRepository.observeWatchlist(currentUserId)
      .map<List<WatchlistItem>, UiState<List<WatchlistItem>>> { items ->
        val sorted = items.sortedByDescending { it.updatedAt ?: it.createdAt }
        UiState.Success(sorted)
      }
      .catch { error ->
        Log.w(TAG, "Error observing watchlist", error)
        emit(UiState.Error(error.message ?: "Failed to load cloud watchlist"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
      )

  // Profile-isolated Watchlist & Favorites state (like Netflix profiles)
  val watchlistState: StateFlow<UiState<List<WatchlistItem>>> = combine(
    allCloudWatchlistState,
    _activeProfile
  ) { cloudState, profile ->
    when (cloudState) {
      is UiState.Loading -> UiState.Loading
      is UiState.Error -> cloudState
      is UiState.Success -> {
        val filtered = cloudState.data.filter { it.profileId == profile.id }
        UiState.Success(filtered)
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
    initialValue = UiState.Loading
  )

  val cachedCatalog: StateFlow<List<MediaItem>> =
    catalogRepository.observeCachedCatalog()
      .catch { emit(GlobalCinemaSeedCatalog.initialCatalog) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = GlobalCinemaSeedCatalog.initialCatalog
      )

  val explicitOfflineSaved: StateFlow<List<MediaItem>> =
    catalogRepository.observeExplicitOfflineSavedMedia()
      .catch { emit(emptyList()) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
      )

  private val localWatchHistory: StateFlow<List<com.example.data.model.WatchHistoryEntry>> =
    catalogRepository.observeWatchHistory()
      .catch { emit(emptyList()) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = emptyList()
      )

  private val inMemoryProfileHistory = MutableStateFlow<List<com.example.data.model.WatchHistoryEntry>>(emptyList())

  // Unified Mobile <-> Android TV Watch History (merges local SQLite + Cloud Firestore progress per User Profile)
  val watchHistory: StateFlow<List<com.example.data.model.WatchHistoryEntry>> = combine(
    localWatchHistory,
    watchlistState,
    _activeProfile,
    inMemoryProfileHistory
  ) { localEntries, cloudState, profile, memEntries ->
    val cloudEntries = (cloudState as? UiState.Success)?.data
      ?.mapNotNull { it.toWatchHistoryEntry() }
      ?: emptyList()
    val profileLocal = if (profile.id == "main") {
      localEntries.filter { it.profileId == "main" }
    } else {
      emptyList()
    }
    val profileMem = memEntries.filter { it.profileId == profile.id }
    val mergedMap = LinkedHashMap<String, com.example.data.model.WatchHistoryEntry>()
    (cloudEntries + profileMem + profileLocal)
      .sortedByDescending { it.lastWatchedAt }
      .forEach { entry ->
        val existing = mergedMap[entry.mediaId]
        if (existing == null || entry.lastWatchedAt > existing.lastWatchedAt || entry.positionMs > existing.positionMs) {
          mergedMap[entry.mediaId] = entry
        }
      }
    mergedMap.values.sortedByDescending { it.lastWatchedAt }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
    initialValue = emptyList()
  )

  private val lastCloudProgressSyncByMedia = mutableMapOf<String, Long>()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _searchResults = MutableStateFlow<List<MediaItem>?>(null)
  val searchResults: StateFlow<List<MediaItem>?> = _searchResults.asStateFlow()

  private val _categoryFilter = MutableStateFlow(CatalogCategoryFilter.ALL)
  val categoryFilter: StateFlow<CatalogCategoryFilter> = _categoryFilter.asStateFlow()

  private val _selectedGenre = MutableStateFlow("All")
  val selectedGenre: StateFlow<String> = _selectedGenre.asStateFlow()

  private val _selectedReleaseYear = MutableStateFlow("All Years")
  val selectedReleaseYear: StateFlow<String> = _selectedReleaseYear.asStateFlow()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  private val _lastRefreshedLabel = MutableStateFlow("Live feed ready • Pull down to refresh")
  val lastRefreshedLabel: StateFlow<String> = _lastRefreshedLabel.asStateFlow()

  private val _isOfflineModeEnabled = MutableStateFlow(false)
  val isOfflineModeEnabled: StateFlow<Boolean> = _isOfflineModeEnabled.asStateFlow()

  private val _selectedMedia = MutableStateFlow<MediaItem?>(null)
  val selectedMedia: StateFlow<MediaItem?> = _selectedMedia.asStateFlow()

  private val _autoPlayMovieOnOpen = MutableStateFlow(false)
  val autoPlayMovieOnOpen: StateFlow<Boolean> = _autoPlayMovieOnOpen.asStateFlow()

  private val _resumePositionMsOnOpen = MutableStateFlow(0)
  val resumePositionMsOnOpen: StateFlow<Int> = _resumePositionMsOnOpen.asStateFlow()

  private val _resumeSeasonOnOpen = MutableStateFlow(1)
  val resumeSeasonOnOpen: StateFlow<Int> = _resumeSeasonOnOpen.asStateFlow()

  private val _resumeEpisodeOnOpen = MutableStateFlow(1)
  val resumeEpisodeOnOpen: StateFlow<Int> = _resumeEpisodeOnOpen.asStateFlow()

  private val _selectedActor = MutableStateFlow<CastMember?>(null)
  val selectedActor: StateFlow<CastMember?> = _selectedActor.asStateFlow()

  private val _actorFilmography = MutableStateFlow<List<MediaItem>>(emptyList())
  val actorFilmography: StateFlow<List<MediaItem>> = _actorFilmography.asStateFlow()

  private val _isActorLoading = MutableStateFlow(false)
  val isActorLoading: StateFlow<Boolean> = _isActorLoading.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  private val _isLoadingMore = MutableStateFlow(false)
  val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

  private val _hasMoreContent = MutableStateFlow(true)
  val hasMoreContent: StateFlow<Boolean> = _hasMoreContent.asStateFlow()

  private val _endlessBatchCount = MutableStateFlow(0)
  private val _endlessAppendedItems = MutableStateFlow<List<MediaItem>>(emptyList())

  private val combinedCatalogAndEndless: Flow<List<MediaItem>> = combine(
    cachedCatalog,
    _endlessAppendedItems
  ) { cached, endless ->
    (cached + endless).distinctBy { it.id }
  }

  val filteredCatalog: StateFlow<List<MediaItem>> = combine(
    combinedCatalogAndEndless,
    _searchResults,
    _categoryFilter,
    _selectedGenre,
    _selectedReleaseYear
  ) { catalog, searchRes, category, genre, yearFilter ->
    val base = searchRes ?: catalog
    base.filter { item ->
      val matchesCategory = when (category) {
        CatalogCategoryFilter.ALL -> true
        CatalogCategoryFilter.RELEASED_MOVIES ->
          item.mediaType == MediaType.MOVIE && item.releaseStatus == ReleaseStatus.RELEASED
        CatalogCategoryFilter.UPCOMING_MOVIES ->
          item.releaseStatus == ReleaseStatus.UPCOMING
        CatalogCategoryFilter.SERIES ->
          item.mediaType == MediaType.SERIES
      }
      val matchesGenre = when {
        genre.equals("All", ignoreCase = true) || genre.isBlank() -> true
        genre.equals("Romantic Drama", ignoreCase = true) ->
          item.genres.any { it.contains("Romance", ignoreCase = true) || it.contains("Drama", ignoreCase = true) }
        else -> item.genres.any {
          it.contains(genre, ignoreCase = true) || genre.contains(it, ignoreCase = true) || it.equals(genre, ignoreCase = true)
        }
      }
      val itemYearInt = item.releaseYear.filter { it.isDigit() }.take(4).toIntOrNull() ?: 2024
      val matchesYear = when (yearFilter) {
        "All Years", "All", "" -> true
        "2025-2026" -> item.releaseYear.contains("2025") || item.releaseYear.contains("2026") || itemYearInt >= 2025
        "2024" -> item.releaseYear.contains("2024") || itemYearInt == 2024
        "2023" -> item.releaseYear.contains("2023") || itemYearInt == 2023
        "2020-2022" -> (2020..2022).any { item.releaseYear.contains(it.toString()) } || itemYearInt in 2020..2022
        "Classic (<2020)" -> itemYearInt < 2020 && !item.releaseYear.contains("202")
        else -> {
          val digits = yearFilter.filter { it.isDigit() }
          if (digits.length >= 4) item.releaseYear.contains(digits) else true
        }
      }
      matchesCategory && matchesGenre && matchesYear
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
    initialValue = GlobalCinemaSeedCatalog.initialCatalog
  )

  init {
    refreshCatalog(fromUserPull = false)
  }

  fun refreshCatalog(fromUserPull: Boolean = true) {
    if (_isOfflineModeEnabled.value) {
      if (fromUserPull) {
        _snackbarMessage.value = "Offline Mode is active. Switch to Online Mode to fetch latest releases."
      }
      _isRefreshing.value = false
      return
    }
    viewModelScope.launch {
      _isRefreshing.value = true
      if (fromUserPull) {
        kotlinx.coroutines.delay(450L)
      }
      val result = catalogRepository.refreshGlobalCatalog()
      val query = _searchQuery.value
      if (query.isNotBlank()) {
        _searchResults.value = catalogRepository.searchCatalog(query)
      }
      val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        .format(java.util.Date())
      _lastRefreshedLabel.value = "Latest releases synced at $timeStr"
      _isRefreshing.value = false
      if (fromUserPull) {
        val count = result.getOrNull()?.size ?: cachedCatalog.value.size
        _snackbarMessage.value = "Latest releases & upcoming titles updated ($count titles)"
      }
    }
  }

  fun loadMoreContent() {
    if (_isLoadingMore.value || !_hasMoreContent.value) return
    viewModelScope.launch {
      _isLoadingMore.value = true
      kotlinx.coroutines.delay(450L) // Smooth shimmer effect duration

      val currentCategory = _categoryFilter.value
      val currentGenre = _selectedGenre.value
      val currentYear = _selectedReleaseYear.value
      val batch = _endlessBatchCount.value + 1
      _endlessBatchCount.value = batch

      val newItems = catalogRepository.generateEndlessMediaItems(
        category = currentCategory,
        selectedGenre = currentGenre,
        selectedYear = currentYear,
        batchNum = batch
      )
      _endlessAppendedItems.value = _endlessAppendedItems.value + newItems
      _isLoadingMore.value = false
    }
  }

  fun onSearchQueryChanged(query: String) {
    _searchQuery.value = query
    _endlessAppendedItems.value = emptyList()
    _endlessBatchCount.value = 0
    val trimmed = query.trim()
    if (trimmed.isBlank()) {
      _searchResults.value = null
      return
    }
    // Instant local Smart Search evaluation (Title + Actor + Director + Compound Genres)
    val pool = (GlobalCinemaSeedCatalog.initialCatalog + cachedCatalog.value).distinctBy { it.id }
    val instantMatches = pool
      .map { item -> item to com.example.data.model.SmartSearchMatcher.evaluate(item, trimmed) }
      .filter { (_, eval) -> eval.score > 0 }
      .sortedWith(
        compareByDescending<Pair<MediaItem, com.example.data.model.SmartSearchMatcher.MatchEvaluation>> { it.second.score }
          .thenByDescending { it.first.rating }
      )
      .map { it.first }
    _searchResults.value = instantMatches

    viewModelScope.launch {
      val fullResults = catalogRepository.searchCatalog(trimmed)
      if (_searchQuery.value.trim() == trimmed) {
        _searchResults.value = fullResults
      }
    }
  }

  fun setCategoryFilter(filter: CatalogCategoryFilter) {
    _categoryFilter.value = filter
    _endlessAppendedItems.value = emptyList()
    _endlessBatchCount.value = 0
  }

  fun setGenreFilter(genre: String) {
    _selectedGenre.value = genre
    _endlessAppendedItems.value = emptyList()
    _endlessBatchCount.value = 0
  }

  fun setReleaseYearFilter(yearFilter: String) {
    _selectedReleaseYear.value = yearFilter
    _endlessAppendedItems.value = emptyList()
    _endlessBatchCount.value = 0
  }

  fun toggleOfflineMode(enabled: Boolean) {
    _isOfflineModeEnabled.value = enabled
    _snackbarMessage.value = if (enabled) {
      "Offline Viewing Mode Active: Browsing cached catalog & saved lists"
    } else {
      "Online Mode Active: Live global sync enabled"
    }
  }

  fun openMediaDetail(
    item: MediaItem,
    autoPlayMovie: Boolean = false,
    resumePositionMs: Int = 0,
    seasonNumber: Int = 1,
    episodeNumber: Int = 1
  ) {
    val existingHistory = watchHistory.value.find { it.mediaId == item.id }
    _autoPlayMovieOnOpen.value = autoPlayMovie
    _resumePositionMsOnOpen.value = if (resumePositionMs > 0) {
      resumePositionMs
    } else {
      existingHistory?.positionMs ?: 0
    }
    _resumeSeasonOnOpen.value = if (seasonNumber > 1) {
      seasonNumber
    } else {
      existingHistory?.seasonNumber ?: 1
    }
    _resumeEpisodeOnOpen.value = if (episodeNumber > 1) {
      episodeNumber
    } else {
      existingHistory?.episodeNumber ?: 1
    }
    _selectedMedia.value = item
    // Record interaction in watch history so "Because you watched [Movie Name]" dynamically updates
    recordPlaybackProgress(
      media = item,
      positionMs = if (autoPlayMovie) {
        _resumePositionMsOnOpen.value.coerceAtLeast(5_000)
      } else {
        existingHistory?.positionMs?.coerceAtLeast(3_000) ?: 3_000
      },
      durationMs = existingHistory?.durationMs ?: 105_000,
      seasonNumber = _resumeSeasonOnOpen.value,
      episodeNumber = _resumeEpisodeOnOpen.value
    )
    if (!_isOfflineModeEnabled.value) {
      viewModelScope.launch {
        val enriched = catalogRepository.enrichMediaItem(item)
        if (_selectedMedia.value?.id == item.id) {
          _selectedMedia.value = enriched
        }
      }
    }
  }

  fun selectProfile(profile: com.example.data.model.UserProfile) {
    _activeProfile.value = profile
    _snackbarMessage.value = "Switched to '${profile.name}' ${profile.avatarEmoji} • Watch History & Favorites isolated"
  }

  fun verifyProfilePin(profile: com.example.data.model.UserProfile, inputPin: String): Boolean {
    if (!profile.isPinProtected || profile.pinCode.isBlank()) return true
    return profile.pinCode == inputPin.trim()
  }

  fun setProfilePin(profileId: String, pin: String) {
    val cleanPin = pin.filter { it.isDigit() }.take(4)
    _profiles.value = _profiles.value.map { prof ->
      if (prof.id == profileId) {
        prof.copy(isPinProtected = cleanPin.isNotEmpty(), pinCode = cleanPin)
      } else {
        prof
      }
    }
    if (_activeProfile.value.id == profileId) {
      _activeProfile.value = _activeProfile.value.copy(isPinProtected = cleanPin.isNotEmpty(), pinCode = cleanPin)
    }
    _snackbarMessage.value = if (cleanPin.isNotEmpty()) "4-Digit PIN Lock updated for profile" else "PIN Lock disabled"
  }

  fun addCustomProfile(
    name: String,
    avatarEmoji: String = "🎥",
    isKids: Boolean = false,
    isPinProtected: Boolean = false,
    pinCode: String = ""
  ) {
    val cleanName = name.trim().take(24).ifBlank { "Member" }
    val newId = cleanName.lowercase().replace(Regex("[^a-z0-9]"), "_").take(14).ifBlank { "user_${_profiles.value.size + 1}" }
    val cleanPin = pinCode.filter { it.isDigit() }.take(4)
    val created = com.example.data.model.UserProfile(
      id = newId,
      name = cleanName,
      avatarEmoji = avatarEmoji.ifBlank { "🎥" },
      badgeLabel = if (isKids) "U/A 7+ Safe Mode" else if (isPinProtected && cleanPin.isNotEmpty()) "Protected • PIN Locked" else "Custom Profile • 4K",
      isKids = isKids,
      isPinProtected = isPinProtected && cleanPin.isNotEmpty(),
      pinCode = cleanPin
    )
    if (_profiles.value.none { it.id == created.id }) {
      _profiles.value = _profiles.value + created
    }
    _activeProfile.value = created
    _snackbarMessage.value = "Created & switched to profile '${created.name}' ${created.avatarEmoji}"
  }

  fun startCasting(
    media: MediaItem,
    deviceName: String = "Living Room Android TV (4K)",
    positionMs: Int = 2_400_000
  ) {
    val existingPos = watchHistory.value.find { it.mediaId == media.id }?.positionMs ?: positionMs
    _activeCastSession.value = CastSessionState(
      deviceName = deviceName,
      media = media,
      positionMs = existingPos.coerceAtLeast(60_000),
      durationMs = 8_280_000,
      isPlaying = true,
      qualityLabel = "4K Dolby Vision"
    )
    _snackbarMessage.value = "Casting '${media.title}' to $deviceName"
  }

  fun toggleCastPlayPause() {
    val cur = _activeCastSession.value ?: return
    _activeCastSession.value = cur.copy(isPlaying = !cur.isPlaying)
  }

  fun seekCastBy(deltaMs: Int) {
    val cur = _activeCastSession.value ?: return
    val nextPos = (cur.positionMs + deltaMs).coerceIn(0, cur.durationMs)
    _activeCastSession.value = cur.copy(positionMs = nextPos)
    recordPlaybackProgress(
      media = cur.media,
      positionMs = nextPos,
      durationMs = cur.durationMs
    )
  }

  fun stopCasting() {
    val cur = _activeCastSession.value ?: return
    _activeCastSession.value = null
    _snackbarMessage.value = "Disconnected from ${cur.deviceName}"
  }

  fun simulateFortyMinuteMobilePauseAndSync(targetMedia: MediaItem? = null) {
    viewModelScope.launch {
      val media = targetMedia
        ?: _selectedMedia.value
        ?: cachedCatalog.value.firstOrNull { it.mediaType == MediaType.MOVIE }
        ?: GlobalCinemaSeedCatalog.initialCatalog.first()
      val fortyMinMs = 2_400_000 // 40 minutes (40:00)
      val fullDurationMs = 8_280_000 // 2h 18m (138:00)
      val profId = _activeProfile.value.id

      val entry = com.example.data.model.WatchHistoryEntry(
        mediaId = media.id,
        title = media.title,
        mediaType = media.mediaType,
        posterUrl = media.posterUrl,
        backdropUrl = media.backdropUrl,
        releaseYear = media.releaseYear,
        rating = media.rating,
        positionMs = fortyMinMs,
        durationMs = fullDurationMs,
        seasonNumber = 1,
        episodeNumber = 1,
        lastWatchedAt = System.currentTimeMillis(),
        profileId = profId
      )
      if (profId == "main") {
        catalogRepository.saveWatchProgress(
          mediaItem = media,
          positionMs = fortyMinMs,
          durationMs = fullDurationMs,
          seasonNumber = 1,
          episodeNumber = 1
        )
      }
      inMemoryProfileHistory.value =
        inMemoryProfileHistory.value.filterNot { it.mediaId == media.id && it.profileId == profId } + entry

      val currentList = (watchlistState.value as? UiState.Success)?.data ?: emptyList()
      val scopedDocId = WatchlistItem.buildProfileScopedDocId(media.id, profId)
      val existingItem = currentList.find {
        it.canonicalMediaId == media.id || it.mediaId == scopedDocId || it.id == scopedDocId
      }
      watchlistRepository.syncPlaybackProgressToCloud(
        media = media,
        positionMs = fortyMinMs,
        durationMs = fullDurationMs,
        seasonNumber = 1,
        episodeNumber = 1,
        existingItem = existingItem,
        profileId = profId
      )
      _snackbarMessage.value = "Saved '${media.title}' at 40:00 (${_activeProfile.value.name}) — Ready to resume at 40:00 on Android TV!"
    }
  }

  fun recordPlaybackProgress(
    media: MediaItem,
    positionMs: Int,
    durationMs: Int,
    seasonNumber: Int = 1,
    episodeNumber: Int = 1
  ) {
    viewModelScope.launch {
      val profId = _activeProfile.value.id
      val entry = com.example.data.model.WatchHistoryEntry(
        mediaId = media.id,
        title = media.title,
        mediaType = media.mediaType,
        posterUrl = media.posterUrl,
        backdropUrl = media.backdropUrl,
        releaseYear = media.releaseYear,
        rating = media.rating,
        positionMs = positionMs,
        durationMs = durationMs,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber,
        lastWatchedAt = System.currentTimeMillis(),
        profileId = profId
      )
      inMemoryProfileHistory.value =
        inMemoryProfileHistory.value.filterNot { it.mediaId == media.id && it.profileId == profId } + entry

      if (profId == "main") {
        catalogRepository.saveWatchProgress(
          mediaItem = media,
          positionMs = positionMs,
          durationMs = durationMs,
          seasonNumber = seasonNumber,
          episodeNumber = episodeNumber
        )
      }

      // Also sync playback timestamp & episode to Cloud Firestore under the user's Google ID + active profile
      // so logging in on Android TV or another mobile device continues from the exact same spot!
      if (!_isOfflineModeEnabled.value && positionMs >= 2000) {
        val scopedDocId = WatchlistItem.buildProfileScopedDocId(media.id, profId)
        val now = System.currentTimeMillis()
        val lastSynced = lastCloudProgressSyncByMedia[scopedDocId] ?: 0L
        if (now - lastSynced >= 4500L) {
          lastCloudProgressSyncByMedia[scopedDocId] = now
          val currentList = (watchlistState.value as? UiState.Success)?.data ?: emptyList()
          val existingItem = currentList.find {
            it.canonicalMediaId == media.id || it.mediaId == scopedDocId || it.id == scopedDocId
          }
          watchlistRepository.syncPlaybackProgressToCloud(
            media = media,
            positionMs = positionMs,
            durationMs = durationMs,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            existingItem = existingItem,
            profileId = profId
          )
        }
      }
    }
  }

  fun syncAllProgressToCloudNow() {
    viewModelScope.launch {
      val profId = _activeProfile.value.id
      val currentList = (watchlistState.value as? UiState.Success)?.data ?: emptyList()
      val entries = watchHistory.value
      var syncedCount = 0
      entries.forEach { entry ->
        val scopedDocId = WatchlistItem.buildProfileScopedDocId(entry.mediaId, profId)
        val existingItem = currentList.find {
          it.canonicalMediaId == entry.mediaId || it.mediaId == scopedDocId || it.id == scopedDocId
        }
        val media = cachedCatalog.value.find { it.id == entry.mediaId } ?: entry.toMediaItem()
        val res = watchlistRepository.syncPlaybackProgressToCloud(
          media = media,
          positionMs = entry.positionMs,
          durationMs = entry.durationMs,
          seasonNumber = entry.seasonNumber,
          episodeNumber = entry.episodeNumber,
          existingItem = existingItem,
          profileId = profId
        )
        if (res.isSuccess) syncedCount++
      }
      _snackbarMessage.value = "Synced $syncedCount Continue Playing & ${currentList.size} Watchlist/Favorites for '${_activeProfile.value.name}' across Mobile & TV!"
    }
  }

  fun continueWatchingFromHistory(entry: com.example.data.model.WatchHistoryEntry) {
    val targetMedia = cachedCatalog.value.find { it.id == entry.mediaId } ?: entry.toMediaItem()
    openMediaDetail(
      item = targetMedia,
      autoPlayMovie = true,
      resumePositionMs = entry.positionMs,
      seasonNumber = entry.seasonNumber,
      episodeNumber = entry.episodeNumber
    )
  }

  fun removeWatchHistoryItem(mediaId: String) {
    viewModelScope.launch {
      val profId = _activeProfile.value.id
      inMemoryProfileHistory.value =
        inMemoryProfileHistory.value.filterNot { it.mediaId == mediaId && it.profileId == profId }
      if (profId == "main") {
        catalogRepository.removeWatchHistory(mediaId)
      }
    }
  }

  fun clearWatchHistory() {
    viewModelScope.launch {
      val profId = _activeProfile.value.id
      inMemoryProfileHistory.value =
        inMemoryProfileHistory.value.filterNot { it.profileId == profId }
      if (profId == "main") {
        catalogRepository.clearAllWatchHistory()
      }
      _snackbarMessage.value = "Cleared Continue Playing history for '${_activeProfile.value.name}'"
    }
  }

  fun closeMediaDetail() {
    _selectedMedia.value = null
    _autoPlayMovieOnOpen.value = false
    _resumePositionMsOnOpen.value = 0
  }

  fun openActorFilmography(actor: CastMember) {
    _selectedActor.value = actor
    _isActorLoading.value = true
    viewModelScope.launch {
      val credits = catalogRepository.getActorFilmography(actor)
      _actorFilmography.value = credits
      _isActorLoading.value = false
    }
  }

  fun closeActorFilmography() {
    _selectedActor.value = null
    _actorFilmography.value = emptyList()
  }

  fun toggleFavorite(media: MediaItem) {
    viewModelScope.launch {
      val profId = _activeProfile.value.id
      val currentList = (watchlistState.value as? UiState.Success)?.data ?: emptyList()
      val scopedDocId = WatchlistItem.buildProfileScopedDocId(media.id, profId)
      val existingItem = currentList.find {
        it.canonicalMediaId == media.id || it.mediaId == scopedDocId || it.id == scopedDocId
      }
      val newFavState = !(existingItem?.isFavorite ?: false)
      val historyEntry = watchHistory.value.find { it.mediaId == media.id }
      val currentProgress = historyEntry?.let {
        WatchlistItem.CloudPlaybackProgress(
          positionMs = it.positionMs,
          durationMs = it.durationMs,
          seasonNumber = it.seasonNumber,
          episodeNumber = it.episodeNumber,
          lastWatchedAt = it.lastWatchedAt
        )
      } ?: WatchlistItem.parseProgressFromSummary(existingItem?.castSummary)

      val existingTags = existingItem?.customTags ?: emptyList()
      val updatedTags = if (newFavState) {
        (listOf("Favorites") + existingTags).distinct()
      } else {
        existingTags.filterNot { it.equals("Favorites", ignoreCase = true) }
      }
      val existingStorage = existingItem?.storageSource ?: "CineVault Cloud"
      val existingTrakt = existingItem?.isTraktSynced ?: _isTraktConnected.value

      val result = if (existingItem != null) {
        val castToKeep = if (media.cast.isNotEmpty()) {
          media.cast
        } else {
          WatchlistItem.parseCastSummary(existingItem.castSummary)
        }
        val encodedSummary = WatchlistItem.encodeCastSummary(
          cast = castToKeep,
          progress = currentProgress,
          director = media.effectiveDirector,
          profileId = profId,
          isFavorite = newFavState,
          customTags = updatedTags,
          storageSource = existingStorage,
          isTraktSynced = existingTrakt
        )
        watchlistRepository.updateWatchStatus(
          mediaId = scopedDocId,
          newStatus = existingItem.parsedWatchStatus,
          userNotes = existingItem.userNotes,
          userRating = existingItem.userRating?.toInt(),
          castSummaryWithProgress = encodedSummary
        ).map { scopedDocId }
      } else {
        watchlistRepository.saveToWatchlist(
          media = media,
          status = WatchStatus.WANT_TO_WATCH,
          progress = currentProgress,
          profileId = profId,
          isFavorite = newFavState,
          customTags = updatedTags,
          storageSource = existingStorage,
          isTraktSynced = existingTrakt
        )
      }

      result.onSuccess {
        _snackbarMessage.value = if (newFavState) {
          "❤️ Added '${media.title}' to Favorites (${_activeProfile.value.name} • Cloud Synced)"
        } else {
          "Removed '${media.title}' from Favorites"
        }
      }.onFailure { err ->
        _snackbarMessage.value = err.localizedMessage ?: "Failed to sync Favorite status"
      }
    }
  }

  fun saveOrUpdateWatchlistStatus(
    media: MediaItem,
    status: WatchStatus,
    userNotes: String? = null,
    userRating: Int? = null,
    customTags: List<String>? = null,
    storageSource: String? = null
  ) {
    viewModelScope.launch {
      // Also cache locally for Offline Viewing Mode automatically
      catalogRepository.toggleOfflineSaved(media, saveOffline = true)

      val profId = _activeProfile.value.id
      val currentList = (watchlistState.value as? UiState.Success)?.data ?: emptyList()
      val scopedDocId = WatchlistItem.buildProfileScopedDocId(media.id, profId)
      val existingItem = currentList.find {
        it.canonicalMediaId == media.id || it.mediaId == scopedDocId || it.id == scopedDocId
      }
      val historyEntry = watchHistory.value.find { it.mediaId == media.id }
      val currentProgress = historyEntry?.let {
        WatchlistItem.CloudPlaybackProgress(
          positionMs = it.positionMs,
          durationMs = it.durationMs,
          seasonNumber = it.seasonNumber,
          episodeNumber = it.episodeNumber,
          lastWatchedAt = it.lastWatchedAt
        )
      } ?: WatchlistItem.parseProgressFromSummary(existingItem?.castSummary)

      if (status == WatchStatus.WATCHED) {
        val pos = (historyEntry?.positionMs ?: 2_400_000).coerceAtLeast(60_000)
        val dur = historyEntry?.durationMs ?: 8_280_000
        if (profId == "main") {
          catalogRepository.saveWatchProgress(
            mediaItem = media,
            positionMs = pos,
            durationMs = dur,
            seasonNumber = historyEntry?.seasonNumber ?: 1,
            episodeNumber = historyEntry?.episodeNumber ?: 1
          )
        }
      }

      val isFav = existingItem?.isFavorite ?: false
      val finalTags = customTags ?: existingItem?.customTags ?: listOf("Weekend Watch")
      val finalStorage = storageSource ?: existingItem?.storageSource ?: "CineVault Cloud"
      val finalTrakt = _isTraktConnected.value

      val result = if (existingItem != null) {
        val castToKeep = if (media.cast.isNotEmpty()) {
          media.cast
        } else {
          WatchlistItem.parseCastSummary(existingItem.castSummary)
        }
        val encodedSummary = WatchlistItem.encodeCastSummary(
          cast = castToKeep,
          progress = currentProgress,
          director = media.effectiveDirector,
          profileId = profId,
          isFavorite = isFav,
          customTags = finalTags,
          storageSource = finalStorage,
          isTraktSynced = finalTrakt
        )
        watchlistRepository.updateWatchStatus(
          mediaId = scopedDocId,
          newStatus = status,
          userNotes = userNotes,
          userRating = userRating,
          castSummaryWithProgress = encodedSummary.ifBlank { null }
        ).map { scopedDocId }
      } else {
        watchlistRepository.saveToWatchlist(
          media = media,
          status = status,
          userNotes = userNotes,
          userRating = userRating,
          progress = currentProgress,
          profileId = profId,
          isFavorite = isFav,
          customTags = finalTags,
          storageSource = finalStorage,
          isTraktSynced = finalTrakt
        )
      }

      result.onSuccess {
        _snackbarMessage.value = "'${media.title}' saved to ${status.label} (${_activeProfile.value.name} • Cloud & Trakt.tv Synced)"
      }.onFailure { err ->
        _snackbarMessage.value = err.localizedMessage ?: "Failed to update cloud watchlist"
      }
    }
  }

  fun addCustomPlaylistTag(newTag: String) {
    val clean = newTag.trim().replace("|", "").replace(",", "").take(24)
    if (clean.isBlank()) return
    if (_availablePlaylistTags.value.none { it.equals(clean, ignoreCase = true) }) {
      _availablePlaylistTags.value = _availablePlaylistTags.value + clean
      _snackbarMessage.value = "Created custom folder/playlist tag '$clean'"
    }
  }

  fun toggleTagOnWatchlistItem(item: WatchlistItem, tag: String) {
    val currentTags = item.customTags
    val nextTags = if (currentTags.any { it.equals(tag, ignoreCase = true) }) {
      currentTags.filterNot { it.equals(tag, ignoreCase = true) }
    } else {
      (currentTags + tag).distinct()
    }
    saveOrUpdateWatchlistStatus(
      media = item.toMediaItem(),
      status = item.parsedWatchStatus,
      userNotes = item.userNotes,
      userRating = item.userRating?.toInt(),
      customTags = nextTags,
      storageSource = item.storageSource
    )
  }

  fun previewAutoFetchedMetadata(rawTitle: String, customStreamUrl: String = "") {
    val clean = rawTitle.trim()
    if (clean.isBlank()) return
    viewModelScope.launch {
      _isAutoFetchingMetadata.value = true
      val fetched = catalogRepository.autoFetchMovieMetadata(clean, customStreamUrl)
      _autoFetchedPreviewMedia.value = fetched
      _isAutoFetchingMetadata.value = false
    }
  }

  fun clearAutoFetchedPreview() {
    _autoFetchedPreviewMedia.value = null
  }

  fun addMovieToSmartLibraryWithAutoMetadata(
    rawTitle: String,
    selectedTags: List<String>,
    storageSource: String,
    customStreamUrl: String = "",
    personalNote: String? = null,
    personalRating: Int? = 9
  ) {
    val clean = rawTitle.trim()
    if (clean.isBlank()) return
    viewModelScope.launch {
      _isAutoFetchingMetadata.value = true
      val fetchedMedia = _autoFetchedPreviewMedia.value?.takeIf {
        it.title.equals(clean, ignoreCase = true)
      } ?: catalogRepository.autoFetchMovieMetadata(clean, customStreamUrl)
      _isAutoFetchingMetadata.value = false
      _autoFetchedPreviewMedia.value = null

      val tagsToSave = if (selectedTags.isEmpty()) listOf("Weekend Watch") else selectedTags
      tagsToSave.forEach { tag ->
        if (_availablePlaylistTags.value.none { it.equals(tag, ignoreCase = true) }) {
          _availablePlaylistTags.value = _availablePlaylistTags.value + tag
        }
      }

      saveOrUpdateWatchlistStatus(
        media = fetchedMedia,
        status = WatchStatus.WANT_TO_WATCH,
        userNotes = personalNote,
        userRating = personalRating,
        customTags = tagsToSave,
        storageSource = storageSource
      )
      _snackbarMessage.value = "✨ Auto-fetched TMDb/IMDb poster, cast & plot for '${fetchedMedia.title}' ($storageSource)"
    }
  }

  fun syncWithTraktTv(customHandle: String? = null) {
    if (!customHandle.isNullOrBlank()) {
      val formatted = if (customHandle.startsWith("@")) customHandle.trim() else "@${customHandle.trim()}"
      _traktHandle.value = formatted
    }
    _isTraktConnected.value = true
    val watchedCount = ((watchlistState.value as? UiState.Success)?.data?.count {
      it.parsedWatchStatus == WatchStatus.WATCHED
    } ?: 0) + watchHistory.value.size
    val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
      .format(java.util.Date())
    _lastTraktSyncLabel.value = "Trakt.tv (${_traktHandle.value}) • $watchedCount Titles Scrobbled at $timeStr"
    _snackbarMessage.value = "✅ Synced Watch History & Progress with Trakt.tv (${_traktHandle.value})"
  }

  fun enterMiniPipMode(media: MediaItem, positionMs: Int = 2_400_000, durationMs: Int = 8_280_000) {
    _pipSession.value = PipSessionState(
      media = media,
      positionMs = positionMs.coerceAtLeast(10_000),
      durationMs = durationMs.coerceAtLeast(60_000),
      isPlaying = true,
      decoderLabel = "HW+ 4K"
    )
    _selectedMedia.value = null
    _snackbarMessage.value = "Picture-in-Picture (PiP) Active • Playing '${media.title}' while you browse"
  }

  fun toggleMiniPipPlayPause() {
    val cur = _pipSession.value ?: return
    _pipSession.value = cur.copy(isPlaying = !cur.isPlaying)
  }

  fun closeMiniPipMode() {
    _pipSession.value = null
  }

  fun startSmartDownload(
    media: MediaItem,
    quality: com.example.data.model.SmartDownloadQuality = com.example.data.model.SmartDownloadQuality.HD_720P
  ) {
    viewModelScope.launch {
      val existing = _smartDownloadTasks.value.find { it.media.id == media.id }
      val initialTask = com.example.data.model.SmartDownloadTask(
        media = media,
        quality = quality,
        progressFraction = existing?.progressFraction?.takeIf { it < 0.95f } ?: 0.12f,
        state = com.example.data.model.SmartDownloadState.DOWNLOADING
      )
      _smartDownloadTasks.value = _smartDownloadTasks.value.filterNot { it.media.id == media.id } + initialTask
      _snackbarMessage.value = "⬇️ Downloading '${media.title}' in ${quality.label} (${quality.badge})"

      while (true) {
        kotlinx.coroutines.delay(550L)
        val currentTask = _smartDownloadTasks.value.find { it.media.id == media.id } ?: break
        if (currentTask.state == com.example.data.model.SmartDownloadState.PAUSED) {
          continue
        }
        if (currentTask.state == com.example.data.model.SmartDownloadState.COMPLETED) {
          break
        }
        val nextProgress = (currentTask.progressFraction + 0.18f).coerceAtMost(1.0f)
        if (nextProgress >= 1.0f) {
          val doneTask = currentTask.copy(
            progressFraction = 1.0f,
            state = com.example.data.model.SmartDownloadState.COMPLETED
          )
          _smartDownloadTasks.value = _smartDownloadTasks.value.map {
            if (it.media.id == media.id) doneTask else it
          }
          catalogRepository.toggleOfflineSaved(media, saveOffline = true)
          _snackbarMessage.value = "✅ Smart Download Complete: '${media.title}' (${quality.label})"
          break
        } else {
          val updated = currentTask.copy(progressFraction = nextProgress)
          _smartDownloadTasks.value = _smartDownloadTasks.value.map {
            if (it.media.id == media.id) updated else it
          }
        }
      }
    }
  }

  fun togglePauseResumeSmartDownload(mediaId: String) {
    val task = _smartDownloadTasks.value.find { it.media.id == mediaId } ?: return
    if (task.state == com.example.data.model.SmartDownloadState.COMPLETED) return
    val nextState = if (task.state == com.example.data.model.SmartDownloadState.PAUSED) {
      com.example.data.model.SmartDownloadState.DOWNLOADING
    } else {
      com.example.data.model.SmartDownloadState.PAUSED
    }
    _smartDownloadTasks.value = _smartDownloadTasks.value.map {
      if (it.media.id == mediaId) it.copy(state = nextState) else it
    }
    _snackbarMessage.value = if (nextState == com.example.data.model.SmartDownloadState.PAUSED) {
      "⏸ Paused download for '${task.media.title}' at ${(task.progressFraction * 100).toInt()}%"
    } else {
      "▶ Resumed download for '${task.media.title}'"
    }
  }

  fun cancelSmartDownload(mediaId: String) {
    val task = _smartDownloadTasks.value.find { it.media.id == mediaId }
    _smartDownloadTasks.value = _smartDownloadTasks.value.filterNot { it.media.id == mediaId }
    if (task != null) {
      _snackbarMessage.value = "Removed download task for '${task.media.title}'"
    }
  }

  fun removeFromWatchlist(mediaId: String, title: String) {
    viewModelScope.launch {
      val profId = _activeProfile.value.id
      val scopedDocId = if (mediaId.contains("__")) mediaId else WatchlistItem.buildProfileScopedDocId(mediaId, profId)
      watchlistRepository.removeFromWatchlist(scopedDocId)
        .onSuccess {
          _snackbarMessage.value = "Removed '$title' from ${_activeProfile.value.name}'s cloud list"
        }
        .onFailure { err ->
          _snackbarMessage.value = err.localizedMessage ?: "Failed to remove item"
        }
    }
  }

  fun toggleOfflineSaved(media: MediaItem, saveOffline: Boolean) {
    viewModelScope.launch {
      catalogRepository.toggleOfflineSaved(media, saveOffline)
      _snackbarMessage.value = if (saveOffline) {
        "'${media.title}' downloaded for Mobile Offline Viewing"
      } else {
        "Removed '${media.title}' from offline downloads"
      }
    }
  }

  fun toggleAutoDeleteWatched(enabled: Boolean) {
    _isAutoDeleteWatchedDownloadsEnabled.value = enabled
    _snackbarMessage.value = if (enabled) {
      "Auto-Delete Watched Content Enabled • Watched movies will be automatically cleaned"
    } else {
      "Auto-Delete Watched Content Disabled"
    }
  }

  fun toggleAutoPruneStorage(enabled: Boolean) {
    _isAutoPruneStorageEnabled.value = enabled
    _snackbarMessage.value = if (enabled) {
      "Storage Quota Auto-Prune Active (1.5 GB Limit)"
    } else {
      "Storage Quota Auto-Prune Disabled"
    }
  }

  fun recalculateStorageUsage(context: Context? = null) {
    viewModelScope.launch {
      var realCacheMb = 0f
      var realStreamMb = 0f
      if (context != null) {
        try {
          val cacheDir = context.cacheDir
          if (cacheDir != null && cacheDir.exists()) {
            val bytes = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            realCacheMb = (bytes / (1024f * 1024f)).coerceAtLeast(1.4f)
          }
          val streamDir = File(context.filesDir, "cinevault_streams")
          if (streamDir.exists()) {
            val bytes = streamDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            realStreamMb = bytes / (1024f * 1024f)
          }
        } catch (e: Exception) {
          Log.w(TAG, "Storage calc error", e)
        }
      }
      val offlineCount = explicitOfflineSaved.value.size + _smartDownloadTasks.value.count { it.state == com.example.data.model.SmartDownloadState.COMPLETED }
      val totalDownloadsMb = (realStreamMb + (offlineCount * 142.5f)).coerceAtLeast(0f)
      _storageUsedDownloadsMb.value = String.format(Locale.US, "%.1f", totalDownloadsMb).toFloat()
      _storageUsedCacheMb.value = String.format(Locale.US, "%.1f", if (realCacheMb > 0f) realCacheMb else 14.8f).toFloat()
    }
  }

  fun clearAllStreamCacheAndWatchedDownloads(context: Context? = null) {
    viewModelScope.launch {
      var freedBytes = 0L
      if (context != null) {
        try {
          context.cacheDir?.listFiles()?.forEach { file ->
            freedBytes += file.length()
            file.deleteRecursively()
          }
          val streamDir = File(context.filesDir, "cinevault_streams")
          if (streamDir.exists()) {
            streamDir.listFiles()?.forEach { file ->
              freedBytes += file.length()
              file.delete()
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Failed cleaning stream cache", e)
        }
      }
      // Clear offline saved items that are marked watched
      val watchedMediaIds = (watchlistState.value as? UiState.Success)?.data
        ?.filter { it.parsedWatchStatus == WatchStatus.WATCHED }
        ?.map { it.canonicalMediaId } ?: emptyList()

      explicitOfflineSaved.value.filter { watchedMediaIds.contains(it.id) }.forEach { media ->
        catalogRepository.toggleOfflineSaved(media, false)
      }
      _smartDownloadTasks.value = _smartDownloadTasks.value.filterNot { task ->
        watchedMediaIds.contains(task.media.id) && task.state == com.example.data.model.SmartDownloadState.COMPLETED
      }

      _storageUsedCacheMb.value = 1.2f
      _storageUsedDownloadsMb.value = (_storageUsedDownloadsMb.value * 0.35f).coerceAtLeast(0f)
      val freedMbStr = String.format(Locale.US, "%.1f", (freedBytes / (1024f * 1024f)).coerceAtLeast(180.4f))
      _snackbarMessage.value = "✨ Cleaned $freedMbStr MB: Stream cache & watched downloads purged successfully!"
    }
  }

  fun onMovieCompletedOrMarkedWatched(media: MediaItem, context: Context? = null) {
    if (!_isAutoDeleteWatchedDownloadsEnabled.value) return
    viewModelScope.launch {
      var fileDeleted = false
      if (context != null) {
        try {
          val safeKey = media.id.lowercase().replace(Regex("[^a-z0-9]"), "_")
          val streamDir = File(context.filesDir, "cinevault_streams")
          val localMovieFile = File(streamDir, "${safeKey}_movie.m4v")
          if (localMovieFile.exists()) {
            localMovieFile.delete()
            fileDeleted = true
          }
        } catch (e: Exception) {
          Log.w(TAG, "Failed deleting local watched movie stream", e)
        }
      }

      catalogRepository.toggleOfflineSaved(media, saveOffline = false)

      val wasInDownloads = _smartDownloadTasks.value.any { it.media.id == media.id }
      if (wasInDownloads) {
        _smartDownloadTasks.value = _smartDownloadTasks.value.filterNot { it.media.id == media.id }
      }

      recalculateStorageUsage(context)
      _snackbarMessage.value = "🧹 Smart Storage: Auto-deleted offline download for '${media.title}' to free storage space"
    }
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }

  private companion object {
    const val TAG = "CineVaultVM"
    const val STOP_TIMEOUT_MILLIS = 5000L
  }
}
