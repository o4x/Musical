package github.o4x.m2.ui.activities.base

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import github.o4x.m2.R
import github.o4x.m2.databinding.MusicPanelLayoutBinding
import github.o4x.m2.ui.activities.PlayerActivity
import github.o4x.m2.ui.fragments.player.MiniPlayerFragment
import github.o4x.m2.util.ViewInsetsUtils.applySystemBarsPadding
import github.o4x.m2.util.color.MediaNotificationProcessor

abstract class AbsMusicPanelActivity : AbsMusicServiceActivity() {

    lateinit var miniPlayerFragment: MiniPlayerFragment

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
        playerViewModel.queue.observe(this) {
            hideBottomBar(it.isEmpty())
        }

        binding.container.applySystemBarsPadding(applyBottom = true)
    }

    protected abstract fun createContentView(): View?

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



    protected fun wrapSlidingMusicPanel(view: View): View {
        val contentContainer =
            binding.root.findViewById<ViewGroup>(R.id.content_container)
        contentContainer.addView(view)
        return binding.root
    }

    override val snackBarContainer: View
        get() = findViewById(R.id.content_container)

    fun setMiniPlayerColor(colors: MediaNotificationProcessor) {
        miniPlayerFragment.setColor(colors)
    }

    fun setMiniPlayerProgressColor(@ColorInt color: Int) {
        miniPlayerFragment.setProgressColor(color)
    }
}