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
abstract class ATHActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up a listener whenever themes changes
        ThemeStore.prefs(this).registerOnSharedPreferenceChangeListener(this)
    }

    override fun onDestroy() {
        // Unregister the listener whenever themes changes
        ThemeStore.prefs(this).unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == ThemeStorePrefKeys.VALUES_CHANGED) {
            updateTheme()
        }
    }

    abstract fun updateTheme()
}