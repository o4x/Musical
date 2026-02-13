package github.o4x.musical.ui.viewmodel

import github.o4x.musical.model.Genre
import github.o4x.musical.model.Playlist
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModules = module {

    viewModel {
        ScrollPositionViewModel()
    }

    viewModel {
        LibraryViewModel(get())
    }

    viewModel { (albumId: Long) ->
        AlbumDetailsViewModel(
            get(),
            albumId
        )
    }

    viewModel { (artistId: Long) ->
        ArtistDetailsViewModel(
            get(),
            artistId
        )
    }

    viewModel { (playlist: Playlist) ->
        PlaylistDetailsViewModel(
            get(),
            playlist
        )
    }

    viewModel { (genre: Genre) ->
        GenreDetailsViewModel(
            get(),
            genre
        )
    }

    viewModel {
        PlayerViewModel()
    }

    viewModel {
        HomeHeaderViewModel(get())
    }
}