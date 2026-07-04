package github.o4x.m2.ui.dialogs


import android.app.Dialog
import android.os.Bundle
import com.afollestad.materialdialogs.MaterialDialog
import github.o4x.m2.R

class DiscardTagsDialog : AbsBlurDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialDialog(requireContext())
            .message(R.string.discard_changes)
            .neutralButton(R.string.cancel)
            .positiveButton(R.string.ok) {
                activity?.finish()
            }
    }

    companion object {
        fun create(): DiscardTagsDialog {
            return DiscardTagsDialog()
        }
    }
}