package com.o4x.musical.ui.activities.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import code.name.monkey.appthemehelper.extensions.surfaceColor
import code.name.monkey.appthemehelper.extensions.textColorPrimary
import code.name.monkey.appthemehelper.extensions.textColorSecondary
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.o4x.musical.R
import com.o4x.musical.databinding.MusicPanelLayoutBinding
import com.o4x.musical.interfaces.CabCallback
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.ui.activities.PlayerActivity
import com.o4x.musical.ui.fragments.player.MiniPlayerFragment
import com.o4x.musical.prefs.PreferenceUtil
import com.o4x.musical.ui.activities.intro.PermissionActivity
import com.o4x.musical.util.color.MediaNotificationProcessor

/**
 * @author Karim Abou Zeid (kabouzeid)
 *
 *
 * Do not use [.setContentView]. Instead wrap your layout with
 * [.wrapSlidingMusicPanel] first and then return it in [.createContentView]
 */
abstract class AbsMusicPanelActivity : AbsMusicServiceActivity(), CabHolder {

    private var cab: AttachedCab? = null
    private lateinit var miniPlayerFragment: MiniPlayerFragment

    private val binding by lazy { MusicPanelLayoutBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(createContentView())

        miniPlayerFragment =
            supportFragmentManager.findFragmentById(R.id.mini_player_fragment) as MiniPlayerFragment
        miniPlayerFragment.requireView().setOnClickListener {
            val myIntent = Intent(this@AbsMusicPanelActivity, PlayerActivity::class.java)
            this@AbsMusicPanelActivity.startActivity(myIntent)
        }
        playerViewModel.queue.observe(this, {
            hideBottomBar(it.isEmpty())
        })
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

    private fun hideBottomBar(hide: Boolean) {
        if (hide) {
            binding.panelContainer.visibility = View.GONE
        } else {
            binding.panelContainer.visibility = View.VISIBLE
        }
    }

    protected fun wrapSlidingMusicPanel(@LayoutRes resId: Int): View {
        val contentContainer =
            binding.root.findViewById<ViewGroup>(R.id.content_container)
        layoutInflater.inflate(resId, contentContainer)
        return binding.root
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