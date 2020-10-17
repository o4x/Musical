package com.o4x.musical.ui.dialogs

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.files.folderChooser
import com.o4x.musical.R
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment.ArrayListPathsAsyncTask
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment.ArrayListPathsAsyncTask.OnPathsListedCallback
import com.o4x.musical.util.PreferenceUtil.startDirectory
import com.o4x.musical.util.scanPaths
import java.lang.ref.WeakReference

/**
 * @author Aidan Follestad (afollestad), modified by Karim Abou Zeid
 */
class ScanMediaFolderChooserDialog : DialogFragment() {

    private var initialPath = startDirectory

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

        return MaterialDialog(requireContext())
            .folderChooser(
                requireContext(),
                initialDirectory = initialPath,
                filter = { file -> file.isDirectory },
                emptyTextRes = R.string.empty
            ) { _, file ->
                    val applicationContext = requireActivity().applicationContext
                    val activityWeakReference = WeakReference<Activity?>(activity)
                    dismiss()
                    ArrayListPathsAsyncTask(
                        activity,
                        object : OnPathsListedCallback {
                            override fun onPathsListed(paths: Array<String>) {
                                scanPaths(paths)
                            }
                        }).execute(
                        ArrayListPathsAsyncTask.LoadingInfo(
                            file, FoldersFragment.AUDIO_FILE_FILTER
                        )
                    )
            }
            .positiveButton(R.string.action_scan_directory)
            .noAutoDismiss()
            .negativeButton(android.R.string.cancel) {
                dismiss()
            }
    }

    companion object {
        fun create(): ScanMediaFolderChooserDialog {
            return ScanMediaFolderChooserDialog()
        }
    }
}