package com.o4x.musical.repository

import org.koin.dsl.module

val dataModule = module {
    single {
        Repository(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }

    single {
        SongRepository(get())
    }

    single {
        GenreRepository(get(), get())
    }

    single {
        AlbumRepository(get())
    }

    single {
        ArtistRepository(get(), get())
    }

    single {
        PlaylistRepository(get())
    }

    single {
        LastAddedRepository(
            get(),
            get(),
            get()
        )
    }

    single {
        SearchRepository(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }

    single {
        RoomRepository(get(), get(), get(), get(), get(), get())
    }
}