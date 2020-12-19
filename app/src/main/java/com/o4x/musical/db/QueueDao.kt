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

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QueueDao {

    @Query("SELECT * FROM queue_songs")
    fun getQueueSongs(): LiveData<List<QueueEntity>>

    @Query("SELECT * FROM queue_songs")
    fun getQueueSongsSync(): List<QueueEntity>

    @Query("DELETE from queue_songs")
    fun clearQueueSongs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllSongs(songs: List<QueueEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSong(song: QueueEntity)

    @Delete
    fun delete(song: QueueEntity)
}
