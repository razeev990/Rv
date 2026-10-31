package com.example.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Upcoming
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ============================================================================
// 1. NEUMORPHISM CORE ENGINE (Color #E0E5EC + Dual Shadows + Inset Pressed Mode)
// ============================================================================

val NeumorphicBaseColor = Color(0xFFE0E5EC)
val NeumorphicLightShadow = Color(0xFFFFFFFF)
val NeumorphicDarkShadow = Color(0xFFA3B1C6)
val NeumorphicAccentBlue = Color(0xFF0984E3)
val NeumorphicTextPrimary = Color(0xFF2D3436)
val NeumorphicTextSecondary = Color(0xFF747D8C)

/**
 * Custom Jetpack Compose modifier that renders true dual-shadow Neumorphism on Android:
 * - When [isInset] is false (Raised / Extruded state):
 *   Draws a top-left white highlight shadow and a bottom-right dark gray (#A3B1C6) shadow
 *   behind an #E0E5EC surface.
 * - When [isInset] is true (Sunken input field or Pressed button state):
 *   Draws an inner sunken shadow (dark top-left inner rim + white bottom-right inner rim).
 */
fun Modifier.neumorphicSurface(
  cornerRadius: Dp = 24.dp,
  shadowOffset: Dp = 7.dp,
  blurRadius: Dp = 12.dp,
  isInset: Boolean = false,
  baseColor: Color = NeumorphicBaseColor
): Modifier = if (!isInset) {
  this
    .drawBehind {
      val radiusPx = cornerRadius.toPx()
      val offsetPx = shadowOffset.toPx()
      val blurPx = blurRadius.toPx().coerceAtLeast(1f)

      drawIntoCanvas { canvas ->
        // 1) Top-Left Light Shadow (White)
        val lightPaint = Paint().also { p ->
          val fw = p.asFrameworkPaint()
          fw.isAntiAlias = true
          fw.color = NeumorphicLightShadow.copy(alpha = 0.92f).toArgb()
          fw.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
          left = -offsetPx,
          top = -offsetPx,
          right = size.width - offsetPx * 0.4f,
          bottom = size.height - offsetPx * 0.4f,
          radiusX = radiusPx,
          radiusY = radiusPx,
          paint = lightPaint
        )

        // 2) Bottom-Right Dark Shadow (#A3B1C6)
        val darkPaint = Paint().also { p ->
          val fw = p.asFrameworkPaint()
          fw.isAntiAlias = true
          fw.color = NeumorphicDarkShadow.copy(alpha = 0.75f).toArgb()
          fw.maskFilter = BlurMaskFilter(blurPx, BlurMaskFilter.Blur.NORMAL)
        }
        canvas.drawRoundRect(
          left = offsetPx * 0.4f,
          top = offsetPx * 0.4f,
          right = size.width + offsetPx,
          bottom = size.height + offsetPx,
          radiusX = radiusPx,
          radiusY = radiusPx,
          paint = darkPaint
        )
      }

      // 3) Same Base Color (#E0E5EC) Fill
      drawRoundRect(
        color = baseColor,
        cornerRadius = CornerRadius(radiusPx, radiusPx)
      )
    }
} else {
  this
    .clip(RoundedCornerShape(cornerRadius))
    .background(baseColor, RoundedCornerShape(cornerRadius))
    .drawWithContent {
      drawContent()
      val radiusPx = cornerRadius.toPx()
      val strokeW = 3.5.dp.toPx()

      // Inner sunken shadow gradient (Dark top-left -> Light bottom-right)
      drawRoundRect(
        brush = Brush.linearGradient(
          colors = listOf(
            NeumorphicDarkShadow.copy(alpha = 0.78f),
            NeumorphicDarkShadow.copy(alpha = 0.25f),
            Color.Transparent,
            NeumorphicLightShadow.copy(alpha = 0.9f)
          ),
          start = Offset(0f, 0f),
          end = Offset(size.width, size.height)
        ),
        cornerRadius = CornerRadius(radiusPx, radiusPx),
        style = Stroke(width = strokeW * 1.6f)
      )

      // Subtle inner top-left shadow overlay for realistic depth
      drawRoundRect(
        brush = Brush.verticalGradient(
          colors = listOf(
            NeumorphicDarkShadow.copy(alpha = 0.24f),
            Color.Transparent,
            NeumorphicLightShadow.copy(alpha = 0.35f)
          )
        ),
        cornerRadius = CornerRadius(radiusPx, radiusPx)
      )
    }
}

