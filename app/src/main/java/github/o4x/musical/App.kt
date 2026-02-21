package github.o4x.musical

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatDelegate
import github.o4x.musical.appshortcuts.DynamicShortcutManager
import github.o4x.musical.prefs.PreferenceUtil
import github.o4x.musical.prefs.PreferenceUtil.isNightMode
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application(), OnSharedPreferenceChangeListener {

    override fun onCreate() {
        super.onCreate()

        instance = this

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }

        AppCompatDelegate.setDefaultNightMode(isNightMode)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(this)


        DynamicShortcutManager(this).initDynamicShortcuts()
    }

    override fun onTerminate() {
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(this)
        super.onTerminate()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PreferenceUtil.LANGUAGE_NAME || key == PreferenceUtil.DARK_MODE ) {
            AppCompatDelegate.setDefaultNightMode(isNightMode)
        }
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }
    }
}
