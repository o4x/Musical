package github.o4x.musical.db

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val roomModule = module {

    single {
        Room.databaseBuilder(androidContext(), MusicalDatabase::class.java, "playlist.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }

    factory {
        get<MusicalDatabase>().lyricsDao()
    }

    factory {
        get<MusicalDatabase>().queueDao()
    }

    factory {
        get<MusicalDatabase>().queueOriginalDao()
    }

    factory {
        get<MusicalDatabase>().playCountDao()
    }

    factory {
        get<MusicalDatabase>().historyDao()
    }
}