@Composable
fun NeumorphicInsetTextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  isPassword: Boolean = false,
  passwordVisible: Boolean = false,
  onTogglePasswordVisibility: (() -> Unit)? = null,
  trailingContent: (@Composable () -> Unit)? = null,
  testTag: String = ""
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(50.dp)
      .neumorphicSurface(
        cornerRadius = 25.dp,
        isInset = true
      )
      .padding(horizontal = 20.dp)
      .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
    contentAlignment = Alignment.CenterStart
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Box(
        modifier = Modifier.weight(1f),
        contentAlignment = Alignment.CenterStart
      ) {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            style = MaterialTheme.typography.bodyMedium,
            color = NeumorphicTextSecondary
          )
        }
        BasicTextField(
          value = value,
          onValueChange = onValueChange,
          singleLine = true,
          textStyle = TextStyle(
            color = NeumorphicTextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          ),
          cursorBrush = SolidColor(NeumorphicAccentBlue),
          visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
          } else {
            VisualTransformation.None
          },
          modifier = Modifier.fillMaxWidth()
        )
      }

      if (isPassword && onTogglePasswordVisibility != null) {
        IconButton(
          onClick = onTogglePasswordVisibility,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = "Toggle Password Visibility",
            tint = NeumorphicTextSecondary,
            modifier = Modifier.size(18.dp)
          )
        }
      } else if (trailingContent != null) {
        trailingContent()
      }
    }
  }
}

@Composable
fun NeumorphicPressableButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  showBlueAccentBar: Boolean = true,
  testTag: String = ""
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(52.dp)
      .neumorphicSurface(
        cornerRadius = 26.dp,
        shadowOffset = 6.dp,
        blurRadius = 11.dp,
        isInset = isPressed
      )
      .clip(RoundedCornerShape(26.dp))
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick
      )
      .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.titleSmall,
      fontWeight = FontWeight.ExtraBold,
      color = NeumorphicAccentBlue,
      letterSpacing = 0.6.sp
    )

    if (showBlueAccentBar) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth(0.68f)
          .height(3.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(NeumorphicAccentBlue)
      )
    }
  }
}

@Composable
fun NeumorphicCircleIconButton(
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  size: Dp = 44.dp,
  testTag: String = ""
) {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  Box(
    modifier = modifier
      .size(size)
      .neumorphicSurface(
        cornerRadius = size / 2,
        shadowOffset = 5.dp,
        blurRadius = 9.dp,
        isInset = isPressed
      )
      .clip(CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick
      )
      .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = NeumorphicAccentBlue,
      modifier = Modifier.size(size * 0.46f)
    )
  }
}

/**
 * Complete Neumorphic Login ("Welcome") & Sign Up ("Create Account") Form Card
 * matching the user's CodeByGaurav reference screenshots (bg1, bg2, bg3 circles,
 * #E0E5EC soft UI, dual shadows, inset input fields, email suggestion dropdown,
 * and smooth toggle between Login and Create Account).
 */
