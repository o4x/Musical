package com.o4x.musical.provider

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.o4x.musical.model.Song
import com.o4x.musical.repository.RealSongRepository
import com.o4x.musical.repository.SortedLongCursor

class QueueStore
/**
 * Constructor of `MusicPlaybackState`
 *
 * @param context The [Context] to use
 */(private val context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, VERSION
) {

    companion object {
        private var sInstance: QueueStore? = null
        const val DATABASE_NAME = "queue_store.db"
        const val PLAYING_QUEUE_TABLE_NAME = "playing_queue"
        const val ORIGINAL_PLAYING_QUEUE_TABLE_NAME = "original_playing_queue"
        private const val VERSION = 1

        /**
         * @param context The [Context] to use
         * @return A new instance of this class.
         */
        @Synchronized
        fun getInstance(context: Context): QueueStore {
            if (sInstance == null) {
                sInstance = QueueStore(context.applicationContext)
            }
            return sInstance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        createTable(db, PLAYING_QUEUE_TABLE_NAME)
        createTable(db, ORIGINAL_PLAYING_QUEUE_TABLE_NAME)
    }

    private fun createTable(db: SQLiteDatabase, tableName: String) {
        val builder = StringBuilder()
        builder.append("CREATE TABLE IF NOT EXISTS ")
        builder.append(tableName)
        builder.append("(")
        builder.append(BaseColumns._ID)
        builder.append(" LONG NOT NULL);")
        db.execSQL(builder.toString())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // not necessary yet
        db.execSQL("DROP TABLE IF EXISTS $PLAYING_QUEUE_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $ORIGINAL_PLAYING_QUEUE_TABLE_NAME")
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // If we ever have downgrade, drop the table to be safe
        db.execSQL("DROP TABLE IF EXISTS $PLAYING_QUEUE_TABLE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $ORIGINAL_PLAYING_QUEUE_TABLE_NAME")
        onCreate(db)
    }

    @Synchronized
    fun saveQueues(playingQueue: List<Song>, originalPlayingQueue: List<Song>) {
        saveQueue(PLAYING_QUEUE_TABLE_NAME, playingQueue)
        saveQueue(ORIGINAL_PLAYING_QUEUE_TABLE_NAME, originalPlayingQueue)
    }

    /**
     * Clears the existing database and saves the queue into the db so that when the
     * app is restarted, the tracks you were listening to is restored
     *
     * @param queue the queue to save
     */
    @Synchronized
    private fun saveQueue(tableName: String, queue: List<Song>) {
        val database = writableDatabase
        database.beginTransaction()
        try {
            database.delete(tableName, null, null)
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
        val NUM_PROCESS = 20
        var position = 0
        while (position < queue.size) {
            database.beginTransaction()
            try {
                var i = position
                while (i < queue.size && i < position + NUM_PROCESS) {
                    val song = queue[i]
                    val values = ContentValues(4)
                    values.put(BaseColumns._ID, song.id)
                    database.insert(tableName, null, values)
                    i++
                }
                database.setTransactionSuccessful()
            } finally {
                database.endTransaction()
                position += NUM_PROCESS
            }
        }
    }

    val savedPlayingQueue: List<Song>
        get() = getQueue(PLAYING_QUEUE_TABLE_NAME)

    val savedOriginalPlayingQueue: List<Song>
        get() = getQueue(ORIGINAL_PLAYING_QUEUE_TABLE_NAME)

    private fun getQueue(tableName: String): List<Song> {
        val cursor = makeRecentTracksCursorImpl(tableName)
        return RealSongRepository(context).songs(cursor)
    }

    private fun queryQueueIds(tableName: String): Cursor {
        val database = readableDatabase
        return database.query(
            tableName,
            arrayOf(BaseColumns._ID), null, null, null, null, null
        )
    }

    private fun makeRecentTracksCursorImpl(
        tableName: String
    ): SortedLongCursor? {
        val songs =
            queryQueueIds(tableName)
        return songs.use {
            makeSortedCursor(
                it,
                it.getColumnIndex(BaseColumns._ID)
            )
        }
    }

    private fun makeSortedCursor(
        cursor: Cursor?, idColumn: Int
    ): SortedLongCursor? {

        if (cursor != null && cursor.moveToFirst()) {
            // create the list of ids to select against
            val selection = StringBuilder()
            selection.append(BaseColumns._ID)
            selection.append(" IN (")

            // this tracks the order of the ids
            val order = LongArray(cursor.count)

            var id = cursor.getLong(idColumn)
            selection.append(id)
            order[cursor.position] = id

            while (cursor.moveToNext()) {
                selection.append(",")

                id = cursor.getLong(idColumn)
                order[cursor.position] = id
                selection.append(id.toString())
            }

            selection.append(")")

            // get a list of songs with the data given the selection statement
            val songCursor = RealSongRepository(context)
                .makeSongCursor(selection.toString(), null)
            if (songCursor != null) {
                // now return the wrapped TopTracksCursor to handle sorting given order
                return SortedLongCursor(
                    songCursor,
                    order,
                    BaseColumns._ID
                )
            }
        }

        return null
    }
}