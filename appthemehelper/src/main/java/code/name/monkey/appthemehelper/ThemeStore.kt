package code.name.monkey.appthemehelper

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.CheckResult


/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
class ThemeStore @SuppressLint("CommitPrefEdits")
private constructor(mContext: Context) : ThemeStorePrefKeys, ThemeStoreInterface {

    private val mEditor: SharedPreferences.Editor

    init {
        mEditor = prefs(mContext).edit()
    }

    override fun commit() {
        mEditor.putLong(ThemeStorePrefKeys.VALUES_CHANGED, System.currentTimeMillis())
            .commit()
    }

    companion object {
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
    }
}