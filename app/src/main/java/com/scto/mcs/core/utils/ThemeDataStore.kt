package com.scto.mcside.core.utils

import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.materialkolor.PaletteStyle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mcside_theme_settings")

data class ThemeState(
    val selectedModeIndex: Int,
    val selectedThemeIndex: Int,
    val isMonetEnabled: Boolean,
    val isCustomTheme: Boolean,
    val customColor: Color,
    val style: PaletteStyle = PaletteStyle.TonalSpot,
    val isLoaded: Boolean = false
)

class ThemeDataStoreRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_MODE = intPreferencesKey("selected_mode")
        val SELECTED_THEME = intPreferencesKey("selected_theme")
        val IS_MONET_ENABLED = booleanPreferencesKey("is_monet_enabled")
        val IS_CUSTOM = booleanPreferencesKey("is_custom")
        val CUSTOM_COLOR = longPreferencesKey("custom_color")
        val SELECTED_STYLE = stringPreferencesKey("selected_style")
    }

    val themeStateFlow: Flow<ThemeState> = context.dataStore.data
        .map { preferences ->
            val modeIndex = preferences[PreferencesKeys.SELECTED_MODE] ?: 0
            val themeIndex = preferences[PreferencesKeys.SELECTED_THEME] ?: 0
            val isMonet = preferences[PreferencesKeys.IS_MONET_ENABLED] ?: (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            val isCustom = preferences[PreferencesKeys.IS_CUSTOM] ?: false
            
            val styleName = preferences[PreferencesKeys.SELECTED_STYLE] ?: PaletteStyle.TonalSpot.name
            val style = try {
                PaletteStyle.valueOf(styleName)
            } catch (_: IllegalArgumentException) {
                PaletteStyle.TonalSpot
            }

            // Read logic: Convert stored Long back to Int, then construct the Color.
            val customColorValue = preferences[PreferencesKeys.CUSTOM_COLOR] ?: 0xFF6750A4L
            val decodedColor = Color(customColorValue.toInt())

            LogCatcher.d("ThemeDebug_Repo", "Repo Read: Monet=$isMonet, Custom=$isCustom, Style=$style, Color=${decodedColor.toArgb()}")

            ThemeState(
                selectedModeIndex = modeIndex,
                selectedThemeIndex = themeIndex,
                isMonetEnabled = isMonet,
                isCustomTheme = isCustom,
                customColor = decodedColor,
                style = style,
                isLoaded = true
            )
        }

    suspend fun saveThemeConfig(
        selectedModeIndex: Int,
        selectedThemeIndex: Int,
        customColor: Color,
        isMonetEnabled: Boolean,
        isCustom: Boolean,
        style: PaletteStyle
    ) {
        // Standardize color to ARGB Int to handle internal representation differences.
        val colorInt = customColor.toArgb()

        LogCatcher.w("ThemeDebug_Repo", "Repo Write: Monet=$isMonetEnabled, Custom=$isCustom, Style=$style, Color=$colorInt")

        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_MODE] = selectedModeIndex
            preferences[PreferencesKeys.SELECTED_THEME] = selectedThemeIndex
            preferences[PreferencesKeys.IS_MONET_ENABLED] = isMonetEnabled
            preferences[PreferencesKeys.IS_CUSTOM] = isCustom
            preferences[PreferencesKeys.CUSTOM_COLOR] = colorInt.toLong()
            preferences[PreferencesKeys.SELECTED_STYLE] = style.name
        }
    }
}