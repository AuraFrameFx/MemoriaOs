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
     * Apply a theme configuration
     */
    fun applyTheme(themeConfig: ThemeConfig) {
        currentTheme = themeConfig
    }
    
    /**
 * Returns the active theme configuration.
 *
 * @return the current ThemeConfig containing the active dark/light mode, system-theme flag, and color values.
 */
    fun getCurrentTheme(): ThemeConfig = currentTheme
    
    /**
     * Returns a Compose ColorScheme reflecting the current theme configuration.
     *
     * Selects a darkColorScheme when currentTheme.isDarkMode is true, otherwise a lightColorScheme.
     * Maps ThemeConfig.primaryColor -> primary, ThemeConfig.secondaryColor -> secondary,
     * and ThemeConfig.accentColor -> tertiary.
     *
     * @return A ColorScheme appropriate for the current theme mode.
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
     * Toggle the theme's dark mode state.
     *
     * Flips the current theme's `isDarkMode` value and sets `useSystemTheme` to false,
     * updating the manager's stored ThemeConfig.
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }
    
    /**
     * Enables following the system theme.
     *
     * Sets the current theme's `useSystemTheme` flag to `true` while preserving other theme fields (including `isDarkMode`).
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }
    
    /**
     * Update the theme's primary, secondary, and accent colors to a "consciousness" palette.
     *
     * Sets the manager's current theme colors to the provided values. Defaults apply a
     * purple primary (consciousness), sky-blue secondary (clarity), and emerald accent (growth).
     *
     * @param primary Primary color used across prominent UI surfaces (default: purple 0xFF9333EA).
     * @param secondary Secondary color used for supportive accents and surfaces (default: sky blue 0xFF0EA5E9).
     * @param accent Accent/tertiary color used for highlights and interactive elements (default: emerald 0xFF10B981).
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
     * Build a lock-screen-specific theme map.
     *
     * Returns a map with the visual values the lock screen needs:
     * - "clockColor": Color — color for the clock text (white in dark mode, black otherwise).
     * - "backgroundColor": Color — background color (black in dark mode, white otherwise).
     * - "accentColor": Color — the current accent color.
     * - "isDarkMode": Boolean — whether dark mode is active.
     *
     * @return Map<String, Any> containing the keys described above.
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