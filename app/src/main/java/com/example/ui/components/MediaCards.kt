package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.MediaItem
import com.example.data.model.ReleaseStatus
import com.example.data.model.WatchStatus
import java.util.Locale

@Composable
fun HeroSpotlightBanner(
  item: MediaItem,
  currentWatchStatus: WatchStatus?,
  onOpenDetail: () -> Unit,
  onWatchMovieNow: () -> Unit,
  onToggleStatus: (WatchStatus) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onOpenDetail)
      .testTag("hero_spotlight_card_${item.id}"),
    shape = RoundedCornerShape(22.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
    ) {
      AsyncImage(
        model = item.backdropUrl.ifBlank { item.posterUrl },
        contentDescription = item.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )

      // Cinema gradient overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.2f),
                Color(0xFF0B0D14).copy(alpha = 0.72f),
                Color(0xFF0B0D14).copy(alpha = 0.96f)
              )
            )
          )
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Top badges
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = if (item.releaseStatus == ReleaseStatus.UPCOMING) {
              Color(0xFFE50914)
            } else {
              MaterialTheme.colorScheme.primary
            },
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = if (item.releaseStatus == ReleaseStatus.UPCOMING) {
                "COMING SOON • ${item.releaseYear}"
              } else {
                "FEATURED • ${item.releaseYear}"
              },
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = if (item.releaseStatus == ReleaseStatus.UPCOMING) Color.White else Color.Black,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }

          Surface(
            color = Color.Black.copy(alpha = 0.65f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = item.mediaType.label,
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }

        // Bottom title, overview, and clean action buttons
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = item.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Text(
            text = item.overview,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.82f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            FilledTonalButton(
              onClick = onOpenDetail,
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White
              ),
              modifier = Modifier.testTag("hero_watch_trailer_btn")
            ) {
              Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Trailer",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Trailer", fontWeight = FontWeight.SemiBold)
            }

            Button(
              onClick = onWatchMovieNow,
              colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("hero_watch_movie_btn")
            ) {
              Icon(
                imageVector = Icons.Default.MovieFilter,
                contentDescription = "Watch Movie",
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Watch Movie", fontWeight = FontWeight.Bold)
            }

            IconButton(
              onClick = { onToggleStatus(WatchStatus.WANT_TO_WATCH) },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                  if (currentWatchStatus == WatchStatus.WANT_TO_WATCH) {
                    MaterialTheme.colorScheme.secondaryContainer
                  } else {
                    Color.White.copy(alpha = 0.14f)
                  }
                )
                .testTag("hero_want_to_watch_btn")
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
                  Color.White
                }
              )
            }

            IconButton(
              onClick = { onToggleStatus(WatchStatus.WATCHED) },
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(
                  if (currentWatchStatus == WatchStatus.WATCHED) {
                    MaterialTheme.colorScheme.tertiaryContainer
                  } else {
                    Color.White.copy(alpha = 0.14f)
                  }
                )
                .testTag("hero_watched_btn")
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
                  Color.White
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MediaPosterCard(
  item: MediaItem,
  currentWatchStatus: WatchStatus?,
  onClick: () -> Unit,
  onWatchMovieNow: () -> Unit = onClick,
  onQuickStatusSelect: (WatchStatus) -> Unit,
  matchBadge: String? = null,
  modifier: Modifier = Modifier
) {
  var isTvFocused by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .onFocusChanged { isTvFocused = it.isFocused }
      .then(
        if (isTvFocused) {
          Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
        } else {
          Modifier
        }
      )
      .focusable()
      .clickable(onClick = onClick)
      .testTag("media_card_${item.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isTvFocused) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
      } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      }
    )
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(0.74f)
      ) {
        AsyncImage(
          model = item.posterUrl,
          contentDescription = item.title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.3f),
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.8f)
                )
              )
            )
        )

        // Top Row: Release status & Rating
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = if (item.releaseStatus == ReleaseStatus.UPCOMING) {
              Color(0xFFE50914)
            } else {
              Color.Black.copy(alpha = 0.7f)
            },
            shape = RoundedCornerShape(6.dp)
          ) {
            Text(
              text = if (item.releaseStatus == ReleaseStatus.UPCOMING) "SOON" else item.mediaType.label,
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }

          Surface(
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(6.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = String.format(Locale.US, "%.1f", item.rating),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Bottom overlay inside poster: Smart Search match badge OR quick Watchlist status pill
        if (!matchBadge.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(8.dp)
          ) {
            Text(
              text = matchBadge,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimary,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        } else if (currentWatchStatus != null) {
          Surface(
            color = if (currentWatchStatus == WatchStatus.WATCHED) {
              MaterialTheme.colorScheme.tertiary
            } else {
              MaterialTheme.colorScheme.secondary
            },
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(8.dp)
          ) {
            Text(
              text = currentWatchStatus.label,
              style = MaterialTheme.typography.labelSmall,
              color = Color.Black,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = item.title,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = "${item.releaseYear} • ${item.genres.take(2).joinToString(", ")}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        if (item.effectiveDirector.isNotBlank()) {
          Text(
            text = "Dir: ${item.effectiveDirector}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilledTonalButton(
            onClick = onClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            modifier = Modifier
              .weight(1f)
              .height(34.dp)
              .testTag("card_watch_movie_btn_${item.id}")
          ) {
            Icon(
              imageVector = Icons.Default.PlayCircleFilled,
              contentDescription = "Open Movie",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Open",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold
            )
          }

          IconButton(
            onClick = { onQuickStatusSelect(WatchStatus.WANT_TO_WATCH) },
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(
                if (currentWatchStatus == WatchStatus.WANT_TO_WATCH) {
                  MaterialTheme.colorScheme.secondaryContainer
                } else {
                  MaterialTheme.colorScheme.surface
                }
              )
              .testTag("card_want_btn_${item.id}")
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
              },
              modifier = Modifier.size(16.dp)
            )
          }

          IconButton(
            onClick = { onQuickStatusSelect(WatchStatus.WATCHED) },
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(
                if (currentWatchStatus == WatchStatus.WATCHED) {
                  MaterialTheme.colorScheme.tertiaryContainer
                } else {
                  MaterialTheme.colorScheme.surface
                }
              )
              .testTag("card_watched_btn_${item.id}")
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
              },
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun DynamicCategoryCarouselSection(
  title: String,
  subtitle: String,
  items: List<MediaItem>,
  sectionTag: String,
  badgeBuilder: (Int, MediaItem) -> String,
  reasonBuilder: ((MediaItem) -> String?)? = null,
  seedOptions: List<String> = emptyList(),
  selectedSeedTitle: String? = null,
  onSelectSeedTitle: ((String) -> Unit)? = null,
  onOpenMedia: (MediaItem) -> Unit,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty()) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag(sectionTag),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.ExtraBold
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    if (seedOptions.size > 1 && onSelectSeedTitle != null) {
      androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(
          count = seedOptions.size,
          key = { idx -> seedOptions[idx] }
        ) { idx ->
          val seedName = seedOptions[idx]
          val isSelected = seedName == selectedSeedTitle
          Surface(
            onClick = { onSelectSeedTitle(seedName) },
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) {
              MaterialTheme.colorScheme.primary
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            },
            modifier = Modifier.testTag("because_watched_seed_chip_$idx")
          ) {
            Text(
              text = "Watched: $seedName",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
              } else {
                MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
          }
        }
      }
    }

    androidx.compose.foundation.lazy.LazyRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(vertical = 2.dp)
    ) {
      items(
        count = items.size,
        key = { idx -> "${sectionTag}_${items[idx].id}" }
      ) { idx ->
        val movie = items[idx]
        val topBadge = badgeBuilder(idx, movie)
        val reason = reasonBuilder?.invoke(movie)
        var isFocused by remember { mutableStateOf(false) }

        Card(
          modifier = Modifier
            .width(156.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .then(
              if (isFocused) {
                Modifier.border(2.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
              } else {
                Modifier
              }
            )
            .focusable()
            .clickable { onOpenMedia(movie) }
            .testTag("${sectionTag}_card_${movie.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          )
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(194.dp)
            ) {
              AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
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
                        Color.Black.copy(alpha = 0.85f)
                      )
                    )
                  )
              )

              // Top-left category badge (#1 Trending, NEW, etc.)
              Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(8.dp)
              ) {
                Text(
                  text = topBadge,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }

              // Top-right Rating
              Surface(
                color = Color.Black.copy(alpha = 0.72f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(8.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(2.dp))
                  Text(
                    text = String.format(Locale.US, "%.1f", movie.rating),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Bottom reason badge inside poster if present
              if (!reason.isNullOrBlank()) {
                Surface(
                  color = Color.Black.copy(alpha = 0.78f),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(6.dp)
                ) {
                  Text(
                    text = reason,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Text(
                text = movie.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = if (movie.effectiveDirector.isNotBlank()) {
                  "${movie.releaseYear} • ${movie.effectiveDirector}"
                } else {
                  "${movie.releaseYear} • ${movie.genres.take(2).joinToString(", ")}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ContinueWatchingSection(
  historyItems: List<com.example.data.model.WatchHistoryEntry>,
  onContinuePlaying: (com.example.data.model.WatchHistoryEntry) -> Unit,
  onRemoveEntry: (String) -> Unit,
  onClearAll: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (historyItems.isEmpty()) return

  Column(
    modifier = modifier
      .fillMaxWidth()
      .testTag("continue_watching_section"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "Continue Watching (${historyItems.size})",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold
      )

      androidx.compose.material3.TextButton(
        onClick = onClearAll,
        modifier = Modifier.testTag("clear_watch_history_button")
      ) {
        Text(
          text = "Clear",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.error,
          fontWeight = FontWeight.SemiBold
        )
      }
    }

    androidx.compose.foundation.lazy.LazyRow(
      horizontalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(vertical = 2.dp)
    ) {
      items(
        count = historyItems.size,
        key = { idx -> historyItems[idx].mediaId }
      ) { idx ->
        val entry = historyItems[idx]
        var isCardFocused by remember { mutableStateOf(false) }
        Card(
          modifier = Modifier
            .width(220.dp)
            .onFocusChanged { isCardFocused = it.isFocused }
            .then(
              if (isCardFocused) {
                Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
              } else {
                Modifier
              }
            )
            .focusable()
            .clickable { onContinuePlaying(entry) }
            .testTag("continue_watching_card_${entry.mediaId}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isCardFocused) {
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            } else {
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            }
          )
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(112.dp)
            ) {
              AsyncImage(
                model = entry.backdropUrl.ifBlank { entry.posterUrl },
                contentDescription = entry.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )

              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        Color.Black.copy(alpha = 0.2f),
                        Color.Black.copy(alpha = 0.72f)
                      )
                    )
                  )
              )

              // Center Play Icon Overlay
              Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                shape = CircleShape,
                modifier = Modifier
                  .size(38.dp)
                  .align(Alignment.Center)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Continue Playing",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }

              // Top-left Resume Timestamp Badge
              Surface(
                color = Color.Black.copy(alpha = 0.75f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                  .align(Alignment.TopStart)
                  .padding(8.dp)
              ) {
                Text(
                  text = entry.resumeBadgeText,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }

              // Top-right Remove From History Button
              Surface(
                onClick = { onRemoveEntry(entry.mediaId) },
                color = Color.Black.copy(alpha = 0.65f),
                shape = CircleShape,
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(6.dp)
                  .size(24.dp)
                  .testTag("remove_history_${entry.mediaId}")
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "✕",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Bottom Progress Bar
              androidx.compose.material3.LinearProgressIndicator(
                progress = { entry.progressFraction },
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(4.dp)
                  .align(Alignment.BottomCenter)
              )
            }

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
              )

              Spacer(modifier = Modifier.width(6.dp))

              Surface(
                onClick = { onContinuePlaying(entry) },
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("continue_play_btn_${entry.mediaId}")
              ) {
                Text(
                  text = "Resume",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onPrimary,
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
