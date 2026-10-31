package com.example.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainNavTab

/**
 * Returns the appropriate Material Design icon for each navigation tab.
 */
fun getMainNavTabIcon(tab: MainNavTab): ImageVector = when (tab) {
  MainNavTab.EXPLORE -> Icons.Default.Explore
  MainNavTab.SERIES -> Icons.Default.Tv
  MainNavTab.WATCH_MOVIES -> Icons.Default.PlayCircleFilled
  MainNavTab.UPCOMING -> Icons.Default.Upcoming
  MainNavTab.WATCHLIST -> Icons.Default.Bookmarks
  MainNavTab.OFFLINE -> Icons.Default.DownloadForOffline
}

/**
 * CineVault Mobile Bottom Navigation Bar.
 * Hosts the 6 core destinations including the dedicated "Series" tab for TV & Web Series.
 */
@Composable
fun CineVaultBottomNavigation(
  currentTab: MainNavTab,
  onTabSelected: (MainNavTab) -> Unit,
  visibleTabs: List<MainNavTab> = MainNavTab.entries,
  watchlistBadgeCount: Int = 0,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    modifier = modifier.testTag("cinevault_bottom_nav_bar")
  ) {
    visibleTabs.forEach { tab ->
      val isSelected = currentTab == tab
      NavigationBarItem(
        selected = isSelected,
        onClick = { onTabSelected(tab) },
        icon = {
          if (tab == MainNavTab.WATCHLIST && watchlistBadgeCount > 0) {
            BadgedBox(
              badge = {
                Badge { Text(watchlistBadgeCount.toString()) }
              }
            ) {
              Icon(
                imageVector = getMainNavTabIcon(tab),
                contentDescription = tab.label
              )
            }
          } else {
            Icon(
              imageVector = getMainNavTabIcon(tab),
              contentDescription = tab.label
            )
          }
        },
        label = {
          Text(
            text = tab.label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag("nav_tab_${tab.route}")
      )
    }
  }
}

/**
 * Dedicated Mobile Main Scaffold for CineVault.
 */
@Composable
fun MobileMainScaffold(
  currentTab: MainNavTab,
  onTabSelected: (MainNavTab) -> Unit,
  visibleTabs: List<MainNavTab> = MainNavTab.entries,
  watchlistBadgeCount: Int = 0,
  topBar: @Composable () -> Unit = {},
  snackbarHost: @Composable () -> Unit = {},
  content: @Composable (PaddingValues) -> Unit
) {
  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = topBar,
    snackbarHost = snackbarHost,
    bottomBar = {
      CineVaultBottomNavigation(
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        visibleTabs = visibleTabs,
        watchlistBadgeCount = watchlistBadgeCount
      )
    },
    modifier = Modifier.fillMaxSize()
  ) { innerPadding ->
    content(innerPadding)
  }
}
