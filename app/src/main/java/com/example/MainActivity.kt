package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import com.example.data.local.CineVaultDatabase
import com.example.data.repository.CinemaCatalogRepository
import com.example.data.repository.WatchlistRepository
import com.example.ui.CatalogCategoryFilter
import com.example.ui.CineVaultViewModel
import com.example.ui.MainNavTab
import com.example.ui.UiState
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.TvQrCodeMatrixCanvas
import com.example.ui.auth.signOut
import com.example.ui.components.CineVaultBottomNavigation
import com.example.ui.components.FrostedGlassSidebar
import com.example.ui.components.GlassSidebarItemKey
import com.example.ui.components.TvVoiceSearchOverlay
import com.example.data.model.UserProfile
import com.example.service.AppUpdateDialog
import com.example.service.AppUpdateInfo
import com.example.service.OtaUpdateManager
import com.example.ui.components.ProfilePinLockDialog
import com.example.ui.screens.ActorFilmographyScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.MediaDetailScreen
import com.example.ui.screens.OfflineModeScreen
import com.example.ui.screens.WatchMoviesScreen
import com.example.ui.screens.WatchlistScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AppNavigation(
          onRequestSystemPip = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              runCatching {
                val params = PictureInPictureParams.Builder()
                  .setAspectRatio(Rational(16, 9))
                  .build()
                enterPictureInPictureMode(params)
              }
            }
          }
        )
      }
    }
  }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth ->
    trySend(auth.currentUser)
  }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation(
  auth: FirebaseAuth = Firebase.auth,
  onRequestSystemPip: (() -> Unit)? = null
) {
  val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  val user = currentUser

  if (user == null) {
    AuthScreen(
      onAuthSuccess = {}
    )
  } else {
    CineVaultAuthenticatedApp(
      currentUserId = user.uid,
      userEmailOrName = user.email ?: user.displayName ?: user.uid.take(10),
      onRequestSystemPip = onRequestSystemPip
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CineVaultAuthenticatedApp(
  currentUserId: String,
  userEmailOrName: String,
  onRequestSystemPip: (() -> Unit)? = null
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  val viewModel: CineVaultViewModel = viewModel(
    key = currentUserId,
    factory = viewModelFactory {
      initializer {
        val app = checkNotNull(this[APPLICATION_KEY]) {
          "APPLICATION_KEY missing from CreationExtras"
        }
        val databaseId = app.getString(R.string.firestore_database_id)
        val firestore = FirebaseFirestore.getInstance(databaseId)
        val roomDb = CineVaultDatabase.getInstance(app)
        CineVaultViewModel(
          watchlistRepository = WatchlistRepository(firestore, Firebase.auth),
          catalogRepository = CinemaCatalogRepository(roomDb.offlineCatalogDao()),
          currentUserId = currentUserId
        )
      }
    }
  )

  val catalog by viewModel.filteredCatalog.collectAsStateWithLifecycle()
  val allCachedCatalog by viewModel.cachedCatalog.collectAsStateWithLifecycle()
  val watchlistState by viewModel.watchlistState.collectAsStateWithLifecycle()
  val offlineSavedMedia by viewModel.explicitOfflineSaved.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
  val selectedGenre by viewModel.selectedGenre.collectAsStateWithLifecycle()
  val selectedReleaseYear by viewModel.selectedReleaseYear.collectAsStateWithLifecycle()
  val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
  val lastRefreshedLabel by viewModel.lastRefreshedLabel.collectAsStateWithLifecycle()
  val isOfflineModeEnabled by viewModel.isOfflineModeEnabled.collectAsStateWithLifecycle()
  val selectedMedia by viewModel.selectedMedia.collectAsStateWithLifecycle()
  val autoPlayMovieOnOpen by viewModel.autoPlayMovieOnOpen.collectAsStateWithLifecycle()
  val resumePositionMsOnOpen by viewModel.resumePositionMsOnOpen.collectAsStateWithLifecycle()
  val resumeSeasonOnOpen by viewModel.resumeSeasonOnOpen.collectAsStateWithLifecycle()
  val resumeEpisodeOnOpen by viewModel.resumeEpisodeOnOpen.collectAsStateWithLifecycle()
  val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
  val selectedActor by viewModel.selectedActor.collectAsStateWithLifecycle()
  val actorFilmography by viewModel.actorFilmography.collectAsStateWithLifecycle()
  val isActorLoading by viewModel.isActorLoading.collectAsStateWithLifecycle()
  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()
  val profiles by viewModel.profiles.collectAsStateWithLifecycle()
  val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
  val activeCastSession by viewModel.activeCastSession.collectAsStateWithLifecycle()
  val pipSession by viewModel.pipSession.collectAsStateWithLifecycle()
  val availablePlaylistTags by viewModel.availablePlaylistTags.collectAsStateWithLifecycle()
  val isTraktConnected by viewModel.isTraktConnected.collectAsStateWithLifecycle()
  val traktHandle by viewModel.traktHandle.collectAsStateWithLifecycle()
  val lastTraktSyncLabel by viewModel.lastTraktSyncLabel.collectAsStateWithLifecycle()
  val autoFetchedPreviewMedia by viewModel.autoFetchedPreviewMedia.collectAsStateWithLifecycle()
  val isAutoFetchingMetadata by viewModel.isAutoFetchingMetadata.collectAsStateWithLifecycle()
  val smartDownloadTasks by viewModel.smartDownloadTasks.collectAsStateWithLifecycle()
  val isAutoDeleteWatchedEnabled by viewModel.isAutoDeleteWatchedDownloadsEnabled.collectAsStateWithLifecycle()
  val isAutoPruneStorageEnabled by viewModel.isAutoPruneStorageEnabled.collectAsStateWithLifecycle()
  val storageUsedDownloadsMb by viewModel.storageUsedDownloadsMb.collectAsStateWithLifecycle()
  val storageUsedCacheMb by viewModel.storageUsedCacheMb.collectAsStateWithLifecycle()
  val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
  val hasMoreContent by viewModel.hasMoreContent.collectAsStateWithLifecycle()

  val watchlistItems = remember(watchlistState) {
    (watchlistState as? UiState.Success)?.data ?: emptyList()
  }
  val watchlistMap = remember(watchlistItems) {
    buildMap {
      watchlistItems.forEach { item ->
        put(item.mediaId.ifBlank { item.id }, item)
        put(item.canonicalMediaId, item)
      }
    }
  }
  val offlineSavedIds = remember(offlineSavedMedia) {
    offlineSavedMedia.map { it.id }.toSet()
  }

  val isHardwareTelevision = remember(context) {
    val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
    uiMode == Configuration.UI_MODE_TYPE_TELEVISION
  }
  var isTvModeOverride by remember { mutableStateOf(isHardwareTelevision) }
  var showTvSyncDialog by remember { mutableStateOf(false) }
  var showProfileDialog by remember { mutableStateOf(false) }
  var pendingPinProfile by remember { mutableStateOf<UserProfile?>(null) }
  var showPinLockDialog by remember { mutableStateOf(false) }
  var newProfilePinInput by remember { mutableStateOf("") }

  // In-App OTA Update State
  var availableUpdate by remember { mutableStateOf<AppUpdateInfo?>(null) }
  var showUpdateDialog by remember { mutableStateOf(false) }

  // Startup OTA Check
  LaunchedEffect(Unit) {
    val update = OtaUpdateManager.checkForUpdates()
    if (update != null) {
      availableUpdate = update
      showUpdateDialog = true
    }
  }

  var showCastDialog by remember { mutableStateOf(false) }
  var showVoiceSearchDialog by remember { mutableStateOf(false) }
  var isGlassSidebarOpen by remember { mutableStateOf(false) }
  var isGlassSidebarCollapsed by remember { mutableStateOf(false) }
  var activeSidebarKey by remember { mutableStateOf(GlassSidebarItemKey.HOME) }
  var newProfileNameInput by remember { mutableStateOf("") }
  var tvFocusedCardIndex by remember { mutableIntStateOf(0) }

  var currentTab by remember { mutableStateOf(MainNavTab.EXPLORE) }
  val snackbarHostState = remember { SnackbarHostState() }

  val handleGlassSidebarSelection: (GlassSidebarItemKey) -> Unit = { key ->
    activeSidebarKey = key
    isGlassSidebarOpen = false
    when (key) {
      GlassSidebarItemKey.HOME -> {
        currentTab = MainNavTab.EXPLORE
        viewModel.setCategoryFilter(CatalogCategoryFilter.ALL)
      }
      GlassSidebarItemKey.CONTINUE_WATCHING -> {
        currentTab = MainNavTab.WATCH_MOVIES
      }
      GlassSidebarItemKey.MY_LIST -> {
        currentTab = MainNavTab.WATCHLIST
      }
      GlassSidebarItemKey.SERIES -> {
        currentTab = MainNavTab.SERIES
        viewModel.setCategoryFilter(CatalogCategoryFilter.SERIES)
      }
      GlassSidebarItemKey.MOVIES -> {
        currentTab = MainNavTab.EXPLORE
        viewModel.setCategoryFilter(CatalogCategoryFilter.RELEASED_MOVIES)
      }
      GlassSidebarItemKey.GAMES_OFFLINE -> {
        if (!isTvModeOverride) {
          currentTab = MainNavTab.OFFLINE
        } else {
          currentTab = MainNavTab.EXPLORE
        }
      }
      GlassSidebarItemKey.RELEASES -> {
        currentTab = MainNavTab.UPCOMING
        viewModel.setCategoryFilter(CatalogCategoryFilter.UPCOMING_MOVIES)
      }
      GlassSidebarItemKey.POPULAR -> {
        currentTab = MainNavTab.EXPLORE
        viewModel.setCategoryFilter(CatalogCategoryFilter.ALL)
      }
      GlassSidebarItemKey.SETTINGS_NEUMORPHIC -> {
        showTvSyncDialog = true
      }
      GlassSidebarItemKey.LOG_OUT -> {
        signOut(
          context = context,
          credentialManager = credentialManager,
          onSignOutComplete = {},
          scope = scope
        )
      }
    }
  }

  // Offline Downloads are Mobile-Only (hidden on Android TV Mode)
  val visibleNavTabs = remember(isTvModeOverride) {
    if (isTvModeOverride) {
      MainNavTab.entries.filter { it != MainNavTab.OFFLINE }
    } else {
      MainNavTab.entries
    }
  }
  LaunchedEffect(isTvModeOverride, currentTab) {
    if (isTvModeOverride && currentTab == MainNavTab.OFFLINE) {
      currentTab = MainNavTab.EXPLORE
    }
  }

  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearSnackbar()
    }
  }

  LaunchedEffect(Unit) {
    viewModel.recalculateStorageUsage(context)
  }

  val currentActor = selectedActor
  val currentDetail = selectedMedia

  // 1. Netflix-Style Multi-Profile Switcher Dialog
  if (showProfileDialog) {
    AlertDialog(
      onDismissRequest = { showProfileDialog = false },
      icon = {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = "User Profiles",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          text = "Who's Watching? (Profiles)",
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
            text = "Ek hi Google Account ($userEmailOrName) ke andar alag-alag family profiles — har profile ki Watch History, Watchlist, aur Favorites alag rehti hai!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          profiles.forEach { prof ->
            val isSelected = prof.id == activeProfile.id
            Surface(
              onClick = {
                if (prof.isPinProtected && prof.id != activeProfile.id) {
                  pendingPinProfile = prof
                  showPinLockDialog = true
                  showProfileDialog = false
                } else {
                  viewModel.selectProfile(prof)
                  showProfileDialog = false
                }
              },
              color = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
              } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
              },
              shape = RoundedCornerShape(14.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_option_${prof.id}")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                  Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = CircleShape,
                    modifier = Modifier.size(42.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(text = prof.avatarEmoji, style = MaterialTheme.typography.titleMedium)
                    }
                  }
                  Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = prof.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                      )
                      if (prof.isPinProtected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                          imageVector = Icons.Default.Lock,
                          contentDescription = "PIN Protected Profile",
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(15.dp)
                        )
                      }
                    }
                    Text(
                      text = if (prof.isPinProtected) "${prof.badgeLabel} • PIN Protected" else prof.badgeLabel,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
                if (isSelected) {
                  Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(8.dp)
                  ) {
                    Text(
                      text = "ACTIVE",
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

          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = newProfileNameInput,
                onValueChange = { newProfileNameInput = it },
                placeholder = { Text("New profile name...") },
                singleLine = true,
                modifier = Modifier
                  .weight(1f)
                  .testTag("new_profile_name_input")
              )
              FilledTonalButton(
                onClick = {
                  if (newProfileNameInput.isNotBlank()) {
                    val pin = newProfilePinInput.trim()
                    viewModel.addCustomProfile(
                      name = newProfileNameInput,
                      isPinProtected = pin.length == 4 && pin.all { it.isDigit() },
                      pinCode = pin
                    )
                    newProfileNameInput = ""
                    newProfilePinInput = ""
                    showProfileDialog = false
                  }
                },
                modifier = Modifier.testTag("add_custom_profile_button")
              ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Profile", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
              }
            }

            OutlinedTextField(
              value = newProfilePinInput,
              onValueChange = {
                if (it.length <= 4 && it.all { char -> char.isDigit() }) newProfilePinInput = it
              },
              placeholder = { Text("Optional 4-Digit PIN Lock (e.g. 1234)") },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("new_profile_pin_input")
            )
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showProfileDialog = false }) {
          Text("Done")
        }
      }
    )
  }

  // 1b. 4-Digit PIN Lock Dialog for Protected/Adult Profiles
  if (showPinLockDialog && pendingPinProfile != null) {
    val target = pendingPinProfile!!
    ProfilePinLockDialog(
      profile = target,
      onPinVerified = {
        viewModel.selectProfile(target)
        pendingPinProfile = null
        showPinLockDialog = false
      },
      onDismiss = {
        pendingPinProfile = null
        showPinLockDialog = false
      }
    )
  }

  // 1c. In-App OTA Update Dialog
  if (showUpdateDialog && availableUpdate != null) {
    AppUpdateDialog(
      updateInfo = availableUpdate!!,
      onDismiss = { showUpdateDialog = false },
      onDownloadAndInstall = {
        OtaUpdateManager.startApkDownloadAndInstall(context, availableUpdate!!)
        showUpdateDialog = false
      }
    )
  }

  // 2. Seamless Casting Dialog (Xiaomi Android TV, Amazon Fire TV, Chromecast)
  if (showCastDialog) {
    val targetMediaToCast = currentDetail
      ?: watchHistory.firstOrNull()?.let { entry ->
        allCachedCatalog.find { it.id == entry.mediaId } ?: entry.toMediaItem()
      }
      ?: catalog.firstOrNull()

    AlertDialog(
      onDismissRequest = { showCastDialog = false },
      icon = {
        Icon(
          imageVector = Icons.Default.Cast,
          contentDescription = "Seamless Casting",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          text = "Seamless Cast (Xiaomi TV / Fire TV / Chromecast)",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Ek click mein mobile se Xiaomi Android TV, Amazon Fire TV, ya kisi bhi Chromecast device par '${targetMediaToCast?.title ?: "Movie"}' 4K Dolby Vision me cast karein:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          listOf(
            "Xiaomi Mi TV 4K (Android TV)" to "Chromecast Built-in • Ready",
            "Amazon Fire TV Stick 4K Max" to "Fire OS Cast • 4K HDR10+",
            "Living Room Sony Bravia Google TV" to "Same Wi-Fi • 1080p / 4K",
            "Home Theater Chromecast Ultra" to "Ultra-Low Latency • Dolby Atmos"
          ).forEachIndexed { idx, (deviceName, subtitle) ->
            Surface(
              onClick = {
                if (targetMediaToCast != null) {
                  viewModel.startCasting(targetMediaToCast, deviceName)
                }
                showCastDialog = false
              },
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("cast_device_item_$idx")
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Tv,
                  contentDescription = deviceName,
                  tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = deviceName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      },
      confirmButton = {
        if (activeCastSession != null) {
          Button(
            onClick = {
              viewModel.stopCasting()
              showCastDialog = false
            }
          ) {
            Text("Stop Casting")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = { showCastDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // 3. Mobile <-> Android TV Sync + QR Code Auto-Login + 40:00 Resume Dialog
  if (showTvSyncDialog) {
    AlertDialog(
      onDismissRequest = { showTvSyncDialog = false },
      icon = {
        Icon(
          imageVector = Icons.Default.Devices,
          contentDescription = "Mobile & Android TV Cloud Sync",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Text(
          text = "📱 Mobile ↔ 📺 Android TV Sync & QR Login",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              TvQrCodeMatrixCanvas(
                codeSeed = "CV-TV-${currentUserId.take(8)}-${activeProfile.id}",
                modifier = Modifier
                  .size(78.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.White)
                  .padding(6.dp)
                  .testTag("sync_dialog_qr_code")
              )
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
              ) {
                Text(
                  text = "TV QR Auto-Login • ${activeProfile.avatarEmoji} ${activeProfile.name}",
                  style = MaterialTheme.typography.labelLarge,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = userEmailOrName,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Scan QR from Mobile app to auto-login on Android TV without typing email/password!",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          val favoritesCount = watchlistItems.count { it.isFavorite }
          Text(
            text = "• Active Profile: ${activeProfile.avatarEmoji} ${activeProfile.name}\n" +
              "• Universal Watchlist: ${watchlistItems.size} Titles ($favoritesCount ❤️ Favorites)\n" +
              "• Continue Watching (Real-Time Timestamp): ${watchHistory.size} Titles\n" +
              "• Trakt.tv Global Sync: ${if (isTraktConnected) "Connected ($traktHandle)" else "Ready"}\n" +
              "• Active UI Mode: ${if (isTvModeOverride) "📺 Android TV (Leanback D-Pad Mode)" else "📱 Mobile Gesture Mode"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
          )


          FilledTonalButton(
            onClick = { isTvModeOverride = !isTvModeOverride },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("dialog_toggle_tv_mode_button")
          ) {
            Icon(
              imageVector = if (isTvModeOverride) Icons.Default.PhoneAndroid else Icons.Default.Tv,
              contentDescription = "Switch Layout Mode"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isTvModeOverride) "Switch to 📱 Mobile Layout" else "Switch to 📺 Android TV Leanback Layout"
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.syncAllProgressToCloudNow()
            showTvSyncDialog = false
          },
          modifier = Modifier.testTag("sync_mobile_tv_now_button")
        ) {
          Icon(
            imageVector = Icons.Default.CloudSync,
            contentDescription = "Sync Now",
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sync Mobile ↔ TV Now")
        }
      },
      dismissButton = {
        TextButton(onClick = { showTvSyncDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // 4. Native Android TV Voice Search Speech-To-Text Pipeline
  TvVoiceSearchOverlay(
    isOpen = showVoiceSearchDialog,
    onDismiss = { showVoiceSearchDialog = false },
    onApplyVoiceQuery = { voiceQuery ->
      viewModel.onSearchQueryChanged(voiceQuery)
      if (currentTab != MainNavTab.EXPLORE) {
        currentTab = MainNavTab.EXPLORE
      }
    }
  )

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isExpandedScreen = maxWidth >= 640.dp || isTvModeOverride

    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        if (currentActor == null && currentDetail == null) {
          TopAppBar(
            navigationIcon = {
              IconButton(
                onClick = {
                  if (isExpandedScreen) {
                    isGlassSidebarCollapsed = !isGlassSidebarCollapsed
                  } else {
                    isGlassSidebarOpen = !isGlassSidebarOpen
                  }
                },
                modifier = Modifier.testTag("top_sidebar_toggle_button")
              ) {
                BadgedBox(
                  badge = {
                    Badge(containerColor = Color(0xFFFF0000)) {
                      Text("4", color = Color.White)
                    }
                  }
                ) {
                  Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Frosted Glass SideBar UI",
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
              }
            },
            title = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = if (isTvModeOverride) Icons.Default.Tv else Icons.Default.Movie,
                  contentDescription = "CineVault",
                  tint = MaterialTheme.colorScheme.primary
                )
                Column {
                  Text(
                    text = if (isTvModeOverride) "CineVault • Android TV" else "CineVault",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = if (isOfflineModeEnabled) {
                      "Offline Mode • ${activeProfile.name}"
                    } else {
                      "${activeProfile.avatarEmoji} ${activeProfile.name} • Synced"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
            },
            actions = {

              // Netflix-Style Profile Switcher Pill
              Surface(
                onClick = { showProfileDialog = true },
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .padding(end = 4.dp)
                  .testTag("profile_switcher_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Text(
                    text = activeProfile.avatarEmoji,
                    style = MaterialTheme.typography.labelMedium
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = activeProfile.name.substringBefore(" "),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Seamless Cast Button
              IconButton(
                onClick = { showCastDialog = true },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("top_cast_button")
              ) {
                Icon(
                  imageVector = if (activeCastSession != null) Icons.Default.CastConnected else Icons.Default.Cast,
                  contentDescription = "Cast to TV",
                  tint = if (activeCastSession != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
              }

              // Native TV / Mobile Voice Search Mic Button
              IconButton(
                onClick = { showVoiceSearchDialog = true },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("top_voice_search_mic_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Mic,
                  contentDescription = "Native TV Voice Search",
                  tint = MaterialTheme.colorScheme.primary
                )
              }

              // 1-Tap Android TV / Mobile Mode Toggle Chip
              Surface(
                onClick = { isTvModeOverride = !isTvModeOverride },
                color = if (isTvModeOverride) {
                  MaterialTheme.colorScheme.secondaryContainer
                } else {
                  MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .padding(end = 4.dp)
                  .testTag("toggle_tv_mode_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = if (isTvModeOverride) Icons.Default.Tv else Icons.Default.PhoneAndroid,
                    contentDescription = "Toggle TV / Mobile Mode",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (isTvModeOverride) "TV" else "Mobile",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Mobile <-> Android TV Cloud Sync & QR Hub Button
              Surface(
                onClick = { showTvSyncDialog = true },
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .padding(end = 4.dp)
                  .testTag("tv_mobile_sync_hub_button")
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = "Mobile & TV Cloud Sync",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "TV Sync",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              IconButton(
                onClick = {
                  signOut(
                    context = context,
                    credentialManager = credentialManager,
                    onSignOutComplete = {},
                    scope = scope
                  )
                },
                modifier = Modifier
                  .size(36.dp)
                  .testTag("sign_out_button")
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.Logout,
                  contentDescription = "Sign Out"
                )
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.background
            )
          )
        }
      },
      bottomBar = {
        if (!isExpandedScreen && currentActor == null && currentDetail == null) {
          CineVaultBottomNavigation(
            currentTab = currentTab,
            onTabSelected = { tab ->
              currentTab = tab
              when (tab) {
                MainNavTab.EXPLORE -> viewModel.setCategoryFilter(CatalogCategoryFilter.ALL)
                MainNavTab.SERIES -> viewModel.setCategoryFilter(CatalogCategoryFilter.SERIES)
                MainNavTab.UPCOMING -> viewModel.setCategoryFilter(CatalogCategoryFilter.UPCOMING_MOVIES)
                MainNavTab.WATCH_MOVIES, MainNavTab.WATCHLIST, MainNavTab.OFFLINE -> {}
              }
            },
            visibleTabs = visibleNavTabs,
            watchlistBadgeCount = watchlistItems.size
          )
        }
      }
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        if (isExpandedScreen && currentActor == null && currentDetail == null) {
          FrostedGlassSidebar(
            activeItem = activeSidebarKey,
            isCollapsed = isGlassSidebarCollapsed,
            onToggleCollapse = { isGlassSidebarCollapsed = !isGlassSidebarCollapsed },
            activeProfileName = activeProfile.name,
            activeProfileEmoji = activeProfile.avatarEmoji,
            myListBadgeCount = watchlistItems.size.coerceAtLeast(7),
            notificationBadgeCount = 4,
            searchQuery = searchQuery,
            onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
            onSwitchAccountClick = { showProfileDialog = true },
            onNotificationClick = { showTvSyncDialog = true },
            onSelectItem = handleGlassSidebarSelection
          )
        }

        Column(modifier = Modifier.fillMaxSize()) {
          // Live Chromecast / Xiaomi TV / Fire TV Controller Bar (when casting to TV)
          val castSession = activeCastSession
          if (castSession != null) {
            val posMin = (castSession.positionMs / 1000) / 60
            val posSec = (castSession.positionMs / 1000) % 60
            val formattedCastTime = String.format(Locale.US, "%02d:%02d", posMin, posSec)
            Surface(
              color = Color(0xFF131B2E),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("active_cast_controller_bar")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = Icons.Default.CastConnected,
                    contentDescription = "Casting",
                    tint = MaterialTheme.colorScheme.primary
                  )
                  Column {
                    Text(
                      text = "Casting '${castSession.media.title}' • $formattedCastTime",
                      style = MaterialTheme.typography.labelLarge,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "${castSession.deviceName} • ${castSession.qualityLabel}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  IconButton(
                    onClick = { viewModel.seekCastBy(-10_000) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Replay10,
                      contentDescription = "Cast Rewind 10s",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  IconButton(
                    onClick = { viewModel.toggleCastPlayPause() },
                    modifier = Modifier.size(34.dp)
                  ) {
                    Icon(
                      imageVector = if (castSession.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                      contentDescription = "Cast Play Pause",
                      tint = MaterialTheme.colorScheme.primary
                    )
                  }
                  IconButton(
                    onClick = { viewModel.seekCastBy(10_000) },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Forward10,
                      contentDescription = "Cast Forward 10s",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                  IconButton(
                    onClick = { viewModel.stopCasting() },
                    modifier = Modifier.size(32.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Stop Casting",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
              }
            }
          }

          // Android TV Leanback D-Pad Remote Bar (Visible in Android TV Mode for 10-foot D-Pad navigation)
          if (isTvModeOverride) {
            val safeCatalog = if (catalog.isNotEmpty()) catalog else allCachedCatalog
            val focusedMovie = safeCatalog.getOrNull(
              tvFocusedCardIndex.coerceIn(0, (safeCatalog.size - 1).coerceAtLeast(0))
            )
            Surface(
              color = Color(0xFF0E1422),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("tv_leanback_dpad_bar")
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
                  .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.SettingsRemote,
                  contentDescription = "TV D-Pad Remote",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
                Text(
                  text = "TV Remote D-Pad • Focus: ${focusedMovie?.title ?: "None"}",
                  style = MaterialTheme.typography.labelLarge,
                  color = Color.White,
                  fontWeight = FontWeight.Bold
                )
                FilledTonalButton(
                  onClick = {
                    if (safeCatalog.isNotEmpty()) {
                      tvFocusedCardIndex = (tvFocusedCardIndex - 1 + safeCatalog.size) % safeCatalog.size
                    }
                  },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(32.dp)
                    .testTag("tv_dpad_left_button")
                ) {
                  Text("◀ Left", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                  onClick = {
                    if (safeCatalog.isNotEmpty()) {
                      tvFocusedCardIndex = (tvFocusedCardIndex + 1) % safeCatalog.size
                    }
                  },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(32.dp)
                    .testTag("tv_dpad_right_button")
                ) {
                  Text("Right ▶", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                  onClick = {
                    focusedMovie?.let { movie ->
                      viewModel.openMediaDetail(movie, autoPlayMovie = false)
                    }
                  },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(32.dp)
                    .testTag("tv_dpad_ok_button")
                ) {
                  Text("● OK (Open)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
                FilledTonalButton(
                  onClick = {
                    val resumeEntry = watchHistory.firstOrNull()
                    if (resumeEntry != null) {
                      viewModel.continueWatchingFromHistory(resumeEntry)
                    } else if (focusedMovie != null) {
                      viewModel.openMediaDetail(
                        item = focusedMovie,
                        autoPlayMovie = true,
                        resumePositionMs = 2_400_000
                      )
                    }
                  },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(32.dp)
                    .testTag("tv_dpad_resume_button")
                ) {
                  Text("▶ Continue Watching", style = MaterialTheme.typography.labelSmall)
                }
                FilledTonalButton(
                  onClick = { showVoiceSearchDialog = true },
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFE50914),
                    contentColor = Color.White
                  ),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier
                    .height(32.dp)
                    .testTag("tv_dpad_voice_search_btn")
                ) {
                  Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Search",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("🎤 Voice Search", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          Box(modifier = Modifier.fillMaxSize()) {
            when {
              currentActor != null -> {
                ActorFilmographyScreen(
                  actor = currentActor,
                  filmography = actorFilmography,
                  isLoading = isActorLoading,
                  watchlistMap = watchlistMap,
                  onBack = { viewModel.closeActorFilmography() },
                  onMediaClick = { clickedMovie ->
                    viewModel.closeActorFilmography()
                    viewModel.openMediaDetail(clickedMovie, autoPlayMovie = true)
                  },
                  onQuickStatusSelect = { media, status ->
                    viewModel.saveOrUpdateWatchlistStatus(media, status)
                  }
                )
              }

              currentDetail != null -> {
                val safeId = currentDetail.id.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
                val existingHistory = watchHistory.find { it.mediaId == currentDetail.id }
                val effectiveResumeMs = if (resumePositionMsOnOpen > 0) {
                  resumePositionMsOnOpen
                } else {
                  existingHistory?.positionMs ?: 0
                }
                val effectiveSeason = if (resumePositionMsOnOpen > 0) {
                  resumeSeasonOnOpen
                } else {
                  existingHistory?.seasonNumber ?: 1
                }
                val effectiveEpisode = if (resumePositionMsOnOpen > 0) {
                  resumeEpisodeOnOpen
                } else {
                  existingHistory?.episodeNumber ?: 1
                }
                MediaDetailScreen(
                  media = currentDetail,
                  watchlistItem = watchlistMap[safeId],
                  isSavedOffline = offlineSavedIds.contains(currentDetail.id),
                  autoPlayMovie = autoPlayMovieOnOpen,
                  resumePositionMs = effectiveResumeMs,
                  initialSeasonNumber = effectiveSeason,
                  initialEpisodeNumber = effectiveEpisode,
                  onPlaybackProgress = { posMs, durMs, sNum, eNum ->
                    viewModel.recordPlaybackProgress(
                      media = currentDetail,
                      positionMs = posMs,
                      durationMs = durMs,
                      seasonNumber = sNum,
                      episodeNumber = eNum
                    )
                    if (durMs > 0 && posMs.toFloat() / durMs.toFloat() >= 0.90f) {
                      viewModel.onMovieCompletedOrMarkedWatched(currentDetail, context)
                    }
                  },
                  onBack = { viewModel.closeMediaDetail() },
                  onSaveStatus = { status, notes, rating ->
                    viewModel.saveOrUpdateWatchlistStatus(currentDetail, status, notes, rating)
                    if (status == com.example.data.model.WatchStatus.WATCHED) {
                      viewModel.onMovieCompletedOrMarkedWatched(currentDetail, context)
                    }
                  },
                  onRemoveFromWatchlist = {
                    viewModel.removeFromWatchlist(safeId, currentDetail.title)
                  },
                  onToggleOfflineSaved = { save ->
                    viewModel.toggleOfflineSaved(currentDetail, save)
                  },
                  onActorClick = { actor ->
                    viewModel.openActorFilmography(actor)
                  },
                  onToggleFavorite = {
                    viewModel.toggleFavorite(currentDetail)
                  },
                  onCastToTv = {
                    showCastDialog = true
                  },
                  availablePlaylistTags = availablePlaylistTags,
                  onToggleTagOnMovie = { tag ->
                    val existingItem = watchlistMap[safeId]
                    if (existingItem != null) {
                      viewModel.toggleTagOnWatchlistItem(existingItem, tag)
                    } else {
                      viewModel.saveOrUpdateWatchlistStatus(
                        media = currentDetail,
                        status = com.example.data.model.WatchStatus.WANT_TO_WATCH,
                        customTags = listOf(tag)
                      )
                    }
                  },
                  onEnterPipMode = { currentPosMs, currentDurMs ->
                    viewModel.enterMiniPipMode(currentDetail, currentPosMs, currentDurMs)
                  },
                  smartDownloadTask = smartDownloadTasks.find { it.media.id == currentDetail.id },
                  onStartSmartDownload = { quality ->
                    viewModel.startSmartDownload(currentDetail, quality)
                  },
                  onTogglePauseResumeSmartDownload = {
                    viewModel.togglePauseResumeSmartDownload(currentDetail.id)
                  },
                  isTvMode = isTvModeOverride
                )
              }

              currentTab == MainNavTab.WATCH_MOVIES -> {
                WatchMoviesScreen(
                  catalog = allCachedCatalog,
                  watchlistMap = watchlistMap,
                  watchHistory = watchHistory,
                  onOpenMediaDetail = { media ->
                    viewModel.openMediaDetail(media, autoPlayMovie = false)
                  },
                  onSaveStatus = { media, status ->
                    viewModel.saveOrUpdateWatchlistStatus(media, status)
                    if (status == com.example.data.model.WatchStatus.WATCHED) {
                      viewModel.onMovieCompletedOrMarkedWatched(media, context)
                    }
                  },
                  onRemoveWatchHistoryEntry = { mediaId ->
                    viewModel.removeWatchHistoryItem(mediaId)
                  },
                  onClearWatchHistory = {
                    viewModel.clearWatchHistory()
                  },
                  onRecordPlaybackProgress = { media, posMs, durMs, sNum, eNum ->
                    viewModel.recordPlaybackProgress(
                      media = media,
                      positionMs = posMs,
                      durationMs = durMs,
                      seasonNumber = sNum,
                      episodeNumber = eNum
                    )
                  }
                )
              }

              currentTab == MainNavTab.WATCHLIST -> {
                WatchlistScreen(
                  userEmailOrId = userEmailOrName,
                  watchlistItems = watchlistItems,
                  watchHistory = watchHistory,
                  availablePlaylistTags = availablePlaylistTags,
                  traktHandle = traktHandle,
                  lastTraktSyncLabel = lastTraktSyncLabel,
                  autoFetchedPreviewMedia = autoFetchedPreviewMedia,
                  isAutoFetchingMetadata = isAutoFetchingMetadata,
                  onPreviewAutoMetadata = { titleQuery, customUrl ->
                    viewModel.previewAutoFetchedMetadata(titleQuery, customUrl)
                  },
                  onClearAutoPreview = {
                    viewModel.clearAutoFetchedPreview()
                  },
                  onAddMovieWithAutoMetadata = { rawTitle, selectedTags, storageSource, customStreamUrl, personalNote ->
                    viewModel.addMovieToSmartLibraryWithAutoMetadata(
                      rawTitle = rawTitle,
                      selectedTags = selectedTags,
                      storageSource = storageSource,
                      customStreamUrl = customStreamUrl,
                      personalNote = personalNote
                    )
                  },
                  onAddCustomPlaylistTag = { newTag ->
                    viewModel.addCustomPlaylistTag(newTag)
                  },
                  onToggleItemTag = { item, tag ->
                    viewModel.toggleTagOnWatchlistItem(item, tag)
                  },
                  onSyncTraktTv = {
                    viewModel.syncWithTraktTv()
                  },
                  onOpenMedia = { media -> viewModel.openMediaDetail(media, autoPlayMovie = true) },
                  onContinueWatchingEntry = { entry ->
                    viewModel.continueWatchingFromHistory(entry)
                  },
                  onSwitchStatus = { item, newStatus ->
                    viewModel.saveOrUpdateWatchlistStatus(
                      media = item.toMediaItem(),
                      status = newStatus,
                      userNotes = item.userNotes,
                      userRating = item.userRating?.toInt()
                    )
                    if (newStatus == com.example.data.model.WatchStatus.WATCHED) {
                      viewModel.onMovieCompletedOrMarkedWatched(item.toMediaItem(), context)
                    }
                  },
                  onRemoveItem = { item ->
                    viewModel.removeFromWatchlist(item.mediaId.ifBlank { item.id }, item.title)
                  },
                  activeProfileName = "${activeProfile.avatarEmoji} ${activeProfile.name}",
                  onToggleFavorite = { media ->
                    viewModel.toggleFavorite(media)
                  }
                )
              }

              currentTab == MainNavTab.OFFLINE && !isTvModeOverride -> {
                OfflineModeScreen(
                  isOfflineModeEnabled = isOfflineModeEnabled,
                  onToggleOfflineMode = { enabled -> viewModel.toggleOfflineMode(enabled) },
                  offlineSavedMedia = offlineSavedMedia,
                  watchlistItems = watchlistItems,
                  cachedCatalogCount = allCachedCatalog.size,
                  smartDownloadTasks = smartDownloadTasks,
                  onStartSmartDownload = { media, quality ->
                    viewModel.startSmartDownload(media, quality)
                  },
                  onTogglePauseResumeDownload = { mediaId ->
                    viewModel.togglePauseResumeSmartDownload(mediaId)
                  },
                  onCancelDownload = { mediaId ->
                    viewModel.cancelSmartDownload(mediaId)
                  },
                  isAutoDeleteWatchedEnabled = isAutoDeleteWatchedEnabled,
                  onToggleAutoDeleteWatched = { enabled -> viewModel.toggleAutoDeleteWatched(enabled) },
                  isAutoPruneStorageEnabled = isAutoPruneStorageEnabled,
                  onToggleAutoPruneStorage = { enabled -> viewModel.toggleAutoPruneStorage(enabled) },
                  storageUsedDownloadsMb = storageUsedDownloadsMb,
                  storageUsedCacheMb = storageUsedCacheMb,
                  onCleanStorageNow = { viewModel.clearAllStreamCacheAndWatchedDownloads(context) },
                  onOpenMedia = { media -> viewModel.openMediaDetail(media, autoPlayMovie = true) }
                )
              }

              else -> {
                DiscoverScreen(
                  catalog = catalog,
                  allCatalogItems = allCachedCatalog,
                  watchlistMap = watchlistMap,
                  watchHistory = watchHistory,
                  searchQuery = searchQuery,
                  onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                  selectedCategory = categoryFilter,
                  onCategoryChange = { viewModel.setCategoryFilter(it) },
                  selectedGenre = selectedGenre,
                  onGenreChange = { viewModel.setGenreFilter(it) },
                  selectedReleaseYear = selectedReleaseYear,
                  onReleaseYearChange = { viewModel.setReleaseYearFilter(it) },
                  onOpenAddMovieDialog = {
                    currentTab = MainNavTab.WATCHLIST
                  },
                  isRefreshing = isRefreshing,
                  lastRefreshedLabel = lastRefreshedLabel,
                  onRefresh = { viewModel.refreshCatalog(fromUserPull = true) },
                  onOpenMedia = { media -> viewModel.openMediaDetail(media, autoPlayMovie = false) },
                  onWatchMovie = { media -> viewModel.openMediaDetail(media, autoPlayMovie = true) },
                  onContinueWatchingEntry = { entry ->
                    viewModel.continueWatchingFromHistory(entry)
                  },
                  onRemoveWatchHistoryEntry = { mediaId ->
                    viewModel.removeWatchHistoryItem(mediaId)
                  },
                  onClearWatchHistory = {
                    viewModel.clearWatchHistory()
                  },
                  onOpenVoiceSearch = {
                    showVoiceSearchDialog = true
                  },
                  isLoadingMore = isLoadingMore,
                  hasMoreContent = hasMoreContent,
                  onLoadMore = { viewModel.loadMoreContent() },
                  onQuickStatusSelect = { media, status ->
                    viewModel.saveOrUpdateWatchlistStatus(media, status)
                  }
                )
              }
            }

            // Floating Picture-in-Picture (PiP) Mini Player Overlay
            val activePip = pipSession
            if (activePip != null) {
              val pipMin = (activePip.positionMs / 1000) / 60
              val pipSec = (activePip.positionMs / 1000) % 60
              val pipTimeFormatted = String.format(Locale.US, "%02d:%02d", pipMin, pipSec)
              Surface(
                color = Color(0xFF0E1525).copy(alpha = 0.96f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                shadowElevation = 12.dp,
                modifier = Modifier
                  .align(Alignment.BottomEnd)
                  .padding(16.dp)
                  .width(285.dp)
                  .testTag("mini_pip_floating_player")
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      viewModel.openMediaDetail(
                        item = activePip.media,
                        autoPlayMovie = true,
                        resumePositionMs = activePip.positionMs
                      )
                      viewModel.closeMiniPipMode()
                    }
                    .padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                  Box(
                    modifier = Modifier
                      .size(width = 68.dp, height = 46.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(Color.Black),
                    contentAlignment = Alignment.Center
                  ) {
                    AsyncImage(
                      model = activePip.media.backdropUrl.ifBlank { activePip.media.posterUrl },
                      contentDescription = activePip.media.title,
                      contentScale = ContentScale.Crop,
                      modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                      imageVector = Icons.Default.PictureInPictureAlt,
                      contentDescription = "PiP Active",
                      tint = Color.White,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = activePip.media.title,
                      style = MaterialTheme.typography.labelLarge,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = "PiP Active • $pipTimeFormatted",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }

                  IconButton(
                    onClick = { viewModel.toggleMiniPipPlayPause() },
                    modifier = Modifier
                      .size(30.dp)
                      .testTag("mini_pip_play_pause_button")
                  ) {
                    Icon(
                      imageVector = if (activePip.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                      contentDescription = "PiP Play Pause",
                      tint = MaterialTheme.colorScheme.primary,
                      modifier = Modifier.size(18.dp)
                    )
                  }

                  IconButton(
                    onClick = {
                      onRequestSystemPip?.invoke()
                      viewModel.openMediaDetail(
                        item = activePip.media,
                        autoPlayMovie = true,
                        resumePositionMs = activePip.positionMs
                      )
                      viewModel.closeMiniPipMode()
                    },
                    modifier = Modifier
                      .size(30.dp)
                      .testTag("mini_pip_expand_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.OpenInFull,
                      contentDescription = "Expand PiP",
                      tint = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }

                  IconButton(
                    onClick = { viewModel.closeMiniPipMode() },
                    modifier = Modifier
                      .size(30.dp)
                      .testTag("mini_pip_close_button")
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Close PiP",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }

            // Smooth Slide-In Frosted Glass SideBar UI Overlay (Mobile & Portrait Mode)
            if (!isExpandedScreen && currentActor == null && currentDetail == null) {
              androidx.compose.animation.AnimatedVisibility(
                visible = isGlassSidebarOpen,
                enter = fadeIn(),
                exit = fadeOut()
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.62f))
                    .clickable { isGlassSidebarOpen = false }
                    .testTag("mobile_sidebar_scrim")
                )
              }

              androidx.compose.animation.AnimatedVisibility(
                visible = isGlassSidebarOpen,
                enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterStart)
              ) {
                FrostedGlassSidebar(
                  activeItem = activeSidebarKey,
                  isCollapsed = isGlassSidebarCollapsed,
                  onToggleCollapse = {
                    if (isGlassSidebarCollapsed) {
                      isGlassSidebarCollapsed = false
                    } else {
                      isGlassSidebarOpen = false
                    }
                  },
                  activeProfileName = activeProfile.name,
                  activeProfileEmoji = activeProfile.avatarEmoji,
                  myListBadgeCount = watchlistItems.size.coerceAtLeast(7),
                  notificationBadgeCount = 4,
                  searchQuery = searchQuery,
                  onSearchQueryChange = { viewModel.onSearchQueryChanged(it) },
                  onSwitchAccountClick = {
                    isGlassSidebarOpen = false
                    showProfileDialog = true
                  },
                  onNotificationClick = {
                    isGlassSidebarOpen = false
                    showTvSyncDialog = true
                  },
                  onSelectItem = handleGlassSidebarSelection
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun tabIcon(tab: MainNavTab): ImageVector = when (tab) {
  MainNavTab.EXPLORE -> Icons.Default.Explore
  MainNavTab.SERIES -> Icons.Default.Tv
  MainNavTab.WATCH_MOVIES -> Icons.Default.PlayCircleFilled
  MainNavTab.UPCOMING -> Icons.Default.Upcoming
  MainNavTab.WATCHLIST -> Icons.Default.Bookmarks
  MainNavTab.OFFLINE -> Icons.Default.DownloadForOffline
}
