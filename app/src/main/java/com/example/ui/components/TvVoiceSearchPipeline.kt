package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Common voice search samples for 10-foot Leanback testing on Android TV
 * or environments without physical microphone hardware.
 */
val TV_VOICE_SEARCH_SAMPLES = listOf(
  "Kalki 2898 AD",
  "Shah Rukh Khan action",
  "Christopher Nolan",
  "Stree 2",
  "Romantic comedy dramas",
  "Interstellar Sci-Fi",
  "Prabhas movies",
  "Marvel Avengers"
)

enum class VoiceSearchCategory(val label: String, val badgeColor: Color) {
  MOVIE("Movie Title", Color(0xFF3B82F6)),
  ACTOR("Actor Filmography", Color(0xFF10B981)),
  DIRECTOR("Director", Color(0xFF8B5CF6)),
  GENRE("Genre / Category", Color(0xFFF59E0B)),
  UNIVERSAL("Global Search", Color(0xFFEC4899))
}

fun detectVoiceQueryCategory(query: String): VoiceSearchCategory {
  val clean = query.lowercase().trim()
  return when {
    clean.contains("actor") || clean.contains("khan") || clean.contains("prabhas") ||
      clean.contains("deepika") || clean.contains("kapoor") || clean.contains("bhatt") ||
      clean.contains("tom") || clean.contains("dicaprio") || clean.contains("bale") -> VoiceSearchCategory.ACTOR
    clean.contains("director") || clean.contains("nolan") || clean.contains("ashwin") ||
      clean.contains("rajamouli") || clean.contains("vanga") || clean.contains("tarantino") -> VoiceSearchCategory.DIRECTOR
    clean.contains("romantic") || clean.contains("action") || clean.contains("comedy") ||
      clean.contains("horror") || clean.contains("sci-fi") || clean.contains("drama") ||
      clean.contains("thriller") || clean.contains("mythology") -> VoiceSearchCategory.GENRE
    clean.isNotBlank() -> VoiceSearchCategory.MOVIE
    else -> VoiceSearchCategory.UNIVERSAL
  }
}

/**
 * Controller for Native TV Speech-To-Text Recognition.
 */
class TvSpeechRecognizerController(
  private val context: Context,
  private val onResultReceived: (String) -> Unit,
  private val onStatusUpdate: (String) -> Unit,
  private val onRmsUpdate: (Float) -> Unit,
  private val onErrorReceived: (String) -> Unit
) {
  private var speechRecognizer: SpeechRecognizer? = null

  fun startListening() {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
      onErrorReceived("Device Speech Recognition service is not available. Please use TV presets or remote voice assistant.")
      return
    }

    destroy()
    try {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
        setRecognitionListener(object : RecognitionListener {
          override fun onReadyForSpeech(params: Bundle?) {
            onStatusUpdate("🎤 Listening... Speak movie name, actor, or genre now")
          }

          override fun onBeginningOfSpeech() {
            onStatusUpdate("🔊 Hearing audio from TV mic...")
          }

          override fun onRmsChanged(rmsdB: Float) {
            val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.1f, 1.0f)
            onRmsUpdate(normalized)
          }

          override fun onBufferReceived(buffer: ByteArray?) {}

          override fun onEndOfSpeech() {
            onStatusUpdate("⚡ Transcribing speech to text...")
          }

          override fun onError(error: Int) {
            val errStr = when (error) {
              SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
              SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
              SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Record audio permission required"
              SpeechRecognizer.ERROR_NETWORK -> "Network connection required for speech service"
              SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network speech recognition timeout"
              SpeechRecognizer.ERROR_NO_MATCH -> "No matching movie or voice input heard"
              SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy, please retry"
              SpeechRecognizer.ERROR_SERVER -> "Server speech error"
              SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected within time limit"
              else -> "Speech recognition error code: $error"
            }
            onErrorReceived(errStr)
          }

          override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val spoken = matches?.firstOrNull()?.trim()
            if (!spoken.isNullOrBlank()) {
              onResultReceived(spoken)
            } else {
              onErrorReceived("Could not parse speech. Try tapping a sample below.")
            }
          }

          override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!partial.isNullOrBlank()) {
              onStatusUpdate("Transcribing: \"$partial\"")
            }
          }

          override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
          putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
          putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
          putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
          putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
          putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        startListening(intent)
      }
    } catch (e: Exception) {
      Log.e("TvVoiceSearch", "Failed to start speech recognizer", e)
      onErrorReceived(e.localizedMessage ?: "Failed to start TV microphone speech engine")
    }
  }

  fun stopListening() {
    try {
      speechRecognizer?.stopListening()
    } catch (_: Exception) {}
  }

  fun destroy() {
    try {
      speechRecognizer?.destroy()
      speechRecognizer = null
    } catch (_: Exception) {}
  }
}

