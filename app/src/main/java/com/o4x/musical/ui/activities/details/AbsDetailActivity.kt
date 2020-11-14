package com.o4x.musical.ui.activities.details

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import code.name.monkey.appthemehelper.extensions.withAlpha
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.targets.palette.NotificationPaletteTargetListener
import com.o4x.musical.interfaces.PaletteColorHolder
import com.o4x.musical.model.Song
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.adapter.song.DetailsSongAdapter
import com.o4x.musical.ui.viewmodel.ScrollPositionViewModel
import com.o4x.musical.util.Util
import com.o4x.musical.util.ViewUtil
import com.o4x.musical.util.color.MediaNotificationProcessor
import kotlinx.android.synthetic.main.activity_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.max
import kotlin.math.min

abstract class AbsDetailActivity<T> : AbsMusicPanelActivity(), PaletteColorHolder {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val TAG_EDITOR_REQUEST = 2001
    }

    val scrollPositionViewModel by viewModel<ScrollPositionViewModel> {
        parametersOf(null)
    }

    var data: T? = null


    var imageHeight: Int? = null
    lateinit var colors: MediaNotificationProcessor

    var songAdapter: DetailsSongAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setDrawUnderStatusBar()

        setupImage()
        initObserver()
        setUpToolBar()
        setupViews()
    }

    override fun createContentView(): View? {
        return wrapSlidingMusicPanel(R.layout.activity_detail)
    }

    override fun getPaletteColor(): Int {
        return colors.backgroundColor
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAG_EDITOR_REQUEST) {
            initObserver()
            setResult(RESULT_OK)
        }
    }

    private fun setupImage() {
        imageHeight = Util.getScreenWidth()
        colors = MediaNotificationProcessor(this)
        image.drawableListener =
            object : NotificationPaletteTargetListener(this) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    setAllColors(colors)
                    setMiniPlayerColor(colors)
                }
            }
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.post {
            ToolbarContentTintHelper.colorizeToolbar(toolbar, colors.primaryTextColor, this)
            setAppbarAlpha(0f)
        }
    }

    open fun setupViews() {
        setupSongsRecycler()
        setupScrollView()
    }

    private fun setupSongsRecycler() {
        songAdapter = DetailsSongAdapter(
            this, getSongs(), R.layout.item_list,  this, data!!, colors
        )
        song_recycler.layoutManager = LinearLayoutManager(this)
        song_recycler.adapter = songAdapter
        songAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (songAdapter!!.itemCount == 0) finish()
            }
        })
    }


    private fun setupScrollView() {
        val displayHeight = Util.getScreenHeight()
        val gradientHeight =
            (imageHeight!! + resources.getDimension(R.dimen.detail_header_height)).toInt()

        song_recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrollPositionViewModel.addPosition(dy)
                val scrollY = scrollPositionViewModel.getPositionValue()

                // Change alpha of overlay
                val alpha = max(0f, min(1f, 2.toFloat() * scrollY / gradientHeight))
                setAppbarAlpha(alpha)

                // Scroll poster
                image.translationY =
                    max(-scrollY / (displayHeight * 2 / imageHeight!!), -imageHeight!!)
                        .toFloat()
            }
        })
    }


    fun setAllColors(colors: MediaNotificationProcessor) {
        this.colors = colors
        songAdapter?.colors = colors

        ToolbarContentTintHelper.colorizeToolbar(toolbar, colors.primaryTextColor, this)
        setNavigationBarColor(colors.backgroundColor)
        setTaskDescriptionColor(colors.backgroundColor)

        ViewUtil.setScrollBarColor(song_recycler, colors.secondaryTextColor.withAlpha(.3f))

        findViewById<View>(android.R.id.content).rootView.setBackgroundColor(colors.backgroundColor)
    }

    private fun setAppbarAlpha(alpha: Float) {
        toolbar.setBackgroundColor(
            withAlpha(colors.backgroundColor, alpha)
        )
        setStatusBarColor(
            withAlpha(colors.backgroundColor, alpha)
        )
        toolbar.setTitleTextColor(
            withAlpha(colors.primaryTextColor, alpha)
        )
    }

    fun getImageLoader(): GlideLoader.GlideBuilder {
        // if we use this for context glide on load sync in rotation will crashed
        return GlideLoader.with(App.getContext())
    }

    abstract fun initObserver()
    abstract fun loadImage()
    protected abstract fun getSongs(): List<Song>
}