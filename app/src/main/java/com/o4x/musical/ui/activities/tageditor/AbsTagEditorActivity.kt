package com.o4x.musical.ui.activities.tageditor

import android.app.SearchManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.NestedScrollView
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.o4x.musical.R
import com.o4x.musical.drawables.CharCoverDrawable
import com.o4x.musical.drawables.CoverData
import com.o4x.musical.extensions.startImagePicker
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.model.Artist
import com.o4x.musical.ui.activities.base.AbsBaseActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AbsSearchOnlineActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity
import com.o4x.musical.ui.dialogs.DiscardTagsDialog
import com.o4x.musical.util.*
import com.o4x.musical.util.TagUtil.ArtworkInfo
import com.o4x.musical.util.TextUtil.makeTextWithTitle
import org.jaudiotagger.tag.FieldKey
import java.io.Serializable
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsTagEditorActivity<RM : Serializable> : AbsBaseActivity() {

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
    @BindView(R.id.album_image)
    var albumImage: ImageView? = null
    @JvmField
    @BindView(R.id.artist_image)
    var artistImage: ImageView? = null
    @JvmField
    @BindView(R.id.header)
    var header: CardView? = null
    @JvmField
    @BindView(R.id.album_text)
    var albumText: MaterialTextView? = null
    @JvmField
    @BindView(R.id.artist_text)
    var artistText: MaterialTextView? = null
    @JvmField
    @BindView(R.id.genre_text)
    var genreText: MaterialTextView? = null
    @JvmField
    @BindView(R.id.year_text)
    var yearText: MaterialTextView? = null
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
    @BindView(R.id.disc_number)
    var discNumber: TextInputEditText? = null
    @JvmField
    @BindView(R.id.lyrics)
    var lyrics: TextInputEditText? = null

    protected var id: Long = 0

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
    private var artistArtBitmap: Bitmap? = null
    private var deleteArtistArt = false

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
                    DiscardTagsDialog.create().show(supportFragmentManager, "TAGS")
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
                id = intentExtras.getLong(EXTRA_ID)
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
        setupAlbumImageView()
        setupArtistImageView()
        setupTextInputEditTexts()
    }

    private fun setupScrollView() {
        scrollView?.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                albumImage?.translationY = scrollY / 2f
            }
        )
    }

    private fun setupSearchButton() {
        searchBtn?.setBackgroundColor(surfaceColor())
        searchBtn?.setOnClickListener { searchOnline() }
    }

    private fun setupAlbumImageView() {

        val bitmap = tagUtil!!.albumArt
        setAlbumImageBitmap(bitmap)

        val items = arrayOf<CharSequence>(
            getString(R.string.pick_from_local_storage),
            getString(R.string.web_search),
            getString(R.string.remove_cover)
        ).asList()
        albumImage?.setOnClickListener {
            MaterialDialog(this@AbsTagEditorActivity)
                .title(R.string.update_image)
                .listItems(items = items) { dialog, index, text ->
                    when (index) {
                        0 -> startImagePicker(REQUEST_CODE_SELECT_ALBUM_IMAGE)
                        1 -> searchImageOnWeb()
                        2 -> deleteAlbumImage()
                    }
                }
                .show()
        }
    }

    private fun setupArtistImageView() {
        artistImage?.let {

            GlideLoader.with(this)
                .load(artist)
                .into(it)

            val items = arrayOf<CharSequence>(
                getString(R.string.pick_from_local_storage),
                getString(R.string.web_search),
                getString(R.string.remove_cover)
            ).asList()
            it.setOnClickListener {
                MaterialDialog(this@AbsTagEditorActivity)
                    .title(R.string.update_image)
                    .listItems(items = items) { dialog, index, text ->
                        when (index) {
                            0 -> startImagePicker(REQUEST_CODE_SELECT_ARTIST_IMAGE)
                            1 -> searchImageOnWeb()
                            2 -> deleteArtistImage()
                        }
                    }.show()
            }
        }
    }

    protected fun setAlbumImageBitmap(bitmap: Bitmap?) {
        albumImage?.let {
            if (bitmap == null) {
                val b: Bitmap =
                    CharCoverDrawable(CoverData(id, tagUtil?.albumTitle ?: ""))
                    .toBitmap(Util.getScreenWidth(), Util.getScreenHeight())
                it.setImageBitmap(b)
            } else {
                it.setImageBitmap(bitmap)
            }
        }
    }

    protected fun setArtistImageBitmap(bitmap: Bitmap?) {
        artistImage?.let {
            if (bitmap == null) {
                val artist = artist
                GlideLoader.with(this)
                    .load(artist).into(it)
            } else {
                it.setImageBitmap(bitmap)
            }
        }
    }

    private fun setupTextInputEditTexts() {
        fillViewsWithFileTags()
        songName?.addTextChangedListener(textWatcher)
        albumName?.addTextChangedListener(textWatcher)
        artistName?.addTextChangedListener(textWatcher)
        genreName?.addTextChangedListener(textWatcher)
        year?.addTextChangedListener(textWatcher)
        trackNumber?.addTextChangedListener(textWatcher)
        lyrics?.addTextChangedListener(textWatcher)
    }

    private fun fillViewsWithFileTags() {
        tagUtil?.let {
            albumText?.text = makeTextWithTitle(this, R.string.label_album, it.albumTitle)
            artistText?.text = makeTextWithTitle(this, R.string.label_artist, it.artistName)
            genreText?.text = makeTextWithTitle(this, R.string.label_genre, it.genreName)
            yearText?.text = makeTextWithTitle(this, R.string.label_year, it.songYear)
            songName?.setText(it.songTitle)
            albumName?.setText(it.albumTitle)
            artistName?.setText(it.artistName)
            genreName?.setText(it.genreName)
            year?.setText(it.songYear)
            trackNumber?.setText(it.trackNumber)
            discNumber?.setText(it.discNumber)
            lyrics?.setText(it.lyrics)
        }
    }

    protected abstract fun fillViewsWithResult(result: RM)

    protected fun dataChanged() {
        isChanged = true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_CODE_SELECT_ALBUM_IMAGE ->
                if (resultCode == RESULT_OK) {
                    val selectedImage = data!!.data
                    loadImageFromFile(selectedImage)
                }

            REQUEST_CODE_SELECT_ARTIST_IMAGE ->
                if (resultCode == RESULT_OK) {
                    val selectedImage = data!!.data
                    loadImageFromFile(selectedImage, true)
                }

            AbsSearchOnlineActivity.REQUEST_CODE -> try {
                if (resultCode == RESULT_OK) {
                    val extras = data!!.extras
                    if (extras != null) {
                        if (extras.containsKey(AlbumSearchActivity.EXTRA_RESULT_ALL)) {
                            val result =
                                extras.getSerializable(AbsSearchOnlineActivity.EXTRA_RESULT_ALL) as RM
                            fillViewsWithResult(result)
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

    private fun deleteAlbumImage() {
        setAlbumImageBitmap(null)
        deleteAlbumArt = true
        dataChanged()
    }

    private fun deleteArtistImage() {
        setArtistImageBitmap(null)
        deleteArtistArt = true
        dataChanged()
    }

    private fun save() {
        val fieldKeyValueMap: MutableMap<FieldKey, String?> = EnumMap(
            FieldKey::class.java)

        songName?.let {
            fieldKeyValueMap[FieldKey.TITLE] = it.getSafeString()
        }
        albumName?.let {
            fieldKeyValueMap[FieldKey.ALBUM] = it.getSafeString()
        }
        artistName?.let {
            fieldKeyValueMap[FieldKey.ARTIST] = it.getSafeString()
        }
        genreName?.let {
            fieldKeyValueMap[FieldKey.GENRE] = it.getSafeString()
        }
        year?.let {
            fieldKeyValueMap[FieldKey.YEAR] = it.getSafeString()
        }
        trackNumber?.let {
            fieldKeyValueMap[FieldKey.TRACK] = it.getSafeString()
        }
        discNumber?.let {
            fieldKeyValueMap[FieldKey.DISC_NO] = it.getSafeString()
        }
        lyrics?.let {
            fieldKeyValueMap[FieldKey.LYRICS] = it.getSafeString()
        }

        tagUtil?.writeValuesToFiles(fieldKeyValueMap,
            when {
                deleteAlbumArt -> ArtworkInfo(id,
                    null)
                albumArtBitmap == null -> null
                else -> ArtworkInfo(
                    id, albumArtBitmap)
            })

        when {
            deleteArtistArt -> {
                CustomImageUtil(artist).resetCustomImage()
            }
            artistArtBitmap != null -> {
                CustomImageUtil(artist).setCustomImage(artistArtBitmap)
            }
        }

        finish()
    }

    private fun getImageTarget(forArtist: Boolean? = null): CustomTarget<Bitmap> {
        return object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                if (forArtist == true) {
                    artistArtBitmap = ImageUtil.resizeBitmap(resource, 2048)
                    setArtistImageBitmap(artistArtBitmap)
                    deleteArtistArt = false
                } else {
                    albumArtBitmap = ImageUtil.resizeBitmap(resource, 2048)
                    setAlbumImageBitmap(albumArtBitmap)
                    deleteAlbumArt = false
                }

                dataChanged()
                setResult(RESULT_OK)
            }

            override fun onLoadCleared(placeholder: Drawable?) {}
        }
    }

    private fun loadImageFromFile(selectedFile: Uri?, forArtist: Boolean? = null) {
        selectedFile?.let {
            GlideApp.with(this)
                .asBitmap()
                .load(selectedFile)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(getImageTarget(forArtist))
        }
    }

    protected fun loadImageFromUrl(url: String?, forArtist: Boolean? = null) {
        url?.let {
            GlideApp.with(this)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(getImageTarget(forArtist))
        }
    }

    protected abstract val contentViewLayout: Int
    protected abstract val artist: Artist
    protected abstract val songPaths: List<String>
    protected abstract fun searchImageOnWeb()
    protected abstract fun searchOnline()

    companion object {
        const val EXTRA_ID = "extra_id"
        const val EXTRA_PALETTE = "extra_palette"
        private val TAG = AbsTagEditorActivity::class.java.simpleName
        private const val REQUEST_CODE_SELECT_ALBUM_IMAGE = 1000
        private const val REQUEST_CODE_SELECT_ARTIST_IMAGE = 1001
    }
}

private fun TextInputEditText.getSafeString(): String {
    return if (text?.length ?: -1 > 0) {
        text.toString()
    } else ""
}