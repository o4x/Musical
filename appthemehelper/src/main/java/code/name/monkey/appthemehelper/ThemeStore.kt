package code.name.monkey.appthemehelper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import androidx.annotation.*
import androidx.annotation.IntRange
import androidx.core.content.ContextCompat
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.ATHUtil.resolveColor
import code.name.monkey.appthemehelper.util.ColorUtil


/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
class ThemeStore @SuppressLint("CommitPrefEdits")
private constructor(private val mContext: Context) : ThemeStorePrefKeys, ThemeStoreInterface {

    private val mEditor: SharedPreferences.Editor

    init {
        mEditor = prefs(mContext).edit()
    }

    override fun activityTheme(@StyleRes theme: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_ACTIVITY_THEME, theme)
        return this
    }

    override fun themeColor(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_THEME_COLOR, color)
        return this
    }

    override fun themeColorRes(@ColorRes colorRes: Int): ThemeStore {
        return themeColor(ContextCompat.getColor(mContext, colorRes))
    }

    override fun themeColorAttr(@AttrRes colorAttr: Int): ThemeStore {
        return themeColor(ATHUtil.resolveColor(mContext, colorAttr))
    }

    override fun statusBarColor(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_STATUS_BAR_COLOR, color)
        return this
    }

    override fun statusBarColorRes(@ColorRes colorRes: Int): ThemeStore {
        return statusBarColor(ContextCompat.getColor(mContext, colorRes))
    }

    override fun statusBarColorAttr(@AttrRes colorAttr: Int): ThemeStore {
        return statusBarColor(ATHUtil.resolveColor(mContext, colorAttr))
    }

    override fun navigationBarColor(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_NAVIGATION_BAR_COLOR, color)
        return this
    }

    // Commit method

    override fun navigationBarColorRes(@ColorRes colorRes: Int): ThemeStore {
        return navigationBarColor(ContextCompat.getColor(mContext, colorRes))
    }

    // Static getters

    override fun navigationBarColorAttr(@AttrRes colorAttr: Int): ThemeStore {
        return navigationBarColor(ATHUtil.resolveColor(mContext, colorAttr))
    }

    override fun textColorPrimary(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY, color)
        return this
    }

    override fun textColorPrimaryRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorPrimary(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorPrimaryAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorPrimary(ATHUtil.resolveColor(mContext, colorAttr))
    }

    override fun textColorPrimaryInverse(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY_INVERSE, color)
        return this
    }

    override fun textColorPrimaryInverseRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorPrimaryInverse(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorPrimaryInverseAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorPrimaryInverse(ATHUtil.resolveColor(mContext, colorAttr))
    }

    override fun textColorSecondary(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY, color)
        return this
    }

    override fun textColorSecondaryRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorSecondary(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorSecondaryAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorSecondary(ATHUtil.resolveColor(mContext, colorAttr))
    }

    override fun textColorSecondaryInverse(@ColorInt color: Int): ThemeStore {
        mEditor.putInt(ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY_INVERSE, color)
        return this
    }

    override fun textColorSecondaryInverseRes(@ColorRes colorRes: Int): ThemeStore {
        return textColorSecondaryInverse(ContextCompat.getColor(mContext, colorRes))
    }

    override fun textColorSecondaryInverseAttr(@AttrRes colorAttr: Int): ThemeStore {
        return textColorSecondaryInverse(ATHUtil.resolveColor(mContext, colorAttr))
    }

    override fun commit() {
        mEditor.putLong(ThemeStorePrefKeys.VALUES_CHANGED, System.currentTimeMillis())
            .commit()
    }

    companion object {

        fun editTheme(context: Context): ThemeStore {
            return ThemeStore(context)
        }

        @CheckResult
        fun prefs(context: Context): SharedPreferences {
            return context.getSharedPreferences(
                ThemeStorePrefKeys.CONFIG_PREFS_KEY_DEFAULT,
                Context.MODE_PRIVATE
            )
        }

        fun markChanged(context: Context) {
            ThemeStore(context).commit()
        }

        @CheckResult
        @StyleRes
        fun activityTheme(context: Context): Int {
            return prefs(context).getInt(ThemeStorePrefKeys.KEY_ACTIVITY_THEME, 0)
        }

        @CheckResult
        @ColorInt
        fun themeColor(context: Context): Int {
            val color = prefs(context).getInt(
                ThemeStorePrefKeys.KEY_THEME_COLOR,
                ATHUtil.resolveColor(context, R.attr.colorAccent, Color.parseColor("#263238"))
            )
            return if (ATHUtil.isWindowBackgroundDark(context)) ColorUtil.desaturateColor(
                color,
                0.4f
            ) else color
        }

        @CheckResult
        @ColorInt
        fun navigationBarColor(context: Context): Int {
            return prefs(context).getInt(
                ThemeStorePrefKeys.KEY_NAVIGATION_BAR_COLOR,
                themeColor(context)
            )
        }

        @CheckResult
        fun autoGeneratePrimaryDark(context: Context): Boolean {
            return prefs(context).getBoolean(ThemeStorePrefKeys.KEY_AUTO_GENERATE_PRIMARYDARK, true)
        }

        @CheckResult
        @ColorInt
        fun textColorPrimary(context: Context): Int {
            return prefs(context).getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY,
                resolveColor(context, android.R.attr.textColorPrimary)
            )
        }

        @CheckResult
        @ColorInt
        fun textColorPrimaryInverse(context: Context): Int {
            return prefs(context).getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_PRIMARY_INVERSE,
                resolveColor(context, android.R.attr.textColorPrimaryInverse)
            )
        }

        @CheckResult
        @ColorInt
        fun textColorSecondary(context: Context): Int {
            return prefs(context).getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY,
                resolveColor(context, android.R.attr.textColorSecondary)
            )
        }

        @CheckResult
        @ColorInt
        fun textColorSecondaryInverse(context: Context): Int {
            return prefs(context).getInt(
                ThemeStorePrefKeys.KEY_TEXT_COLOR_SECONDARY_INVERSE,
                resolveColor(context, android.R.attr.textColorSecondaryInverse)
            )
        }

        @SuppressLint("CommitPrefEdits")
        fun isConfigured(
            context: Context, @IntRange(
                from = 0,
                to = Integer.MAX_VALUE.toLong()
            ) version: Int
        ): Boolean {
            val prefs = prefs(context)
            val lastVersion = prefs.getInt(ThemeStorePrefKeys.IS_CONFIGURED_VERSION_KEY, -1)
            if (version > lastVersion) {
                prefs.edit().putInt(ThemeStorePrefKeys.IS_CONFIGURED_VERSION_KEY, version).commit()
                return false
            }
            return true
        }
    }
}