@Composable
fun NeumorphicAuthFormSection(
  onSubmitAuth: (email: String, name: String, isSignUp: Boolean) -> Unit,
  onGoogleSignInClick: () -> Unit,
  isLoading: Boolean = false,
  errorMessage: String? = null,
  modifier: Modifier = Modifier
) {
  var isCreateAccountMode by remember { mutableStateOf(false) }
  var fullName by remember { mutableStateOf("") }
  var emailAddress by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var rememberMe by remember { mutableStateOf(true) }
  var showEmailSuggestions by remember { mutableStateOf(false) }
  var localValidationMessage by remember { mutableStateOf<String?>(null) }

  val suggestedEmails = remember {
    listOf(
      "gaurav@gmail.com",
      "dddd@gmail.com",
      "gaurav123@gmail.com"
    )
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(36.dp))
      .background(NeumorphicBaseColor)
      .padding(vertical = 28.dp, horizontal = 18.dp)
      .testTag("neumorphic_auth_container"),
    contentAlignment = Alignment.Center
  ) {
    // Decorative Floating Neumorphic Circles (bg-circle bg1, bg2, bg3 from reference HTML/CSS)
    Box(
      modifier = Modifier
        .size(126.dp)
        .align(Alignment.TopStart)
        .offset(x = (-18).dp, y = (-14).dp)
        .neumorphicSurface(
          cornerRadius = 63.dp,
          shadowOffset = 8.dp,
          blurRadius = 14.dp,
          isInset = false
        )
    )

    Box(
      modifier = Modifier
        .size(56.dp)
        .align(Alignment.TopEnd)
        .offset(x = (-6).dp, y = 42.dp)
        .neumorphicSurface(
          cornerRadius = 28.dp,
          shadowOffset = 5.dp,
          blurRadius = 10.dp,
          isInset = false
        )
    )

    Box(
      modifier = Modifier
        .size(104.dp)
        .align(Alignment.BottomEnd)
        .offset(x = 12.dp, y = 12.dp)
        .neumorphicSurface(
          cornerRadius = 52.dp,
          shadowOffset = 7.dp,
          blurRadius = 13.dp,
          isInset = false
        )
    )

    // Main Neumorphic Card (#E0E5EC with Dual Top-Left White & Bottom-Right Dark Shadows)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 380.dp)
        .padding(horizontal = 8.dp, vertical = 10.dp)
        .neumorphicSurface(
          cornerRadius = 32.dp,
          shadowOffset = 10.dp,
          blurRadius = 18.dp,
          isInset = false
        )
        .padding(horizontal = 24.dp, vertical = 28.dp)
        .testTag("neumorphic_form_card")
    ) {
      AnimatedContent(
        targetState = isCreateAccountMode,
        transitionSpec = {
          fadeIn(tween(220)) togetherWith fadeOut(tween(180))
        },
        label = "NeumorphicAuthModeTransition"
      ) { createMode ->
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Circular Neumorphic Top Avatar / Lock Icon
          Box(
            modifier = Modifier
              .size(72.dp)
              .neumorphicSurface(
                cornerRadius = 36.dp,
                shadowOffset = 6.dp,
                blurRadius = 11.dp,
                isInset = false
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (createMode) Icons.Default.Person else Icons.Default.Lock,
              contentDescription = if (createMode) "Create Account" else "Welcome Login",
              tint = NeumorphicAccentBlue,
              modifier = Modifier.size(30.dp)
            )
          }

          // Title & Subtitle
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = if (createMode) "Create Account" else "Welcome",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.ExtraBold,
              color = NeumorphicTextPrimary
            )
            Text(
              text = if (createMode) {
                "Start your journey with us"
              } else {
                "Login to continue your journey"
              },
              style = MaterialTheme.typography.labelMedium,
              color = NeumorphicTextSecondary
            )
          }

          if (createMode) {
            NeumorphicInsetTextField(
              value = fullName,
              onValueChange = {
                fullName = it
                localValidationMessage = null
              },
              placeholder = "Full Name",
              testTag = "neumorphic_fullname_input"
            )
          }

          // Email Address Field + Quick Autocomplete Dropdown (from Screenshot 3)
          Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            NeumorphicInsetTextField(
              value = emailAddress,
              onValueChange = {
                emailAddress = it
                localValidationMessage = null
                showEmailSuggestions = it.isEmpty() || it.length < 12
              },
              placeholder = "Email Address",
              trailingContent = {
                Text(
                  text = "▾",
                  color = NeumorphicTextSecondary,
                  fontSize = 14.sp,
                  modifier = Modifier
                    .clickable { showEmailSuggestions = !showEmailSuggestions }
                    .padding(horizontal = 6.dp)
                    .testTag("neumorphic_email_suggestions_toggle")
                )
              },
              testTag = "neumorphic_email_input"
            )

            AnimatedVisibility(visible = showEmailSuggestions && !createMode) {
              Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                shadowElevation = 6.dp,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 8.dp)
                  .testTag("neumorphic_email_dropdown")
              ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                  suggestedEmails.forEach { suggestion ->
                    Text(
                      text = suggestion,
                      style = MaterialTheme.typography.labelLarge,
                      fontWeight = FontWeight.Bold,
                      color = NeumorphicTextPrimary,
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          emailAddress = suggestion
                          showEmailSuggestions = false
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                  }
                }
              }
            }
          }

          NeumorphicInsetTextField(
            value = password,
            onValueChange = {
              password = it
              localValidationMessage = null
            },
            placeholder = "Password",
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
            testTag = "neumorphic_password_input"
          )

          if (createMode) {
            NeumorphicInsetTextField(
              value = confirmPassword,
              onValueChange = {
                confirmPassword = it
                localValidationMessage = null
              },
              placeholder = "Confirm Password",
              isPassword = true,
              passwordVisible = passwordVisible,
              testTag = "neumorphic_confirm_password_input"
            )
          } else {
            // Remember me row in Login mode
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .clickable { rememberMe = !rememberMe },
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(18.dp)
                  .neumorphicSurface(
                    cornerRadius = 5.dp,
                    shadowOffset = 2.dp,
                    blurRadius = 4.dp,
                    isInset = rememberMe
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (rememberMe) {
                  Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Remember me",
                    tint = NeumorphicAccentBlue,
                    modifier = Modifier.size(12.dp)
                  )
                }
              }
              Text(
                text = "Remember me",
                style = MaterialTheme.typography.labelMedium,
                color = NeumorphicTextSecondary
              )
            }
          }

          val activeError = localValidationMessage ?: errorMessage
          if (!activeError.isNullOrBlank()) {
            Text(
              text = activeError,
              style = MaterialTheme.typography.labelSmall,
              color = Color(0xFFD63031),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(horizontal = 8.dp)
            )
          }

          // Primary Neumorphic Action Button (Login / CREATE ACCOUNT)
          NeumorphicPressableButton(
            text = if (createMode) "CREATE ACCOUNT" else "Login",
            onClick = {
              val cleanEmail = emailAddress.trim().ifBlank { "gaurav@gmail.com" }
              val cleanName = fullName.trim().ifBlank {
                cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
              }
              if (createMode && confirmPassword.isNotBlank() && password != confirmPassword) {
                localValidationMessage = "Passwords do not match"
              } else {
                localValidationMessage = null
                onSubmitAuth(cleanEmail, cleanName, createMode)
              }
            },
            enabled = !isLoading,
            testTag = if (createMode) "neumorphic_create_account_btn" else "neumorphic_login_btn"
          )

          // Google Sign-In Neumorphic Pill Button
          NeumorphicPressableButton(
            text = if (isLoading) "Signing in..." else "Continue with Google",
            onClick = onGoogleSignInClick,
            enabled = !isLoading,
            showBlueAccentBar = false,
            testTag = "google_sign_in_button"
          )

          if (isLoading) {
            CircularProgressIndicator(
              color = NeumorphicAccentBlue,
              strokeWidth = 2.5.dp,
              modifier = Modifier.size(22.dp)
            )
          }

          // Bottom Mode Switcher Row ("Don't have an account? [+]" / "Already have an account? [←]")
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = if (createMode) "Already have an account?" else "Don't have an account?",
              style = MaterialTheme.typography.labelMedium,
              color = NeumorphicTextSecondary
            )

            NeumorphicCircleIconButton(
              icon = if (createMode) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Add,
              contentDescription = if (createMode) "Switch to Login" else "Switch to Create Account",
              onClick = {
                isCreateAccountMode = !createMode
                localValidationMessage = null
                showEmailSuggestions = false
              },
              size = 40.dp,
              testTag = "neumorphic_switch_mode_button"
            )
          }
        }
      }
    }
  }
}

