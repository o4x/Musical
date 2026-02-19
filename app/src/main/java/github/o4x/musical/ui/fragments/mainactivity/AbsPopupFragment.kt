package github.o4x.musical.ui.fragments.mainactivity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import github.o4x.musical.util.Util

open class AbsPopupFragment(@LayoutRes layout: Int) : AbsMainActivityFragment(layout) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        mainActivity.setDrawerEnabled(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        mainActivity.setDrawerEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Util.hideSoftKeyboard(mainActivity)
            mainActivity.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}