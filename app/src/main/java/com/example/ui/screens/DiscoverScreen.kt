package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.ReleaseStatus
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.CatalogCategoryFilter
import com.example.data.model.SmartSearchMatcher
import com.example.ui.components.ContinueWatchingSection
import com.example.ui.components.DynamicCategoryCarouselSection
import com.example.ui.components.HeroSpotlightBanner
import com.example.ui.components.MediaPosterCard

private val DEFAULT_GENRE_OPTIONS = listOf(
  "All",
  "Romantic Drama",
  "Action",
  "Romance",
  "Comedy",
  "Sci-Fi",
  "Drama",
  "Thriller",
  "Crime",
  "Horror",
  "Mystery",
  "Animation",
  "Fantasy",
  "Mythology",
  "Adventure",
  "Biography",
  "History",
  "Family"
)

private val RELEASE_YEAR_FILTER_OPTIONS = listOf(
  "All Years",
  "2025-2026",
  "2024",
  "2023",
  "2020-2022",
  "Classic (<2020)"
)

private val SMART_SEARCH_SUGGESTIONS = listOf(
  "Romantic Dramas",
  "Shah Rukh Khan",
  "Christopher Nolan",
  "Sci-Fi Thriller",
  "Rajkumar Hirani",
  "Prabhas",
  "Action Crime"
)

private fun genreIcon(genre: String): ImageVector = when (genre.lowercase()) {
  "all" -> Icons.Default.Category
  "action" -> Icons.Default.LocalFireDepartment
  "romance" -> Icons.Default.AutoAwesome
  "sci-fi" -> Icons.Default.Science
  "drama" -> Icons.Default.TheaterComedy
  "thriller" -> Icons.Default.Psychology
  "crime" -> Icons.Default.Gavel
  "mythology" -> Icons.Default.AutoAwesome
  "adventure" -> Icons.Default.Explore
  "biography" -> Icons.Default.HistoryEdu
  else -> Icons.Default.Movie
}

