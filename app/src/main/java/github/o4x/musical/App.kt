package github.o4x.musical

import android.app.Application
import com.o4x.appthemehelper.util.VersionUtils
import github.o4x.musical.appshortcuts.DynamicShortcutManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {

        super.onCreate()

        instance = this

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }

        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(this).initDynamicShortcuts()
    }

    companion object {
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }
    }
}
