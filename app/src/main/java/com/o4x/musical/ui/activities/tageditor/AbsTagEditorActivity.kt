package com.o4x.musical.ui.activities.tageditor

import android.app.SearchManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.palette.graphics.Palette
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.ATHUtil
import code.name.monkey.appthemehelper.util.TintHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.o4x.musical.R
import com.o4x.musical.extensions.appHandleColor
import com.o4x.musical.ui.activities.base.AbsBaseActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AbsSearchOnlineActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity
import com.o4x.musical.util.ImageUtil
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.TagUtil
import com.o4x.musical.util.TagUtil.ArtworkInfo
import org.jaudiotagger.tag.FieldKey
import java.io.Serializable
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsTagEditorActivity<RM : Serializable?> : AbsBaseActivity() {
    @JvmField
    @BindView(R.id.play_pause_fab)
    var fab: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.search_online_btn)
    var searchBtn: AppCompatButton? = null

    @JvmField
    @BindView(R.id.nested_scroll_view)
    var scrollView: NestedScrollView? = null

    @JvmField
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.image)
    var image: ImageView? = null

    @JvmField
    @BindView(R.id.header)
    var header: LinearLayout? = null

    @JvmField
    @BindView(R.id.song_name)
    var songName: TextInputEditText? = null
    @JvmField
    @BindView(R.id.album_name)
    var albumName: TextInputEditText? = null
    @JvmField
    @BindView(R.id.artist_name)
    var artistName: TextInputEditText? = null
    @JvmField
    @BindView(R.id.genre_name)
    var genreName: TextInputEditText? = null
    @JvmField
    @BindView(R.id.year)
    var year: TextInputEditText? = null
    @JvmField
    @BindView(R.id.track_number)
    var trackNumber: TextInputEditText? = null
    @JvmField
    @BindView(R.id.lyrics)
    var lyrics: TextInputEditText? = null

    protected var id = 0
        private set
    private var headerVariableSpace = 0
    private var paletteColorPrimary = 0
    private var colorPrimary = 0
    private var textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {
            dataChanged()
        }
    }
    private var albumArtBitmap: Bitmap? = null
    private var deleteAlbumArt = false
    @JvmField
    protected var tagUtil: TagUtil? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewLayout)
        ButterKnife.bind(this)
        intentExtras
        createTagUtil()
        headerVariableSpace = resources.getDimensionPixelSize(R.dimen.tagEditorHeaderVariableSpace)
        setupViews()
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.action_tag_editor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private val intentExtras: Unit
        get() {
            val intentExtras = intent.extras
            if (intentExtras != null) {
                id = intentExtras.getInt(EXTRA_ID)
            }
        }

    private fun createTagUtil() {
        val songPaths = songPaths
        if (songPaths.isEmpty()) {
            finish()
            return
        }
        tagUtil = TagUtil(this, songPaths)
    }

    private fun setupViews() {
        setupScrollView()
        setupFab()
        setupSearchButton()
        setupImageView()
        setupTextInputEditTexts()
    }

    private fun setupScrollView() {
        scrollView!!.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                image!!.translationY = scrollY / 2f
            }
        )
    }

    private fun setupFab() {
        fab!!.scaleX = 0f
        fab!!.scaleY = 0f
        fab!!.isEnabled = false
        fab!!.setOnClickListener { save() }
        TintHelper.setTintAuto(fab!!, ThemeStore.accentColor(this), true)
    }

    private fun setupSearchButton() {
        searchBtn!!.setBackgroundColor(ThemeStore.primaryColor(this))
        searchBtn!!.setOnClickListener { searchOnline() }
    }

    private fun setupImageView() {
        loadCurrentImage()
        val items = arrayOf<CharSequence>(
            getString(R.string.download_from_last_fm),
            getString(R.string.pick_from_local_storage),
            getString(R.string.web_search),
            getString(R.string.remove_cover)
        )
        image!!.setOnClickListener {
            MaterialDialog.Builder(this@AbsTagEditorActivity)
                .title(R.string.update_image)
                .items(*items)
                .itemsCallback { _: MaterialDialog?, _: View?, which: Int, _: CharSequence? ->
                    when (which) {
                        0 -> getImageFromLastFM()
                        1 -> startImagePicker()
                        2 -> searchImageOnWeb()
                        3 -> deleteImage()
                    }
                }.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupTextInputEditTexts() {
        fillViewsWithFileTags()
        if (songName != null) songName!!.addTextChangedListener(textWatcher)
        if (albumName != null) albumName!!.addTextChangedListener(textWatcher)
        if (artistName != null) artistName!!.addTextChangedListener(textWatcher)
        if (genreName != null) genreName!!.addTextChangedListener(textWatcher)
        if (year != null) year!!.addTextChangedListener(textWatcher)
        if (trackNumber != null) trackNumber!!.addTextChangedListener(textWatcher)
        if (lyrics != null) lyrics!!.addTextChangedListener(textWatcher)
    }

    private fun fillViewsWithFileTags() {
        if (songName != null) songName!!.setText(tagUtil!!.songTitle)
        if (albumName != null) albumName!!.setText(tagUtil!!.albumTitle)
        if (artistName != null) artistName!!.setText(tagUtil!!.artistName)
        if (genreName != null) genreName!!.setText(tagUtil!!.genreName)
        if (year != null) year!!.setText(tagUtil!!.songYear)
        if (trackNumber != null) trackNumber!!.setText(tagUtil!!.trackNumber)
        if (lyrics != null) lyrics!!.setText(tagUtil!!.lyrics)
    }

    protected abstract fun fillViewsWithResult(result: RM)
    protected fun dataChanged() {
        showFab()
    }

    private fun showFab() {
        fab!!.animate()
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .scaleX(1f)
            .scaleY(1f)
            .start()
        fab!!.isEnabled = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> if (resultCode == RESULT_OK) {
                val selectedImage = data!!.data
                loadImageFromFile(selectedImage)
            }
            AbsSearchOnlineActivity.REQUEST_CODE -> try {
                if (resultCode == RESULT_OK) {
                    val extras = data!!.extras
                    if (extras != null) {
                        if (extras.containsKey(AlbumSearchActivity.EXTRA_RESULT_ALL)) {
                            val result =
                                extras.getSerializable(AbsSearchOnlineActivity.EXTRA_RESULT_ALL) as RM?
                            result?.let { fillViewsWithResult(it) }
                        } else if (extras.containsKey(AbsSearchOnlineActivity.EXTRA_RESULT_COVER)) {
                            loadImageFromUrl(
                                extras.getString(AbsSearchOnlineActivity.EXTRA_RESULT_COVER)
                            )
                        }
                    }
                } else {
                    Log.i(TAG, "ResultCode = $resultCode")
                }
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
            }
            else -> {
            }
        }
    }

    protected fun setImageBitmap(bitmap: Bitmap?, bgColor: Int) {
        if (bitmap == null) {
            image!!.setImageResource(R.drawable.default_album_art)
        } else {
            image!!.setImageBitmap(bitmap)
        }
        setColors(bgColor)
    }

    private fun setColors(color: Int) {
        paletteColorPrimary = color
        albumName?.appHandleColor()
//        TintHelper.colorHandles(songName!!, color)
//        TintHelper.colorHandles(albumName!!, color)
//        TintHelper.colorHandles(artistName!!, color)
//        TintHelper.colorHandles(genreName!!, color)
//        TintHelper.colorHandles(year!!, color)
//        TintHelper.colorHandles(trackNumber!!, color)
//        TintHelper.colorHandles(lyrics!!, color)

        colorPrimary = ATHUtil.resolveColor(this, R.attr.colorSurface)
        header!!.setBackgroundColor(colorPrimary)
        toolbar!!.setBackgroundColor(colorPrimary)
        setStatusBarColor(colorPrimary)
        setNavigationBarColor(colorPrimary)
        setTaskDescriptionColor(colorPrimary)
    }

    protected fun searchWebFor(vararg keys: String?) {
        val stringBuilder = StringBuilder()
        for (key in keys) {
            stringBuilder.append(key)
            stringBuilder.append(" ")
        }
        val intent = Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, stringBuilder.toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun startImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent,
            getString(R.string.pick_from_local_storage)), REQUEST_CODE_SELECT_IMAGE)
    }

    private fun loadCurrentImage() {
        val bitmap = tagUtil!!.albumArt
        setImageBitmap(bitmap,
            PhonographColorUtil.getColor(PhonographColorUtil.generatePalette(bitmap),
                ATHUtil.resolveColor(this, R.attr.defaultFooterColor)))
        deleteAlbumArt = false
    }

    private fun deleteImage() {
        setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.default_album_art),
            ATHUtil.resolveColor(this, R.attr.defaultFooterColor))
        deleteAlbumArt = true
        dataChanged()
    }

    private fun save() {
        val fieldKeyValueMap: MutableMap<FieldKey, String> = EnumMap(
            FieldKey::class.java)
        if (songName != null) fieldKeyValueMap[FieldKey.TITLE] = songName!!.text.toString()
        if (albumName != null) fieldKeyValueMap[FieldKey.ALBUM] = albumName!!.text.toString()
        if (artistName != null) fieldKeyValueMap[FieldKey.ARTIST] = artistName!!.text.toString()
        if (genreName != null) fieldKeyValueMap[FieldKey.GENRE] = genreName!!.text.toString()
        if (year != null) fieldKeyValueMap[FieldKey.YEAR] = year!!.text.toString()
        if (trackNumber != null) fieldKeyValueMap[FieldKey.TRACK] = trackNumber!!.text.toString()
        if (lyrics != null) fieldKeyValueMap[FieldKey.LYRICS] = lyrics!!.text.toString()
        tagUtil!!.writeValuesToFiles(fieldKeyValueMap,
            when {
                deleteAlbumArt -> ArtworkInfo(id,
                    null)
                albumArtBitmap == null -> null
                else -> ArtworkInfo(
                    id, albumArtBitmap)
            })
    }

    private fun loadImageFromFile(selectedFile: Uri?) {
        Glide.with(this@AbsTagEditorActivity)
            .asBitmap()
            .load(selectedFile)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    val palette = Palette.from(resource).generate()
                    PhonographColorUtil.getColor(palette, Color.TRANSPARENT)
                    albumArtBitmap = ImageUtil.resizeBitmap(resource, 2048)
                    setImageBitmap(albumArtBitmap,
                        PhonographColorUtil.getColor(palette,
                            ATHUtil.resolveColor(this@AbsTagEditorActivity,
                                R.attr.defaultFooterColor)))
                    deleteAlbumArt = false
                    dataChanged()
                    setResult(RESULT_OK)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    protected fun loadImageFromUrl(url: String?) {
        Glide.with(this@AbsTagEditorActivity)
            .asBitmap()
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .error(R.drawable.default_album_art)
            .into(object : CustomTarget<Bitmap?>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                    albumArtBitmap = ImageUtil.resizeBitmap(resource, 2048)
                    setImageBitmap(albumArtBitmap,
                        PhonographColorUtil.getColor(Palette.from(resource).generate(),
                            ATHUtil.resolveColor(this@AbsTagEditorActivity,
                                R.attr.defaultFooterColor)))
                    deleteAlbumArt = false
                    dataChanged()
                    setResult(RESULT_OK)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    protected abstract val contentViewLayout: Int
    protected abstract val songPaths: List<String>
    protected abstract fun getImageFromLastFM()
    protected abstract fun searchImageOnWeb()
    protected abstract fun searchOnline()

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_PALETTE = "extra_palette"
        private val TAG = AbsTagEditorActivity::class.java.simpleName
        private const val REQUEST_CODE_SELECT_IMAGE = 1000
    }
}