package com.o4x.musical.ui.fragments.mainactivity

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.R
import com.o4x.musical.extensions.primaryColor
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.MainActivity.MainActivityFragmentCallbacks
import com.o4x.musical.util.Util
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import kotlinx.android.synthetic.main.fragment_library.*
import kotlin.math.max
import kotlin.math.min

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsMainActivityFragment : Fragment(), MainActivityFragmentCallbacks {

    val mainActivity: MainActivity
        get() = activity as MainActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(getLayout(), container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.setStatusBarColorAuto()
        mainActivity.toolbar.setBackgroundColor(primaryColor())
        mainActivity.appbar.elevation = resources.getDimension(R.dimen.appbar_elevation)
        mainActivity.tabs.visibility = View.GONE
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun handleBackPress(): Boolean {
        return false
    }

    @LayoutRes
    abstract fun getLayout(): Int

    fun toolbarHeight(): Int {
        return mainActivity.toolbar.layoutParams.height
    }

    fun appbarHeight(): Int {
        val h = Util.getStatusBarHeight(mainActivity) + toolbarHeight()
        return if (mainActivity.tabs.visibility == View.VISIBLE) {
            h + resources.getDimension(R.dimen.tab_height).toInt()
        } else {
            h
        }
    }

    fun showAppbar() {
        val from = mainActivity.appbar.y.toInt()
        val to = 0
        val animation = ValueAnimator.ofInt(from, to)
        animation.duration =
            resources.getInteger(android.R.integer.config_mediumAnimTime).toLong() // milliseconds
        animation.addUpdateListener { animator: ValueAnimator ->
            mainActivity.appbar.y = (animator.animatedValue as Int).toFloat()
        }
        animation.start()
    }

    fun RecyclerView.addAppbarListener() {
        val appbarHeight = appbarHeight()
        val toolbarHeight = toolbarHeight()

        setPadding(0, appbarHeight, 0, 0)

        addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    when {
                        dy > 0 -> { // Scrolling up
                            val changes = max(-toolbarHeight.toFloat(), mainActivity.appbar.y - (dy))
                            mainActivity.appbar.y = changes
                            setPadding(0, (changes + appbarHeight).toInt(), 0, 0);
                        }
                        dy < 0 -> { // Scrolling down
                            val changes = min(0f, mainActivity.appbar.y - (dy))
                            mainActivity.appbar.y = changes
                            setPadding(0, (changes + appbarHeight).toInt(), 0, 0);
                        }
                        else -> { // on start page
                            showAppbar()
                        }
                    }

                }

            }
        )
    }

    fun setAppbarListener(recyclerView: FastScrollRecyclerView) {
        val appbarHeight = appbarHeight()
        val toolbarHeight = toolbarHeight()

        recyclerView.setPadding(0, appbarHeight, 0, 0)

        recyclerView.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                    when {
                        dy > 0 -> { // Scrolling up
                            val changes = max(-toolbarHeight.toFloat(), mainActivity.appbar.y - (dy))
                            mainActivity.appbar.y = changes
                            recyclerView.setPadding(0, (changes + appbarHeight).toInt(), 0, 0);
                        }
                        dy < 0 -> { // Scrolling down
                            val changes = min(0f, mainActivity.appbar.y - (dy))
                            mainActivity.appbar.y = changes
                            recyclerView.setPadding(0, (changes + appbarHeight).toInt(), 0, 0);
                        }
                        else -> { // on start page
                            showAppbar()
                        }
                    }

                }

            }
        )
    }
}