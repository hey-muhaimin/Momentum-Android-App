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
   Momentum Design System — DESIGN.md v1.0
   Single source of truth for color, type, shape. Light + dark
   ship together; no feature lands light-only.
   ════════════════════════════════════════════════════════════ */

/* ── Color roles · DESIGN.md §3 ─────────────────────────────── */

private val LightColors = lightColorScheme(
    primary            = Color(0xFF2F855A),   // checks, flame, FAB, active chips
    onPrimary          = Color(0xFFFFFFFF),
    primaryContainer   = Color(0xFFE8F7EE),   // mood panel, done rows, nav pill
    onPrimaryContainer = Color(0xFF0E1B14),
    secondary          = Color(0xFF519075),   // wordmark "um", sparkline
    onSecondary        = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8F7EE),
    onSecondaryContainer = Color(0xFF0E1B14),
    tertiary           = Color(0xFF7FD1B9),   // chain filled cells
    onTertiary         = Color(0xFF0E1B14),
    tertiaryContainer  = Color(0xFFE8F7EE),
    onTertiaryContainer = Color(0xFF0E1B14),
    error              = Color(0xFFBA4A3F),   // destructive actions ONLY — never streaks
    onError            = Color(0xFFFFFFFF),
    errorContainer     = Color(0xFFFBE3E1),
    onErrorContainer   = Color(0xFF40211D),
    background         = Color(0xFFF8FBF8),
    onBackground       = Color(0xFF0E1B14),
    surface            = Color(0xFFF5F8F4),   // screen well / inset panels
    onSurface          = Color(0xFF0E1B14),
    surfaceVariant     = Color(0xFFFFFFFF),   // cards, habit rows, mood buttons
    onSurfaceVariant   = Color(0xFF5D6D63),   // meta text, kickers
    surfaceTint        = Color(0xFF2F855A),
    outline            = Color(0x140E1B14),   // ink @ 8% hairlines
    outlineVariant     = Color(0x0F0E1B14),
    inverseSurface     = Color(0xFF0E1B14),
    inverseOnSurface   = Color(0xFFF5F8F4),
    inversePrimary     = Color(0xFF5CB98A),
    scrim              = Color(0x80000000),
)

private val DarkColors = darkColorScheme(
    primary            = Color(0xFF5CB98A),
    onPrimary          = Color(0xFF0E1B14),
    primaryContainer   = Color(0xFF1D3529),
    onPrimaryContainer = Color(0xFFEAF4EE),
    secondary          = Color(0xFF7FD1B9),
    onSecondary        = Color(0xFF0E1B14),
    secondaryContainer = Color(0xFF1D3529),
    onSecondaryContainer = Color(0xFFEAF4EE),
    tertiary           = Color(0xFF7FD1B9),
    onTertiary         = Color(0xFF0E1B14),
    tertiaryContainer  = Color(0xFF1D3529),
    onTertiaryContainer = Color(0xFFEAF4EE),
    error              = Color(0xFFE58A80),
    onError            = Color(0xFF40211D),
    errorContainer     = Color(0xFF3A221F),
    onErrorContainer   = Color(0xFFE58A80),
    background         = Color(0xFF0B100D),
    onBackground       = Color(0xFFEAF4EE),
    surface            = Color(0xFF101712),
    onSurface          = Color(0xFFEAF4EE),
    surfaceVariant     = Color(0xFF141B16),
    onSurfaceVariant   = Color(0xFF9BAFA3),
    surfaceTint        = Color(0xFF5CB98A),
    outline            = Color(0x1AEAF4EE),   // ink @ 10%
    outlineVariant     = Color(0x14EAF4EE),
    inverseSurface     = Color(0xFFEAF4EE),
    inverseOnSurface   = Color(0xFF0E1B14),
    inversePrimary     = Color(0xFF2F855A),
    scrim              = Color(0x80000000),
)

/* ── Habit icon tints · DESIGN.md §3.2 ──────────────────────── */

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

/** Usage: `Momentum.Tints.sleep` inside any composable. */
object Momentum {
    val Tints: HabitTints @Composable get() = LocalHabitTints.current
}

/* ── Typography · DESIGN.md §4 ─────────────────────────────────
   Mapped onto M3 slots; platform default sans, weights 400–800. */

val MomentumTypography = Typography(
    // Display — 32sp/800 · streak count, stat numbers
    displayLarge = TextStyle(
        fontWeight = FontWeight.ExtraBold, fontSize = 32.sp,
        lineHeight = 35.sp, letterSpacing = (-0.5).sp,
    ),
    // Headline — 20sp/800 · wordmark, card titles
    headlineMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold, fontSize = 20.sp,
        lineHeight = 23.sp, letterSpacing = (-0.4).sp,
    ),
    // Title — 16sp/700 · habit names, dialog titles
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 21.sp,
    ),
    // Body — 14sp/400 · notes, descriptions
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 22.sp,
    ),
    // Caption — 12sp/500 · streak line, "2/4 done"
    bodySmall = TextStyle(
        fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 17.sp,
    ),
    // Kicker — 11sp/700 uppercase +14% · section labels.
    // Apply `uppercase()` on the string at call site; tracking ≈ 1.5sp.
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 11.sp,
        lineHeight = 13.sp, letterSpacing = 1.5.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold, fontSize = 12.sp, letterSpacing = 1.5.sp,
    ),
)

/* ── Shape · DESIGN.md §5 ────────────────────────────────────── */

val MomentumShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chain cells
    small      = RoundedCornerShape(12.dp),  // radius.xs — icon tiles, info notes
    medium     = RoundedCornerShape(16.dp),  // habit rows, cards, mood panel
    large      = RoundedCornerShape(20.dp),  // radius.md
    extraLarge = RoundedCornerShape(32.dp),  // radius.lg — sheets, dialogs
)

/* ── Theme ──────────────────────────────────────────────────── */

@Composable
fun MomentumTheme(
    darkTheme: Boolean = true,
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



