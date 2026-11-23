package com.momentum.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ════════════════════════════════════════════════════════════
   Momentum Design System — Organic Intelligence (DESIGN.md v2.0)
   Dark olive surfaces, luminous lime accents, editorial type.
   ════════════════════════════════════════════════════════════ */

private val DarkColors = darkColorScheme(
    primary            = Color(0xFFD7F75B),   // Luminous lime
    onPrimary          = Color(0xFF18200A),
    primaryContainer   = Color(0xFF303B16),
    onPrimaryContainer = Color(0xFFEAF4EE),
    secondary          = Color(0xFF91B8D0),
    onSecondary        = Color(0xFF0B0E08),
    secondaryContainer = Color(0xFF1D2833),
    onSecondaryContainer = Color(0xFFEAF4EE),
    tertiary           = Color(0xFFA9D95B),
    onTertiary         = Color(0xFF0B0E08),
    tertiaryContainer  = Color(0xFF263318),
    onTertiaryContainer = Color(0xFFEAF4EE),
    error              = Color(0xFFE28D80),
    onError            = Color(0xFF40211D),
    errorContainer     = Color(0xFF3A221F),
    onErrorContainer   = Color(0xFFE28D80),
    background         = Color(0xFF0B0E08),
    onBackground       = Color(0xFFF2F3E9),
    surface            = Color(0xFF11160D),
    onSurface          = Color(0xFFF2F3E9),
    surfaceVariant     = Color(0xFF191F12),
    onSurfaceVariant   = Color(0xFFB4B9A4),
    surfaceTint        = Color(0xFFD7F75B),
    outline            = Color(0xFF343C27),
    outlineVariant     = Color(0xFF262D1B),
    inverseSurface     = Color(0xFFF2F3E9),
    inverseOnSurface   = Color(0xFF0B0E08),
    inversePrimary     = Color(0xFF526B16),
    scrim              = Color(0x80000000),
)

private val LightColors = lightColorScheme(
    primary            = Color(0xFF526B16),
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFE4F0B4),
    onPrimaryContainer = Color(0xFF1C2114),
    secondary          = Color(0xFF416D86),
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFDCE6EC),
    onSecondaryContainer = Color(0xFF1C2114),
    tertiary           = Color(0xFF557C25),
    onTertiary         = Color(0xFFFFFFFF),
    tertiaryContainer  = Color(0xFFE2EED0),
    onTertiaryContainer = Color(0xFF1C2114),
    error              = Color(0xFFB64F43),
    onError            = Color(0xFFFFFFFF),
    errorContainer     = Color(0xFFFBE3E1),
    onErrorContainer   = Color(0xFF40211D),
    background         = Color(0xFFF4F5EA),
    onBackground       = Color(0xFF1C2114),
    surface            = Color(0xFAFBEF),
    onSurface          = Color(0xFF1C2114),
    surfaceVariant     = Color(0xFFFFFFFF),
    onSurfaceVariant   = Color(0xFF59604A),
    surfaceTint        = Color(0xFF526B16),
    outline            = Color(0xFFD9DEC9),
    outlineVariant     = Color(0xFFE9EDDA),
    inverseSurface     = Color(0xFF1C2114),
    inverseOnSurface   = Color(0xFFFAFBEF),
    inversePrimary     = Color(0xFFD7F75B),
    scrim              = Color(0x80000000),
)

data class HabitTints(val sleep: Color, val move: Color, val noPhone: Color, val water: Color)

private val LightHabitTints = HabitTints(
    sleep   = Color(0xFFD8F0EA),
    move    = Color(0xFFFBEED7),
    noPhone = Color(0xFFFBE3E1),
    water   = Color(0xFFDDEEFC),
)
private val DarkHabitTints = HabitTints(
    sleep   = Color(0xFF1D352F),
    move    = Color(0xFF3A2F1B),
    noPhone = Color(0xFF3A221F),
    water   = Color(0xFF1B2B3A),
)

val LocalHabitTints = staticCompositionLocalOf { DarkHabitTints }

object Momentum {
    val Tints: HabitTints @Composable get() = LocalHabitTints.current
}

val MomentumTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold, fontSize = 34.sp,
        lineHeight = 38.sp, letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 26.sp,
        lineHeight = 30.sp, letterSpacing = (-0.4).sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 11.sp,
        lineHeight = 13.sp, letterSpacing = 1.5.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.5.sp,
    ),
)

val MomentumShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small      = RoundedCornerShape(12.dp),
    medium     = RoundedCornerShape(16.dp),
    large      = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun MomentumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalHabitTints provides if (darkTheme) DarkHabitTints else LightHabitTints,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography  = MomentumTypography,
            shapes      = MomentumShapes,
            content     = content,
        )
    }
}
