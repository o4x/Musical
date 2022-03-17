package github.o4x.musical.extensions

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import github.o4x.musical.R
import github.o4x.musical.model.Genre
import github.o4x.musical.model.Playlist
import github.o4x.musical.ui.fragments.mainactivity.datails.AbsDetailFragment
import github.o4x.musical.ui.fragments.mainactivity.datails.AbsDetailFragment.Companion.EXTRA
import github.o4x.musical.ui.fragments.mainactivity.datails.GenreDetailFragment
import github.o4x.musical.ui.fragments.mainactivity.datails.PlaylistDetailFragment


fun Fragment.navigate(@IdRes id: Int) = findNavController().navigate(id)

fun Fragment.findNavController(@IdRes id: Int): NavController {
    val fragment = childFragmentManager.findFragmentById(id) as NavHostFragment
    return fragment.navController
}

fun Fragment.findActivityNavController(@IdRes id: Int): NavController {
    return requireActivity().findNavController(id)
}

fun AppCompatActivity.findNavController(@IdRes id: Int): NavController {
    val fragment = supportFragmentManager.findFragmentById(id) as NavHostFragment
    return fragment.navController
}

fun NavController.toPlaylistDetail(playlist: Playlist) {
    val bundle = bundleOf(EXTRA to playlist)
    this.navigate(R.id.action_to_playlist, bundle)
}

fun NavController.toGenreDetail(genre: Genre) {
    val bundle = bundleOf(EXTRA to genre)
    this.navigate(R.id.action_to_genre, bundle)
}