package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.Html
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.model.Playlist
import com.o4x.musical.util.PlaylistsUtil
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class DeletePlaylistDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlists: ArrayList<Playlist>? = requireArguments().getParcelableArrayList("playlists")
        val title: Int
        val content: CharSequence
        if (playlists!!.size > 1) {
            title = R.string.delete_playlists_title
            content = Html.fromHtml(getString(R.string.delete_x_playlists, playlists.size))
        } else {
            title = R.string.delete_playlist_title
            content = Html.fromHtml(getString(R.string.delete_playlist_x, playlists[0].name))
        }
        return MaterialDialog(requireContext())
            .title(title)
            .message(text = content)
            .positiveButton(R.string.delete_action) {
                if (activity == null) return@positiveButton
                PlaylistsUtil.deletePlaylists(requireActivity(), playlists)
            }
            .negativeButton(R.string.cancel)
    }

    companion object {
        @JvmStatic
        fun create(playlist: Playlist): DeletePlaylistDialog {
            val list: MutableList<Playlist> = ArrayList()
            list.add(playlist)
            return create(list)
        }

        @JvmStatic
        fun create(playlists: List<Playlist>): DeletePlaylistDialog {
            val dialog = DeletePlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList("playlists", ArrayList(playlists))
            dialog.arguments = args
            return dialog
        }
    }
}