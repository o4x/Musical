package com.o4x.musical.ui.fragments.mainactivity

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.o4x.musical.R
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.misc.OverScrollGridLayoutManager
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.misc.VerticalScrollListener
import com.o4x.musical.misc.isRecyclerScrollable
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.MainActivity.MainActivityFragmentCallbacks
import com.o4x.musical.ui.viewmodel.LibraryViewModel
import com.o4x.musical.util.Util
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import kotlin.math.max
import kotlin.math.min


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsMainActivityFragment(@LayoutRes layout: Int) : Fragment(layout), MainActivityFragmentCallbacks {

    val libraryViewModel: LibraryViewModel by sharedViewModel()

    val mainActivity: MainActivity
        get() = requireActivity() as MainActivity


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.setStatusBarColorAuto()
        mainActivity.toolbar.setBackgroundColor(surfaceColor())
        mainActivity.appbar.elevation = resources.getDimension(R.dimen.appbar_elevation)
        hideSubToolbar()
        showAppbar()
    }

    override fun onResume() {
        super.onResume()
        mainActivity.appbar.elevation = resources.getDimension(R.dimen.appbar_elevation)
    }

    override fun onDestroy() {
        animation?.cancel()
        super.onDestroy()
    }

    fun hideSubToolbar() {
        mainActivity.search.visibility = View.GONE
        mainActivity.tabs.visibility = View.GONE
        mainActivity.bread_crumbs.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun handleBackPress(): Boolean {
        return false
    }

    fun toolbarHeight(): Int {
        return mainActivity.toolbar.layoutParams.height
    }

    fun appbarHeight(): Int {
        val h = Util.getStatusBarHeight(mainActivity) + toolbarHeight()
        return if (mainActivity.tabs.visibility == View.VISIBLE || mainActivity.bread_crumbs.visibility == View.VISIBLE) {
            h + resources.getDimension(R.dimen.tab_height).toInt()
        } else {
            h
        }
    }

    private var animation: ValueAnimator? = null
    fun showAppbar() {
        mainActivity.appbar.setExpanded(true, true)
        val from = mainActivity.appbar.y.toInt()
        val to = 0
        animation?.cancel()
        animation = ValueAnimator.ofInt(from, to)
        animation?.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        animation?.addUpdateListener { animator: ValueAnimator ->
            mainActivity.appbar.y = (animator.animatedValue as Int).toFloat()
        }
        animation?.start()
    }

    fun setAppbarPadding(view: View) {
        view.setPadding(0, appbarHeight(), 0, 0)
    }

    fun RecyclerView.addAppbarListener() {
        setAppbarListener(this)
    }

    fun setAppbarListener(recyclerView: RecyclerView) {
        val appbarHeight = appbarHeight()
        val toolbarHeight = toolbarHeight()

        setAppbarPadding(recyclerView)

        val handler = { dy: Int ->
            when {
                dy > 0 -> { // Scrolling up
                    val changes = max(-toolbarHeight.toFloat(),
                        mainActivity.appbar.y - (dy))
                    mainActivity.appbar.y = changes
                    recyclerView.setPadding(0, (changes + appbarHeight).toInt(), 0, 0)
                }
                dy < 0 -> { // Scrolling down
                    val changes = min(0f, mainActivity.appbar.y - (dy))
                    mainActivity.appbar.y = changes
                    recyclerView.setPadding(0, (changes + appbarHeight).toInt(), 0, 0)
                }
            }
        }

        val verticalScrollListener =
            object : VerticalScrollListener() {
                override fun onScroll(dy: Int) {
                    if (recyclerView.isRecyclerScrollable()) {
                        animation?.cancel()
                        handler(dy)
                    } else {
                        setAppbarPadding(recyclerView)
                    }
                }
            }

        val lm = recyclerView.layoutManager
        if (lm is OverScrollLinearLayoutManager) {
            lm.setOnVerticalScrollListener(verticalScrollListener)
        } else if (lm is OverScrollGridLayoutManager) {
            lm.setOnVerticalScrollListener(verticalScrollListener)
        }
    }
}