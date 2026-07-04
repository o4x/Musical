package github.o4x.m2.preferences

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import github.o4x.m2.R
import github.o4x.m2.model.CategoryInfo
import github.o4x.m2.ui.adapter.CategoryInfoAdapter
import github.o4x.m2.prefs.PreferenceUtil
import github.o4x.m2.ui.dialogs.AbsBlurDialogFragment

class LibraryPreferenceDialog : AbsBlurDialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.preference_dialog_library_categories, null)

        val categoryAdapter = CategoryInfoAdapter(PreferenceUtil.libraryCategory)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = categoryAdapter
        categoryAdapter.attachToRecyclerView(recyclerView)

        return MaterialDialog(requireContext())
            .title(R.string.library_categories)
            .neutralButton(R.string.reset_action) {
                updateCategories(PreferenceUtil.defaultCategories)
            }
            .negativeButton(R.string.cancel)
            .positiveButton(R.string.ok) {
                updateCategories(categoryAdapter.categoryInfos)
            }
            .customView(view = view, horizontalPadding = true)
    }

    private fun updateCategories(categories: List<CategoryInfo>) {
        if (getSelected(categories) == 0) return
        if (getSelected(categories) > 5) {
            Toast.makeText(context, "Not more than 5 items", Toast.LENGTH_SHORT).show()
            return
        }
        PreferenceUtil.libraryCategory = categories
    }

    private fun getSelected(categories: List<CategoryInfo>): Int {
        var selected = 0
        for (categoryInfo in categories) {
            if (categoryInfo.visible)
                selected++
        }
        return selected
    }

    companion object {
        @JvmStatic
        fun newInstance(): LibraryPreferenceDialog {
            return LibraryPreferenceDialog()
        }
    }
}