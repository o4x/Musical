package com.o4x.musical.db

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {

    single {
        Room.databaseBuilder(androidContext(), RetroDatabase::class.java, "playlist.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    factory {
        get<RetroDatabase>().lyricsDao()
    }

    factory {
        get<RetroDatabase>().playlistDao()
    }

    factory {
        get<RetroDatabase>().queueDao()
    }

    factory {
        get<RetroDatabase>().queueOriginalDao()
    }

    factory {
        get<RetroDatabase>().playCountDao()
    }

    factory {
        get<RetroDatabase>().historyDao()
    }
}