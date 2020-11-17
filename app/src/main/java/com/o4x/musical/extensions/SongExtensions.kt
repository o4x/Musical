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
package com.o4x.musical.extensions

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.o4x.musical.db.QueueSongEntity
import com.o4x.musical.model.Song
import com.o4x.musical.repository.SongRepository
import com.o4x.musical.util.MusicUtil

fun Song.toQueueSongEntity() = QueueSongEntity(null, this.id)

fun List<Song>.toQueueSongEntityList() = map { it.toQueueSongEntity() }

fun List<QueueSongEntity>.toSongIDs() = map { it.id }.toLongArray()

fun List<Song>?.toSongIds() = this?.map { it.id }?.toLongArray() ?: LongArray(0)

fun List<Song?>.toQueue(): List<MediaSessionCompat.QueueItem> {
    return filter { it != null }.map {
        require(it != null)
        MediaSessionCompat.QueueItem(it.toDescription(), it.id)
    }
}

fun LongArray.toQueue(songsRepository: SongRepository): List<MediaSessionCompat.QueueItem> {
    val songList = songsRepository.songs(this)
    // the list returned above is sorted in default order, need to map it to same as the input array and preserve the original order
    songList.keepInOrder(this)?.let {
        return it.toQueue()
    } ?: return songList.toQueue()
}

fun List<Song>.keepInOrder(queue: LongArray): List<Song>? {
    //this may happen if user deletes some item from his library and then comes back to app after we stored the current queue ids
    //if the two arrays are different return the array as is
    if (size != queue.size) return this
    return if (isNotEmpty() && queue.isNotEmpty()) {
        val keepOrderList = Array(size, init = { Song.emptySong })
        forEach {
            keepOrderList[queue.indexOf(it.id)] = it
        }
        keepOrderList.asList()
    } else null
}

fun LongArray.toQueueSongEntityList(songsRepository: SongRepository): List<QueueSongEntity> {
    return songsRepository.songs(this).toQueueSongEntityList()
}

fun List<MediaSessionCompat.QueueItem>?.toIDList(): LongArray {
    return this?.map { it.queueId }?.toLongArray() ?: LongArray(0)
}

// TODO GLIDE
fun Song.toDescription(): MediaDescriptionCompat {
    return MediaDescriptionCompat.Builder()
            .setTitle(title)
            .setMediaId(id.toString())
            .setSubtitle(artistName)
            .setDescription(albumName)
            .setIconUri(MusicUtil.getMediaStoreAlbumCoverUri(albumId)).build()
}
