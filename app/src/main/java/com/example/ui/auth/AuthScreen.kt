package com.example.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onUnauthenticated: () -> Unit,
  scope: CoroutineScope
) {
  if (Firebase.auth.currentUser != null) {
    onAuthSuccess()
    return
  }
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onUnauthenticated()
    return
  }

  val googleIdOption = GetGoogleIdOption.Builder()
    .setFilterByAuthorizedAccounts(true)
    .setServerClientId(clientId)
    .setAutoSelectEnabled(true)
    .build()

  val request = GetCredentialRequest.Builder()
    .addCredentialOption(googleIdOption)
    .build()

  scope.launch {
    try {
      val result = credentialManager.getCredential(context, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onUnauthenticated()
      }
    } catch (e: Exception) {
      onUnauthenticated()
    }
  }
}

fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope,
  onAuthCancelled: () -> Unit = {}
) {
  val clientId = try {
    context.getString(R.string.default_web_client_id)
  } catch (e: Exception) {
    onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
    return
  }

  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder()
    .addCredentialOption(signInOption)
    .build()

  scope.launch {
    try {
      val activityContext = context as? Activity ?: context
      val result = credentialManager.getCredential(activityContext, request)
      val credential = result.credential
      if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
        val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
        Firebase.auth.signInWithCredential(authCredential).await()
        onAuthSuccess()
      } else {
        onAuthError("Unexpected credential type")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In cancelled or dismissed: ${e.message}", e)
      onAuthCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onAuthError(e.localizedMessage ?: "Sign in failed")
    }
  }
}

fun signOut(
  context: Context,
  credentialManager: CredentialManager,
  onSignOutComplete: () -> Unit,
  scope: CoroutineScope
) {
  Firebase.auth.signOut()
  scope.launch {
    try {
      credentialManager.clearCredentialState(ClearCredentialStateRequest())
    } catch (e: Exception) {
      Log.e("Auth", "Failed to clear credential state", e)
    } finally {
      onSignOutComplete()
    }
  }
}

