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
package com.o4x.musical.db


import com.o4x.musical.extensions.equalsBy
import com.o4x.musical.extensions.toQueueSongEntityList
import com.o4x.musical.repository.SongRepository

interface QueueHelper {

    fun updateQueueSongs(
        queueSongs: LongArray?,
        currentSongId: Long?
    )

    fun updateQueueData(queueData: QueueEntity)
}

class RealQueueHelper(
    private val queueDao: QueueDao,
    private val songRepository: SongRepository
) : QueueHelper {

    override fun updateQueueSongs(queueSongs: LongArray?, currentSongId: Long?) {
        if (queueSongs == null || currentSongId == null) {
            return
        }
        val currentList = queueDao.getQueueSongsSync()
        val songListToSave = queueSongs.toQueueSongEntityList(songRepository)

        val listsEqual = currentList.equalsBy(songListToSave) { left, right ->
            left.id == right.id
        }
        if (queueSongs.isNotEmpty() && !listsEqual) {
            queueDao.clearQueueSongs()
            queueDao.insertAllSongs(songListToSave)
            setCurrentSongId(currentSongId)
        } else {
            setCurrentSongId(currentSongId)
        }
    }

    override fun updateQueueData(queueData: QueueEntity) = queueDao.insert(queueData)

    private fun setCurrentSongId(id: Long) = queueDao.setCurrentId(id)
}
