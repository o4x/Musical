/*
 * Copyright (c) 2019 Naman Dwivedi.
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
package com.o4x.musical.playback.players

import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.STATE_NONE

import com.o4x.musical.db.QueueDao
import com.o4x.musical.model.MediaID
import com.o4x.musical.repository.SongRepository

class MediaSessionCallback(
    private val mediaSession: MediaSessionCompat,
    private val songPlayer: SongPlayer,
    private val songRepository: SongRepository,
    private val queueDao: QueueDao
) : MediaSessionCompat.Callback() {

    override fun onPause() = songPlayer.pause()

    override fun onPlay() = songPlayer.playSong()

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        query?.let {
            val song = songRepository.songs(query)
            if (song.isNotEmpty()) {
                songPlayer.playSong(song.first())
            }
        } ?: onPlay()
    }

    override fun onPlayFromMediaId(mediaId: String, extras: Bundle?) {
        val songId = MediaID().fromString(mediaId).mediaId!!.toLong()
        songPlayer.playSong(songId)

        if (extras == null) return
        val queue = extras.getLongArray(SONGS_LIST)
        val seekTo = extras.getInt(SEEK_TO_POS)
        val queueTitle = extras.getString(QUEUE_TITLE) ?: ""

        if (queue != null) {
            songPlayer.setQueue(queue, queueTitle)
        }
        if (seekTo > 0) {
            songPlayer.seekTo(seekTo)
        }
    }

    override fun onSeekTo(pos: Long) = songPlayer.seekTo(pos.toInt())

    override fun onSkipToNext() = songPlayer.nextSong()

    override fun onSkipToPrevious() = songPlayer.previousSong()

    override fun onStop() = songPlayer.stop()

    override fun onSetRepeatMode(repeatMode: Int) {
        super.onSetRepeatMode(repeatMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        songPlayer.setPlaybackState(
                PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                        .setExtras(bundle.apply {
                            putInt(REPEAT_MODE, repeatMode)
                        }
                        ).build()
        )
    }

    override fun onSetShuffleMode(shuffleMode: Int) {
        super.onSetShuffleMode(shuffleMode)
        val bundle = mediaSession.controller.playbackState.extras ?: Bundle()
        songPlayer.setPlaybackState(
                PlaybackStateCompat.Builder(mediaSession.controller.playbackState)
                        .setExtras(bundle.apply {
                            putInt(SHUFFLE_MODE, shuffleMode)
                        }).build()
        )
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        when (action) {
            ACTION_SET_MEDIA_STATE -> setSavedMediaSessionState()
            ACTION_REPEAT_SONG -> songPlayer.repeatSong()
            ACTION_REPEAT_QUEUE -> songPlayer.repeatQueue()

            ACTION_PLAY_NEXT -> {
                val nextSongId = extras!!.getLong(SONG)
                songPlayer.playNext(nextSongId)
            }

            ACTION_QUEUE_REORDER -> {
                val from = extras!!.getInt(QUEUE_FROM)
                val to = extras.getInt(QUEUE_TO)
                songPlayer.swapQueueSongs(from, to)
            }

            ACTION_SONG_DELETED -> {
                val id = extras!!.getLong(SONG)
                songPlayer.removeFromQueue(id)
            }

            ACTION_RESTORE_MEDIA_SESSION -> restoreMediaSession()
        }
    }

    private fun setSavedMediaSessionState() {
        // Only set saved session from db if we know there is not any active media session
        val controller = mediaSession.controller ?: return
        if (controller.playbackState == null || controller.playbackState.state == STATE_NONE) {
            val queueData = queueDao.getQueueDataSync() ?: return
            songPlayer.restoreFromQueueData(queueData)
        } else {
            // Force update the playback state and metadata from the media session so that the
            // attached Observer in NowPlayingViewModel gets the current state.
            restoreMediaSession()
        }
    }

    private fun restoreMediaSession() {
        songPlayer.setPlaybackState(mediaSession.controller.playbackState)
        mediaSession.setMetadata(mediaSession.controller.metadata)
    }

    companion object {
        const val SONGS_LIST = "songs_list"
        const val QUEUE_TITLE = "queue_title"
        const val SEEK_TO_POS = "seek_to_pos"
        const val ACTION_SET_MEDIA_STATE = "action_set_media_state"
        const val ACTION_REPEAT_SONG = "action_repeat_song"
        const val ACTION_REPEAT_QUEUE = "action_repeat_queue"
        const val ACTION_PLAY_NEXT = "action_play_next"
        const val ACTION_QUEUE_REORDER = "action_queue_reorder"
        const val ACTION_SONG_DELETED = "action_song_deleted"
        const val ACTION_REMOVED_FROM_PLAYLIST = "action_removed_from_playlist"
        const val ACTION_RESTORE_MEDIA_SESSION = "action_restore_media_session"
        const val ACTION_CAST_CONNECTED = "action_cast_connected"
        const val ACTION_CAST_DISCONNECTED = "action_cast_disconnected"
        const val ALBUM = "album"
        const val ARTIST = "artist"
        const val SONG = "song"
        const val SONGS = "songs"
        const val QUEUE_FROM = "queue_from"
        const val QUEUE_TO = "queue_to"
        const val REPEAT_MODE = "repeat_mode"
        const val SHUFFLE_MODE = "shuffle_mode"
        const val CATEGORY_SONG_DATA = "category_song_data"
        const val NOW_PLAYING = "now_playing"
        const val ACTION_PLAY_PAUSE = "action_play_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
        const val APP_PACKAGE_NAME = "com.naman14.timberx"
    }
}
