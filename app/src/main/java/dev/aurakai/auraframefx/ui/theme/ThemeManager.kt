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

// --- imports at top of ThemeManager.kt ---


// ...


    /**
     * Returns a Compose ColorScheme constructed from the manager's current ThemeConfig.
     *
     * Produces a darkColorScheme when currentTheme.isDarkMode is true, otherwise a lightColorScheme.
     * The scheme's primary, secondary, and tertiary colors are taken from currentTheme.primaryColor,
     * currentTheme.secondaryColor, and currentTheme.accentColor.
     *
     * @return A ColorScheme appropriate for the active theme (dark or light).
     * Generate a ColorScheme based on current theme, respecting system settings and Android 12+ dynamic color.
     */
    @Composable
    fun getColorScheme(): ColorScheme {
        return if (currentTheme.isDarkMode) {
            darkColorScheme(
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
     * Toggle between light and dark mode
     */
    fun toggleDarkMode() {
        currentTheme = currentTheme.copy(
            isDarkMode = !currentTheme.isDarkMode,
            useSystemTheme = false
        )
    }

    /**
     * Enable system theme following
     */
    fun enableSystemTheme() {
        currentTheme = currentTheme.copy(useSystemTheme = true)
    }

    /**
     * Set custom colors for consciousness-themed UI
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
     * Get lock screen specific theme configuration
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