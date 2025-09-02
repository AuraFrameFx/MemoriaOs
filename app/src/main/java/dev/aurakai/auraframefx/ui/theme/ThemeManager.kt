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
     * Sets the active theme configuration for the manager.
     *
     * The provided ThemeConfig becomes the manager's current theme and will be used by composable consumers (for example, getColorScheme()) and lock-screen theming.
     *
     * @param themeConfig ThemeConfig to apply as the active theme.
     */
    fun applyTheme(themeConfig: ThemeConfig) {
        currentTheme = themeConfig
    }
    
    /**
     * Get the current theme configuration
     */
    fun getCurrentTheme(): ThemeConfig = currentTheme
    
    /**
     * Returns a Compose ColorScheme constructed from the manager's current ThemeConfig.
     *
     * Produces a darkColorScheme when currentTheme.isDarkMode is true, otherwise a lightColorScheme.
     * The scheme's primary, secondary, and tertiary colors are taken from currentTheme.primaryColor,
     * currentTheme.secondaryColor, and currentTheme.accentColor.
     *
     * @return A ColorScheme appropriate for the active theme (dark or light).
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
     * Toggles the manager's dark mode state and disables following the system theme.
     *
     * Updates the active ThemeConfig by flipping `isDarkMode` and setting `useSystemTheme` to false.
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }
    
    /**
     * Enables following the system theme for the app.
     *
     * Sets the manager's ThemeConfig to use the system theme for light/dark selection (i.e., sets `useSystemTheme` to true).
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }
    
    /**
     * Update the theme's palette with a consciousness-themed color set.
     *
     * Allows specifying primary, secondary, and accent colors used across the UI;
     * defaults are provided for a purple/sky-blue/emerald scheme.
     *
     * @param primary Primary color (used for key surfaces and interactive elements).
     * @param secondary Secondary color (used for supporting surfaces and highlights).
     * @param accent Accent color (used for accents and emphasis).
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
     * The returned map contains values intended for lock-screen rendering:
     * - "clockColor": Color — white when dark mode is active, otherwise black.
     * - "backgroundColor": Color — black when dark mode is active, otherwise white.
     * - "accentColor": Color — the current theme's accent color.
     * - "isDarkMode": Boolean — whether dark mode is active.
     *
     * @return A map with keys "clockColor", "backgroundColor", "accentColor", and "isDarkMode".
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