/**
 * TV Leanback Voice Search Overlay Dialog
 */
@Composable
fun TvVoiceSearchOverlay(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onApplyVoiceQuery: (String) -> Unit
) {
  if (!isOpen) return

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var spokenResult by remember { mutableStateOf("") }
  var statusMessage by remember { mutableStateOf("🎤 Listening... Speak movie, actor, director, or genre") }
  var audioRmsLevel by remember { mutableFloatStateOf(0.4f) }
  var isAudioPermissionGranted by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    )
  }
  var isListeningActive by remember { mutableStateOf(false) }

  // Fallback to Native Leanback Speech Intent
  val leanbackSpeechIntentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result: ActivityResult ->
    if (result.resultCode == Activity.RESULT_OK) {
      val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
      val query = matches?.firstOrNull()?.trim()
      if (!query.isNullOrBlank()) {
        spokenResult = query
        statusMessage = "✅ Recognized: \"$query\""
        onApplyVoiceQuery(query)
        onDismiss()
      }
    }
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    isAudioPermissionGranted = granted
    if (!granted) {
      statusMessage = "Microphone access denied. You can select any voice query preset below:"
    }
  }

  val controller = remember {
    TvSpeechRecognizerController(
      context = context,
      onResultReceived = { query ->
        spokenResult = query
        statusMessage = "✅ Recognized: \"$query\""
        isListeningActive = false
        coroutineScope.launch {
          delay(400)
          onApplyVoiceQuery(query)
          onDismiss()
        }
      },
      onStatusUpdate = { status ->
        statusMessage = status
      },
      onRmsUpdate = { rms ->
        audioRmsLevel = rms
      },
      onErrorReceived = { err ->
        isListeningActive = false
        statusMessage = err
      }
    )
  }

  DisposableEffect(Unit) {
    onDispose {
      controller.destroy()
    }
  }

  fun triggerListening() {
    if (!isAudioPermissionGranted) {
      permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
      return
    }
    isListeningActive = true
    spokenResult = ""
    statusMessage = "🎤 Initializing TV remote microphone..."
    controller.startListening()
  }

  LaunchedEffect(isAudioPermissionGranted) {
    if (isAudioPermissionGranted) {
      triggerListening()
    }
  }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse_anim")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  AlertDialog(
    onDismissRequest = {
      controller.stopListening()
      onDismiss()
    },
    modifier = Modifier
      .fillMaxWidth(0.92f)
      .testTag("tv_voice_search_dialog"),
    shape = RoundedCornerShape(24.dp),
    containerColor = Color(0xFF111827),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Tv,
            contentDescription = "Android TV Voice Search",
            tint = Color(0xFFE50914),
            modifier = Modifier.size(24.dp)
          )
          Text(
            text = "TV Voice Search • Speech-to-Text",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
          )
        }
        IconButton(
          onClick = {
            controller.stopListening()
            onDismiss()
          },
          modifier = Modifier.testTag("close_voice_search_dialog_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close Voice Search",
            tint = Color.LightGray
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Visual Microphone Pulse HUD
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier
            .size(120.dp)
            .padding(8.dp)
        ) {
          // Glowing outer pulse ring
          Box(
            modifier = Modifier
              .size((80 * (if (isListeningActive) pulseScale else 1.0f)).dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  colors = listOf(
                    Color(0xFFE50914).copy(alpha = 0.45f),
                    Color(0xFFE50914).copy(alpha = 0.05f)
                  )
                )
              )
          )
          // Inner mic button
          Surface(
            onClick = {
              if (isListeningActive) {
                controller.stopListening()
                isListeningActive = false
                statusMessage = "Microphone paused. Tap to listen again."
              } else {
                triggerListening()
              }
            },
            shape = CircleShape,
            color = if (isListeningActive) Color(0xFFE50914) else Color(0xFF1F2937),
            modifier = Modifier
              .size(64.dp)
              .testTag("tv_voice_mic_trigger_btn")
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isListeningActive) Icons.Default.Mic else Icons.Default.MicNone,
                contentDescription = "Microphone",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }

        // Live Audio Equalizer Waveform indicator
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.height(24.dp)
        ) {
          listOf(0.4f, 0.7f, 1.0f, 0.8f, 0.5f, 0.9f, 0.6f).forEach { mult ->
            val barHeight = if (isListeningActive) (8 + (16 * audioRmsLevel * mult)).dp else 4.dp
            Box(
              modifier = Modifier
                .width(4.dp)
                .height(barHeight)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isListeningActive) Color(0xFF38BDF8) else Color.DarkGray)
            )
          }
        }

        // Recognized or Live Status Text
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = if (spokenResult.isNotBlank()) "\"$spokenResult\"" else statusMessage,
            style = MaterialTheme.typography.titleMedium,
            color = if (spokenResult.isNotBlank()) Color(0xFF34D399) else Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )

          if (spokenResult.isNotBlank()) {
            val cat = detectVoiceQueryCategory(spokenResult)
            Surface(
              color = cat.badgeColor.copy(alpha = 0.25f),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, cat.badgeColor)
            ) {
              Text(
                text = "Target: ${cat.label}",
                style = MaterialTheme.typography.labelSmall,
                color = cat.badgeColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }

        // TV Remote Assistant Launcher Button
        OutlinedButton(
          onClick = {
            try {
              val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak movie, actor, director, or genre...")
              }
              leanbackSpeechIntentLauncher.launch(intent)
            } catch (e: Exception) {
              statusMessage = "Google TV Assistant voice overlay unavailable on this device."
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("launch_system_tv_speech_intent_btn"),
          colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color(0xFF60A5FA)
          )
        ) {
          Icon(imageVector = Icons.Default.SettingsRemote, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Use Google TV Remote Mic Assistant", fontSize = 13.sp)
        }

        // Quick Spoken TV Presets (For Remote D-Pad Navigation & Emulator Testing)
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Text(
            text = "TV Remote Voice Presets (1-Click Speak Simulation):",
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray,
            fontWeight = FontWeight.SemiBold
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            TV_VOICE_SEARCH_SAMPLES.forEach { sample ->
              Surface(
                onClick = {
                  spokenResult = sample
                  statusMessage = "🎤 Simulating speech input: \"$sample\""
                  onApplyVoiceQuery(sample)
                  onDismiss()
                },
                color = Color(0xFF1E293B),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.testTag("tv_voice_preset_${sample.lowercase().replace(" ", "_")}")
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(14.dp)
                  )
                  Text(
                    text = sample,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {
      if (spokenResult.isNotBlank()) {
        Button(
          onClick = {
            onApplyVoiceQuery(spokenResult)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE50914)),
          modifier = Modifier.testTag("confirm_voice_query_btn")
        ) {
          Icon(imageVector = Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Search \"$spokenResult\"")
        }
      } else {
        FilledTonalButton(
          onClick = { triggerListening() },
          modifier = Modifier.testTag("retry_voice_listen_btn")
        ) {
          Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Listen Again")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = {
        controller.stopListening()
        onDismiss()
      }) {
        Text("Cancel", color = Color.LightGray)
      }
    }
  )
}
