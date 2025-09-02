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
     * Replace the manager's active theme with the provided configuration.
     *
     * Updates the stored ThemeConfig used by the app; subsequent calls to
     * getColorScheme() and lock-screen theme queries will reflect the new settings.
     *
     * @param themeConfig The new theme configuration to apply (replaces the current theme).
     */
    fun applyTheme(themeConfig: ThemeConfig) {
        currentTheme = themeConfig
    }
    
    /**
     * Get the current theme configuration
     */
    fun getCurrentTheme(): ThemeConfig = currentTheme
    
    /**
     * Return a Compose ColorScheme derived from the manager's current theme.
     *
     * Chooses a dark or light scheme based on `currentTheme.isDarkMode`. The scheme's
     * primary, secondary, and tertiary colors are taken from `currentTheme.primaryColor`,
     * `currentTheme.secondaryColor`, and `currentTheme.accentColor`.
     *
     * @return A ColorScheme configured from the active ThemeConfig.
     */
    @Composable
    fun getColorScheme(): ColorScheme {
        return if (currentTheme.isDarkMode) {
            darkColorScheme(
                primary = currentTheme.primaryColor,
                secondary = currentTheme.secondaryColor,
                tertiary = currentTheme.accentColor
            )
        } else {
            lightColorScheme(
                primary = currentTheme.primaryColor,
                secondary = currentTheme.secondaryColor,
                tertiary = currentTheme.accentColor
            )
        }
    }
    
    /**
     * Toggles the manager's dark mode state.
     *
     * Inverts the currentTheme.isDarkMode and sets useSystemTheme to false by replacing
     * the stored ThemeConfig with an updated copy.
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }
    
    /**
     * Enable following the system theme for the app.
     *
     * Sets the manager's current theme to a copy with `useSystemTheme = true`, so the app will
     * follow the host/system theme preference. Does not modify `isDarkMode` or other color values.
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }
    
    /**
     * Update the theme's primary, secondary, and accent colors used for the "consciousness" color palette.
     *
     * Replaces the current theme's color values with the provided colors.
     *
     * @param primary Color used as the primary (consciousness) color. Defaults to a purple (0xFF9333EA).
     * @param secondary Color used as the secondary (clarity) color. Defaults to a sky blue (0xFF0EA5E9).
     * @param accent Color used as the accent (growth) color. Defaults to an emerald green (0xFF10B981).
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
     * Returns a lock-screen-specific theme map derived from the current ThemeConfig.
     *
     * The returned map contains keys used by lock-screen UI code:
     * - "clockColor": Color — white when dark mode is enabled, otherwise black.
     * - "backgroundColor": Color — black when dark mode is enabled, otherwise white.
     * - "accentColor": Color — the theme's accent color.
     * - "isDarkMode": Boolean — whether dark mode is enabled.
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