package com.o4x.musical.ui.activities.details

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.afollestad.materialcab.MaterialCab
import com.o4x.musical.R
import com.o4x.musical.extensions.withAlpha
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.targets.MusicColoredTargetListener
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.interfaces.PaletteColorHolder
import com.o4x.musical.model.Song
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.adapter.song.DetailsSongAdapter
import com.o4x.musical.ui.viewmodel.ScrollPositionViewModel
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.Util
import com.o4x.musical.util.ViewUtil
import com.o4x.musical.util.color.MediaNotificationProcessor
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.max
import kotlin.math.min

abstract class AbsDetailActivity<T> : AbsMusicPanelActivity(), PaletteColorHolder, CabHolder {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val TAG_EDITOR_REQUEST = 2001
    }

    val scrollPositionViewModel by viewModel<ScrollPositionViewModel> {
        parametersOf(null)
    }

    var data: T? = null

    @BindView(R.id.song_recycler)
    lateinit var songRecyclerView: RecyclerView
    @BindView(R.id.image)
    lateinit var image: ImageView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    var cab: MaterialCab? = null
    var imageHeight: Int? = null
    var colors: MediaNotificationProcessor? = null

    var songAdapter: DetailsSongAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ButterKnife.bind(this)
        setDrawUnderStatusBar()

        imageHeight = Util.getScreenWidth()
        initObserver()
        setUpToolBar()
        setupViews()
    }

    override fun createContentView(): View? {
        return wrapSlidingMusicPanel(R.layout.activity_detail)
    }

    override fun getPaletteColor(): Int {
        return colors!!.actionBarColor
    }

    override fun openCab(menuRes: Int, callback: MaterialCab.Callback): MaterialCab {
        if (cab != null && cab!!.isActive) cab!!.finish()
        cab = MaterialCab(this, R.id.cab_stub)
            .setMenu(menuRes)
            .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
            .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(paletteColor))
            .start(object : MaterialCab.Callback {
                override fun onCabCreated(materialCab: MaterialCab, menu: Menu): Boolean {
                    return callback.onCabCreated(materialCab, menu)
                }

                override fun onCabItemClicked(menuItem: MenuItem): Boolean {
                    return callback.onCabItemClicked(menuItem)
                }

                override fun onCabFinished(materialCab: MaterialCab): Boolean {
                    return callback.onCabFinished(materialCab)
                }
            })
        return cab!!
    }

    override fun onBackPressed() {
        if (cab != null && cab!!.isActive) cab!!.finish() else {
            songRecyclerView.stopScroll()
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAG_EDITOR_REQUEST) {
            initObserver()
            setResult(RESULT_OK)
        }
    }

    private fun setUpToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.post {
            ToolbarContentTintHelper.colorizeToolbar(toolbar, colors!!.primaryTextColor, this)
            setAppbarAlpha(0f)
        }
    }

    open fun setupViews() {
        setupSongsRecycler()
        setupScrollView()
    }

    private fun setupSongsRecycler() {
        songAdapter = DetailsSongAdapter(
            this, getSongs(), R.layout.item_list,  this, data!!, colors!!
        )
        songRecyclerView.layoutManager = LinearLayoutManager(this)
        songRecyclerView.adapter = songAdapter
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

        songRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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


    public fun setColors(color: Int, colors: MediaNotificationProcessor?) {
        if (colors != null) {
            this.colors = colors
            songAdapter?.colors = colors
            ToolbarContentTintHelper.colorizeToolbar(toolbar, colors.primaryTextColor, this)
            setNavigationBarColor(colors.actionBarColor)
            setTaskDescriptionColor(colors.actionBarColor)

            ViewUtil.setScrollBarColor(songRecyclerView, colors.secondaryTextColor.withAlpha(.3f))
        }

        findViewById<View>(android.R.id.content).rootView.setBackgroundColor(color)
    }

    private fun setAppbarAlpha(alpha: Float) {
        if (colors == null) return
        toolbar.setBackgroundColor(
            withAlpha(colors!!.actionBarColor, alpha)
        )
        setStatusBarColor(
            withAlpha(colors!!.actionBarColor, alpha)
        )
        toolbar.setTitleTextColor(
            withAlpha(colors!!.primaryTextColor, alpha)
        )
    }

    val imageLoader: GlideLoader.GlideBuilder
        get() = GlideLoader.with(this)
            .withListener(object : MusicColoredTargetListener() {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    setColors(colors.backgroundColor, colors)
                    setMiniPlayerColor(colors)
                }
            })

    abstract fun initObserver()
    abstract fun loadImage()
    abstract fun loadImageSync()
    protected abstract fun getSongs(): List<Song>
}