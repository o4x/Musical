/*
 * Copyright (c) 2020 Hemanth Savarla.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.o4x.musical.helper.menu

import android.app.Activity
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.o4x.musical.App.Companion.getContext
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.loader.PlaylistSongLoader.getPlaylistSongList
import com.o4x.musical.model.AbsCustomPlaylist
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.Song
import com.o4x.musical.ui.dialogs.AddToPlaylistDialog
import com.o4x.musical.ui.dialogs.DeletePlaylistDialog
import com.o4x.musical.ui.dialogs.RenamePlaylistDialog
import com.o4x.musical.util.PlaylistsUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import java.io.IOException

object PlaylistMenuHelper : KoinComponent {

    @JvmStatic
    fun handleMenuClick(
        activity: FragmentActivity,
        playlist: Playlist,
        item: MenuItem
    ): Boolean {
        when (item.itemId) {
            R.id.action_play -> {
                MusicPlayerRemote.openQueue(
                    getPlaylistSongs(activity, playlist), 0, true
                )
                return true
            }
            R.id.action_play_next -> {
                MusicPlayerRemote.playNext(getPlaylistSongs(activity, playlist))
                return true
            }
            R.id.action_add_to_playlist -> {
                AddToPlaylistDialog.create(getPlaylistSongs(activity, playlist))
                    .show(activity.supportFragmentManager, "ADD_PLAYLIST")
                return true
            }
            R.id.action_add_to_current_playing -> {
                MusicPlayerRemote.enqueue(getPlaylistSongs(activity, playlist))
                return true
            }
            R.id.action_rename_playlist -> {
                RenamePlaylistDialog.create(playlist.id)
                    .show(activity.supportFragmentManager, "RENAME_PLAYLIST")
                return true
            }
            R.id.action_delete_playlist -> {
                DeletePlaylistDialog.create(playlist)
                    .show(activity.supportFragmentManager, "DELETE_PLAYLIST")
                return true
            }
            R.id.action_save_playlist -> {
                activity.lifecycleScope.launch(IO) {
                    val msg = try {
                        String.format(
                            activity.getString(R.string.saved_playlist_to),
                            PlaylistsUtil.savePlaylist(
                                getContext().applicationContext, playlist
                            )
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                        String.format(
                            activity.getString(R.string.failed_to_save_playlist),
                            e
                        )
                    }
                    withContext(Main) {
                        Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
                    }
                }
                return true
            }
        }
        return false
    }

    private fun getPlaylistSongs(activity: Activity, playlist: Playlist): List<Song> {
        return if (playlist is AbsCustomPlaylist) playlist.getSongs()
        else getPlaylistSongList(
            activity,
            playlist.id
        )
    }
}
