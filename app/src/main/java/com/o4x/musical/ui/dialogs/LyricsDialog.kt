package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.model.lyrics.Lyrics

class LyricsDialog : DialogFragment() {
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