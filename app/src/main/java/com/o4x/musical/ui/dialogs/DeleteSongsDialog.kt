package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.model.Song
import com.o4x.musical.util.MusicUtil
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
class DeleteSongsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val songs: List<Song>? = requireArguments().getParcelableArrayList("songs")
        val title: Int
        val content: CharSequence
        if (songs!!.size > 1) {
            title = R.string.delete_songs_title
            content = Html.fromHtml(getString(R.string.delete_x_songs, songs.size))
        } else {
            title = R.string.delete_song_title
            content = Html.fromHtml(getString(R.string.delete_song_x, songs[0].title))
        }
        return MaterialDialog(requireContext())
            .title(title)
            .message(text = content)
            .positiveButton(R.string.delete_action) {
                if (activity == null) return@positiveButton
                MusicUtil.deleteTracks(requireActivity(), songs)
            }
            .negativeButton(android.R.string.cancel)

    }

    companion object {
        @JvmStatic
        fun create(song: Song): DeleteSongsDialog {
            val list: MutableList<Song> = ArrayList()
            list.add(song)
            return create(list)
        }

        @JvmStatic
        fun create(songs: List<Song>?): DeleteSongsDialog {
            val dialog = DeleteSongsDialog()
            val args = Bundle()
            args.putParcelableArrayList("songs", ArrayList(songs!!))
            dialog.arguments = args
            return dialog
        }
    }
}