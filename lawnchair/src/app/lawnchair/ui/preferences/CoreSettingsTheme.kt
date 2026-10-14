package app.lawnchair.ui.preferences

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import app.lawnchair.preferences.observeAsState
import app.lawnchair.preferences.preferenceManager
import app.lawnchair.ui.theme.isSelectedThemeDark

val LocalCoreSettingsGlass = staticCompositionLocalOf { false }
val LocalCoreSettingsBackdrop = staticCompositionLocalOf { false }
val LocalCoreSettingsOpacity = staticCompositionLocalOf { 1f }
val LocalCoreSettingsBorder = staticCompositionLocalOf { 0f }
val LocalCoreSettingsCompactHeader = staticCompositionLocalOf { false }

internal fun coreAccent(key: String, dark: Boolean, system: Color): Color = when (key) {
    "system" -> system
    "purple" -> if (dark) Color(0xFFC5ACFF) else Color(0xFF6842BD)
    "cyan" -> if (dark) Color(0xFF75D8E8) else Color(0xFF006879)
    "emerald" -> if (dark) Color(0xFF78DCA7) else Color(0xFF006D44)
    "wine" -> if (dark) Color(0xFFFFA9C5) else Color(0xFF8E244D)
    "orange" -> if (dark) Color(0xFFFFB77B) else Color(0xFF934B00)
    "graphite" -> if (dark) Color(0xFFBFC8D5) else Color(0xFF49586D)
    else -> if (dark) Color(0xFFA6C8FF) else Color(0xFF2865CF)
}

@Composable
fun CoreSettingsTheme(content: @Composable () -> Unit) {
    val prefs = preferenceManager()
    val miuix by prefs.coreSettingsMiuix.observeAsState()
    val glass by prefs.coreSettingsGlass.observeAsState()
    val opacity by prefs.coreSettingsOpacity.observeAsState()
    val accentKey by prefs.coreSettingsAccent.observeAsState()
    val backgroundMode by prefs.coreSettingsBackground.observeAsState()
    val radius by prefs.coreSettingsRadius.observeAsState()
    val border by prefs.coreSettingsBorder.observeAsState()
    val tint by prefs.coreSettingsTint.observeAsState()
    val compactHeader by prefs.coreSettingsCompactHeader.observeAsState()
    val dark = isSelectedThemeDark
    val base = MaterialTheme.colorScheme
    // Appearance overrides stay inside the settings composition, leaving launcher colors alone.
    val accent = if (miuix) coreAccent(accentKey, dark, base.primary) else base.primary
    val background = when {
        !miuix -> base.surface
        backgroundMode == "amoled" && dark -> Color.Black
        dark -> Color(0xFF08090B)
        else -> Color(0xFFF3F4F6)
    }
    val neutralCard = if (dark) Color(0xFF202126) else Color.White
    val card = lerp(neutralCard, accent, if (glass) tint.coerceIn(0f, 0.3f) else 0f)
    val scheme = if (!miuix) base else base.copy(
        background = background,
        surface = background,
        surfaceDim = background,
        surfaceBright = card,
        surfaceContainerLowest = background,
        surfaceContainerLow = card,
        surfaceContainer = card,
        surfaceContainerHigh = card,
        surfaceContainerHighest = card,
        onBackground = if (dark) Color(0xFFF5F5F7) else Color(0xFF17181B),
        onSurface = if (dark) Color(0xFFF5F5F7) else Color(0xFF17181B),
        onSurfaceVariant = if (dark) Color(0xFFB9BCC5) else Color(0xFF60636C),
        primary = accent,
        onPrimary = if (dark) Color(0xFF101318) else Color.White,
        primaryContainer = lerp(background, accent, if (dark) 0.24f else 0.14f),
        onPrimaryContainer = if (dark) Color(0xFFF5F5F7) else Color(0xFF17181B),
        secondary = accent,
        onSecondary = if (dark) Color(0xFF101318) else Color.White,
        secondaryContainer = lerp(background, accent, 0.18f),
        onSecondaryContainer = if (dark) Color(0xFFF5F5F7) else Color(0xFF17181B),
        outline = if (dark) Color(0xFF8C9099) else Color(0xFF747780),
        outlineVariant = if (dark) Color(0xFF41444B) else Color(0xFFD9DBE1),
    )
    val typography = MaterialTheme.typography
    val cleanTypography = if (!miuix) typography else typography.copy(
        headlineLarge = typography.headlineLarge.copy(fontFamily = FontFamily.SansSerif),
        headlineMedium = typography.headlineMedium.copy(fontFamily = FontFamily.SansSerif),
        headlineSmall = typography.headlineSmall.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = typography.titleLarge.copy(fontFamily = FontFamily.SansSerif),
        titleMedium = typography.titleMedium.copy(fontFamily = FontFamily.SansSerif),
        bodyLarge = typography.bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = typography.bodyMedium.copy(fontFamily = FontFamily.SansSerif),
    )
    val gradient = if (miuix) backgroundMode == "gradient" else glass
    val corner = radius.coerceIn(12f, 36f).dp
    val shapes = if (miuix) MaterialTheme.shapes.copy(
        large = RoundedCornerShape(corner), extraLarge = RoundedCornerShape(corner),
    ) else MaterialTheme.shapes
    MaterialTheme(colorScheme = scheme, typography = cleanTypography, shapes = shapes) {
        CompositionLocalProvider(
            LocalCoreSettingsGlass provides glass,
            LocalCoreSettingsOpacity provides opacity.coerceIn(0.4f, 1f),
            LocalCoreSettingsBackdrop provides gradient,
            LocalCoreSettingsBorder provides if (miuix) border.coerceIn(0f, 0.3f) else 0f,
            LocalCoreSettingsCompactHeader provides (miuix && compactHeader),
        ) {
            val backdrop = if (gradient) Modifier.background(Brush.verticalGradient(
                listOf(lerp(background, accent, 0.16f), background, lerp(background, accent, 0.08f)),
            )) else Modifier.background(background)
            Box(Modifier.fillMaxSize().then(backdrop)) { content() }
        }
    }
}