// ============================================================================
// 2. FROSTED GLASSMORPHISM SIDEBAR UI (Matching Screenshot 1: "SideBar UI")
// ============================================================================

enum class GlassSidebarItemKey(
  val title: String,
  val icon: ImageVector,
  val section: String
) {
  HOME("Home", Icons.Default.Home, "MENU"),
  CONTINUE_WATCHING("Continue watching", Icons.Default.PlayArrow, "MENU"),
  MY_LIST("My List", Icons.Default.Add, "MENU"),
  SERIES("Series", Icons.Default.Tv, "MENU"),
  MOVIES("Movies", Icons.Default.Movie, "MENU"),
  GAMES_OFFLINE("Games & Offline", Icons.Default.SportsEsports, "MENU"),
  RELEASES("Releases", Icons.Default.Upcoming, "DISCOVERY"),
  POPULAR("Popular", Icons.Default.Star, "DISCOVERY"),
  SETTINGS_NEUMORPHIC("Settings & TV Sync", Icons.Default.Settings, "GENERAL"),
  LOG_OUT("Log out", Icons.AutoMirrored.Filled.Logout, "GENERAL")
}

/**
 * Frosted Glassmorphism Collapsible Sidebar matching Screenshot 1 ("SideBar UI"):
 * - Translucent dark blurred glass look with 1.dp white/10% border (`rgba(255,255,255,0.12)`)
 *   and `borderRadius = 34.dp`.
 * - Top macOS dots + "SideBar UI" + Bell Notification icon with Red Badge `4` + "Collapse" pill button.
 * - Profile Switcher Card ("Dorian / Active Profile" + "⇄ Switch account").
 * - Rounded dark glass "SEARCH..." bar.
 * - MENU / DISCOVERY / GENERAL sections with bright red (`#FF0000` / `0xFFE50914`) active highlight
 *   and red notification badge on "My List".
 * - Smooth width/slide collapse animation.
 */
