/*
 * Copyright (c) 2019 Hemanth Savarala.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by
 *  the Free Software Foundation either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.o4x.musical.preferences

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat.SRC_IN
import androidx.core.text.HtmlCompat
import androidx.fragment.app.DialogFragment
import code.name.monkey.appthemehelper.common.prefs.supportv7.ATEDialogPreference
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.afollestad.materialdialogs.list.updateListItems
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.extensions.colorControlNormal
import com.o4x.musical.provider.BlacklistStore
import com.o4x.musical.ui.dialogs.BlacklistFolderChooserDialog
import java.io.File
import java.util.*

class BlacklistPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : ATEDialogPreference(context, attrs, defStyleAttr, defStyleRes) {

    init {
        icon?.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                context.colorControlNormal(),
                SRC_IN
            )
    }
}

class BlacklistPreferenceDialog : DialogFragment(), BlacklistFolderChooserDialog.FolderCallback {
    companion object {
        fun newInstance(): BlacklistPreferenceDialog {
            return BlacklistPreferenceDialog()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val chooserDialog =
            childFragmentManager.findFragmentByTag("FOLDER_CHOOSER") as BlacklistFolderChooserDialog?
        chooserDialog?.setCallback(this)
        refreshBlacklistData()
        return MaterialDialog(requireContext())
            .title(R.string.blacklist)
            .positiveButton(R.string.done)
            .neutralButton(R.string.clear_action) {
                MaterialDialog(requireContext())
                    .title(R.string.clear_blacklist)
                    .positiveButton(R.string.clear_action) {
                        BlacklistStore.getInstance(
                            it.context
                        ).clear()
                    }
                    .negativeButton(android.R.string.cancel)
                    .show()
            }
            .negativeButton(R.string.add_action) {
                val dialog = BlacklistFolderChooserDialog.create()
                dialog.setCallback(this@BlacklistPreferenceDialog)
                dialog.show(requireActivity().supportFragmentManager, "FOLDER_CHOOSER")
            }
            .listItems(items = paths, waitForPositiveButton = false) { _, _, text ->
                MaterialDialog(requireContext())
                    .title(R.string.remove_from_blacklist)
                    .message(
                        text = HtmlCompat.fromHtml(
                            String.format(
                                getString(
                                    R.string.do_you_want_to_remove_from_the_blacklist
                                ),
                                text
                            ),
                            HtmlCompat.FROM_HTML_MODE_LEGACY
                        )
                    )
                    .positiveButton(R.string.remove_action) {
                        BlacklistStore.getInstance(App.getContext())
                            .removePath(File(text.toString()))
                        refreshBlacklistData()
                    }
                    .negativeButton(android.R.string.cancel)
                    .show()
            }
    }

    private lateinit var paths: ArrayList<String>

    private fun refreshBlacklistData() {
        this.paths = BlacklistStore.getInstance(App.getContext()).paths
        val dialog = dialog as MaterialDialog?
        dialog?.updateListItems(items = paths)
    }

    override fun onFolderSelection(dialog: BlacklistFolderChooserDialog, folder: File) {
        BlacklistStore.getInstance(App.getContext()).addPath(folder)
        refreshBlacklistData()
    }
}
