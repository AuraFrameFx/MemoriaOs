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
     * Replaces the current theme with the provided ThemeConfig; subsequent calls to
     * getColorScheme(), getLockScreenTheme(), and other theme-dependent APIs will
     * reflect the new configuration.
     *
     * @param themeConfig The ThemeConfig to apply as the active theme.
     */
    fun applyTheme(themeConfig: ThemeConfig) {
        currentTheme = themeConfig
    }
    
    /**
 * Returns the active theme configuration.
 *
 * @return The current ThemeConfig in use by the ThemeManager.
 */
    fun getCurrentTheme(): ThemeConfig = currentTheme
    
    /**
     * Create a Material 3 ColorScheme from the manager's current ThemeConfig.
     *
     * Returns a dark or light ColorScheme depending on `currentTheme.isDarkMode`, mapping
     * `currentTheme.primaryColor` → primary, `currentTheme.secondaryColor` → secondary,
     * and `currentTheme.accentColor` → tertiary.
     *
     * @return A Compose Material3 ColorScheme matching the active theme configuration.
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
     * Toggle the theme's dark-mode flag and disable system theme usage.
     *
     * Flips `currentTheme.isDarkMode` to the opposite value and sets `currentTheme.useSystemTheme`
     * to false so the app no longer follows the system theme after this change.
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }
    
    /**
     * Re-enables following the system theme for the app.
     *
     * Updates the manager's currentTheme to set `useSystemTheme = true` while preserving other theme fields.
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }
    
    /**
     * Update the theme's palette to a "consciousness" set of colors.
     *
     * Replaces the current theme's primary, secondary, and accent colors with the provided values.
     *
     * @param primary Primary color (defaults to a purple used for "consciousness").
     * @param secondary Secondary color (defaults to a sky-blue used for "clarity").
     * @param accent Accent color (defaults to an emerald used for "growth").
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
     * Builds a minimal lock-screen theme payload derived from the current ThemeConfig.
     *
     * The returned map contains four entries used by lock-screen UI code:
     * - "clockColor": Color — white when dark mode is active, black otherwise.
     * - "backgroundColor": Color — black when dark mode is active, white otherwise.
     * - "accentColor": Color — the current accent color from ThemeConfig.
     * - "isDarkMode": Boolean — whether dark mode is active.
     *
     * @return A Map<String, Any> with the keys `"clockColor"`, `"backgroundColor"`, `"accentColor"`, and `"isDarkMode"`.
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