package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.o4x.musical.R
import com.o4x.musical.model.Song
import com.o4x.musical.util.PlaylistsUtil
import com.o4x.musical.util.PreferenceUtil.smartPlaylistLimit
import java.util.ArrayList

class ChangeSmartPlaylistLimit : DialogFragment() {

    companion object {
        @JvmStatic
        fun create(): ChangeSmartPlaylistLimit {
            return ChangeSmartPlaylistLimit()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext())
            .title(R.string.change_smart_playlist_limit)
            .positiveButton(R.string.change)
            .negativeButton(android.R.string.cancel)
            .input(
                prefill = smartPlaylistLimit.toString(),
                inputType = InputType.TYPE_CLASS_NUMBER
            ) { _, charSequence ->
                val num = Integer.parseInt(charSequence.toString())
                if (num >= 10) {
                    smartPlaylistLimit = num
                } else {
                    Toast.makeText(
                        activity, requireActivity().resources.getString(
                            R.string.limit_size
                        ), Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}