@Composable
fun AuthScreen(
  onAuthSuccess: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = coroutineScope
    )
  }

  val triggerGoogleSignIn = {
    errorMessage = null
    isLoading = true
    onGoogleSignInClicked(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {
        isLoading = false
        onAuthSuccess()
      },
      onAuthError = { msg ->
        isLoading = false
        errorMessage = msg
      },
      scope = coroutineScope,
      onAuthCancelled = {
        isLoading = false
      }
    )
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF080D18))
      .windowInsetsPadding(WindowInsets.safeDrawing),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 440.dp)
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
      // CineVault Brand Header
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = Color(0xFFE50914),
          modifier = Modifier.size(68.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            androidx.compose.material3.Icon(
              imageVector = Icons.Filled.Movie,
              contentDescription = "CineVault Cinema",
              tint = Color.White,
              modifier = Modifier.size(38.dp)
            )
          }
        }
        Text(
          text = "CineVault Cinema",
          style = MaterialTheme.typography.headlineMedium,
          color = Color.White,
          fontWeight = FontWeight.Black
        )
        Text(
          text = "Official 4K Cinema • Cross-Device Library & TV Sync",
          style = MaterialTheme.typography.bodyMedium,
          color = Color.White.copy(alpha = 0.7f),
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }

      // Direct Google Sign-In Card (Only Google Sign-In)
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF111827),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = "Welcome",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Sign in with your Google account to sync your watchlist, ratings, and streaming library seamlessly across Mobile & Android TV.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.65f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )

          if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator(
              color = Color(0xFFE50914),
              modifier = Modifier.size(36.dp)
            )
          } else {
            androidx.compose.material3.Button(
              onClick = triggerGoogleSignIn,
              colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1F1F1F)
              ),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("sign_in_with_google")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
              ) {
                // Google "G" 4-color icon
                GoogleGIconCanvas(modifier = Modifier.size(20.dp))
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.size(12.dp))
                Text(
                  text = "Sign in with Google",
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFF1F1F1F)
                )
              }
            }
          }

          if (errorMessage != null) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0x33FF4444),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = errorMessage.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF6B6B),
                modifier = Modifier.padding(10.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      }

      // Android TV QR Auto-Login Card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF111827),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("tv_qr_login_card")
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          TvQrCodeMatrixCanvas(
            codeSeed = "CINEVAULT-TV-LOGIN-4K",
            modifier = Modifier
              .size(88.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color.White)
              .padding(8.dp)
              .testTag("tv_qr_code_canvas")
          )

          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = "📺 Android TV QR Auto-Login",
              style = MaterialTheme.typography.titleSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Scan QR from Mobile app to auto-login on Android TV & sync your watchlist.",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.65f)
            )
            Surface(
              color = Color(0xFF3880FF).copy(alpha = 0.18f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "TV Code: CV-4098-TV",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF70A1FF),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun GoogleGIconCanvas(modifier: Modifier = Modifier) {
  androidx.compose.foundation.Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = w * 0.18f

    // Blue horizontal bar
    drawLine(
      color = Color(0xFF4285F4),
      start = androidx.compose.ui.geometry.Offset(w * 0.45f, h * 0.5f),
      end = androidx.compose.ui.geometry.Offset(w * 0.95f, h * 0.5f),
      strokeWidth = strokeW,
      cap = androidx.compose.ui.graphics.StrokeCap.Round
    )

    // Red arc (top)
    drawArc(
      color = Color(0xFFEA4335),
      startAngle = -45f,
      sweepAngle = -135f,
      useCenter = false,
      topLeft = androidx.compose.ui.geometry.Offset(strokeW / 2, strokeW / 2),
      size = androidx.compose.ui.geometry.Size(w - strokeW, h - strokeW),
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
    )

    // Yellow arc (left)
    drawArc(
      color = Color(0xFFFBBC05),
      startAngle = -180f,
      sweepAngle = -90f,
      useCenter = false,
      topLeft = androidx.compose.ui.geometry.Offset(strokeW / 2, strokeW / 2),
      size = androidx.compose.ui.geometry.Size(w - strokeW, h - strokeW),
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
    )

    // Green arc (bottom)
    drawArc(
      color = Color(0xFF34A853),
      startAngle = 90f,
      sweepAngle = -135f,
      useCenter = false,
      topLeft = androidx.compose.ui.geometry.Offset(strokeW / 2, strokeW / 2),
      size = androidx.compose.ui.geometry.Size(w - strokeW, h - strokeW),
      style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeW)
    )
  }
}

@Composable
fun TvQrCodeMatrixCanvas(
  codeSeed: String,
  modifier: Modifier = Modifier
) {
  androidx.compose.foundation.Canvas(modifier = modifier) {
    val grid = 15
    val cellW = size.width / grid.toFloat()
    val cellH = size.height / grid.toFloat()
    val seedHash = kotlin.math.abs(codeSeed.hashCode())

    fun isFinderPattern(r: Int, c: Int): Boolean {
      val topLeft = r in 0..4 && c in 0..4
      val topRight = r in 0..4 && c in (grid - 5) until grid
      val bottomLeft = r in (grid - 5) until grid && c in 0..4
      return topLeft || topRight || bottomLeft
    }

    fun isFinderFilled(r: Int, c: Int): Boolean {
      val lr = when {
        r in 0..4 -> r
        r >= grid - 5 -> r - (grid - 5)
        else -> 0
      }
      val lc = when {
        c in 0..4 -> c
        c >= grid - 5 -> c - (grid - 5)
        else -> 0
      }
      val isOuterRing = lr == 0 || lr == 4 || lc == 0 || lc == 4
      val isCenterDot = lr == 2 && lc == 2
      return isOuterRing || isCenterDot
    }

    for (r in 0 until grid) {
      for (c in 0 until grid) {
        val filled = if (isFinderPattern(r, c)) {
          isFinderFilled(r, c)
        } else {
          ((seedHash shr ((r * 3 + c * 5) % 24)) xor (r * 7 + c * 11)) % 2 == 0
        }
        if (filled) {
          drawRect(
            color = Color(0xFF0A0E17),
            topLeft = androidx.compose.ui.geometry.Offset(c * cellW, r * cellH),
            size = androidx.compose.ui.geometry.Size(cellW * 0.92f, cellH * 0.92f)
          )
        }
      }
    }
  }
}
