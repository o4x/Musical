package com.o4x.musical.ui.activities.details

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Spanned
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.util.ColorUtil.withAlpha
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.afollestad.materialcab.MaterialCab
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.util.DialogUtils
import com.google.android.material.textview.MaterialTextView
import com.o4x.musical.R
import com.o4x.musical.imageloader.universalil.listener.PaletteMusicLoadingListener
import com.o4x.musical.imageloader.universalil.loader.UniversalIL
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.interfaces.PaletteColorHolder
import com.o4x.musical.model.Song
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.adapter.song.DetailsSongAdapter
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.Util
import com.o4x.musical.util.color.MediaNotificationProcessor
import kotlinx.android.synthetic.main.fragment_search.*
import kotlin.math.max
import kotlin.math.min

abstract class AbsDetailActivity : AbsMusicPanelActivity(), PaletteColorHolder, CabHolder {

    @BindView(R.id.song_recycler)
    lateinit var songRecyclerView: RecyclerView
    @BindView(R.id.image)
    lateinit var image: ImageView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    var cab: MaterialCab? = null
    var imageHeight = 0
    var colors: MediaNotificationProcessor? = null

    var wiki: Spanned? = null
    var wikiDialog: MaterialDialog? = null

    lateinit var songAdapter: DetailsSongAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setDrawUnderStatusBar()
        setUpToolBar()
        setupViews()
        initObserver()
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
    }

    open fun setupViews() {
        setupScrollView()
        setupSongsRecycler()
    }

    private fun setupScrollView() {
        val displayHeight = Util.getScreenHeight()
        imageHeight = Util.getScreenWidth()
        val gradientHeight =
            (imageHeight + resources.getDimension(R.dimen.detail_header_height)).toInt()
        songRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            private var scrollY = 0
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                scrollY += dy

                // Change alpha of overlay
                val alpha = max(0f, min(1f, 2.toFloat() * scrollY / gradientHeight))
                setAppbarAlpha(alpha)

                // Scroll poster
                image.translationY =
                    max(-scrollY / (displayHeight * 2 / imageHeight), -imageHeight)
                        .toFloat()
            }
        })
    }

    private fun setupSongsRecycler() {
        songAdapter = DetailsSongAdapter(this, getSongs(), R.layout.item_list, false, this)
        songRecyclerView.layoutManager = LinearLayoutManager(this)
        songRecyclerView.adapter = songAdapter
        songAdapter.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (songAdapter.itemCount == 0) finish()
            }
        })
    }

    private fun setColors(color: Int, colors: MediaNotificationProcessor?) {
        if (colors != null) {
            this.colors = colors
            ToolbarContentTintHelper.colorizeToolbar(toolbar, colors.primaryTextColor, this)
            setNavigationBarColor(colors.actionBarColor)
            setTaskDescriptionColor(colors.actionBarColor)
        }

        findViewById<View>(android.R.id.content).rootView.setBackgroundColor(color)
        setAppbarAlpha(0f)
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

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        initObserver()
    }

    val imageLoader: UniversalIL
        get() = UniversalIL(
            image,
            object : PaletteMusicLoadingListener() {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    setColors(colors.backgroundColor, colors)
                    setMiniPlayerColor(colors)
                    songAdapter.colors = colors
                }
            }, imageHeight
        )

    abstract fun initObserver()
    abstract fun loadImage()
    protected abstract fun getSongs(): List<Song>

    companion object {
        const val TAG_EDITOR_REQUEST = 2001
    }
}