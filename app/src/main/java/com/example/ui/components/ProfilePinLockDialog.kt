package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * 4-Digit Secure PIN Lock Dialog for Netflix-style Protected and Adult Profiles.
 * Prevents unauthorized switching to restricted profiles with on-screen numpad,
 * auto-validation on the 4th digit, and animated shake feedback on incorrect PIN.
 */
@Composable
fun ProfilePinLockDialog(
  profile: UserProfile,
  onPinVerified: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var enteredPin by remember { mutableStateOf("") }
  var isError by remember { mutableStateOf(false) }
  val shakeOffset = remember { Animatable(0f) }
  val coroutineScope = rememberCoroutineScope()

  fun triggerShake() {
    coroutineScope.launch {
      isError = true
      shakeOffset.animateTo(18f, animationSpec = tween(50))
      shakeOffset.animateTo(-18f, animationSpec = tween(50))
      shakeOffset.animateTo(12f, animationSpec = tween(50))
      shakeOffset.animateTo(-12f, animationSpec = tween(50))
      shakeOffset.animateTo(0f, animationSpec = tween(50))
      enteredPin = ""
    }
  }

  fun handleDigitInput(digit: String) {
    if (enteredPin.length < 4) {
      val next = enteredPin + digit
      enteredPin = next
      isError = false
      if (next.length == 4) {
        if (next == profile.pinCode || profile.pinCode.isBlank()) {
          onPinVerified()
        } else {
          triggerShake()
        }
      }
    }
  }

  fun handleBackspace() {
    if (enteredPin.isNotEmpty()) {
      enteredPin = enteredPin.dropLast(1)
      isError = false
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier.testTag("profile_pin_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = CircleShape,
          modifier = Modifier.size(36.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "PIN Lock",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
        Column {
          Text(
            text = "Profile PIN Lock",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${profile.avatarEmoji} ${profile.name}",
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
          .offset { IntOffset(shakeOffset.value.roundToInt(), 0) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Enter 4-digit PIN to access this protected profile",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )

        // 4 Visual PIN Indicator Circles
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(vertical = 8.dp)
        ) {
          repeat(4) { idx ->
            val isFilled = idx < enteredPin.length
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                  when {
                    isError -> MaterialTheme.colorScheme.error
                    isFilled -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                  }
                )
                .border(
                  width = 2.dp,
                  color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFilled -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.outline
                  },
                  shape = CircleShape
                )
                .testTag("pin_circle_$idx")
            )
          }
        }

        AnimatedVisibility(visible = isError) {
          Text(
            text = "Incorrect PIN. Please try again.",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }

        // On-Screen TV & Touch Friendly Keypad (1 to 9, Clear, 0, Backspace)
        Column(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.fillMaxWidth()
        ) {
          val keypadRows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "⌫")
          )

          keypadRows.forEach { row ->
            Row(
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              row.forEach { key ->
                Surface(
                  onClick = {
                    when (key) {
                      "C" -> {
                        enteredPin = ""
                        isError = false
                      }
                      "⌫" -> handleBackspace()
                      else -> handleDigitInput(key)
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  color = if (key == "C" || key == "⌫") {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                  } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                  },
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("pin_key_$key")
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    if (key == "⌫") {
                      Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        modifier = Modifier.size(20.dp)
                      )
                    } else {
                      Text(
                        text = key,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("pin_cancel_button")
      ) {
        Text("Cancel")
      }
    }
  )
}
