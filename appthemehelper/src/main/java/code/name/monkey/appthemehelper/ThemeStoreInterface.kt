package code.name.monkey.appthemehelper

import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StyleRes

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
internal interface ThemeStoreInterface {

    // Activity theme

    fun activityTheme(@StyleRes theme: Int): ThemeStore

    // Accent colors

    fun themeColor(@ColorInt color: Int): ThemeStore

    fun themeColorRes(@ColorRes colorRes: Int): ThemeStore

    fun themeColorAttr(@AttrRes colorAttr: Int): ThemeStore

    // Status bar color

    fun statusBarColor(@ColorInt color: Int): ThemeStore

    fun statusBarColorRes(@ColorRes colorRes: Int): ThemeStore

    fun statusBarColorAttr(@AttrRes colorAttr: Int): ThemeStore

    // Navigation bar color

    fun navigationBarColor(@ColorInt color: Int): ThemeStore

    fun navigationBarColorRes(@ColorRes colorRes: Int): ThemeStore

    fun navigationBarColorAttr(@AttrRes colorAttr: Int): ThemeStore

    // Primary text color

    fun textColorPrimary(@ColorInt color: Int): ThemeStore

    fun textColorPrimaryRes(@ColorRes colorRes: Int): ThemeStore

    fun textColorPrimaryAttr(@AttrRes colorAttr: Int): ThemeStore

    fun textColorPrimaryInverse(@ColorInt color: Int): ThemeStore

    fun textColorPrimaryInverseRes(@ColorRes colorRes: Int): ThemeStore

    fun textColorPrimaryInverseAttr(@AttrRes colorAttr: Int): ThemeStore

    // Secondary text color

    fun textColorSecondary(@ColorInt color: Int): ThemeStore

    fun textColorSecondaryRes(@ColorRes colorRes: Int): ThemeStore

    fun textColorSecondaryAttr(@AttrRes colorAttr: Int): ThemeStore

    fun textColorSecondaryInverse(@ColorInt color: Int): ThemeStore

    fun textColorSecondaryInverseRes(@ColorRes colorRes: Int): ThemeStore

    fun textColorSecondaryInverseAttr(@AttrRes colorAttr: Int): ThemeStore

    // Commit/apply

    fun commit()
}
