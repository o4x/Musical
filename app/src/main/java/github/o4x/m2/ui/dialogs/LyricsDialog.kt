package github.o4x.m2.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import github.o4x.m2.model.lyrics.Lyrics

class LyricsDialog : AbsBlurDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext())
            .title(text = requireArguments().getString("title"))
            .message(text = requireArguments().getString("lyrics"))
    }

    companion object {
        @JvmStatic
        fun create(lyrics: Lyrics): LyricsDialog {
            val dialog = LyricsDialog()
            val args = Bundle()
            args.putString("title", lyrics.song.title)
            args.putString("lyrics", lyrics.text)
            dialog.arguments = args
            return dialog
        }
    }
}