private fun computeSimilarityReason(seed: MediaItem, candidate: MediaItem): Pair<Int, String> {
  var score = 0
  val reasons = mutableListOf<String>()

  if (seed.effectiveDirector.isNotBlank() &&
    seed.effectiveDirector.equals(candidate.effectiveDirector, ignoreCase = true)
  ) {
    score += 55
    reasons.add("Dir: ${candidate.effectiveDirector}")
  }

  val seedActorNames = seed.cast.map { it.name.lowercase() }.toSet()
  val sharedActor = candidate.cast.firstOrNull { it.name.lowercase() in seedActorNames }
  if (sharedActor != null) {
    score += 48
    reasons.add("Actor: ${sharedActor.name}")
  }

  val seedGenres = seed.genres.map { it.lowercase() }.toSet()
  val sharedGenres = candidate.genres.filter { it.lowercase() in seedGenres }
  if (sharedGenres.isNotEmpty()) {
    score += sharedGenres.size * 25
    if (reasons.isEmpty()) {
      reasons.add(sharedGenres.take(2).joinToString(" • "))
    }
  }

  if (seed.language.substringBefore("/").trim().equals(
      candidate.language.substringBefore("/").trim(),
      ignoreCase = true
    )
  ) {
    score += 10
  }

  score += (candidate.rating * 2).toInt()
  val badge = reasons.firstOrNull() ?: candidate.genres.take(2).joinToString(" • ")
  return score to badge
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
  catalog: List<MediaItem>,
  allCatalogItems: List<MediaItem> = catalog,
  watchlistMap: Map<String, WatchlistItem>,
  watchHistory: List<WatchHistoryEntry> = emptyList(),
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  selectedCategory: CatalogCategoryFilter,
  onCategoryChange: (CatalogCategoryFilter) -> Unit,
  selectedGenre: String,
  onGenreChange: (String) -> Unit,
  selectedReleaseYear: String = "All Years",
  onReleaseYearChange: (String) -> Unit = {},
  onOpenAddMovieDialog: (() -> Unit)? = null,
  isRefreshing: Boolean,
  lastRefreshedLabel: String = "Pull down to refresh latest releases",
  onRefresh: () -> Unit,
  onOpenMedia: (MediaItem) -> Unit,
  onWatchMovie: (MediaItem) -> Unit = onOpenMedia,
  onContinueWatchingEntry: (WatchHistoryEntry) -> Unit = {},
  onRemoveWatchHistoryEntry: (String) -> Unit = {},
  onClearWatchHistory: () -> Unit = {},
  onOpenVoiceSearch: () -> Unit = {},
  isLoadingMore: Boolean = false,
  hasMoreContent: Boolean = true,
  onLoadMore: () -> Unit = {},
  onQuickStatusSelect: (MediaItem, WatchStatus) -> Unit
) {
  val heroItem = catalog.firstOrNull()
  val pullToRefreshState = rememberPullToRefreshState()
  val gridState = rememberLazyGridState()
  var isGenreSidebarOpen by remember { mutableStateOf(false) }
  var isAllGenreChipsExpanded by remember { mutableStateOf(false) }

  val shouldLoadMore by remember {
    derivedStateOf {
      val layoutInfo = gridState.layoutInfo
      val visibleItems = layoutInfo.visibleItemsInfo
      if (visibleItems.isEmpty()) false
      else {
        val lastVisibleIndex = visibleItems.last().index
        val totalCount = layoutInfo.totalItemsCount
        totalCount > 0 && lastVisibleIndex >= totalCount - 4
      }
    }
  }

  LaunchedEffect(shouldLoadMore) {
    if (shouldLoadMore && !isLoadingMore && hasMoreContent) {
      onLoadMore()
    }
  }

  // Compute available genres and live item counts within the selected category
  val categoryScopedPool = remember(allCatalogItems, selectedCategory) {
    allCatalogItems.filter { item ->
      when (selectedCategory) {
        CatalogCategoryFilter.ALL -> true
        CatalogCategoryFilter.RELEASED_MOVIES ->
          item.mediaType == MediaType.MOVIE && item.releaseStatus == ReleaseStatus.RELEASED
        CatalogCategoryFilter.UPCOMING_MOVIES ->
          item.releaseStatus == ReleaseStatus.UPCOMING
        CatalogCategoryFilter.SERIES ->
          item.mediaType == MediaType.SERIES
      }
    }
  }

  // --- Dynamic Categories Computation ---
  // 1) "Trending Now": Top-rated & popular blockbusters
  val trendingNowItems = remember(allCatalogItems) {
    allCatalogItems
      .sortedWith(
        compareByDescending<MediaItem> { it.releaseStatus == ReleaseStatus.RELEASED }
          .thenByDescending { it.rating }
      )
      .take(10)
  }

  // 2) "New Releases": Latest released movies & series (2024-2026 & recent releases)
  val newReleasesItems = remember(allCatalogItems) {
    allCatalogItems
      .filter { it.releaseYear >= "2023" }
      .sortedWith(
        compareByDescending<MediaItem> { it.releaseStatus == ReleaseStatus.RELEASED && it.releaseYear >= "2024" }
          .thenByDescending { it.releaseYear }
          .thenByDescending { it.rating }
      )
      .take(10)
  }

  // 3) "Because you watched [Movie Name]": Dynamically driven by watchHistory + WATCHED items
  val watchedSeedMovies = remember(allCatalogItems, watchHistory, watchlistMap) {
    val byId = allCatalogItems.associateBy { it.id }
    val fromHistory = watchHistory.mapNotNull { entry ->
      byId[entry.mediaId] ?: allCatalogItems.find { it.title.equals(entry.title, ignoreCase = true) }
    }
    val fromWatchlistWatched = watchlistMap.values
      .filter { it.parsedWatchStatus == WatchStatus.WATCHED }
      .mapNotNull { wl ->
        byId[wl.mediaId] ?: allCatalogItems.find { it.title.equals(wl.title, ignoreCase = true) }
      }
    val combined = (fromHistory + fromWatchlistWatched).distinctBy { it.id }
    if (combined.isNotEmpty()) {
      combined
    } else {
      listOfNotNull(allCatalogItems.firstOrNull())
    }
  }

  var selectedWatchedSeedTitle by remember(watchedSeedMovies.firstOrNull()?.title) {
    mutableStateOf(watchedSeedMovies.firstOrNull()?.title)
  }

  val activeWatchedSeedMovie = remember(watchedSeedMovies, selectedWatchedSeedTitle) {
    watchedSeedMovies.find { it.title == selectedWatchedSeedTitle } ?: watchedSeedMovies.firstOrNull()
  }

  val becauseYouWatchedRecommendations = remember(allCatalogItems, activeWatchedSeedMovie) {
    val seed = activeWatchedSeedMovie ?: return@remember emptyList<Pair<MediaItem, String>>()
    allCatalogItems
      .filter { it.id != seed.id }
      .map { candidate ->
        val (score, reason) = computeSimilarityReason(seed, candidate)
        Triple(candidate, score, reason)
      }
      .sortedByDescending { it.second }
      .take(10)
      .map { it.first to it.third }
  }

  val youMightAlsoLikeRecommendations = remember(allCatalogItems, watchlistMap, watchHistory) {
    val savedGenres = (
      watchlistMap.values.flatMap { it.genres.split(",") }.map { it.trim().lowercase() } +
        watchHistory.flatMap { listOf("action", "drama", "sci-fi") }
      ).filter { it.isNotEmpty() }
    val savedIds = watchlistMap.values.map { it.canonicalMediaId }.toSet()
    val pool = allCatalogItems.filterNot { it.id in savedIds }.ifEmpty { allCatalogItems }
    pool.map { candidate ->
      val genreBoost = candidate.genres.count { it.lowercase() in savedGenres } * 18
      val matchPercent = (84 + (candidate.rating * 1.4).toInt() + genreBoost.coerceAtMost(12)).coerceIn(86, 99)
      val tagReason = candidate.genres.take(2).joinToString(" • ") + " • ${candidate.releaseYear}"
      Triple(candidate, matchPercent, tagReason)
    }
      .sortedByDescending { it.second }
      .take(10)
  }

  val genreOptions = remember(categoryScopedPool, catalog) {
    val dynamicGenres = (categoryScopedPool + catalog).flatMap { it.genres }.map { it.trim() }.filter { it.isNotEmpty() }
    (DEFAULT_GENRE_OPTIONS + dynamicGenres).distinctBy { it.lowercase() }
  }

  val genreCounts = remember(categoryScopedPool, catalog, genreOptions) {
    val pool = (categoryScopedPool + catalog).distinctBy { it.id }
    genreOptions.associateWith { genre ->
      when {
        genre.equals("All", ignoreCase = true) -> pool.size
        genre.equals("Romantic Drama", ignoreCase = true) -> pool.count { item ->
          item.genres.any { it.contains("Romance", ignoreCase = true) || it.contains("Drama", ignoreCase = true) }
        }
        else -> pool.count { item ->
          item.genres.any { it.contains(genre, ignoreCase = true) || genre.contains(it, ignoreCase = true) || it.equals(genre, ignoreCase = true) }
        }
      }
    }
  }

  Row(modifier = Modifier.fillMaxSize()) {
    // Collapsible Genre Filtering Sidebar on the left of the catalog screen
    AnimatedVisibility(
      visible = isGenreSidebarOpen,
      enter = expandHorizontally() + fadeIn(),
      exit = shrinkHorizontally() + fadeOut()
    ) {
      GenreFilterSidebar(
        genreOptions = genreOptions,
        genreCounts = genreCounts,
        selectedGenre = selectedGenre,
        selectedCategory = selectedCategory,
        onSelectGenre = { genre ->
          onGenreChange(genre)
        },
        onSelectCategory = onCategoryChange,
        onResetFilters = {
          onGenreChange("All")
          onCategoryChange(CatalogCategoryFilter.ALL)
        },
        onCloseSidebar = { isGenreSidebarOpen = false }
      )
    }

    // Main Pull-to-Refresh Catalog Grid
    PullToRefreshBox(
      isRefreshing = isRefreshing,
      onRefresh = onRefresh,
      state = pullToRefreshState,
      indicator = {
        PullToRefreshDefaults.Indicator(
          state = pullToRefreshState,
          isRefreshing = isRefreshing,
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier
            .align(Alignment.TopCenter)
            .testTag("pull_to_refresh_indicator")
        )
      },
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .testTag("discover_pull_to_refresh_box")
    ) {
      LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(minSize = 158.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
          .fillMaxSize()
          .testTag("discover_screen")
      ) {
        // 1. Smart Search Bar + Quick Actor/Director/Genre Suggestions
        item(span = { GridItemSpan(maxLineSpan) }) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Genre Filtering Sidebar Toggle Button
              Surface(
                onClick = { isGenreSidebarOpen = !isGenreSidebarOpen },
                shape = RoundedCornerShape(14.dp),
                color = if (isGenreSidebarOpen || selectedGenre != "All") {
                  MaterialTheme.colorScheme.primaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
                modifier = Modifier
                  .height(52.dp)
                  .testTag("toggle_genre_sidebar_button")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  BadgedBox(
                    badge = {
                      if (selectedGenre != "All") {
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                          Text("1")
                        }
                      }
                    }
                  ) {
                    Icon(
                      imageVector = Icons.Default.Tune,
                      contentDescription = "Toggle Genre Sidebar",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                  Text(
                    text = "Genres",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                }
              }

              OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search movie, actor, director, or 'romantic dramas'...") },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                  )
                },
                trailingIcon = {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    if (searchQuery.isNotEmpty()) {
                      IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                          imageVector = Icons.Default.Clear,
                          contentDescription = "Clear Search"
                        )
                      }
                    }
                    IconButton(
                      onClick = onOpenVoiceSearch,
                      modifier = Modifier.testTag("search_voice_mic_button")
                    ) {
                      Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Native TV Voice Search",
                        tint = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = MaterialTheme.colorScheme.primary,
                  unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                  .weight(1f)
                  .testTag("search_input")
              )

              IconButton(
                onClick = onRefresh,
                modifier = Modifier
                  .size(48.dp)
                  .testTag("refresh_catalog_button")
              ) {
                if (isRefreshing) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                  )
                } else {
                  Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh Live Catalog",
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }

            // Smart Search Quick Suggestions (Actor, Director, Compound Genres)
            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.testTag("smart_search_suggestions_row")
            ) {
              items(SMART_SEARCH_SUGGESTIONS) { suggestion ->
                val isActive = searchQuery.equals(suggestion, ignoreCase = true)
                Surface(
                  onClick = {
                    if (isActive) {
                      onSearchQueryChange("")
                    } else {
                      onSearchQueryChange(suggestion)
                    }
                  },
                  shape = RoundedCornerShape(10.dp),
                  color = if (isActive) {
                    MaterialTheme.colorScheme.primary
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                  },
                  modifier = Modifier.testTag("smart_search_chip_${suggestion.lowercase().replace(" ", "_")}")
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.AutoAwesome,
                      contentDescription = null,
                      tint = if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                      } else {
                        MaterialTheme.colorScheme.primary
                      },
                      modifier = Modifier.size(13.dp)
                    )
                    Text(
                      text = suggestion,
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                      color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimary
                      } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                      }
                    )
                  }
                }
              }
            }
          }
        }

        // 2. Category Chips & Interactive Chip-Based Genre Selection Menu
        item(span = { GridItemSpan(maxLineSpan) }) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Category Row (All, Released Movies, Upcoming, Series)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(CatalogCategoryFilter.entries) { filter ->
                FilterChip(
                  selected = selectedCategory == filter,
                  onClick = { onCategoryChange(filter) },
                  label = {
                    Text(
                      text = filter.label,
                      fontWeight = if (selectedCategory == filter) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                  ),
                  modifier = Modifier.testTag("category_chip_${filter.name}")
                )
              }
            }

            // Genre Chip Selection Header + Expand/Sidebar Controls
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.FilterList,
                  contentDescription = "Filter by Genre",
                  tint = MaterialTheme.colorScheme.secondary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = if (selectedGenre == "All") {
                    "Browse by Genre (${genreOptions.size - 1} Genres)"
                  } else {
                    "Active Genre: $selectedGenre (${genreCounts[selectedGenre] ?: catalog.size})"
                  },
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                if (selectedGenre != "All") {
                  TextButton(
                    onClick = { onGenreChange("All") },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                      .height(28.dp)
                      .testTag("clear_genre_filter_button")
                  ) {
                    Text(
                      text = "Reset Genre",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.error,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }

                TextButton(
                  onClick = { isAllGenreChipsExpanded = !isAllGenreChipsExpanded },
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                  modifier = Modifier
                    .height(28.dp)
                    .testTag("expand_genre_chips_button")
                ) {
                  Text(
                    text = if (isAllGenreChipsExpanded) "Compact Row" else "Show All Chips",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            // Chip-Based Genre Selection Menu (Expandable FlowRow OR Horizontal LazyRow)
            if (isAllGenreChipsExpanded) {
              FlowRow(
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("genre_chips_flow_grid"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                genreOptions.forEach { genre ->
                  val isSelected = selectedGenre.equals(genre, ignoreCase = true)
                  val count = genreCounts[genre] ?: 0
                  FilterChip(
                    selected = isSelected,
                    onClick = { onGenreChange(genre) },
                    leadingIcon = {
                      Icon(
                        imageVector = genreIcon(genre),
                        contentDescription = genre,
                        modifier = Modifier.size(16.dp)
                      )
                    },
                    label = {
                      Text(
                        text = "$genre ($count)",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.secondary,
                      selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                      selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.testTag("genre_chip_$genre")
                  )
                }
              }
            } else {
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("genre_chips_row")
              ) {
                items(genreOptions) { genre ->
                  val isSelected = selectedGenre.equals(genre, ignoreCase = true)
                  val count = genreCounts[genre] ?: 0
                  FilterChip(
                    selected = isSelected,
                    onClick = { onGenreChange(genre) },
                    leadingIcon = {
                      Icon(
                        imageVector = genreIcon(genre),
                        contentDescription = genre,
                        modifier = Modifier.size(16.dp)
                      )
                    },
                    label = {
                      Text(
                        text = "$genre ($count)",
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                      )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.secondary,
                      selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                      selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    modifier = Modifier.testTag("genre_chip_$genre")
                  )
                }
              }
            }

            // Release Year Filter Row + Ad-Free Dark Cinema & Auto-Metadata Add Button
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("release_year_filter_row")
              ) {
                items(RELEASE_YEAR_FILTER_OPTIONS) { yrOption ->
                  val isYrSelected = selectedReleaseYear == yrOption
                  FilterChip(
                    selected = isYrSelected,
                    onClick = { onReleaseYearChange(yrOption) },
                    label = {
                      Text(
                        text = yrOption,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isYrSelected) FontWeight.Bold else FontWeight.Medium
                      )
                    },
                    modifier = Modifier.testTag("year_filter_chip_${yrOption.lowercase().replace(" ", "_")}")
                  )
                }
              }

              if (onOpenAddMovieDialog != null) {
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                  onClick = onOpenAddMovieDialog,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  shape = RoundedCornerShape(10.dp),
                  modifier = Modifier
                    .height(34.dp)
                    .testTag("discover_add_movie_auto_fetch_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Auto-Fetch Movie",
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "+ Auto-Fetch",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        // 3. Featured Hero Spotlight (When no search query is active)
        if (searchQuery.isBlank() && heroItem != null) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            val safeHeroId = heroItem.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
            HeroSpotlightBanner(
              item = heroItem,
              currentWatchStatus = watchlistMap[safeHeroId]?.parsedWatchStatus,
              onOpenDetail = { onOpenMedia(heroItem) },
              onWatchMovieNow = { onWatchMovie(heroItem) },
              onToggleStatus = { status -> onQuickStatusSelect(heroItem, status) }
            )
          }
        }

        // 3b. Recently Watched & Continue Playing Rail
        if (watchHistory.isNotEmpty() && searchQuery.isBlank()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            ContinueWatchingSection(
              historyItems = watchHistory,
              onContinuePlaying = onContinueWatchingEntry,
              onRemoveEntry = onRemoveWatchHistoryEntry,
              onClearAll = onClearWatchHistory
            )
          }
        }

        // 3c. Dynamic Category 1: "Trending Now"
        if (searchQuery.isBlank() && selectedGenre == "All" && trendingNowItems.isNotEmpty()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            DynamicCategoryCarouselSection(
              title = "🔥 Trending Now",
              subtitle = "Most watched blockbusters & top-rated global cinema today",
              items = trendingNowItems,
              sectionTag = "category_row_trending_now",
              badgeBuilder = { idx, _ -> "#${idx + 1} Trending" },
              reasonBuilder = { movie -> movie.genres.take(2).joinToString(" • ") },
              onOpenMedia = onOpenMedia
            )
          }
        }

        // 3d. Dynamic Category 2: "New Releases"
        if (searchQuery.isBlank() && selectedGenre == "All" && newReleasesItems.isNotEmpty()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            DynamicCategoryCarouselSection(
              title = "✨ New Releases",
              subtitle = "Fresh theatrical arrivals, new seasons & latest premieres",
              items = newReleasesItems,
              sectionTag = "category_row_new_releases",
              badgeBuilder = { _, movie ->
                if (movie.releaseStatus == ReleaseStatus.UPCOMING) {
                  "SOON • ${movie.releaseYear}"
                } else {
                  "NEW • ${movie.releaseYear}"
                }
              },
              reasonBuilder = { movie -> movie.releaseDateFull },
              onOpenMedia = onOpenMedia
            )
          }
        }

        // 3e. Dynamic Category 3: "Because you watched [Movie Name]"
        if (searchQuery.isBlank() && activeWatchedSeedMovie != null && becauseYouWatchedRecommendations.isNotEmpty()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            val recItems = remember(becauseYouWatchedRecommendations) {
              becauseYouWatchedRecommendations.map { it.first }
            }
            val reasonMap = remember(becauseYouWatchedRecommendations) {
              becauseYouWatchedRecommendations.associate { it.first.id to it.second }
            }
            DynamicCategoryCarouselSection(
              title = "Because you watched ${activeWatchedSeedMovie.title}",
              subtitle = "Personalized picks matching ${activeWatchedSeedMovie.title}'s director (${activeWatchedSeedMovie.effectiveDirector.ifBlank { "Top Filmmakers" }}), cast & ${activeWatchedSeedMovie.genres.take(2).joinToString("/")} genres",
              items = recItems,
              sectionTag = "category_row_because_you_watched",
              badgeBuilder = { _, _ -> "Recommended" },
              reasonBuilder = { movie -> reasonMap[movie.id] },
              seedOptions = watchedSeedMovies.map { it.title }.take(5),
              selectedSeedTitle = activeWatchedSeedMovie.title,
              onSelectSeedTitle = { selectedWatchedSeedTitle = it },
              onOpenMedia = onOpenMedia
            )
          }
        }

        // 3f. Dynamic Category 4: "You Might Also Like" (Collection + Watch History Driven)
        if (searchQuery.isBlank() && youMightAlsoLikeRecommendations.isNotEmpty()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            val likeItems = remember(youMightAlsoLikeRecommendations) {
              youMightAlsoLikeRecommendations.map { it.first }
            }
            val matchPercentMap = remember(youMightAlsoLikeRecommendations) {
              youMightAlsoLikeRecommendations.associate { it.first.id to "${it.second}% Match" }
            }
            val reasonMap = remember(youMightAlsoLikeRecommendations) {
              youMightAlsoLikeRecommendations.associate { it.first.id to it.third }
            }
            DynamicCategoryCarouselSection(
              title = "🎯 You Might Also Like",
              subtitle = "Curated from your Smart Collection tags, ratings & watch history • Ad-Free Dark Cinema",
              items = likeItems,
              sectionTag = "category_row_you_might_also_like",
              badgeBuilder = { _, movie -> matchPercentMap[movie.id] ?: "96% Match" },
              reasonBuilder = { movie -> reasonMap[movie.id] },
              onOpenMedia = onOpenMedia
            )
          }
        }

        // 4. Section Title & Pull-to-Refresh Sync Status Pill
        item(span = { GridItemSpan(maxLineSpan) }) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = when {
                  searchQuery.isNotBlank() ->
                    "Smart Search Results for \"$searchQuery\" (${catalog.size})"
                  selectedGenre == "All" && selectedReleaseYear == "All Years" ->
                    "${selectedCategory.label} (${catalog.size})"
                  selectedGenre != "All" && selectedReleaseYear != "All Years" ->
                    "$selectedGenre • $selectedReleaseYear • ${selectedCategory.label} (${catalog.size})"
                  selectedGenre != "All" ->
                    "$selectedGenre • ${selectedCategory.label} (${catalog.size})"
                  else ->
                    "$selectedReleaseYear • ${selectedCategory.label} (${catalog.size})"
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
              )
              Text(
                text = if (searchQuery.isNotBlank()) {
                  "Matched across Movie Titles, Actors, Directors & Compound Genres"
                } else if (isRefreshing) {
                  "Refreshing latest releases..."
                } else {
                  lastRefreshedLabel
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("last_refreshed_label")
              )
            }

            Surface(
              onClick = onRefresh,
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
              modifier = Modifier.testTag("pull_to_refresh_chip")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = "Sync Latest Releases",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(14.dp)
                )
                Text(
                  text = "Latest Releases",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }

        if (catalog.isEmpty()) {
          item(span = { GridItemSpan(maxLineSpan) }) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Text(
                  text = if (searchQuery.isNotBlank()) {
                    "No movies, actors, directors, or genres matched '$searchQuery'."
                  } else {
                    "No movies or series matched '$selectedGenre'."
                  },
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FilledTonalButton(
                  onClick = {
                    if (searchQuery.isNotBlank()) {
                      onSearchQueryChange("")
                    } else {
                      onGenreChange("All")
                    }
                  }
                ) {
                  Text(if (searchQuery.isNotBlank()) "Clear Search" else "Show All Genres")
                }
              }
            }
          }
        } else {
          items(catalog, key = { it.id }) { media ->
            val safeId = media.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
            val matchBadge = remember(media.id, searchQuery) {
              if (searchQuery.isNotBlank()) {
                SmartSearchMatcher.evaluate(media, searchQuery).badge
              } else {
                null
              }
            }
            MediaPosterCard(
              item = media,
              currentWatchStatus = watchlistMap[safeId]?.parsedWatchStatus,
              onClick = { onOpenMedia(media) },
              onWatchMovieNow = { onWatchMovie(media) },
              onQuickStatusSelect = { status -> onQuickStatusSelect(media, status) },
              matchBadge = matchBadge
            )
          }

          if (isLoadingMore) {
            items(4) {
              ShimmerMovieCard()
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 12.dp)
                  .testTag("endless_loading_shimmer_row"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (selectedCategory == CatalogCategoryFilter.SERIES) "Loading more TV & Web Series..." else "Loading more global movies...",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }

        item(span = { GridItemSpan(maxLineSpan) }) {
          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun GenreFilterSidebar(
  genreOptions: List<String>,
  genreCounts: Map<String, Int>,
  selectedGenre: String,
  selectedCategory: CatalogCategoryFilter,
  onSelectGenre: (String) -> Unit,
  onSelectCategory: (CatalogCategoryFilter) -> Unit,
  onResetFilters: () -> Unit,
  onCloseSidebar: () -> Unit
) {
  Surface(
    modifier = Modifier
      .width(220.dp)
      .fillMaxHeight()
      .testTag("genre_filter_sidebar"),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    tonalElevation = 6.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Tune,
            contentDescription = "Genre Sidebar",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
          )
          Text(
            text = "Genre Filter",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        IconButton(
          onClick = onCloseSidebar,
          modifier = Modifier
            .size(32.dp)
            .testTag("close_genre_sidebar_button")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Genre Sidebar",
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Text(
        text = "Narrow down movies & series by genre or format",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
      )

      HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

      LazyColumn(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        item {
          Text(
            text = "GENRES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
          )
        }

        items(genreOptions, key = { it }) { genre ->
          val isSelected = selectedGenre.equals(genre, ignoreCase = true)
          val count = genreCounts[genre] ?: 0

          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            },
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSelectGenre(genre) }
              .testTag("sidebar_genre_item_$genre")
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
              ) {
                Icon(
                  imageVector = genreIcon(genre),
                  contentDescription = genre,
                  tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.primary
                  },
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = genre,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  },
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }

              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) {
                  MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                }
              ) {
                Text(
                  text = count.toString(),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimary
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  },
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
        }

        item {
          HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
          Text(
            text = "FORMAT",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
          )
        }

        items(CatalogCategoryFilter.entries) { category ->
          val isSelectedCat = selectedCategory == category
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelectedCat) {
              MaterialTheme.colorScheme.secondaryContainer
            } else {
              MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            },
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .clickable { onSelectCategory(category) }
              .testTag("sidebar_category_item_${category.name}")
          ) {
            Text(
              text = category.label,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = if (isSelectedCat) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelectedCat) {
                MaterialTheme.colorScheme.onSecondaryContainer
              } else {
                MaterialTheme.colorScheme.onSurface
              },
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      FilledTonalButton(
        onClick = onResetFilters,
        colors = ButtonDefaults.filledTonalButtonColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sidebar_reset_filters_button")
      ) {
        Text("Reset All Filters")
      }
    }
  }
}

@Composable
fun ShimmerMovieCard(modifier: Modifier = Modifier) {
  val transition = rememberInfiniteTransition(label = "shimmer_transition")
  val translateAnim by transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "shimmer_anim"
  )
  val shimmerBrush = Brush.linearGradient(
    colors = listOf(
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ),
    start = Offset.Zero,
    end = Offset(x = translateAnim, y = translateAnim)
  )

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
    modifier = modifier
      .fillMaxWidth()
      .testTag("shimmer_movie_card")
  ) {
    Column {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(2f / 3f)
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .background(shimmerBrush)
      )
      Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(14.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(shimmerBrush)
        )
        Box(
          modifier = Modifier
            .fillMaxWidth(0.5f)
            .height(10.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(shimmerBrush)
        )
      }
    }
  }
}
