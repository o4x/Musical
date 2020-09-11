package code.name.monkey.appthemehelper

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
internal interface ThemeStorePrefKeys {
    companion object {

        const val CONFIG_PREFS_KEY_DEFAULT = "[[kabouzeid_app-theme-helper]]"
        const val IS_CONFIGURED_VERSION_KEY = "is_configured_version"
        const val VALUES_CHANGED = "values_changed"
        const val KEY_ACTIVITY_THEME = "activity_theme"

        const val KEY_THEME_COLOR = "accent_color"
        const val KEY_STATUS_BAR_COLOR = "status_bar_color"
        const val KEY_NAVIGATION_BAR_COLOR = "navigation_bar_color"

        const val KEY_TEXT_COLOR_PRIMARY = "text_color_primary"
        const val KEY_TEXT_COLOR_PRIMARY_INVERSE = "text_color_primary_inverse"
        const val KEY_TEXT_COLOR_SECONDARY = "text_color_secondary"
        const val KEY_TEXT_COLOR_SECONDARY_INVERSE = "text_color_secondary_inverse"

        const val KEY_AUTO_GENERATE_PRIMARYDARK = "auto_generate_primarydark"
    }
}