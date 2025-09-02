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
     * Sets the manager's active theme configuration.
     *
     * Replaces the current ThemeConfig with the provided one; the new configuration will be used by
     * getColorScheme(), getLockScreenTheme(), and other theme-aware APIs.
     *
     * @param themeConfig The ThemeConfig to apply as the active theme.
     */
    fun applyTheme(themeConfig: ThemeConfig) {
        currentTheme = themeConfig
    }

    /**
     * Get the current theme configuration
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
     * Resolves and returns the active ColorScheme for the app based on the current ThemeConfig.
     *
     * Chooses dark vs light by following the system theme when `currentTheme.useSystemTheme` is true;
     * otherwise uses `currentTheme.isDarkMode`. When following the system theme on Android 12+
     * (SDK S+) dynamic color is used via `dynamicLightColorScheme` / `dynamicDarkColorScheme`.
     * If dynamic color is not available the scheme is built from `currentTheme`'s primary, secondary,
     * and accent colors.
     *
     * @return The resolved [ColorScheme] to apply to the UI.
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
     * Toggle the manager's dark mode state and stop following the system theme.
     *
     * Flips the currentTheme.isDarkMode flag and sets currentTheme.useSystemTheme to false by updating the stored ThemeConfig.
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }

    /**
     * Enable following the system-wide (light/dark) theme.
     *
     * Sets the manager to derive dark mode from the system instead of the explicit
     * theme flag. Updates the stored ThemeConfig's `useSystemTheme` to `true`
     * without modifying `isDarkMode`.
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }

    /**
     * Update the theme's primary, secondary, and accent colors used for the "consciousness" palette.
     *
     * These colors are applied to the manager's active ThemeConfig immediately. Defaults are:
     * primary = purple (consciousness), secondary = sky blue (clarity), accent = emerald (growth).
     *
     * @param primary Primary brand/accent color (semantic "consciousness" color).
     * @param secondary Secondary color (semantic "clarity" color).
     * @param accent Tertiary/accent color (semantic "growth" color).
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
     * Returns a small theme payload tailored for the lock screen.
     *
     * The map contains four keys:
     * - `"clockColor"`: Color for the clock text (White when dark mode, Black when light).
     * - `"backgroundColor"`: Background color for the lock screen (Black when dark mode, White when light).
     * - `"accentColor"`: Accent color from the current theme (Color).
     * - `"isDarkMode"`: Boolean indicating whether dark mode is active.
     *
     * @return A Map<String, Any> with the lock-screen color values and dark-mode flag.
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