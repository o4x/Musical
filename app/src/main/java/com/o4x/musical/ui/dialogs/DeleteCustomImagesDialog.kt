package com.o4x.musical.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.o4x.musical.R
import com.o4x.musical.extensions.showToast
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.util.CustomImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeleteCustomImagesDialog : DialogFragment() {

    companion object {
        @JvmStatic
        fun create(): DeleteCustomImagesDialog {
            return DeleteCustomImagesDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext())
            .title(R.string.pref_title_delete_custom_images)
            .message(R.string.are_you_sure)
            .positiveButton(R.string.yes) {

                lifecycleScope.launch(Dispatchers.IO) {
                    CustomImageUtil.deleteAll()
                }

                showToast(R.string.delete_success)
            }
            .negativeButton(R.string.no)
    }
}