package github.o4x.musical.appshortcuts.shortcuttype

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.ShortcutInfo
import android.os.Build
import github.o4x.musical.appshortcuts.AppShortcutIconGenerator
import github.o4x.musical.R
import github.o4x.musical.appshortcuts.AppShortcutLauncherActivity

@TargetApi(Build.VERSION_CODES.N_MR1)
class LastAddedShortcutType(context: Context) : BaseShortcutType(context) {

    override val shortcutInfo: ShortcutInfo
        get() = ShortcutInfo.Builder(
            context,
            id
        ).setShortLabel(context.getString(R.string.app_shortcut_last_added_short))
            .setIcon(
            AppShortcutIconGenerator.generateThemedIcon(
                context,
                R.drawable.ic_app_shortcut_last_added
            )
        ).setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.SHORTCUT_TYPE_LAST_ADDED))
            .build()

    companion object {

        val id: String
            get() = BaseShortcutType.ID_PREFIX + "last_added"
    }
}
