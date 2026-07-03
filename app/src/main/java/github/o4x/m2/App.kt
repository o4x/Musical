package github.o4x.m2

import android.app.Application
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.appcompat.app.AppCompatDelegate
import github.o4x.m2.appshortcuts.DynamicShortcutManager
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.prefs.PreferenceUtil.isNightMode
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
        if (key == PreferenceUtil.DARK_MODE) {
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
