package github.o4x.m2.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import github.o4x.m2.R
import github.o4x.m2.extensions.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DeleteCachedDialog : AbsBlurDialogFragment() {

    companion object {
        @JvmStatic
        fun create(): DeleteCachedDialog {
            return DeleteCachedDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext())
            .title(R.string.pref_title_delete_cached_images)
            .message(R.string.are_you_sure)
            .positiveButton(R.string.yes) {
                Glide.get(requireContext()).clearMemory()

                lifecycleScope.launch(Dispatchers.IO) {
                    Glide.get(requireContext()).clearDiskCache()
                }

                showToast(R.string.delete_success)
            }
            .negativeButton(R.string.no)
    }

}