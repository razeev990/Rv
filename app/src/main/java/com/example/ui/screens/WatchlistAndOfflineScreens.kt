package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DeleteSweep
import java.util.Locale
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.MediaType
import com.example.data.model.SmartDownloadQuality
import com.example.data.model.SmartDownloadState
import com.example.data.model.SmartDownloadTask
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem

private val STORAGE_SOURCE_OPTIONS = listOf(
  "CineVault Cloud",
  "Google Drive Stream",
  "Dropbox Cloud",
  "Local Device Storage"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WatchlistScreen(
  userEmailOrId: String,
  watchlistItems: List<WatchlistItem>,
  watchHistory: List<WatchHistoryEntry> = emptyList(),
  availablePlaylistTags: List<String> = WatchlistItem.DEFAULT_PLAYLIST_TAGS,
  traktHandle: String = "@cinevault_global",
  lastTraktSyncLabel: String = "Trakt.tv Scrobble Active • Global History Synced",
  autoFetchedPreviewMedia: MediaItem? = null,
  isAutoFetchingMetadata: Boolean = false,
  onPreviewAutoMetadata: (String, String) -> Unit = { _, _ -> },
  onClearAutoPreview: () -> Unit = {},
  onAddMovieWithAutoMetadata: (String, List<String>, String, String, String?) -> Unit = { _, _, _, _, _ -> },
  onAddCustomPlaylistTag: (String) -> Unit = {},
  onToggleItemTag: (WatchlistItem, String) -> Unit = { _, _ -> },
  onSyncTraktTv: () -> Unit = {},
  onOpenMedia: (MediaItem) -> Unit,
  onContinueWatchingEntry: (WatchHistoryEntry) -> Unit = {},
  onSwitchStatus: (WatchlistItem, WatchStatus) -> Unit,
  onRemoveItem: (WatchlistItem) -> Unit,
  activeProfileName: String = "Main Profile",
  onToggleFavorite: ((MediaItem) -> Unit)? = null
) {
  val context = LocalContext.current

  // 0 = Continue Watching, 1 = Unwatched (Want to Watch), 2 = Watched, 3 = Favorites ❤️
  var selectedTabIndex by remember { mutableIntStateOf(1) }
  var selectedFolderTag by remember { mutableStateOf("All") }
  var selectedTypeFilter by remember { mutableStateOf<MediaType?>(null) }

  var showAddMovieDialog by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var newFolderNameInput by remember { mutableStateOf("") }

  val continueWatchingCount = watchHistory.size
  val unwatchedCount = watchlistItems.count { it.parsedWatchStatus == WatchStatus.WANT_TO_WATCH }
  val watchedCount = watchlistItems.count { it.parsedWatchStatus == WatchStatus.WATCHED }
  val favoritesCount = watchlistItems.count { it.isFavorite }

  val filteredItems = remember(
    watchlistItems,
    selectedTabIndex,
    selectedFolderTag,
    selectedTypeFilter
  ) {
    watchlistItems.filter { item ->
      val matchesTab = when (selectedTabIndex) {
        0 -> item.toWatchHistoryEntry() != null
        1 -> item.parsedWatchStatus == WatchStatus.WANT_TO_WATCH
        2 -> item.parsedWatchStatus == WatchStatus.WATCHED
        else -> item.isFavorite
      }
      val matchesFolder = selectedFolderTag == "All" ||
        item.customTags.any { it.equals(selectedFolderTag, ignoreCase = true) }
      val matchesType = selectedTypeFilter == null || item.parsedMediaType == selectedTypeFilter
      matchesTab && matchesFolder && matchesType
    }
  }

  // Create New Custom Folder / Playlist Tag Dialog
  if (showNewFolderDialog) {
    AlertDialog(
      onDismissRequest = { showNewFolderDialog = false },
      icon = {
        Icon(
          imageVector = Icons.Default.CreateNewFolder,
          contentDescription = "New Folder",
          tint = MaterialTheme.colorScheme.primary
        )
      },
      title = { Text("Create Custom Tag / Folder", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Apne Smart Library collection ke liye naya folder ya playlist tag banayein (e.g., 'Weekend Watch', 'Late Night Thriller', 'Family'):",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          OutlinedTextField(
            value = newFolderNameInput,
            onValueChange = { newFolderNameInput = it },
            placeholder = { Text("Folder / Tag name...") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("new_folder_tag_input")
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFolderNameInput.isNotBlank()) {
              val clean = newFolderNameInput.trim()
              onAddCustomPlaylistTag(clean)
              selectedFolderTag = clean
              newFolderNameInput = ""
              showNewFolderDialog = false
            }
          },
          modifier = Modifier.testTag("confirm_new_folder_button")
        ) {
          Text("Create Folder")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFolderDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Auto-Metadata Fetching + Cloud/Local Storage Add Movie Dialog
  if (showAddMovieDialog) {
    SmartLibraryAddMovieDialog(
      availableTags = availablePlaylistTags,
      autoFetchedPreview = autoFetchedPreviewMedia,
      isAutoFetching = isAutoFetchingMetadata,
      onPreviewMetadata = onPreviewAutoMetadata,
      onDismiss = {
        onClearAutoPreview()
        showAddMovieDialog = false
      },
      onSaveToLibrary = { title, tags, storageSource, streamUrl, reviewNote ->
        onAddMovieWithAutoMetadata(title, tags, storageSource, streamUrl, reviewNote)
        showAddMovieDialog = false
      }
    )
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("watchlist_screen")
  ) {
    // 1. Smart Library Header Card: Auto-Metadata + Trakt.tv Global Sync + Share Collection
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = Icons.Default.FolderSpecial,
              contentDescription = "Smart Library",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Smart Library • $activeProfileName",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "$lastTraktSyncLabel • ${watchlistItems.size} Titles",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Share Collection Link Button (Community & Sharing)
          IconButton(
            onClick = {
              val folderSlug = selectedFolderTag.lowercase().replace(" ", "-")
              val shareUrl = "https://cinevault.app/collection/${activeProfileName.lowercase().replace(Regex("[^a-z0-9]"), "")}/$folderSlug"
              val movieNames = filteredItems.take(6).joinToString("\n") {
                "• ${it.title} (${it.releaseYear}) — ⭐ ${it.userRating ?: it.rating.toInt()}/10"
              }.ifBlank { "• Kalki 2898 AD (2024)\n• Jawan (2023)\n• Dune: Part Two (2024)" }
              val shareBody = "🎬 Check out my '$selectedFolderTag' Movie Collection on CineVault ($activeProfileName):\n\n$movieNames\n\n🔗 Open Collection: $shareUrl"
              val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "CineVault Collection: $selectedFolderTag")
                putExtra(Intent.EXTRA_TEXT, shareBody)
              }
              runCatching {
                context.startActivity(
                  Intent.createChooser(sendIntent, "Share Movie Collection Link")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
              }
            },
            modifier = Modifier
              .size(36.dp)
              .testTag("share_collection_button")
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Share Collection Link",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }

        // Action Buttons Row: + Add Movie (Auto-Fetch TMDb/IMDb), Trakt.tv Sync, + Custom Folder
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Button(
            onClick = { showAddMovieDialog = true },
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("smart_library_add_movie_button")
          ) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = "Add Movie with Auto-Metadata",
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "+ Add Movie (Auto-Fetch TMDb/IMDb)",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }

          FilledTonalButton(
            onClick = onSyncTraktTv,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("trakt_sync_button")
          ) {
            Icon(
              imageVector = Icons.Default.CloudSync,
              contentDescription = "Trakt.tv Sync",
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Trakt.tv Sync ($traktHandle)",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }

          FilledTonalButton(
            onClick = { showNewFolderDialog = true },
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .height(34.dp)
              .testTag("create_custom_folder_button")
          ) {
            Icon(
              imageVector = Icons.Default.CreateNewFolder,
              contentDescription = "Create Custom Folder",
              modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "+ Folder / Tag",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // 2. Custom Tags & Folders Filter Row ("All", "Weekend Watch", "Favorites", "Family", etc.)
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 6.dp)
        .testTag("custom_tags_playlists_row")
    ) {
      val allFolders = listOf("All") + availablePlaylistTags.distinct()
      items(allFolders) { folderTag ->
        val isSelected = selectedFolderTag.equals(folderTag, ignoreCase = true)
        FilterChip(
          selected = isSelected,
          onClick = { selectedFolderTag = folderTag },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = folderTag,
              modifier = Modifier.size(15.dp)
            )
          },
          label = {
            Text(
              text = if (folderTag == "All") "All Folders" else folderTag,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.secondary,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
          ),
          modifier = Modifier.testTag("playlist_tag_chip_${folderTag.lowercase().replace(" ", "_")}")
        )
      }
    }

    // 3. Four Watch Status Division Tabs: Continue Watching | Unwatched | Watched | Favorites ❤️
    SecondaryScrollableTabRow(
      selectedTabIndex = selectedTabIndex,
      edgePadding = 0.dp,
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
    ) {
      Tab(
        selected = selectedTabIndex == 0,
        onClick = { selectedTabIndex = 0 },
        text = {
          Text(
            text = "Continue ($continueWatchingCount)",
            fontWeight = FontWeight.Bold
          )
        },
        icon = {
          Icon(
            imageVector = Icons.Default.PlayCircleFilled,
            contentDescription = "Continue Watching Tab"
          )
        },
        modifier = Modifier.testTag("tab_continue_watching")
      )

      Tab(
        selected = selectedTabIndex == 1,
        onClick = { selectedTabIndex = 1 },
        text = {
          Text(
            text = "Unwatched ($unwatchedCount)",
            fontWeight = FontWeight.Bold
          )
        },
        icon = {
          Icon(
            imageVector = Icons.Default.BookmarkAdded,
            contentDescription = "Unwatched Tab"
          )
        },
        modifier = Modifier.testTag("tab_want_to_watch")
      )

      Tab(
        selected = selectedTabIndex == 2,
        onClick = { selectedTabIndex = 2 },
        text = {
          Text(
            text = "Watched ($watchedCount)",
            fontWeight = FontWeight.Bold
          )
        },
        icon = {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Watched Tab"
          )
        },
        modifier = Modifier.testTag("tab_watched")
      )

      Tab(
        selected = selectedTabIndex == 3,
        onClick = { selectedTabIndex = 3 },
        text = {
          Text(
            text = "Favorites ($favoritesCount)",
            fontWeight = FontWeight.Bold
          )
        },
        icon = {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Favorites Tab",
            tint = Color(0xFFEF4444)
          )
        },
        modifier = Modifier.testTag("tab_favorites")
      )
    }

    // Type Filter Chips (All, Movies, Series)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = selectedTypeFilter == null,
        onClick = { selectedTypeFilter = null },
        label = { Text("All Formats") }
      )
      FilterChip(
        selected = selectedTypeFilter == MediaType.MOVIE,
        onClick = { selectedTypeFilter = MediaType.MOVIE },
        label = { Text("Movies") }
      )
      FilterChip(
        selected = selectedTypeFilter == MediaType.SERIES,
        onClick = { selectedTypeFilter = MediaType.SERIES },
        label = { Text("Series") }
      )
    }

    // Tab 0: Continue Watching Dedicated Division
    if (selectedTabIndex == 0) {
      if (watchHistory.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No movies in Continue Watching yet. Start watching any title to sync real-time progress across Mobile, Tablet & Android TV!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(bottom = 24.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(watchHistory, key = { it.mediaId }) { entry ->
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onContinueWatchingEntry(entry) }
                .testTag("continue_watching_library_item_${entry.mediaId}"),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
              )
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  AsyncImage(
                    model = entry.posterUrl,
                    contentDescription = entry.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                      .width(68.dp)
                      .height(96.dp)
                      .clip(RoundedCornerShape(10.dp))
                  )
                  Spacer(modifier = Modifier.width(12.dp))
                  Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Text(
                      text = entry.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "⏱️ ${entry.resumeBadgeText}",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = "✓ Synced to Cloud & Trakt.tv ($traktHandle)",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.tertiary
                    )
                    LinearProgressIndicator(
                      progress = { entry.progressFraction },
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Button(onClick = { onContinueWatchingEntry(entry) }) {
                    Icon(
                      imageVector = Icons.Default.PlayArrow,
                      contentDescription = "Resume",
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resume")
                  }
                }
              }
            }
          }
        }
      }
    } else if (filteredItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(28.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            imageVector = if (selectedTabIndex == 3) Icons.Default.FavoriteBorder else Icons.Default.Movie,
            contentDescription = "Empty Smart Library",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(52.dp)
          )
          Text(
            text = when (selectedTabIndex) {
              1 -> "No 'Unwatched' movies in '$selectedFolderTag'"
              2 -> "No 'Watched' movies in '$selectedFolderTag'"
              else -> "No 'Favorites ❤️' in '$selectedFolderTag'"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
          )
          Button(onClick = { showAddMovieDialog = true }) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Movie with Auto-Metadata")
          }
        }
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredItems, key = { it.mediaId.ifBlank { it.id } }) { item ->
          WatchlistRowCard(
            item = item,
            availableTags = availablePlaylistTags,
            onClick = { onOpenMedia(item.toMediaItem()) },
            onToggleFavorite = { onToggleFavorite?.invoke(item.toMediaItem()) },
            onToggleTag = { tag -> onToggleItemTag(item, tag) },
            onToggleOtherStatus = {
              val next = if (item.parsedWatchStatus == WatchStatus.WANT_TO_WATCH) {
                WatchStatus.WATCHED
              } else {
                WatchStatus.WANT_TO_WATCH
              }
              onSwitchStatus(item, next)
            },
            onDelete = { onRemoveItem(item) }
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SmartLibraryAddMovieDialog(
  availableTags: List<String>,
  autoFetchedPreview: MediaItem?,
  isAutoFetching: Boolean,
  onPreviewMetadata: (String, String) -> Unit,
  onDismiss: () -> Unit,
  onSaveToLibrary: (String, List<String>, String, String, String?) -> Unit
) {
  var movieTitleInput by remember { mutableStateOf("") }
  var selectedStorageSource by remember { mutableStateOf(STORAGE_SOURCE_OPTIONS.first()) }
  var customStreamOrDriveUrl by remember { mutableStateOf("") }
  var personalReviewNote by remember { mutableStateOf("") }
  var selectedTags by remember { mutableStateOf(listOf("Weekend Watch")) }

  // Android Local Video Picker (Zero-permission system file picker)
  val localVideoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri ->
    if (uri != null) {
      selectedStorageSource = "Local Device Storage"
      customStreamOrDriveUrl = uri.toString()
      if (movieTitleInput.isBlank()) {
        movieTitleInput = "Local Cinema Movie"
      }
      onPreviewMetadata(movieTitleInput, uri.toString())
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = "Auto-Fetch Metadata",
        tint = MaterialTheme.colorScheme.primary
      )
    },
    title = {
      Text(
        text = "Add Movie • Auto-Fetch TMDb/IMDb",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Movie ka naam likhein — app automatically TMDb / IMDb se Poster, Cast, Director, Rating aur Summary fetch kar lega:",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = movieTitleInput,
            onValueChange = { movieTitleInput = it },
            placeholder = { Text("e.g. Interstellar, Salaar, Stree 2...") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("auto_metadata_title_input")
          )
          FilledTonalButton(
            onClick = {
              if (movieTitleInput.isNotBlank()) {
                onPreviewMetadata(movieTitleInput, customStreamOrDriveUrl)
              }
            },
            modifier = Modifier.testTag("fetch_metadata_now_button")
          ) {
            if (isAutoFetching) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
              Text("Fetch")
            }
          }
        }

        // Quick Auto-Fetch Presets
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Interstellar", "Salaar", "Animal", "Stree 2", "Kalki 2898 AD").forEach { sample ->
            Surface(
              onClick = {
                movieTitleInput = sample
                onPreviewMetadata(sample, customStreamOrDriveUrl)
              },
              color = MaterialTheme.colorScheme.surfaceVariant,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = sample,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Live Auto-Fetched TMDb / IMDb Preview Card
        if (autoFetchedPreview != null) {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("auto_fetched_metadata_preview_card"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            )
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              AsyncImage(
                model = autoFetchedPreview.posterUrl,
                contentDescription = autoFetchedPreview.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .width(60.dp)
                  .height(86.dp)
                  .clip(RoundedCornerShape(8.dp))
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                  text = "✨ ${autoFetchedPreview.title} (${autoFetchedPreview.releaseYear})",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "⭐ ${autoFetchedPreview.rating} • Dir: ${autoFetchedPreview.effectiveDirector.ifBlank { "Top Director" }}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold
                )
                if (autoFetchedPreview.cast.isNotEmpty()) {
                  Text(
                    text = "Cast: ${autoFetchedPreview.cast.take(3).joinToString { it.name }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
                Text(
                  text = autoFetchedPreview.overview,
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
                )
              }
            }
          }
        }

        // Local & Cloud Storage Support
        Text(
          text = "Storage Source (Local Device or Cloud Drive):",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          STORAGE_SOURCE_OPTIONS.forEach { src ->
            FilterChip(
              selected = selectedStorageSource == src,
              onClick = { selectedStorageSource = src },
              label = { Text(src, style = MaterialTheme.typography.labelSmall) },
              modifier = Modifier.testTag("storage_source_chip_${src.lowercase().replace(" ", "_")}")
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = customStreamOrDriveUrl,
            onValueChange = { customStreamOrDriveUrl = it },
            placeholder = { Text("Cloud Drive / WebDAV / Local URI (optional)") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("storage_stream_uri_input")
          )
          FilledTonalButton(
            onClick = { localVideoPickerLauncher.launch("video/*") },
            modifier = Modifier.testTag("pick_local_video_button")
          ) {
            Icon(imageVector = Icons.Default.Storage, contentDescription = "Pick Local File", modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Device", style = MaterialTheme.typography.labelSmall)
          }
        }

        // Custom Tags & Playlists Selection
        Text(
          text = "Assign Custom Tags / Folders:",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          availableTags.forEach { tag ->
            val isSelected = selectedTags.any { it.equals(tag, ignoreCase = true) }
            FilterChip(
              selected = isSelected,
              onClick = {
                selectedTags = if (isSelected) {
                  selectedTags.filterNot { it.equals(tag, ignoreCase = true) }
                } else {
                  selectedTags + tag
                }
              },
              label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }

        OutlinedTextField(
          value = personalReviewNote,
          onValueChange = { personalReviewNote = it },
          placeholder = { Text("Write a personal review or comment (optional)...") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("add_movie_review_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val finalTitle = movieTitleInput.ifBlank { autoFetchedPreview?.title ?: "Interstellar" }
          onSaveToLibrary(
            finalTitle,
            selectedTags,
            selectedStorageSource,
            customStreamOrDriveUrl,
            personalReviewNote.ifBlank { null }
          )
        },
        modifier = Modifier.testTag("confirm_add_smart_library_button")
      ) {
        Text("Save to Smart Library")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WatchlistRowCard(
  item: WatchlistItem,
  availableTags: List<String>,
  onClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleTag: (String) -> Unit,
  onToggleOtherStatus: () -> Unit,
  onDelete: () -> Unit
) {
  var showTagAssignMenu by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("watchlist_item_${item.canonicalMediaId}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        AsyncImage(
          model = item.posterUrl,
          contentDescription = item.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .width(76.dp)
            .height(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = item.parsedMediaType.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            Surface(
              color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = item.storageSource,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
            if (item.isTraktSynced) {
              Text(
                text = "✓ Trakt.tv",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = item.title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Text(
            text = "${item.releaseYear} • ⭐ ${item.rating} • ${item.genres}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          // Custom Tags & Folder Chips
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            item.customTags.forEach { tag ->
              Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = "🏷️ $tag",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Surface(
              onClick = { showTagAssignMenu = !showTagAssignMenu },
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
              shape = RoundedCornerShape(6.dp)
            ) {
              Text(
                text = if (showTagAssignMenu) "Done" else "+ Tag",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          val progressEntry = item.toWatchHistoryEntry()
          if (progressEntry != null) {
            Text(
              text = "⏱️ ${progressEntry.resumeBadgeText}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }

          if (!item.userNotes.isNullOrBlank()) {
            Text(
              text = "💬 Review: \"${item.userNotes}\"",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )
          }

          if (item.userRating != null) {
            Text(
              text = "Personal Rating: ${item.userRating}/10 ⭐",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.tertiary,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Column(
          horizontalAlignment = Alignment.End,
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.testTag("favorite_watchlist_btn_${item.canonicalMediaId}")
          ) {
            Icon(
              imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Toggle Favorite",
              tint = if (item.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = onToggleOtherStatus,
            modifier = Modifier.testTag("switch_status_btn_${item.canonicalMediaId}")
          ) {
            Icon(
              imageVector = Icons.Default.SwapHoriz,
              contentDescription = "Switch between Unwatched and Watched",
              tint = MaterialTheme.colorScheme.primary
            )
          }

          IconButton(
            onClick = onDelete,
            modifier = Modifier.testTag("delete_watchlist_btn_${item.canonicalMediaId}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Remove from Watchlist",
              tint = MaterialTheme.colorScheme.error
            )
          }
        }
      }

      if (showTagAssignMenu) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          availableTags.forEach { tag ->
            val hasTag = item.customTags.any { it.equals(tag, ignoreCase = true) }
            FilterChip(
              selected = hasTag,
              onClick = { onToggleTag(tag) },
              label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun OfflineModeScreen(
  isOfflineModeEnabled: Boolean,
  onToggleOfflineMode: (Boolean) -> Unit,
  offlineSavedMedia: List<MediaItem>,
  watchlistItems: List<WatchlistItem>,
  cachedCatalogCount: Int,
  smartDownloadTasks: List<SmartDownloadTask> = emptyList(),
  onStartSmartDownload: (MediaItem, SmartDownloadQuality) -> Unit = { _, _ -> },
  onTogglePauseResumeDownload: (String) -> Unit = {},
  onCancelDownload: (String) -> Unit = {},
  isAutoDeleteWatchedEnabled: Boolean = true,
  onToggleAutoDeleteWatched: (Boolean) -> Unit = {},
  isAutoPruneStorageEnabled: Boolean = true,
  onToggleAutoPruneStorage: (Boolean) -> Unit = {},
  storageUsedDownloadsMb: Float = 284.5f,
  storageUsedCacheMb: Float = 96.2f,
  onCleanStorageNow: () -> Unit = {},
  onOpenMedia: (MediaItem) -> Unit
) {
  var selectedQuality by remember { mutableStateOf(SmartDownloadQuality.HD_720P) }
  val combinedOfflineItems = remember(offlineSavedMedia, watchlistItems) {
    (offlineSavedMedia + watchlistItems.map { it.toMediaItem() }).distinctBy { it.id }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("offline_mode_screen"),
    contentPadding = PaddingValues(vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isOfflineModeEnabled) {
            MaterialTheme.colorScheme.tertiaryContainer
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          }
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Icon(
              imageVector = if (isOfflineModeEnabled) {
                Icons.Default.SignalWifiOff
              } else {
                Icons.Default.DownloadDone
              },
              contentDescription = "Offline Mode",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(30.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Smart Offline Downloads & Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "$cachedCatalogCount catalog titles & ${combinedOfflineItems.size} offline titles • Quality selector + Pause/Resume enabled.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Switch(
            checked = isOfflineModeEnabled,
            onCheckedChange = onToggleOfflineMode,
            modifier = Modifier.testTag("offline_mode_switch")
          )
        }
      }
    }

    // Automatic Smart Storage Auto-Delete Card
    item {
      val totalStorageMb = storageUsedDownloadsMb + storageUsedCacheMb
      val quotaMb = 1536f // 1.5 GB Quota
      val progressRatio = (totalStorageMb / quotaMb).coerceIn(0f, 1f)

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("smart_storage_auto_delete_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = "Smart Storage Auto-Delete",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
              Column {
                Text(
                  text = "Automatic Smart Storage Auto-Delete",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Reclaims phone storage when movies are watched",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            Surface(
              color = MaterialTheme.colorScheme.primaryContainer,
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "${String.format(Locale.US, "%.0f", totalStorageMb)} MB / 1.5 GB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          // Real-time Storage Meter
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LinearProgressIndicator(
              progress = { progressRatio },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = if (progressRatio > 0.85f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Downloads: ${String.format(Locale.US, "%.1f", storageUsedDownloadsMb)} MB • Cache: ${String.format(Locale.US, "%.1f", storageUsedCacheMb)} MB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${String.format(Locale.US, "%.0f", (1f - progressRatio) * 100)}% Free",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }

          // Toggle 1: Auto-Delete Watched Content
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Auto-Delete Watched Content",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Dekhi hui movies (Watched) ke offline files automatically delete ho jate hain.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isAutoDeleteWatchedEnabled,
              onCheckedChange = onToggleAutoDeleteWatched,
              modifier = Modifier.testTag("auto_delete_watched_switch")
            )
          }

          // Toggle 2: Storage Quota Auto-Prune (1.5 GB)
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Storage Quota Auto-Prune (1.5 GB)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "Storage 1.5 GB exceed karne par purani dekhi hui movies auto-prune ho jati hain.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Switch(
              checked = isAutoPruneStorageEnabled,
              onCheckedChange = onToggleAutoPruneStorage,
              modifier = Modifier.testTag("auto_prune_storage_switch")
            )
          }

          // 1-Tap Action: Clean Stream Cache & Watched Storage Now
          Button(
            onClick = onCleanStorageNow,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("clean_storage_now_btn"),
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.secondary
            )
          ) {
            Icon(
              imageVector = Icons.Default.DeleteSweep,
              contentDescription = "Clean Cache",
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clean Stream Cache & Watched Storage Now")
          }
        }
      }
    }

    // Smart Download Quality Selector Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              imageVector = Icons.Default.HighQuality,
              contentDescription = "Smart Download Quality",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Default Smart Download Quality: ${selectedQuality.label} (${selectedQuality.badge})",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          }

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SmartDownloadQuality.entries) { qOpt ->
              FilterChip(
                selected = selectedQuality == qOpt,
                onClick = { selectedQuality = qOpt },
                label = { Text(qOpt.label) },
                modifier = Modifier.testTag("smart_download_quality_chip_${qOpt.name.lowercase()}")
              )
            }
          }
        }
      }
    }

    // Active / Queued Smart Downloads with Pause & Resume Controls
    if (smartDownloadTasks.isNotEmpty()) {
      item {
        Text(
          text = "Smart Download Manager (${smartDownloadTasks.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      items(smartDownloadTasks, key = { "dl_${it.media.id}" }) { task ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .testTag("smart_download_task_${task.media.id}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
          )
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = task.media.title,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${task.quality.label} • ${task.downloadedMb} MB / ${task.quality.totalMb} MB • ${task.state.name}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                if (task.state != SmartDownloadState.COMPLETED) {
                  FilledTonalButton(
                    onClick = { onTogglePauseResumeDownload(task.media.id) },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                      .height(32.dp)
                      .testTag("pause_resume_download_btn_${task.media.id}")
                  ) {
                    Icon(
                      imageVector = if (task.state == SmartDownloadState.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                      contentDescription = "Pause or Resume Download",
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (task.state == SmartDownloadState.PAUSED) "Resume" else "Pause",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                IconButton(
                  onClick = { onCancelDownload(task.media.id) },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove Download",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }

            LinearProgressIndicator(
              progress = { task.progressFraction },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp)),
              color = if (task.state == SmartDownloadState.PAUSED) {
                MaterialTheme.colorScheme.secondary
              } else {
                MaterialTheme.colorScheme.primary
              }
            )
          }
        }
      }
    }

    item {
      Text(
        text = "Saved for Offline Viewing (${combinedOfflineItems.size})",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground
      )
    }

    if (combinedOfflineItems.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
          )
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Text(
              text = "No Offline Titles Pinned Yet",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Download any movie with quality choice (480p / 720p / 1080p / 4K) and pause/resume support.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    } else {
      items(combinedOfflineItems, key = { it.id }) { media ->
        val existingTask = smartDownloadTasks.find { it.media.id == media.id }
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenMedia(media) },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            AsyncImage(
              model = media.posterUrl,
              contentDescription = media.title,
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .width(64.dp)
                .height(92.dp)
                .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = media.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${media.mediaType.label} • ${media.releaseYear} • ${media.genres.joinToString(", ")}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = media.overview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (existingTask == null) {
              FilledTonalButton(
                onClick = { onStartSmartDownload(media, selectedQuality) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                  .height(34.dp)
                  .testTag("start_smart_download_btn_${media.id}")
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDownload,
                  contentDescription = "Download",
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(selectedQuality.label.substringBefore(" "), style = MaterialTheme.typography.labelSmall)
              }
            } else if (existingTask.state != SmartDownloadState.COMPLETED) {
              FilledTonalButton(
                onClick = { onTogglePauseResumeDownload(media.id) },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Text(
                  text = if (existingTask.state == SmartDownloadState.PAUSED) "▶ Resume" else "⏸ ${(existingTask.progressFraction * 100).toInt()}%",
                  style = MaterialTheme.typography.labelSmall
                )
              }
            } else {
              Surface(
                color = Color(0xFF15803D).copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text(
                  text = "✓ ${existingTask.quality.label.substringBefore(" ")}",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF4ADE80),
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
