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
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.NestedScrollView
import androidx.palette.graphics.Palette
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.util.ATHUtil
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.textfield.TextInputEditText
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import com.o4x.musical.R
import com.o4x.musical.imageloader.universalil.UniversalIL
import com.o4x.musical.ui.activities.base.AbsBaseActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AbsSearchOnlineActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity
import com.o4x.musical.ui.dialogs.DiscardTagsDialog
import com.o4x.musical.util.*
import com.o4x.musical.util.TagUtil.ArtworkInfo
import org.jaudiotagger.tag.FieldKey
import java.io.Serializable
import java.util.*
import com.o4x.musical.extensions.surfaceColor
import kotlin.math.min

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsTagEditorActivity<RM : Serializable?> : AbsBaseActivity() {

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

    private var isChanged: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(contentViewLayout)
        ButterKnife.bind(this)
        intentExtras
        createTagUtil()
        headerVariableSpace = resources.getDimensionPixelSize(R.dimen.tagEditorHeaderVariableSpace)
        setupViews()
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()
        toolbar?.setBackgroundColor(surfaceColor())
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle(R.string.action_tag_editor)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tag_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> save()
            android.R.id.home -> {
                if (isChanged) {
                    DiscardTagsDialog.create().show(fragmentManager, "TAGS")
                    return true
                }
                super.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
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

    private fun setupSearchButton() {
        searchBtn?.setBackgroundColor(surfaceColor())
        searchBtn?.setOnClickListener { searchOnline() }
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
        isChanged = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> if (resultCode == RESULT_OK) {
                val selectedImage = data!!.data
                loadImageFromFile(selectedImage, null)
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
                                extras.getString(AbsSearchOnlineActivity.EXTRA_RESULT_COVER), null
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

    protected fun setImageBitmap(bitmap: Bitmap?) {
        if (bitmap == null) {
            val b: Bitmap = ColorCoverUtil.createSquareCoverWithText(
                this, tagUtil?.albumTitle ?: "", id, Util.getScreenWidth())
            image?.setImageBitmap(b)
        } else {
            image?.setImageBitmap(bitmap)
        }
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
        setImageBitmap(bitmap)
        deleteAlbumArt = false
    }

    private fun deleteImage() {
        setImageBitmap(null)
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

    private fun loadImageFromFile(selectedFile: Uri?, name: String?) {
        loadImageFromUrl(selectedFile?.toString(), name)
    }

    protected fun loadImageFromUrl(url: String?, name: String?) {
        url?.let {

            UniversalIL.imageLoader?.loadImage(
                url,
                DisplayImageOptions
                    .Builder()
                    .cacheOnDisk(true)
                    .cacheInMemory(true)
                    .build(),
                object: SimpleImageLoadingListener() {
                    override fun onLoadingComplete(
                        imageUri: String?,
                        view: View?,
                        loadedImage: Bitmap?,
                    ) {
                        super.onLoadingComplete(imageUri, view, loadedImage)
                        loadedImage?.let {
                            albumArtBitmap = ImageUtil.resizeBitmap(loadedImage, 2048)
                            setImageBitmap(albumArtBitmap)
                            deleteAlbumArt = false
                            dataChanged()
                            setResult(RESULT_OK)
                        }
                    }
                }
            )
        }
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