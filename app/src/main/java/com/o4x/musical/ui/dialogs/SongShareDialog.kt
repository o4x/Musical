package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.o4x.musical.R
import com.o4x.musical.model.Song
import com.o4x.musical.util.MusicUtil

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class SongShareDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val song: Song? = requireArguments().getParcelable("song")
        val currentlyListening =
            getString(R.string.currently_listening_to_x_by_x, song?.title, song?.artistName)
        return MaterialDialog(requireContext())
            .title(R.string.what_do_you_want_to_share)
            .listItems(items = arrayOf(
                getString(R.string.the_audio_file), "\u201C" + currentlyListening + "\u201D"
            ).asList()) { _, i, _ ->
                when (i) {
                    0 -> startActivity(
                        Intent.createChooser(
                            MusicUtil.createShareSongFileIntent(
                                song!!, context
                            ), null
                        )
                    )
                    1 -> requireActivity().startActivity(
                        Intent.createChooser(
                            Intent()
                                .setAction(Intent.ACTION_SEND)
                                .putExtra(Intent.EXTRA_TEXT, currentlyListening)
                                .setType("text/plain"),
                            null
                        )
                    )
                }
            }
    }

    companion object {
        @JvmStatic
        fun create(song: Song?): SongShareDialog {
            val dialog = SongShareDialog()
            val args = Bundle()
            args.putParcelable("song", song)
            dialog.arguments = args
            return dialog
        }
    }
}