package com.o4x.musical.ui.dialogs

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.FileFilter
import com.afollestad.materialdialogs.files.folderChooser
import com.afollestad.materialdialogs.list.listItems
import com.o4x.musical.R
import java.io.File
import java.util.*

/**
 * @author Aidan Follestad (afollestad), modified by Karim Abou Zeid
 */
class BlacklistFolderChooserDialog : DialogFragment() {

    private var callback: FolderCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
            && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            return MaterialDialog(requireActivity())
                .title(R.string.error)
                .message(R.string.storage_perm_error)
                .positiveButton(R.string.ok)
        }

        val initialPath = getExternalStorageDirectory()

        return MaterialDialog(requireContext())
            .folderChooser(
                requireContext(),
                initialDirectory = initialPath,
                filter = { file -> file.isDirectory },
                emptyTextRes = R.string.empty
            ) { _, file ->
                callback?.onFolderSelection(this, file)
            }
            .negativeButton(android.R.string.cancel)
    }

    fun setCallback(callback: FolderCallback?) {
        this.callback = callback
    }

    interface FolderCallback {
        fun onFolderSelection(dialog: BlacklistFolderChooserDialog, folder: File)
    }

    companion object {
        fun create(): BlacklistFolderChooserDialog {
            return BlacklistFolderChooserDialog()
        }
    }
}