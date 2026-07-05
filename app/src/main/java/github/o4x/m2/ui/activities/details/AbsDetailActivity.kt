package github.o4x.m2.ui.activities.details

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.forEach
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import github.o4x.m2.App
import github.o4x.m2.R
import github.o4x.m2.databinding.ActivityDetailBinding
import github.o4x.m2.imageloader.glide.loader.GlideLoader
import github.o4x.m2.imageloader.glide.targets.palette.NotificationPaletteTargetListener
import github.o4x.m2.model.Song
import github.o4x.m2.ui.activities.base.AbsMusicPanelActivity
import github.o4x.m2.ui.adapter.song.DetailsSongAdapter
import github.o4x.m2.ui.viewmodel.ScrollPositionViewModel
import github.o4x.m2.util.BlurViewUtils.setupBlur
import github.o4x.m2.util.ColorUtil.withAlpha
import github.o4x.m2.util.Util
import github.o4x.m2.util.ViewInsetsUtils.applyAppBarPadding
import github.o4x.m2.util.ViewInsetsUtils.applyMiniPlayerPadding
import github.o4x.m2.util.ViewInsetsUtils.applySystemBarsPadding
import github.o4x.m2.util.ViewUtil
import github.o4x.m2.util.color.MediaNotificationProcessor
import github.o4x.m2.util.withAlpha
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.max
import kotlin.math.min

abstract class AbsDetailActivity<T> : AbsMusicPanelActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val TAG_EDITOR_REQUEST = 2001
        private const val COLOR_FADE_DURATION = 300L
    }

    val scrollPositionViewModel by viewModel<ScrollPositionViewModel>()

    // View Binding instance
    lateinit var binding: ActivityDetailBinding

    var data: T? = null

    var imageHeight: Int? = null
    var displayHeight: Int? = null
    var gradientHeight: Int? = null
    lateinit var colors: MediaNotificationProcessor
    private var paletteLoaded = false
    private var colorAnimator: ValueAnimator? = null

    var songAdapter: DetailsSongAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        binding.appbar.applySystemBarsPadding(applyTop = true)
        binding.appbarBlur.setupBlur(window, binding.blurTarget)

        setupImage()
        initObserver()
        setUpToolBar()
        setupViews()
    }

    override fun createContentView(): View? {
        val contentView = wrapSlidingMusicPanel(R.layout.activity_detail)
        val container = contentView.findViewById<ViewGroup>(R.id.content_container)
        val detailLayoutView = container.getChildAt(0)
        binding = ActivityDetailBinding.bind(detailLayoutView)

        return contentView
    }

    override fun onDestroy() {
        colorAnimator?.cancel()
        colorAnimator = null
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TAG_EDITOR_REQUEST) {
            refreshData()
            setResult(RESULT_OK)
        }
    }

    protected open fun refreshData() {}

    private fun setupImage() {
        imageHeight = Util.getScreenWidth()
        colors = MediaNotificationProcessor(this)
        displayHeight = Util.getScreenHeight()
        gradientHeight =
            (imageHeight!! + resources.getDimension(R.dimen.detail_header_height)).toInt()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    open fun setupViews() {
        setupScrollView()
    }

    protected fun setupSongsRecycler() {
        songAdapter = DetailsSongAdapter(
            this, getSongs(), R.layout.item_list, data!!, colors
        )
        binding.songRecycler.layoutManager = LinearLayoutManager(this)
        binding.songRecycler.adapter = songAdapter
        binding.songRecycler.applyMiniPlayerPadding()
        songAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                if (songAdapter!!.itemCount == 0) finish()
            }
        })
    }

    private fun setupScrollView() {
        binding.songRecycler.post {
            binding.songRecycler.layoutManager?.scrollToPosition(0)
            scrollPositionViewModel.setPosition(0)
            binding.image.translationY = 0f
        }

        binding.songRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                scrollPositionViewModel.addPosition(dy)
                val scrollY = scrollPositionViewModel.getPositionValue()

                onScrollChange(scrollY)
            }
        })
    }

    fun onScrollChange(scrollY: Int) {
        // Scroll poster. Compute the parallax divisor in floating point so that
        // wide/landscape windows (where displayHeight * 2 < imageHeight) don't
        // produce an integer 0 divisor and crash with a divide-by-zero.
        val parallaxDivisor = displayHeight!! * 2f / imageHeight!!
        binding.image.translationY =
            max(-scrollY / parallaxDivisor, -imageHeight!!.toFloat())
    }

    fun setAllColors(colors: MediaNotificationProcessor) {
        // Start the fade from wherever we currently are: the theme background on
        // the first load, or the previous palette color when the art changes.
        val fromColor =
            if (paletteLoaded) this.colors.backgroundColor
            else resolveThemeColor(android.R.attr.colorBackground)

        this.colors = colors
        this.paletteLoaded = true
        songAdapter?.colors = colors

        ViewUtil.setScrollBarColor(binding.songRecycler, colors.secondaryTextColor.withAlpha(.3f))
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = colors.isLight
        tintToolbarContent(colors.primaryTextColor)

        // Cross-fade the root background and the palette scrim (same BlurView the
        // mini player uses; it extends behind the status bar) so the palette
        // reveal reads as a smooth transition instead of an abrupt flash.
        val rootView = findViewById<View>(android.R.id.content).rootView
        val toColor = colors.backgroundColor
        colorAnimator?.cancel()
        colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), fromColor, toColor).apply {
            duration = COLOR_FADE_DURATION
            addUpdateListener {
                val blended = it.animatedValue as Int
                rootView.setBackgroundColor(blended)
                binding.appbarBlur.setOverlayColor(withAlpha(blended, SCRIM_ALPHA))
            }
            start()
        }
    }

    @ColorInt
    private fun resolveThemeColor(attr: Int): Int {
        val typedValue = TypedValue()
        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.data
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // The menu can be inflated after the palette arrived; re-apply the tint.
        if (paletteLoaded) tintToolbarContent(colors.primaryTextColor)
        return super.onPrepareOptionsMenu(menu)
    }

    private fun tintToolbarContent(@ColorInt color: Int) {
        binding.toolbar.setNavigationIconTint(color)
        binding.toolbar.overflowIcon?.setTint(color)
        binding.toolbar.menu.forEach { it.icon?.setTint(color) }
    }

    fun getImageLoader(): GlideLoader.GlideBuilder {
        // if we use this for context glide on load sync in rotation will crashed
        return GlideLoader.with(App.getContext())
            .withListener(object : NotificationPaletteTargetListener(this) {
                override fun onColorReady(colors: MediaNotificationProcessor) {
                    setAllColors(colors)
                    setMiniPlayerColor(colors)
                }
            }.apply { loadPlaceholderPalette = true })
    }

    abstract fun initObserver()
    abstract fun loadImage()
    protected abstract fun getSongs(): List<Song>
}
