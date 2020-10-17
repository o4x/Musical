package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.o4x.musical.R
import com.o4x.musical.util.PlaylistsUtil

/**
 * @author Karim Abou Zeid (kabouzeid), Aidan Follestad (afollestad)
 */
class RenamePlaylistDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val playlistId = requireArguments().getLong(PLAYLIST_ID)
        return MaterialDialog(requireContext())
            .title(R.string.rename_playlist_title)
            .positiveButton(R.string.rename_action)
            .negativeButton(android.R.string.cancel)
            .input(
                hintRes = R.string.playlist_name_empty,
                prefill = PlaylistsUtil.getNameForPlaylist(
                    requireActivity(), playlistId
                ),
                inputType = InputType.TYPE_CLASS_TEXT or
                        InputType.TYPE_TEXT_VARIATION_PERSON_NAME or
                        InputType.TYPE_TEXT_FLAG_CAP_WORDS
            ) { _, charSequence ->
                val name: String = charSequence.toString().trim()
                if (name.isNotEmpty()) {
                    val playlistId1 = requireArguments().getLong(PLAYLIST_ID)
                    PlaylistsUtil.renamePlaylist(requireActivity(), playlistId1, name)
                }
            }
    }

    companion object {
        private const val PLAYLIST_ID = "playlist_id"
        @JvmStatic
        fun create(playlistId: Long): RenamePlaylistDialog {
            val dialog = RenamePlaylistDialog()
            val args = Bundle()
            args.putLong(PLAYLIST_ID, playlistId)
            dialog.arguments = args
            return dialog
        }
    }
}