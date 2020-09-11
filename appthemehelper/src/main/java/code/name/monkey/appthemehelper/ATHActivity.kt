package code.name.monkey.appthemehelper

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

/**
 * @author Aidan Follestad (afollestad), Karim Abou Zeid (kabouzeid)
 */
open class ATHActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private var updateTime: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateTime = System.currentTimeMillis()
        // Set up a listener whenever themes changes
        ThemeStore.prefs(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        // Unregister the listener whenever themes changes
        ThemeStore.prefs(this).unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    fun postRecreate() {
        // hack to prevent java.lang.RuntimeException: Performing pause of activity that is not resumed
        // makes sure recreate() is called right after and not in onResume()
        Handler().post { recreate() }
    }

    private fun onThemeChanged() {
        postRecreate()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (ATH.didThemeValuesChange(this, updateTime)) {
            updateTime = System.currentTimeMillis()
            onThemeChanged()
        }
    }
}