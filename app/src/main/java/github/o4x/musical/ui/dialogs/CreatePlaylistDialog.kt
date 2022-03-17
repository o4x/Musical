package github.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import github.o4x.musical.R
import github.o4x.musical.model.Song
import github.o4x.musical.util.PlaylistsUtil
import java.util.*

class CreatePlaylistDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext())
            .title(R.string.new_playlist_title)
            .positiveButton(R.string.create_action)
            .negativeButton(R.string.cancel)
            .input(
                hintRes = R.string.playlist_name_empty,
                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
            ) { _, charSequence ->
                if (activity == null) return@input
                val name: String = charSequence.toString().trim()
                if (name.isNotEmpty()) {
                    if (!PlaylistsUtil.doesPlaylistExist(requireActivity(), name)) {
                        val playlistId = PlaylistsUtil.createPlaylist(requireActivity(), name)
                        if (activity != null) {
                            val songs: ArrayList<Song>? = requireArguments().getParcelableArrayList(SONGS)
                            if (songs != null && songs.isNotEmpty()) {
                                PlaylistsUtil.addToPlaylist(requireActivity(), songs, playlistId, true)
                            }
                        }
                    } else {
                        Toast.makeText(
                            activity, requireActivity().resources.getString(
                                R.string.playlist_exists, name
                            ), Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
    }

    companion object {
        private const val SONGS = "songs"
        @JvmOverloads
        fun create(song: Song? = null): CreatePlaylistDialog {
            val list: MutableList<Song> = ArrayList()
            if (song != null) list.add(song)
            return create(list)
        }

        @JvmStatic
        fun create(songs: List<Song>?): CreatePlaylistDialog {
            val dialog = CreatePlaylistDialog()
            val args = Bundle()
            args.putParcelableArrayList(SONGS, ArrayList(songs!!))
            dialog.arguments = args
            return dialog
        }
    }
}