@Composable
fun FrostedGlassSidebar(
  activeItem: GlassSidebarItemKey,
  isCollapsed: Boolean,
  onToggleCollapse: () -> Unit,
  activeProfileName: String,
  activeProfileEmoji: String,
  myListBadgeCount: Int,
  notificationBadgeCount: Int = 4,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onSwitchAccountClick: () -> Unit,
  onNotificationClick: () -> Unit,
  onSelectItem: (GlassSidebarItemKey) -> Unit,
  modifier: Modifier = Modifier
) {
  val animatedWidth by animateDpAsState(
    targetValue = if (isCollapsed) 82.dp else 276.dp,
    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
    label = "FrostedSidebarWidth"
  )

  val glassBackgroundBrush = remember {
    Brush.verticalGradient(
      colors = listOf(
        Color(0xFF2B2E37).copy(alpha = 0.88f),
        Color(0xFF1B1D24).copy(alpha = 0.92f),
        Color(0xFF12141A).copy(alpha = 0.95f)
      )
    )
  }

  Box(
    modifier = modifier
      .width(animatedWidth)
      .fillMaxHeight()
      .padding(vertical = 10.dp, horizontal = 8.dp)
      .clip(RoundedCornerShape(34.dp))
      .background(glassBackgroundBrush)
      .border(
        width = 1.dp,
        brush = Brush.verticalGradient(
          colors = listOf(
            Color.White.copy(alpha = 0.24f),
            Color.White.copy(alpha = 0.08f),
            Color.White.copy(alpha = 0.14f)
          )
        ),
        shape = RoundedCornerShape(34.dp)
      )
      .testTag("frosted_glass_sidebar")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = if (isCollapsed) 10.dp else 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Top Window Dots + "SideBar UI" + Bell Badge (4) + Collapse Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(Color(0xFFFF5F56))
          )
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(Color(0xFFFFBD2E))
          )
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(Color(0xFF27C93F))
          )
        }

        if (!isCollapsed) {
          Surface(
            onClick = onToggleCollapse,
            color = Color.White.copy(alpha = 0.92f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.testTag("sidebar_collapse_button")
          ) {
            Text(
              text = "Collapse",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFF991B1B),
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
          }
        }
      }

      // Header Row: "SideBar UI" + Notification Bell with Red Badge (4)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.SpaceBetween
      ) {
        if (!isCollapsed) {
          Text(
            text = "SideBar UI",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Notification Bell with Red Badge '4'
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .clickable(onClick = onNotificationClick)
              .testTag("sidebar_notification_bell"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Notifications,
              contentDescription = "Notifications",
              tint = Color.White.copy(alpha = 0.88f),
              modifier = Modifier.size(20.dp)
            )
            if (notificationBadgeCount > 0) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .offset(x = (-2).dp, y = 2.dp)
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFFF0000)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = notificationBadgeCount.toString(),
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }
          }

          if (isCollapsed) {
            IconButton(
              onClick = onToggleCollapse,
              modifier = Modifier
                .size(28.dp)
                .testTag("sidebar_expand_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Expand Sidebar",
                tint = Color.White
              )
            }
          }
        }
      }

      // 2. Profile Card ("Dorian / Active Profile" + "⇄ Switch account")
      Surface(
        onClick = onSwitchAccountClick,
        color = Color.White.copy(alpha = 0.08f),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("sidebar_switch_account_card")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.16f))
                .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = activeProfileEmoji,
                style = MaterialTheme.typography.titleMedium
              )
            }

            if (!isCollapsed) {
              Column {
                Text(
                  text = activeProfileName,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                  )
                  Text(
                    text = "Switch account",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                  )
                }
              }
            }
          }

          if (!isCollapsed) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = "Switch account",
              tint = Color.White.copy(alpha = 0.6f),
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      // 3. Inline Search Bar ("SEARCH...")
      if (!isCollapsed) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Explore,
            contentDescription = "Search",
            tint = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .height(36.dp)
              .clip(RoundedCornerShape(18.dp))
              .background(Color.White.copy(alpha = 0.08f))
              .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
              .padding(horizontal = 14.dp)
              .testTag("sidebar_search_input"),
            contentAlignment = Alignment.CenterStart
          ) {
            if (searchQuery.isEmpty()) {
              Text(
                text = "SEARCH...",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 1.sp
              )
            }
            BasicTextField(
              value = searchQuery,
              onValueChange = onSearchQueryChange,
              singleLine = true,
              textStyle = TextStyle(
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              ),
              cursorBrush = SolidColor(Color(0xFFFF0000)),
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }

      // 4. MENU Section
      SidebarSectionGroup(
        sectionTitle = "MENU",
        items = listOf(
          GlassSidebarItemKey.HOME,
          GlassSidebarItemKey.CONTINUE_WATCHING,
          GlassSidebarItemKey.MY_LIST,
          GlassSidebarItemKey.SERIES,
          GlassSidebarItemKey.MOVIES,
          GlassSidebarItemKey.GAMES_OFFLINE
        ),
        activeItem = activeItem,
        isCollapsed = isCollapsed,
        myListBadgeCount = myListBadgeCount,
        onSelectItem = onSelectItem
      )

      HorizontalDivider(color = Color.White.copy(alpha = 0.09f))

      // 5. DISCOVERY Section
      SidebarSectionGroup(
        sectionTitle = "DISCOVERY",
        items = listOf(
          GlassSidebarItemKey.RELEASES,
          GlassSidebarItemKey.POPULAR
        ),
        activeItem = activeItem,
        isCollapsed = isCollapsed,
        myListBadgeCount = 0,
        onSelectItem = onSelectItem
      )

      HorizontalDivider(color = Color.White.copy(alpha = 0.09f))

      // 6. GENERAL Section
      SidebarSectionGroup(
        sectionTitle = "GENERAL",
        items = listOf(
          GlassSidebarItemKey.SETTINGS_NEUMORPHIC,
          GlassSidebarItemKey.LOG_OUT
        ),
        activeItem = activeItem,
        isCollapsed = isCollapsed,
        myListBadgeCount = 0,
        onSelectItem = onSelectItem
      )
    }
  }
}

