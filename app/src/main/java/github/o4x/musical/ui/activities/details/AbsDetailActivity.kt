package github.o4x.musical.ui.activities.details

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import github.o4x.musical.App
import github.o4x.musical.R
import github.o4x.musical.databinding.ActivityDetailBinding
import github.o4x.musical.imageloader.glide.loader.GlideLoader
import github.o4x.musical.imageloader.glide.targets.palette.NotificationPaletteTargetListener
import github.o4x.musical.model.Song
import github.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import github.o4x.musical.ui.adapter.song.DetailsSongAdapter
import github.o4x.musical.ui.viewmodel.ScrollPositionViewModel
import github.o4x.musical.util.ColorUtil.withAlpha
import github.o4x.musical.util.Util
import github.o4x.musical.util.ViewUtil
import github.o4x.musical.util.color.MediaNotificationProcessor
import github.o4x.musical.util.withAlpha
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.max
import kotlin.math.min

abstract class AbsDetailActivity<T> : AbsMusicPanelActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        const val TAG_EDITOR_REQUEST = 2001
    }

    val scrollPositionViewModel by viewModel<ScrollPositionViewModel>()

    // View Binding instance
    lateinit var binding: ActivityDetailBinding

    var data: T? = null

    var imageHeight: Int? = null
    var displayHeight: Int? = null
    var gradientHeight: Int? = null
    lateinit var colors: MediaNotificationProcessor

    var songAdapter: DetailsSongAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        displayHeight = Util.getScreenHeight()
        gradientHeight =
            (imageHeight!! + resources.getDimension(R.dimen.detail_header_height)).toInt()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.post {
            setAppbarAlpha(0f)
        }
    }

    open fun setupViews() {
        setupSongsRecycler()
        setupScrollView()
    }

    private fun setupSongsRecycler() {
        songAdapter = DetailsSongAdapter(
            this, getSongs(), R.layout.item_list, data!!, colors
        )
        binding.songRecycler.layoutManager = LinearLayoutManager(this)
        binding.songRecycler.adapter = songAdapter
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
            setAppbarAlpha(0f)
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
        // Scroll poster
        binding.image.translationY =
            max(-scrollY / (displayHeight!! * 2 / imageHeight!!), -imageHeight!!)
                .toFloat()
    }

    fun setAllColors(colors: MediaNotificationProcessor) {
        this.colors = colors
        songAdapter?.colors = colors


        ViewUtil.setScrollBarColor(binding.songRecycler, colors.secondaryTextColor.withAlpha(.3f))

        findViewById<View>(android.R.id.content).rootView.setBackgroundColor(colors.backgroundColor)
    }

    private fun setAppbarAlpha(alpha: Float) {
        binding.toolbar.setBackgroundColor(
            withAlpha(colors.backgroundColor, alpha)
        )
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
