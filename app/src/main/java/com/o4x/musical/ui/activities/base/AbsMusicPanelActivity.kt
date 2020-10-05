package com.o4x.musical.ui.activities.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import butterknife.BindView
import butterknife.ButterKnife
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.ui.activities.PlayerActivity
import com.o4x.musical.ui.fragments.player.MiniPlayerFragment
import com.o4x.musical.ui.viewmodel.LibraryViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author Karim Abou Zeid (kabouzeid)
 *
 *
 * Do not use [.setContentView]. Instead wrap your layout with
 * [.wrapSlidingMusicPanel] first and then return it in [.createContentView]
 */
abstract class AbsMusicPanelActivity : AbsMusicServiceActivity() {

    protected val libraryViewModel by viewModel<LibraryViewModel>()

    private var miniPlayerFragment: MiniPlayerFragment? = null

    @JvmField
    @BindView(R.id.panel_container)
    var panelContainer: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createContentView())
        ButterKnife.bind(this)
        miniPlayerFragment =
            supportFragmentManager.findFragmentById(R.id.mini_player_fragment) as MiniPlayerFragment?
        miniPlayerFragment?.requireView()?.setOnClickListener { v: View? ->
            val myIntent = Intent(this@AbsMusicPanelActivity, PlayerActivity::class.java)
            this@AbsMusicPanelActivity.startActivity(myIntent)
        }
    }

    protected abstract fun createContentView(): View?
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onQueueChanged() {
        super.onQueueChanged()
        hideBottomBar(MusicPlayerRemote.playingQueue.isEmpty())
    }

    fun hideBottomBar(hide: Boolean) {
        if (hide) {
            panelContainer!!.visibility = View.GONE
        } else {
            panelContainer!!.visibility = View.VISIBLE
        }
    }

    protected fun wrapSlidingMusicPanel(@LayoutRes resId: Int): View {
        @SuppressLint("InflateParams") val slidingMusicPanelLayout =
            layoutInflater.inflate(R.layout.music_panel_layout, null)
        val contentContainer =
            slidingMusicPanelLayout.findViewById<ViewGroup>(R.id.content_container)
        layoutInflater.inflate(resId, contentContainer)
        return slidingMusicPanelLayout
    }

    override val snackBarContainer: View
        get() = findViewById(R.id.content_container)

    override fun onBackPressed() {
        if (!handleBackPress()) super.onBackPressed()
    }

    open fun handleBackPress(): Boolean {
        return false
    }
}