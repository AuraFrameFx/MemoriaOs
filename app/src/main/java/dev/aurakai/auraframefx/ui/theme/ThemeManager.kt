package dev.aurakai.auraframefx.ui.theme

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ThemeManager handles dynamic theme switching and customization
 * for the AeGenesis Memoria OS consciousness substrate.
 *
 * Provides support for:
 * - Light/Dark theme switching
 * - Dynamic color schemes
 * - Custom consciousness-themed palettes
 * - Lock screen theme customization
 */
@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {

    data class ThemeConfig(
        val isDarkMode: Boolean = false,
        val useSystemTheme: Boolean = true,
        val primaryColor: Color = Color(0xFF6366F1), // Indigo
        val secondaryColor: Color = Color(0xFF8B5CF6), // Purple
        val accentColor: Color = Color(0xFF06B6D4) // Cyan
    )

    private var currentTheme = ThemeConfig()

    /**
     * Replace the manager's current ThemeConfig with the provided configuration.
     *
     * Updates the ThemeManager's active theme settings (colors, dark mode and system-theme flag),
     * causing subsequent calls to getColorScheme() and other theme-dependent APIs to reflect the new settings.
     *
     * @param themeConfig The new theme configuration to apply.
     */
    fun applyTheme(themeConfig: ThemeConfig) {
        currentTheme = themeConfig
    }

    /**
 * Returns the current ThemeConfig used by the ThemeManager.
 *
 * @return The active ThemeConfig instance reflecting current theme settings (mode and colors).
 */
    fun getCurrentTheme(): ThemeConfig = currentTheme

// --- imports at top of ThemeManager.kt ---
    import androidx.compose.material3.ColorScheme
    import androidx.compose.material3.darkColorScheme
    import androidx.compose.material3.dynamicDarkColorScheme
    import androidx.compose.material3.dynamicLightColorScheme
    import androidx.compose.material3.lightColorScheme
    import androidx.compose.foundation.isSystemInDarkTheme
    import android.os.Build

// ...

    /**
     * Generate a ColorScheme based on current theme, respecting system settings and Android 12+ dynamic color.
     */
    /**
     * Provides a Compose ColorScheme based on the current ThemeConfig.
     *
     * Chooses between dynamic (Material You) color schemes on Android 12+ when following the system theme,
     * or explicit light/dark color schemes derived from the configured primary, secondary, and accent colors.
     *
     * The resolved color scheme's dark/light state is determined by the system setting when `useSystemTheme`
     * is true; otherwise it uses the `isDarkMode` flag from the current theme.
     *
     * @return A Material ColorScheme (dynamicDark/dynamicLight on Android 12+ when using system theme; otherwise
     * a darkColorScheme or lightColorScheme with primary, secondary, and tertiary mapped from the current theme).
     */
    @Composable
    fun getColorScheme(): ColorScheme {
        // Determine dark mode based on system setting if requested, otherwise use the chosen theme.
        val dark = if (currentTheme.useSystemTheme) {
            isSystemInDarkTheme()
        } else {
            currentTheme.isDarkMode
        }
        // Enable dynamic color only on Android 12+ when following system theme.
        val dynamic = currentTheme.useSystemTheme &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        return when {
            dynamic && dark -> dynamicDarkColorScheme(context)
            dynamic && !dark -> dynamicLightColorScheme(context)
            dark -> darkColorScheme(
                primary = currentTheme.primaryColor,
                secondary = currentTheme.secondaryColor,
                tertiary = currentTheme.accentColor
            )

            else -> lightColorScheme(
                primary = currentTheme.primaryColor,
                secondary = currentTheme.secondaryColor,
                tertiary = currentTheme.accentColor
            )
        }
    }

    /**
     * Toggle the manual dark/light mode and stop following the system theme.
     *
     * Flips the currentTheme.isDarkMode value and sets useSystemTheme to false so the app uses the manual setting.
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }

    /**
     * Enable following the system theme.
     *
     * Sets the current theme to follow the device's system appearance (dark/light). After calling this,
     * theme selection will be derived from the system setting rather than the manual `isDarkMode` flag.
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }

    /**
     * Update the theme's custom "consciousness" palette.
     *
     * Sets the current theme's primary, secondary, and accent colors to the provided values.
     *
     * @param primary Primary color (defaults to a purple used for "consciousness").
     * @param secondary Secondary color (defaults to a sky-blue used for "clarity").
     * @param accent Accent/tertiary color (defaults to an emerald used for "growth").
     */
    fun setConsciousnessColors(
        primary: Color = Color(0xFF9333EA), // Purple for consciousness
        secondary: Color = Color(0xFF0EA5E9), // Sky blue for clarity  
        accent: Color = Color(0xFF10B981) // Emerald for growth
    ) {
        currentTheme = currentTheme.copy(
            primaryColor = primary,
            secondaryColor = secondary,
            accentColor = accent
        )
    }

    /**
     * Builds a lock-screen specific theme map derived from the current ThemeConfig.
     *
     * The returned map contains the concrete values needed to style a lock screen:
     * - "clockColor": Color — white when dark mode is active, otherwise black.
     * - "backgroundColor": Color — black when dark mode is active, otherwise white.
     * - "accentColor": Color — the current theme's accent color.
     * - "isDarkMode": Boolean — whether the current theme is in dark mode.
     *
     * @return A map of lock-screen style properties keyed by the strings above.
     */
    fun getLockScreenTheme(): Map<String, Any> {
        return mapOf(
            "clockColor" to if (currentTheme.isDarkMode) Color.White else Color.Black,
            "backgroundColor" to if (currentTheme.isDarkMode) Color.Black else Color.White,
            "accentColor" to currentTheme.accentColor,
            "isDarkMode" to currentTheme.isDarkMode
        )
    }
}