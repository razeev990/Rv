package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.WatchHistoryEntry
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.components.AudioTrackOption
import com.example.ui.components.CaptionTrackOption
import com.example.ui.components.ContinueWatchingSection
import com.example.ui.components.InAppCinemaPlayer
import com.example.ui.components.PlayerStreamMode
import java.util.Locale

@Composable
fun WatchMoviesScreen(
  catalog: List<MediaItem>,
  watchlistMap: Map<String, WatchlistItem>,
  watchHistory: List<WatchHistoryEntry> = emptyList(),
  onOpenMediaDetail: (MediaItem) -> Unit,
  onSaveStatus: (MediaItem, WatchStatus) -> Unit,
  onRemoveWatchHistoryEntry: (String) -> Unit = {},
  onClearWatchHistory: () -> Unit = {},
  onRecordPlaybackProgress: (MediaItem, Int, Int, Int, Int) -> Unit = { _, _, _, _, _ -> }
) {
  val historyMap = remember(watchHistory) {
    watchHistory.associateBy { it.mediaId }
  }
  var nowPlayingItem by remember(catalog) {
    mutableStateOf(
      watchHistory.firstOrNull()?.let { recent ->
        catalog.find { it.id == recent.mediaId } ?: recent.toMediaItem()
      } ?: catalog.firstOrNull()
    )
  }
  var activeStreamMode by remember {
    mutableStateOf(PlayerStreamMode.FULL_MOVIE_SERVER_1)
  }
  var selectedSeasonNumber by remember(nowPlayingItem?.id) {
    mutableStateOf(historyMap[nowPlayingItem?.id]?.seasonNumber ?: 1)
  }
  var selectedEpisodeNumber by remember(nowPlayingItem?.id) {
    mutableStateOf(historyMap[nowPlayingItem?.id]?.episodeNumber ?: 1)
  }
  var currentResumePositionMs by remember(nowPlayingItem?.id) {
    mutableStateOf(historyMap[nowPlayingItem?.id]?.positionMs ?: 0)
  }
  var playlistAudioTrack by remember {
    mutableStateOf(AudioTrackOption.HINDI_DUB)
  }
  var playlistCaptionTrack by remember {
    mutableStateOf(CaptionTrackOption.OFF)
  }
  // Do NOT auto-play when entering the screen; open player only when user clicks Trailer or Watch Movie
  var isPlayerVisible by remember { mutableStateOf(false) }
  var showLanguageSelector by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("watch_movies_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Clean Cinema Header + On-Demand Video Player
    item {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Cinema Playlist",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = "Tap Trailer or Watch Movie on any title below",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          Surface(
            onClick = { showLanguageSelector = !showLanguageSelector },
            shape = RoundedCornerShape(10.dp),
            color = if (showLanguageSelector) {
              MaterialTheme.colorScheme.primaryContainer
            } else {
              MaterialTheme.colorScheme.surfaceVariant
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Audio & Subtitles",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "${playlistAudioTrack.shortTag} • ${playlistCaptionTrack.shortTag}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        // Collapsible Audio & Subtitle Default Selector
        AnimatedVisibility(visible = showLanguageSelector) {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("playlist_audio_caption_selector_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.GraphicEq,
                  contentDescription = "Audio Language",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Audio Language",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                AudioTrackOption.entries.forEach { audioOpt ->
                  FilterChip(
                    selected = playlistAudioTrack == audioOpt,
                    onClick = { playlistAudioTrack = audioOpt },
                    label = { Text(audioOpt.label) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primary,
                      selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("playlist_audio_chip_${audioOpt.name.lowercase(Locale.US)}")
                  )
                }
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Subtitles,
                  contentDescription = "Subtitles",
                  tint = MaterialTheme.colorScheme.tertiary,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "Subtitles (CC)",
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                CaptionTrackOption.entries.forEach { ccOpt ->
                  FilterChip(
                    selected = playlistCaptionTrack == ccOpt,
                    onClick = { playlistCaptionTrack = ccOpt },
                    label = { Text(ccOpt.label) },
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                      selectedLabelColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    modifier = Modifier.testTag("playlist_caption_chip_${ccOpt.name.lowercase(Locale.US)}")
                  )
                }
              }
            }
          }
        }

        val currentMovie = nowPlayingItem
        AnimatedVisibility(visible = currentMovie != null && isPlayerVisible) {
          if (currentMovie != null) {
            val currentSeasons = remember(currentMovie.id, currentMovie.mediaType, currentMovie.durationOrSeasons) {
              currentMovie.buildSeasons()
            }
            InAppCinemaPlayer(
              title = currentMovie.title,
              mediaId = currentMovie.id,
              trailerUrl = currentMovie.trailerUrl,
              fullMovieUrl = currentMovie.fullMovieUrl,
              posterUrl = currentMovie.posterUrl,
              backdropUrl = currentMovie.backdropUrl,
              initialMode = activeStreamMode,
              initialAudioTrack = playlistAudioTrack,
              initialCaptionTrack = playlistCaptionTrack,
              seasons = currentSeasons,
              initialSeasonNumber = selectedSeasonNumber,
              initialEpisodeNumber = selectedEpisodeNumber,
              initialResumePositionMs = currentResumePositionMs,
              onEpisodeChanged = { sNum, eNum ->
                selectedSeasonNumber = sNum
                selectedEpisodeNumber = eNum
                currentResumePositionMs = 0
              },
              onPlaybackProgress = { posMs, durMs, sNum, eNum ->
                onRecordPlaybackProgress(currentMovie, posMs, durMs, sNum, eNum)
              },
              onAudioTrackChanged = { playlistAudioTrack = it },
              onCaptionTrackChanged = { playlistCaptionTrack = it },
              onClose = { isPlayerVisible = false },
              onMarkWatched = {
                onSaveStatus(currentMovie, WatchStatus.WATCHED)
              }
            )
          }
        }
      }
    }

    // 2. Continue Watching Rail
    if (watchHistory.isNotEmpty()) {
      item {
        ContinueWatchingSection(
          historyItems = watchHistory,
          onContinuePlaying = { entry ->
            val targetMedia = catalog.find { it.id == entry.mediaId } ?: entry.toMediaItem()
            nowPlayingItem = targetMedia
            selectedSeasonNumber = entry.seasonNumber.coerceAtLeast(1)
            selectedEpisodeNumber = entry.episodeNumber.coerceAtLeast(1)
            currentResumePositionMs = entry.positionMs.coerceAtLeast(0)
            activeStreamMode = PlayerStreamMode.FULL_MOVIE_SERVER_1
            isPlayerVisible = true
          },
          onRemoveEntry = onRemoveWatchHistoryEntry,
          onClearAll = onClearWatchHistory
        )
      }
    }

    // 3. Clean Movie & Series List with Explicit "Trailer" and "Watch Movie" Buttons
    items(catalog, key = { it.id }) { movie ->
      val safeId = movie.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
      val currentWatchStatus = watchlistMap[safeId]?.parsedWatchStatus
      val recentEntry = historyMap[movie.id]
      val isCurrentlyPlaying = nowPlayingItem?.id == movie.id && isPlayerVisible
      val itemSeasons = remember(movie.id, movie.mediaType, movie.durationOrSeasons) {
        movie.buildSeasons()
      }

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onOpenMediaDetail(movie) }
          .testTag("stream_movie_row_${movie.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isCurrentlyPlaying) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          }
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          AsyncImage(
            model = movie.posterUrl,
            contentDescription = movie.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .width(74.dp)
              .height(106.dp)
              .clip(RoundedCornerShape(12.dp))
          )

          Spacer(modifier = Modifier.width(14.dp))

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = movie.title,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Text(
              text = "${movie.releaseYear} • ${movie.mediaType.label} • ⭐ ${movie.rating}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
              text = movie.overview,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            // Side-by-side Trailer & Watch Movie buttons
            Row(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              FilledTonalButton(
                onClick = {
                  nowPlayingItem = movie
                  currentResumePositionMs = 0
                  activeStreamMode = PlayerStreamMode.TRAILER
                  isPlayerVisible = true
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.SmartDisplay,
                  contentDescription = "Trailer",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = "Trailer",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
              }

              Button(
                onClick = {
                  nowPlayingItem = movie
                  if (recentEntry != null) {
                    selectedSeasonNumber = recentEntry.seasonNumber.coerceAtLeast(1)
                    selectedEpisodeNumber = recentEntry.episodeNumber.coerceAtLeast(1)
                    currentResumePositionMs = recentEntry.positionMs.coerceAtLeast(0)
                  } else {
                    currentResumePositionMs = 0
                  }
                  activeStreamMode = PlayerStreamMode.FULL_MOVIE_SERVER_1
                  isPlayerVisible = true
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.height(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.MovieFilter,
                  contentDescription = "Watch Movie",
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = when {
                    isCurrentlyPlaying && activeStreamMode != PlayerStreamMode.TRAILER -> "Playing"
                    recentEntry != null -> "Continue"
                    itemSeasons.isNotEmpty() -> "Watch Series"
                    else -> "Watch Movie"
                  },
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
              onClick = { onSaveStatus(movie, WatchStatus.WANT_TO_WATCH) }
            ) {
              Icon(
                imageVector = if (currentWatchStatus == WatchStatus.WANT_TO_WATCH) {
                  Icons.Default.BookmarkAdded
                } else {
                  Icons.Default.BookmarkAdd
                },
                contentDescription = "Want to Watch",
                tint = if (currentWatchStatus == WatchStatus.WANT_TO_WATCH) {
                  MaterialTheme.colorScheme.secondary
                } else {
                  MaterialTheme.colorScheme.onSurfaceVariant
                }
              )
            }

            IconButton(
              onClick = { onSaveStatus(movie, WatchStatus.WATCHED) }
            ) {
              Icon(
                imageVector = if (currentWatchStatus == WatchStatus.WATCHED) {
                  Icons.Default.CheckCircle
                } else {
                  Icons.Default.CheckCircleOutline
                },
                contentDescription = "Watched",
                tint = if (currentWatchStatus == WatchStatus.WATCHED) {
                  MaterialTheme.colorScheme.tertiary
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
}
