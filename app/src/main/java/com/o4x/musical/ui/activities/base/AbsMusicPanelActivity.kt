package com.o4x.musical.ui.activities.base

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.extensions.surfaceColor
import code.name.monkey.appthemehelper.extensions.textColorPrimary
import code.name.monkey.appthemehelper.extensions.textColorSecondary
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.o4x.musical.R
import com.o4x.musical.interfaces.CabCallback
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.ui.activities.PlayerActivity
import com.o4x.musical.ui.fragments.player.MiniPlayerFragment
import com.o4x.musical.ui.viewmodel.LibraryViewModel
import com.o4x.musical.util.PreferenceUtil
import com.o4x.musical.util.color.MediaNotificationProcessor
import kotlinx.android.synthetic.main.music_panel_layout.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * @author Karim Abou Zeid (kabouzeid)
 *
 *
 * Do not use [.setContentView]. Instead wrap your layout with
 * [.wrapSlidingMusicPanel] first and then return it in [.createContentView]
 */
abstract class AbsMusicPanelActivity : AbsMusicServiceActivity(), CabHolder {

    val libraryViewModel by viewModel<LibraryViewModel>()

    private var cab: AttachedCab? = null
    private lateinit var miniPlayerFragment: MiniPlayerFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(createContentView())
        ButterKnife.bind(this)

        addMusicServiceEventListener(libraryViewModel)
        PreferenceUtil.registerOnSharedPreferenceChangedListener(libraryViewModel)

        miniPlayerFragment =
            supportFragmentManager.findFragmentById(R.id.mini_player_fragment) as MiniPlayerFragment
        miniPlayerFragment.requireView().setOnClickListener { _: View? ->
            val myIntent = Intent(this@AbsMusicPanelActivity, PlayerActivity::class.java)
            this@AbsMusicPanelActivity.startActivity(myIntent)
        }
        libraryViewModel.getQueue().observe(this, {
            hideBottomBar(it.isEmpty())
        })
    }

    override fun onDestroy() {
        PreferenceUtil.unregisterOnSharedPreferenceChangedListener(libraryViewModel)
        removeMusicServiceEventListener(libraryViewModel)
        super.onDestroy()
    }

    protected abstract fun createContentView(): View?

    override fun openCab(menuRes: Int, callback: CabCallback): AttachedCab {
        if (cab != null && cab!!.isActive()) cab!!.destroy()

        cab = createCab(R.id.cab_stub) {
            closeDrawable(R.drawable.ic_close)
            backgroundColor(literal = surfaceColor())
            titleColor(literal = textColorPrimary())
            subtitleColor(literal = textColorSecondary())
            popupTheme(PreferenceUtil.getGeneralThemeRes())
            menu(menuRes)
            menuIconColor(literal = textColorPrimary())

            onCreate { cab, menu ->
                callback.onCreate(cab, menu)
            }
            onSelection { item ->
                return@onSelection callback.onSelection(item)
            }
            onDestroy { cab ->
                return@onDestroy callback.onDestroy(cab)
            }
        }

        return cab!!
    }

    fun hideBottomBar(hide: Boolean) {
        if (hide) {
            panel_container?.visibility = View.GONE
        } else {
            panel_container?.visibility = View.VISIBLE
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
        if (cab != null && cab!!.isActive()) {
            cab!!.destroy()
            return true
        }
        return false
    }

    fun setMiniPlayerColor(colors: MediaNotificationProcessor) {
        miniPlayerFragment.setColor(colors)
    }
}