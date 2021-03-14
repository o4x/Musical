package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R

class SetTagsDialog(private val on: On) : DialogFragment() {
    open class On {
        open fun allTags() {}
        open fun justImage() {}
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        return MaterialDialog(requireContext())
            .title(R.string.download_tags)
            .message(R.string.download_tags_message)
            .neutralButton(R.string.cancel)
            .negativeButton(R.string.just_image) {
                on.justImage()
            }
            .positiveButton(R.string.all_tags) {
                on.allTags()
            }
    }

    companion object {
        @JvmStatic
        fun create(on: On): SetTagsDialog {
            return SetTagsDialog(on)
        }
    }
}