package com.o4x.musical.ui.fragments.mainactivity

import android.animation.ValueAnimator
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import code.name.monkey.appthemehelper.util.ColorUtil
import com.o4x.musical.extensions.primaryColor
import com.o4x.musical.ui.activities.MainActivity
import com.o4x.musical.ui.activities.MainActivity.MainActivityFragmentCallbacks
import com.o4x.musical.util.Util

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsMainActivityFragment : Fragment(), MainActivityFragmentCallbacks {

    val mainActivity: MainActivity
        get() = activity as MainActivity

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
        return Util.getStatusBarHeight(mainActivity) + toolbarHeight()
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
}