package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.updateListItems
import com.o4x.musical.R
import com.o4x.musical.model.Playlist
import com.o4x.musical.model.Song
import com.o4x.musical.ui.activities.base.AbsMusicServiceActivity
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.ui.viewmodel.LibraryViewModel
import com.o4x.musical.util.PlaylistsUtil
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
class AddToPlaylistDialog : DialogFragment() {

    private val libraryViewModel by lazy {
        (activity as AbsMusicServiceActivity).libraryViewModel
    }

    var playlists = emptyList<Playlist>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val playlists = libraryViewModel.getLegacyPlaylist().value!!

        val playlistNames = mutableListOf<CharSequence>()
        playlistNames.add(requireActivity().resources.getString(R.string.action_new_playlist))
        for (playlist in playlists) {
            playlistNames.add(playlist.name)
        }

        return MaterialDialog(requireContext())
            .title(R.string.add_playlist_title)
            .listItems(items = playlistNames) { materialDialog, i, charSequence ->
                val songs: List<Song> = requireArguments().getParcelableArrayList("songs")
                    ?: return@listItems
                if (i == 0) {
                    materialDialog.dismiss()
                    CreatePlaylistDialog.create(songs)
                        .show(requireActivity().supportFragmentManager, "ADD_TO_PLAYLIST")
                } else {
                    materialDialog.dismiss()
                    PlaylistsUtil
                        .addToPlaylist(requireActivity(), songs, playlists[i - 1].id, true)
                }
            }
    }

    companion object {
        @JvmStatic
        fun create(song: Song): AddToPlaylistDialog {
            val list: MutableList<Song> = ArrayList()
            list.add(song)
            return create(list)
        }

        @JvmStatic
        fun create(songs: List<Song>?): AddToPlaylistDialog {
            val dialog = AddToPlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", ArrayList(songs))
            dialog.arguments = args
            return dialog
        }
    }
}