package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CastMember
import com.example.data.model.MediaItem
import com.example.data.model.WatchStatus
import com.example.data.model.WatchlistItem
import com.example.ui.components.MediaPosterCard

@Composable
fun ActorFilmographyScreen(
  actor: CastMember,
  filmography: List<MediaItem>,
  isLoading: Boolean,
  watchlistMap: Map<String, WatchlistItem>,
  onBack: () -> Unit,
  onMediaClick: (MediaItem) -> Unit,
  onQuickStatusSelect: (MediaItem, WatchStatus) -> Unit
) {
  BackHandler(onBack = onBack)

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("actor_filmography_screen")
  ) {
    // Top App Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBack,
        modifier = Modifier
          .size(48.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .testTag("actor_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = MaterialTheme.colorScheme.onSurface
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = actor.name,
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Complete Actor Filmography (Movies & Series)",
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }

    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 160.dp),
      contentPadding = PaddingValues(16.dp),
      horizontalArrangement = Arrangement.spacedBy(14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // Actor Profile Header Card
      item(span = { GridItemSpan(maxLineSpan) }) {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(92.dp)
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
                  modifier = Modifier.size(44.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = actor.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
              )
              if (actor.characterName.isNotBlank()) {
                Text(
                  text = "Known Role: ${actor.characterName}",
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Text(
                text = actor.bio.ifBlank {
                  "Explore all released & upcoming movies and series featuring ${actor.name}."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "Filmography count",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "${filmography.size} Movies & Series Found",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      if (isLoading) {
        item(span = { GridItemSpan(maxLineSpan) }) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
          }
        }
      } else {
        items(filmography, key = { it.id }) { item ->
          val safeId = item.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
          MediaPosterCard(
            item = item,
            currentWatchStatus = watchlistMap[safeId]?.parsedWatchStatus,
            onClick = { onMediaClick(item) },
            onQuickStatusSelect = { status -> onQuickStatusSelect(item, status) }
          )
        }
      }
    }
  }
}
