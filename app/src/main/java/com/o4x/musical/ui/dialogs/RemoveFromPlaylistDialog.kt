package com.o4x.musical.ui.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.model.PlaylistSong
import com.o4x.musical.util.PlaylistsUtil
import java.util.*

class RemoveFromPlaylistDialog : DialogFragment() {

    @SuppressLint("StringFormatInvalid")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs: ArrayList<PlaylistSong>? = requireArguments().getParcelableArrayList("songs")
        val title: Int
        val content: CharSequence
        if (songs!!.size > 1) {
            title = R.string.remove_songs_from_playlist_title
            content = Html.fromHtml(getString(R.string.remove_x_songs_from_playlist, songs.size))
        } else {
            title = R.string.remove_song_from_playlist_title
            content = Html.fromHtml(getString(R.string.remove_song_x_from_playlist, songs[0].title))
        }
        return MaterialDialog(requireContext())
            .title(title)
            .message(text = content)
            .positiveButton(R.string.remove_action) {
                if (activity == null) return@positiveButton
                PlaylistsUtil.removeFromPlaylist(requireActivity(), songs)
            }
            .negativeButton(R.string.cancel)
    }

    companion object {
        @JvmStatic
        fun create(song: PlaylistSong): RemoveFromPlaylistDialog {
            val list: MutableList<PlaylistSong> = ArrayList()
            list.add(song)
            return create(list)
        }

        @JvmStatic
        fun create(songs: List<PlaylistSong>?): RemoveFromPlaylistDialog {
            val dialog = RemoveFromPlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", ArrayList(songs!!))
            dialog.arguments = args
            return dialog
        }
    }
}