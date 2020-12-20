package com.o4x.musical.ui.fragments.mainactivity

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.extensions.colorControlNormal
import code.name.monkey.appthemehelper.extensions.surfaceColor
import code.name.monkey.appthemehelper.extensions.textColorTertiary
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.o4x.musical.R
import com.o4x.musical.misc.OverScrollGridLayoutManager
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.misc.VerticalScrollListener
import com.o4x.musical.misc.isRecyclerScrollable
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.MainActivity.MainActivityFragmentCallbacks
import com.o4x.musical.ui.fragments.AbsMusicServiceFragment
import com.o4x.musical.util.Util
import com.o4x.musical.util.ViewUtil
import kotlin.math.max
import kotlin.math.min


/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsMainActivityFragment(@LayoutRes layout: Int) :
    AbsMusicServiceFragment(layout), MainActivityFragmentCallbacks {

    val mainActivity: MainActivity by lazy { requireActivity() as MainActivity }

    val libraryViewModel by lazy { mainActivity.libraryViewModel }
    val navController by lazy { mainActivity.navController }

    // animations //
    private var appbarAnimation: ValueAnimator? = null
    private val toolbarAnimation = ValueAnimator.ofFloat(0f, 1f)
    private val statusAnimation = ValueAnimator.ofFloat(0f, 1f)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showStatusBar()
        showAppbar()
        onReloadSubToolbar()
    }

    override fun onResume() {
        super.onResume()
        mainActivity.backPressCallbacks.add(this)
        setToolbarTitle(navController.currentDestination?.label.toString())
        mainActivity.appbar.elevation = resources.getDimension(R.dimen.appbar_elevation)
    }

    override fun onPause() {
        super.onPause()
        mainActivity.backPressCallbacks.remove(this)
    }

    override fun onDestroy() {
        appbarAnimation?.cancel()
        toolbarAnimation.cancel()
        statusAnimation.cancel()
        super.onDestroy()
    }

    open fun showStatusBar() {
        mainActivity.setStatusBarColorAuto()
        mainActivity.toolbar.setBackgroundColor(surfaceColor())
    }

    open fun onReloadSubToolbar() {
        mainActivity.search.visibility = View.GONE
        mainActivity.tabs.visibility = View.GONE
        mainActivity.bread_crumbs.visibility = View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val color = colorControlNormal()
        ToolbarContentTintHelper.colorizeToolbar(mainActivity.toolbar,
            color, serviceActivity)
        ToolbarContentTintHelper.tintAllIcons(menu, color)
        mainActivity.toggle.drawerArrowDrawable.color = color
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(mainActivity, mainActivity.toolbar)
        super.onPrepareOptionsMenu(menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun handleBackPress(): Boolean {
        return false
    }

    fun setToolbarTitle(title: String?) {
        mainActivity.toolbar.title = title
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


    fun showAppbar() {
        mainActivity.appbar.setExpanded(true, true)
        val from = mainActivity.appbar.y.toInt()
        val to = 0
        appbarAnimation?.cancel()
        appbarAnimation = ValueAnimator.ofInt(from, to)
        appbarAnimation?.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        appbarAnimation?.addUpdateListener { animator: ValueAnimator ->
            mainActivity.appbar.y = (animator.animatedValue as Int).toFloat()
        }
        appbarAnimation?.start()
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

        val handler = { dy: Int, withAppBar: Boolean ->
            when {
                dy > 0 -> { // Scrolling up
                    val changes = max(-toolbarHeight.toFloat(),
                        mainActivity.appbar.y - (dy))
                    if (withAppBar)
                    mainActivity.appbar.y = changes
                    recyclerView.setPadding(0, (changes + appbarHeight).toInt(), 0, 0)
                }
                dy < 0 -> { // Scrolling down
                    val changes = min(0f, mainActivity.appbar.y - (dy))
                    if (withAppBar)
                    mainActivity.appbar.y = changes
                    recyclerView.setPadding(0, (changes + appbarHeight).toInt(), 0, 0)
                }
            }
        }

        val verticalScrollListener =
            object : VerticalScrollListener() {
                override fun onScroll(dy: Int) {
                    if (recyclerView.isRecyclerScrollable()) {
                        appbarAnimation?.cancel()
                        handler(dy, true)
                    } else {
                        if (mainActivity.appbar.y != 0f) {
                            if (appbarAnimation?.isRunning != true)
                                showAppbar()
                        }
                        if (recyclerView.paddingTop != appbarHeight) {
                            handler(dy, false)
                        }
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

    fun toolbarColorVisible(show: Boolean) {
        toolbarAnimation.cancel()

        val color = surfaceColor()
        val current = ViewUtil.getViewBackgroundColor(mainActivity.toolbar)

        // break if current color equal final color
        if (
            show && current == ColorUtil.withAlpha(current, 1f)
            ||
            !show && current == ColorUtil.withAlpha(current, 0f)
        ) {
            mainActivity.toolbar.setBackgroundColor(
                if (show) color else ColorUtil.withAlpha(
                    color,
                    0f
                )
            )
            return
        }

        toolbarAnimation.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        toolbarAnimation.addUpdateListener { animator: ValueAnimator ->
            val f = if (show) animator.animatedFraction else 1 - animator.animatedFraction
            mainActivity.toolbar.setBackgroundColor(ColorUtil.withAlpha(color, f))
        }
        toolbarAnimation.start()
    }

    fun statusBarColorVisible(show: Boolean) {
        statusAnimation.cancel()

        val color = ColorUtil.darkenColor(surfaceColor())
        val current = mainActivity.window.statusBarColor

        // break if current color equal final color
        if (
            show && current == ColorUtil.withAlpha(current, 1f)
            ||
            !show && current == ColorUtil.withAlpha(current, 0f)
        ) {
            mainActivity.window.statusBarColor = if (show) color else ColorUtil.withAlpha(color, 0f)
            return
        }

        statusAnimation.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        statusAnimation.addUpdateListener { animator: ValueAnimator ->
            val f = if (show) animator.animatedFraction else 1 - animator.animatedFraction
            mainActivity.window.statusBarColor = ColorUtil.withAlpha(color, f)
        }
        mainActivity.setLightStatusBarAuto(color)
        statusAnimation.start()
    }
}