package com.example.service

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class AppUpdateInfo(
  val versionCode: Int,
  val versionName: String,
  val changelog: String,
  val downloadUrl: String,
  val releaseDate: String = "Latest Release",
  val isForceUpdate: Boolean = false
)

/**
 * In-App OTA Updater Service:
 * Checks remote version configuration, handles background APK download via Android's
 * DownloadManager, and prompts the user with secure APK installation intents.
 */
object OtaUpdateManager {
  private const val DEFAULT_REMOTE_CONFIG_URL =
    "https://raw.githubusercontent.com/cinevault/releases/main/update_config.json"

  suspend fun checkForUpdates(
    remoteConfigUrl: String = DEFAULT_REMOTE_CONFIG_URL
  ): AppUpdateInfo? = withContext(Dispatchers.IO) {
    runCatching {
      val url = URL(remoteConfigUrl)
      val conn = (url.openConnection() as HttpURLConnection).apply {
        connectTimeout = 6000
        readTimeout = 8000
        requestMethod = "GET"
        setRequestProperty("Accept", "application/json")
      }
      val responseCode = conn.responseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
        val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
        val root = JSONObject(jsonText)
        val latestCode = root.optInt("versionCode", 1)
        val latestName = root.optString("versionName", "1.0")
        val changelog = root.optString("changelog", "Bug fixes and performance improvements.")
        val apkUrl = root.optString("apkUrl", "")
        val force = root.optBoolean("forceUpdate", false)

        val currentCode = BuildConfig.VERSION_CODE
        if (latestCode > currentCode && apkUrl.isNotBlank()) {
          AppUpdateInfo(
            versionCode = latestCode,
            versionName = latestName,
            changelog = changelog,
            downloadUrl = apkUrl,
            isForceUpdate = force
          )
        } else {
          null
        }
      } else {
        null
      }
    }.getOrNull()
  }

  fun startApkDownloadAndInstall(context: Context, updateInfo: AppUpdateInfo): Long {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
      ?: run {
        Toast.makeText(context, "DownloadManager unavailable on this device", Toast.LENGTH_SHORT).show()
        return -1L
      }

    val uri = Uri.parse(updateInfo.downloadUrl)
    val fileName = "CineVault_v${updateInfo.versionName}.apk"

    val request = DownloadManager.Request(uri).apply {
      setTitle("CineVault Update v${updateInfo.versionName}")
      setDescription("Downloading latest official CineVault APK release...")
      setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
      setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
      setMimeType("application/vnd.android.package-archive")
    }

    val downloadId = downloadManager.enqueue(request)
    Toast.makeText(context, "Downloading CineVault v${updateInfo.versionName}...", Toast.LENGTH_SHORT).show()

    // Register receiver to trigger APK installer upon completion
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L) ?: -1L
        if (id == downloadId) {
          ctx?.unregisterReceiver(this)
          launchApkInstaller(ctx ?: context, fileName)
        }
      }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      context.registerReceiver(
        receiver,
        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
        Context.RECEIVER_NOT_EXPORTED
      )
    } else {
      context.registerReceiver(
        receiver,
        IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
      )
    }

    return downloadId
  }

  private fun launchApkInstaller(context: Context, fileName: String) {
    runCatching {
      val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
      if (file.exists()) {
        val fileUri: Uri = try {
          androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
          )
        } catch (e: Exception) {
          Uri.fromFile(file)
        }

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
          setDataAndType(fileUri, "application/vnd.android.package-archive")
          flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        context.startActivity(installIntent)
      } else {
        Toast.makeText(context, "Update download completed in Downloads folder", Toast.LENGTH_LONG).show()
      }
    }.onFailure {
      Toast.makeText(context, "Ready to install: Open $fileName in Downloads", Toast.LENGTH_LONG).show()
    }
  }
}

/**
 * In-App OTA Update Dialog adhering to Material 3 Design.
 */
@Composable
fun AppUpdateDialog(
  updateInfo: AppUpdateInfo,
  onDownloadAndInstall: () -> Unit,
  onDismiss: () -> Unit,
  isDownloading: Boolean = false,
  modifier: Modifier = Modifier
) {
  AlertDialog(
    onDismissRequest = {
      if (!updateInfo.isForceUpdate && !isDownloading) onDismiss()
    },
    modifier = modifier.testTag("app_update_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = CircleShape,
          modifier = Modifier.size(40.dp)
        ) {
          Icon(
            imageVector = Icons.Default.SystemUpdate,
            contentDescription = "App Update",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(8.dp)
          )
        }
        Column {
          Text(
            text = "Update Available",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "CineVault v${updateInfo.versionName} (Build ${updateInfo.versionCode})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "What's New in this Release:",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold
        )
        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          shape = MaterialTheme.shapes.medium,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = updateInfo.changelog,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
          )
        }

        if (isDownloading) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(top = 8.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Downloading APK in background...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDownloadAndInstall,
        enabled = !isDownloading,
        modifier = Modifier.testTag("confirm_update_button")
      ) {
        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(if (isDownloading) "Downloading..." else "Update Now")
      }
    },
    dismissButton = {
      if (!updateInfo.isForceUpdate) {
        TextButton(
          onClick = onDismiss,
          enabled = !isDownloading,
          modifier = Modifier.testTag("dismiss_update_button")
        ) {
          Text("Later")
        }
      }
    }
  )
}
