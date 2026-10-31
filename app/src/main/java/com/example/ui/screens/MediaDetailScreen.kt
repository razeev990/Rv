package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.ReleaseStatus
import com.example.data.model.SmartDownloadQuality
import com.example.data.model.SmartDownloadState
import com.example.data.model.SmartDownloadTask
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.components.InAppCinemaPlayer
import com.example.ui.components.PlayerStreamMode
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaDetailScreen(
  media: MediaItem,
  watchlistItem: WatchlistItem?,
  isSavedOffline: Boolean,
  autoPlayMovie: Boolean = false,
  resumePositionMs: Int = 0,
  initialSeasonNumber: Int = 1,
  initialEpisodeNumber: Int = 1,
  onPlaybackProgress: ((Int, Int, Int, Int) -> Unit)? = null,
  onBack: () -> Unit,
  onSaveStatus: (WatchStatus, String?, Int?) -> Unit,
  onRemoveFromWatchlist: () -> Unit,
  onToggleOfflineSaved: (Boolean) -> Unit,
  onActorClick: (CastMember) -> Unit,
  onToggleFavorite: (() -> Unit)? = null,
  onCastToTv: (() -> Unit)? = null,
  availablePlaylistTags: List<String> = WatchlistItem.DEFAULT_PLAYLIST_TAGS,
  onToggleTagOnMovie: ((String) -> Unit)? = null,
  onEnterPipMode: ((Int, Int) -> Unit)? = null,
  smartDownloadTask: SmartDownloadTask? = null,
  onStartSmartDownload: ((SmartDownloadQuality) -> Unit)? = null,
  onTogglePauseResumeSmartDownload: (() -> Unit)? = null,
  isTvMode: Boolean = false
) {
  BackHandler(onBack = onBack)

  val currentStatus = watchlistItem?.parsedWatchStatus
  val isFavorite = watchlistItem?.isFavorite == true
  val seriesSeasons = remember(media.id, media.mediaType, media.durationOrSeasons) {
    media.buildSeasons()
  }
  var selectedSeasonNumber by remember(media.id, initialSeasonNumber) {
    mutableStateOf(initialSeasonNumber.coerceAtLeast(1))
  }
  var selectedEpisodeNumber by remember(media.id, initialEpisodeNumber) {
    mutableStateOf(initialEpisodeNumber.coerceAtLeast(1))
  }
  var activeResumePositionMs by remember(media.id, resumePositionMs) {
    mutableStateOf(resumePositionMs.coerceAtLeast(0))
  }
  // Do NOT auto-play when clicking a movie card; only open player when user taps Trailer or Watch Movie
  var activePlayerMode by remember(media.id, autoPlayMovie) {
    mutableStateOf<PlayerStreamMode?>(
      if (autoPlayMovie) PlayerStreamMode.FULL_MOVIE_SERVER_1 else null
    )
  }
  var personalNotes by remember(watchlistItem?.userNotes) {
    mutableStateOf(watchlistItem?.userNotes.orEmpty())
  }
  var personalRating by remember(watchlistItem?.userRating) {
    mutableFloatStateOf((watchlistItem?.userRating ?: 9L).toFloat())
  }
  var selectedDownloadQuality by remember {
    mutableStateOf(SmartDownloadQuality.FULL_HD_1080P)
  }

  val activeSeasonObj = remember(seriesSeasons, selectedSeasonNumber) {
    seriesSeasons.find { it.seasonNumber == selectedSeasonNumber } ?: seriesSeasons.firstOrNull()
  }

  val isTrailerSelected = activePlayerMode == PlayerStreamMode.TRAILER ||
    activePlayerMode == PlayerStreamMode.TRAILER_YOUTUBE
  val isMovieSelected = activePlayerMode != null && !isTrailerSelected

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("media_detail_screen"),
    contentPadding = PaddingValues(bottom = 40.dp)
  ) {
    // 1. Clean Hero Backdrop + Back Button
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(250.dp)
      ) {
        AsyncImage(
          model = media.backdropUrl.ifBlank { media.posterUrl },
          contentDescription = media.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.35f),
                  Color.Transparent,
                  MaterialTheme.colorScheme.background
                )
              )
            )
        )

        IconButton(
          onClick = onBack,
          modifier = Modifier
            .padding(16.dp)
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.65f))
            .align(Alignment.TopStart)
            .testTag("detail_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }

        // Release Status & Rating Pill
        Row(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(horizontal = 20.dp, vertical = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = if (media.releaseStatus == ReleaseStatus.UPCOMING) {
              Color(0xFFE50914)
            } else {
              MaterialTheme.colorScheme.primary
            },
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = if (media.releaseStatus == ReleaseStatus.UPCOMING) {
                "COMING SOON • ${media.releaseYear}"
              } else {
                media.releaseDateFull
              },
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = if (media.releaseStatus == ReleaseStatus.UPCOMING) Color.White else Color.Black,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = String.format(Locale.US, "%.1f", media.rating),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }
    }

    // 2. Title, Clean Action Buttons (Trailer & Watch Movie), and On-Demand Video Player
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = media.title,
          style = MaterialTheme.typography.headlineLarge,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onBackground
        )

        Text(
          text = "${media.mediaType.label} • ${media.language} • ${media.durationOrSeasons}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (media.effectiveDirector.isNotBlank()) {
          Surface(
            onClick = {
              onActorClick(
                CastMember(
                  id = "director_${media.effectiveDirector.lowercase().replace(Regex("[^a-z0-9]"), "_")}",
                  name = media.effectiveDirector,
                  characterName = "Director",
                  profileUrl = media.posterUrl,
                  bio = "Acclaimed filmmaker and director of ${media.title}.",
                  knownForDepartment = "Directing"
                )
              )
            },
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("detail_director_chip")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "Director",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Director: ${media.effectiveDirector}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          media.genres.forEach { genre ->
            Surface(
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text(
                text = genre,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
              )
            }
          }
        }

        // Primary Choice Buttons: "Trailer" and "Watch Movie" side by side
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // 1) TRAILER BUTTON — Plays ONLY Trailer when clicked
          FilledTonalButton(
            onClick = {
              activeResumePositionMs = 0
              activePlayerMode = PlayerStreamMode.TRAILER
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = if (isTrailerSelected) {
                MaterialTheme.colorScheme.secondaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant
              },
              contentColor = if (isTrailerSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
              } else {
                MaterialTheme.colorScheme.onSurface
              }
            ),
            border = if (isTrailerSelected) {
              BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary)
            } else {
              null
            },
            modifier = Modifier
              .weight(1f)
              .height(54.dp)
              .testTag("watch_trailer_button")
          ) {
            Icon(
              imageVector = Icons.Default.SmartDisplay,
              contentDescription = "Watch Trailer",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Trailer",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          // 2) WATCH MOVIE BUTTON — Plays Full Movie / Episode when clicked
          Button(
            onClick = {
              if (resumePositionMs == 0) {
                activeResumePositionMs = 0
              }
              activePlayerMode = PlayerStreamMode.FULL_MOVIE_SERVER_1
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .weight(1.35f)
              .height(54.dp)
              .testTag("watch_movie_button")
          ) {
            Icon(
              imageVector = Icons.Default.MovieFilter,
              contentDescription = "Watch Movie",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (seriesSeasons.isNotEmpty()) {
                "Watch S${selectedSeasonNumber}•E${selectedEpisodeNumber}"
              } else {
                "Watch Movie"
              },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        // Cross-Platform Quick Actions: Favorite ❤️ + Cast to TV (Xiaomi/Fire TV/Chromecast) + PiP Mode + 40:00 Sync
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (onToggleFavorite != null) {
            FilledTonalButton(
              onClick = onToggleFavorite,
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = if (isFavorite) {
                  Color(0xFFEF4444).copy(alpha = 0.22f)
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
                contentColor = if (isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
              ),
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("detail_favorite_button")
            ) {
              Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isFavorite) "Loved" else "Favorite",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            }
          }

          if (onCastToTv != null) {
            FilledTonalButton(
              onClick = onCastToTv,
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(1f)
                .height(42.dp)
                .testTag("detail_cast_button")
            ) {
              Icon(
                imageVector = Icons.Default.Cast,
                contentDescription = "Cast to TV",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Cast TV",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            }
          }

          if (onEnterPipMode != null) {
            FilledTonalButton(
              onClick = { onEnterPipMode(activeResumePositionMs, 8_280_000) },
              shape = RoundedCornerShape(12.dp),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
              modifier = Modifier
                .weight(0.95f)
                .height(42.dp)
                .testTag("detail_pip_button")
            ) {
              Icon(
                imageVector = Icons.Default.PictureInPictureAlt,
                contentDescription = "Picture-in-Picture Mode",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "PiP",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
            }
          }
        }

        // Optional Continue Playing Button (if user has saved watch progress)
        if (resumePositionMs > 0 || activeResumePositionMs > 0) {
          val effectivePos = if (activeResumePositionMs > 0) activeResumePositionMs else resumePositionMs
          val totalSec = (effectivePos / 1000).coerceAtLeast(0)
          val mm = totalSec / 60
          val ss = totalSec % 60
          val formattedPos = String.format(Locale.US, "%02d:%02d", mm, ss)
          FilledTonalButton(
            onClick = {
              activeResumePositionMs = effectivePos
              selectedSeasonNumber = initialSeasonNumber.coerceAtLeast(1)
              selectedEpisodeNumber = initialEpisodeNumber.coerceAtLeast(1)
              activePlayerMode = PlayerStreamMode.FULL_MOVIE_SERVER_1
            },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer,
              contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("detail_continue_playing_button")
          ) {
            Icon(
              imageVector = Icons.Default.PlayCircleFilled,
              contentDescription = "Continue Playing",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (seriesSeasons.isNotEmpty()) {
                "Continue S$initialSeasonNumber•E$initialEpisodeNumber ($formattedPos)"
              } else {
                "Continue Playing ($formattedPos)"
              },
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }
        }

        // On-Demand Video Player — Opens ONLY after clicking "Trailer" or "Watch Movie"
        AnimatedVisibility(visible = activePlayerMode != null) {
          activePlayerMode?.let { mode ->
            InAppCinemaPlayer(
              title = media.title,
              mediaId = media.id,
              trailerUrl = media.trailerUrl,
              fullMovieUrl = media.fullMovieUrl,
              posterUrl = media.posterUrl,
              backdropUrl = media.backdropUrl,
              initialMode = mode,
              seasons = seriesSeasons,
              initialSeasonNumber = selectedSeasonNumber,
              initialEpisodeNumber = selectedEpisodeNumber,
              initialResumePositionMs = activeResumePositionMs,
              onEpisodeChanged = { sNum, eNum ->
                selectedSeasonNumber = sNum
                selectedEpisodeNumber = eNum
                activeResumePositionMs = 0
              },
              onPlaybackProgress = onPlaybackProgress,
              onCastClick = onCastToTv,
              onEnterPipMode = onEnterPipMode,
              onClose = { activePlayerMode = null },
              onMarkWatched = {
                onSaveStatus(
                  WatchStatus.WATCHED,
                  personalNotes.ifBlank { null },
                  personalRating.roundToInt()
                )
              },
              modifier = Modifier.padding(top = 4.dp)
            )
          }
        }

        // Storyline Overview
        Column(
          modifier = Modifier.padding(top = 4.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "Storyline",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
          Text(
            text = media.overview,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        // Seasons & Episodes Selector Card for TV & Web Series
        if (seriesSeasons.isNotEmpty() && activeSeasonObj != null) {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("series_seasons_episodes_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Text(
                text = "Seasons & Episodes (${seriesSeasons.size})",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )

              // Season Tabs / Chips
              LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(seriesSeasons, key = { it.seasonNumber }) { season ->
                  val isSelectedSeason = season.seasonNumber == selectedSeasonNumber
                  Button(
                    onClick = {
                      selectedSeasonNumber = season.seasonNumber
                      selectedEpisodeNumber = 1
                    },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = if (isSelectedSeason) {
                        MaterialTheme.colorScheme.primary
                      } else {
                        MaterialTheme.colorScheme.surface
                      },
                      contentColor = if (isSelectedSeason) {
                        MaterialTheme.colorScheme.onPrimary
                      } else {
                        MaterialTheme.colorScheme.onSurface
                      }
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("detail_season_tab_${season.seasonNumber}")
                  ) {
                    Text(
                      text = "${season.title} (${season.episodes.size})",
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = if (isSelectedSeason) FontWeight.Bold else FontWeight.Normal
                    )
                  }
                }
              }

              // Episodes List for Selected Season
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                activeSeasonObj.episodes.forEach { episode ->
                  val isCurrentEpisode = isMovieSelected &&
                    selectedSeasonNumber == episode.seasonNumber &&
                    selectedEpisodeNumber == episode.episodeNumber

                  Surface(
                    onClick = {
                      selectedSeasonNumber = episode.seasonNumber
                      selectedEpisodeNumber = episode.episodeNumber
                      activePlayerMode = PlayerStreamMode.FULL_MOVIE_SERVER_1
                    },
                    color = if (isCurrentEpisode) {
                      MaterialTheme.colorScheme.primaryContainer
                    } else {
                      MaterialTheme.colorScheme.surface
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                      .fillMaxWidth()
                      .testTag("detail_episode_card_${episode.code}")
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                      ) {
                        Row(
                          horizontalArrangement = Arrangement.spacedBy(8.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Text(
                            text = episode.code,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                          )
                          Text(
                            text = episode.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                          )
                        }
                        Text(
                          text = episode.duration,
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }

                      Spacer(modifier = Modifier.width(10.dp))

                      FilledTonalButton(
                        onClick = {
                          selectedSeasonNumber = episode.seasonNumber
                          selectedEpisodeNumber = episode.episodeNumber
                          activePlayerMode = PlayerStreamMode.FULL_MOVIE_SERVER_1
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                      ) {
                        Icon(
                          imageVector = Icons.Default.PlayArrow,
                          contentDescription = "Play ${episode.code}",
                          modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          text = if (isCurrentEpisode) "Playing" else "Play",
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Smart Library, Custom Tags/Folders, Reviews & Comments, and Smart Downloads Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          )
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = "Cloud Watchlist",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Smart Library & Personal Collection",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              if (currentStatus != null) {
                OutlinedButton(
                  onClick = onRemoveFromWatchlist,
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(32.dp)
                    .testTag("detail_remove_watchlist_button")
                ) {
                  Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Remove",
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Remove", style = MaterialTheme.typography.labelSmall)
                }
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              Button(
                onClick = {
                  onSaveStatus(
                    WatchStatus.WANT_TO_WATCH,
                    personalNotes.ifBlank { null },
                    personalRating.roundToInt()
                  )
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (currentStatus == WatchStatus.WANT_TO_WATCH) {
                    MaterialTheme.colorScheme.secondary
                  } else {
                    MaterialTheme.colorScheme.surface
                  },
                  contentColor = if (currentStatus == WatchStatus.WANT_TO_WATCH) {
                    MaterialTheme.colorScheme.onSecondary
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
                  .testTag("detail_want_to_watch_button")
              ) {
                Icon(
                  imageVector = if (currentStatus == WatchStatus.WANT_TO_WATCH) {
                    Icons.Default.BookmarkAdded
                  } else {
                    Icons.Default.BookmarkAdd
                  },
                  contentDescription = "Unwatched",
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Unwatched")
              }

              Button(
                onClick = {
                  onSaveStatus(
                    WatchStatus.WATCHED,
                    personalNotes.ifBlank { null },
                    personalRating.roundToInt()
                  )
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (currentStatus == WatchStatus.WATCHED) {
                    MaterialTheme.colorScheme.tertiary
                  } else {
                    MaterialTheme.colorScheme.surface
                  },
                  contentColor = if (currentStatus == WatchStatus.WATCHED) {
                    MaterialTheme.colorScheme.onTertiary
                  } else {
                    MaterialTheme.colorScheme.onSurface
                  }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
                  .testTag("detail_watched_button")
              ) {
                Icon(
                  imageVector = if (currentStatus == WatchStatus.WATCHED) {
                    Icons.Default.CheckCircle
                  } else {
                    Icons.Default.CheckCircleOutline
                  },
                  contentDescription = "Watched",
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Watched")
              }
            }

            // Custom Tags & Playlists / Folders Organization
            if (onToggleTagOnMovie != null && availablePlaylistTags.isNotEmpty()) {
              Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.FolderSpecial,
                    contentDescription = "Custom Tags & Playlists",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                  )
                  Text(
                    text = "Custom Tags & Playlists (Organize Collection):",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                  )
                }

                val currentTags = watchlistItem?.customTags ?: emptyList()
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  availablePlaylistTags.forEach { tag ->
                    val isTagged = currentTags.any { it.equals(tag, ignoreCase = true) }
                    FilterChip(
                      selected = isTagged,
                      onClick = { onToggleTagOnMovie(tag) },
                      label = { Text(if (isTagged) "✓ $tag" else "+ $tag") },
                      modifier = Modifier.testTag("detail_tag_chip_${tag.lowercase().replace(" ", "_")}")
                    )
                  }
                }
              }
            }

            // Personal Reviews & Comments + Rating Section
            Surface(
              color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
                      imageVector = Icons.Default.RateReview,
                      contentDescription = "Personal Review",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = "Your Personal Review & Rating",
                      style = MaterialTheme.typography.labelLarge,
                      color = MaterialTheme.colorScheme.onSurface,
                      fontWeight = FontWeight.Bold
                    )
                  }
                  Text(
                    text = "${personalRating.roundToInt()} / 10 ⭐",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                }

                Slider(
                  value = personalRating,
                  onValueChange = { personalRating = it },
                  valueRange = 1f..10f,
                  steps = 8,
                  modifier = Modifier.testTag("personal_rating_slider")
                )

                OutlinedTextField(
                  value = personalNotes,
                  onValueChange = { personalNotes = it },
                  label = { Text("Write your review or personal comment...") },
                  modifier = Modifier
                    .fillMaxWidth()
                    .testTag("personal_notes_input"),
                  singleLine = false,
                  maxLines = 3,
                  shape = RoundedCornerShape(12.dp)
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End
                ) {
                  FilledTonalButton(
                    onClick = {
                      onSaveStatus(
                        currentStatus ?: WatchStatus.WANT_TO_WATCH,
                        personalNotes.ifBlank { null },
                        personalRating.roundToInt()
                      )
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("save_personal_review_button")
                  ) {
                    Text("Save Review & Rating", fontWeight = FontWeight.Bold)
                  }
                }
              }
            }

            // Smart Offline Downloads with Quality Selection & Pause/Resume (Mobile Only)
            if (!isTvMode) {
              Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.65f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = "Smart Offline Download (Choose Quality + Pause/Resume)",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )

                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    SmartDownloadQuality.entries.forEach { q ->
                      FilterChip(
                        selected = selectedDownloadQuality == q,
                        onClick = { selectedDownloadQuality = q },
                        label = { Text("${q.label} (${q.badge})") },
                        modifier = Modifier.testTag("detail_download_quality_${q.name.lowercase()}")
                      )
                    }
                  }

                  if (smartDownloadTask != null) {
                    LinearProgressIndicator(
                      progress = { smartDownloadTask.progressFraction },
                      modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                    )
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(
                        text = "${smartDownloadTask.quality.label} • ${(smartDownloadTask.progressFraction * 100).roundToInt()}% (${smartDownloadTask.state.name})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                      )
                      if (smartDownloadTask.state != SmartDownloadState.COMPLETED &&
                        onTogglePauseResumeSmartDownload != null
                      ) {
                        FilledTonalButton(
                          onClick = onTogglePauseResumeSmartDownload,
                          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                          modifier = Modifier
                            .height(32.dp)
                            .testTag("detail_pause_resume_download_button")
                        ) {
                          Icon(
                            imageVector = if (smartDownloadTask.state == SmartDownloadState.PAUSED) {
                              Icons.Default.PlayArrow
                            } else {
                              Icons.Default.Pause
                            },
                            contentDescription = "Pause or Resume Download",
                            modifier = Modifier.size(15.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = if (smartDownloadTask.state == SmartDownloadState.PAUSED) "Resume" else "Pause",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }
                    }
                  }

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    if (onStartSmartDownload != null) {
                      Button(
                        onClick = { onStartSmartDownload(selectedDownloadQuality) },
                        modifier = Modifier
                          .weight(1f)
                          .height(44.dp)
                          .testTag("detail_smart_download_button"),
                        shape = RoundedCornerShape(12.dp)
                      ) {
                        Icon(
                          imageVector = if (isSavedOffline) {
                            Icons.Default.DownloadDone
                          } else {
                            Icons.Default.DownloadForOffline
                          },
                          contentDescription = "Smart Download",
                          modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = if (isSavedOffline) {
                            "Downloaded (${selectedDownloadQuality.label})"
                          } else {
                            "Download ${selectedDownloadQuality.label}"
                          },
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                    } else {
                      FilledTonalButton(
                        onClick = { onToggleOfflineSaved(!isSavedOffline) },
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(44.dp)
                          .testTag("detail_offline_save_button"),
                        shape = RoundedCornerShape(12.dp)
                      ) {
                        Icon(
                          imageVector = if (isSavedOffline) {
                            Icons.Default.DownloadDone
                          } else {
                            Icons.Default.DownloadForOffline
                          },
                          contentDescription = "Offline Save",
                          modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isSavedOffline) "Downloaded Offline (Mobile)" else "Download Offline (Mobile Only)")
                      }
                    }
                  }
                }
              }
            }
          }
        }

        // Cast & Actors Header
        Text(
          text = stringResource(R.string.cast_section_title),
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground,
          modifier = Modifier.padding(top = 4.dp)
        )
      }
    }

    // 3. Horizontal Cast Carousel
    item {
      if (media.cast.isEmpty()) {
        Text(
          text = "Cast details loading...",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )
      } else {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(media.cast, key = { it.id + it.characterName }) { actor ->
            ActorCastCard(
              actor = actor,
              onClick = { onActorClick(actor) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ActorCastCard(
  actor: CastMember,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .width(136.dp)
      .clickable(onClick = onClick)
      .testTag("actor_card_${actor.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(72.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
      ) {
        if (actor.profileUrl.isNotBlank()) {
          AsyncImage(
            model = actor.profileUrl,
            contentDescription = actor.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = actor.name,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Text(
        text = actor.name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = actor.characterName,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