@Composable
private fun SidebarSectionGroup(
  sectionTitle: String,
  items: List<GlassSidebarItemKey>,
  activeItem: GlassSidebarItemKey,
  isCollapsed: Boolean,
  myListBadgeCount: Int,
  onSelectItem: (GlassSidebarItemKey) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    if (!isCollapsed) {
      Text(
        text = sectionTitle,
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.45f),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
      )
    }

    items.forEach { item ->
      val isActive = item == activeItem
      val badgeVal = if (item == GlassSidebarItemKey.MY_LIST) myListBadgeCount else 0

      // Active Menu State: Bright Red (#FF0000 / 0xFFE50914) background with 100% white bold text/icon
      // Inactive Menu State: Transparent background with rgba(255, 255, 255, 0.6) text/icon
      val rowBg = if (isActive) Color(0xFFE50914) else Color.Transparent
      val contentTint = if (isActive) Color.White else Color.White.copy(alpha = 0.6f)

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(rowBg)
          .clickable { onSelectItem(item) }
          .padding(horizontal = if (isCollapsed) 0.dp else 12.dp)
          .testTag("sidebar_item_${item.name.lowercase()}"),
        contentAlignment = if (isCollapsed) Alignment.Center else Alignment.CenterStart
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                  if (isActive) Color.Transparent else Color.White.copy(alpha = 0.06f)
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                tint = contentTint,
                modifier = Modifier.size(16.dp)
              )
            }

            if (!isCollapsed) {
              Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                color = contentTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }

          // Red Notification Badge on "My List" (aligned right, red circle, small white font)
          if (badgeVal > 0 && !isCollapsed) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isActive) Color.White else Color(0xFFFF0000))
                .testTag("sidebar_mylist_badge"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = badgeVal.coerceAtMost(99).toString(),
                color = if (isActive) Color(0xFFFF0000) else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }
          }
        }
      }
    }
  }
}
