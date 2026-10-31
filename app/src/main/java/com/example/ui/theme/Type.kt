package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OutfitFontFamily = FontFamily(
  Font(R.font.outfit, FontWeight.Normal),
  Font(R.font.outfit, FontWeight.Medium),
  Font(R.font.outfit, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
  Font(R.font.plus_jakarta_sans, FontWeight.Normal),
  Font(R.font.plus_jakarta_sans, FontWeight.Medium),
  Font(R.font.plus_jakarta_sans, FontWeight.SemiBold)
)

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 36.sp,
    lineHeight = 42.sp,
    letterSpacing = (-0.5).sp
  ),
  headlineLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 28.sp,
    lineHeight = 34.sp
  ),
  headlineMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp
  ),
  titleLarge = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 19.sp,
    lineHeight = 25.sp
  ),
  titleMedium = TextStyle(
    fontFamily = OutfitFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp,
    lineHeight = 22.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = PlusJakartaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = PlusJakartaFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
  ),
  labelLarge = TextStyle(
    fontFamily = PlusJakartaFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 13.sp,
    lineHeight = 18.sp
  ),
  labelMedium = TextStyle(
    fontFamily = PlusJakartaFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 16.sp
  )
)
