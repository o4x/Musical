package com.o4x.musical.appshortcuts

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.o4x.musical.appshortcuts.shortcuttype.LastAddedShortcutType
import com.o4x.musical.appshortcuts.shortcuttype.ShuffleAllShortcutType
import com.o4x.musical.appshortcuts.shortcuttype.TopTracksShortcutType
import com.o4x.musical.extensions.extraNotNull
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.smartplaylist.LastAddedPlaylist
import com.o4x.musical.model.smartplaylist.ShuffleAllPlaylist
import com.o4x.musical.model.smartplaylist.TopTracksPlaylist
import com.o4x.musical.service.MusicService
import com.o4x.musical.service.MusicService.Companion.ACTION_PLAY_PLAYLIST
import com.o4x.musical.service.MusicService.Companion.INTENT_EXTRA_PLAYLIST
import com.o4x.musical.service.MusicService.Companion.INTENT_EXTRA_SHUFFLE_MODE
import com.o4x.musical.service.MusicService.Companion.SHUFFLE_MODE_NONE
import com.o4x.musical.service.MusicService.Companion.SHUFFLE_MODE_SHUFFLE

class AppShortcutLauncherActivity : Activity() {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (extraNotNull(KEY_SHORTCUT_TYPE, SHORTCUT_TYPE_NONE).value) {
            SHORTCUT_TYPE_SHUFFLE_ALL -> {
                startServiceWithPlaylist(
                    SHUFFLE_MODE_SHUFFLE, ShuffleAllPlaylist()
                )
                DynamicShortcutManager.reportShortcutUsed(this, ShuffleAllShortcutType.id)
            }
            SHORTCUT_TYPE_TOP_TRACKS -> {
                startServiceWithPlaylist(
                    SHUFFLE_MODE_NONE, TopTracksPlaylist()
                )
                DynamicShortcutManager.reportShortcutUsed(this, TopTracksShortcutType.id)
            }
            SHORTCUT_TYPE_LAST_ADDED -> {
                startServiceWithPlaylist(
                    SHUFFLE_MODE_NONE, LastAddedPlaylist()
                )
                DynamicShortcutManager.reportShortcutUsed(this, LastAddedShortcutType.id)
            }
        }
        finish()
    }

    private fun startServiceWithPlaylist(shuffleMode: Int, playlist: Playlist) {
        val intent = Intent(this, MusicService::class.java)
        intent.action = ACTION_PLAY_PLAYLIST

        val bundle = Bundle()
        bundle.putParcelable(INTENT_EXTRA_PLAYLIST, playlist)
        bundle.putInt(INTENT_EXTRA_SHUFFLE_MODE, shuffleMode)

        intent.putExtras(bundle)

        startService(intent)
    }

    companion object {
        const val KEY_SHORTCUT_TYPE = "com.o4x.musical.appshortcuts.ShortcutType"
        const val SHORTCUT_TYPE_SHUFFLE_ALL = 0L
        const val SHORTCUT_TYPE_TOP_TRACKS = 1L
        const val SHORTCUT_TYPE_LAST_ADDED = 2L
        const val SHORTCUT_TYPE_NONE = 4L
    }
}
