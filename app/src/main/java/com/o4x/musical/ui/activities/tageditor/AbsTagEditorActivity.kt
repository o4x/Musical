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
import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import code.name.monkey.appthemehelper.extensions.colorControlNormal
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.o4x.musical.R
import com.o4x.musical.ads.TapselUtils
import com.o4x.musical.databinding.ActivityTagBinding
import com.o4x.musical.extensions.applyToolbar
import com.o4x.musical.extensions.startImagePicker
import com.o4x.musical.imageloader.glide.loader.GlideLoader
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.model.Album
import com.o4x.musical.model.Artist
import com.o4x.musical.repository.Repository
import com.o4x.musical.ui.activities.base.AbsBaseActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AbsSearchOnlineActivity
import com.o4x.musical.ui.activities.tageditor.onlinesearch.AlbumSearchActivity
import com.o4x.musical.ui.dialogs.DiscardTagsDialog
import com.o4x.musical.util.*
import com.o4x.musical.util.TagUtil.ArtworkInfo
import org.jaudiotagger.tag.FieldKey
import org.koin.android.ext.android.inject
import java.io.Serializable
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
abstract class AbsTagEditorActivity<RM : Serializable> : AbsBaseActivity() {

    val repository by inject<Repository>()
    protected val id: Long
            by lazy { intent.extras?.getLong(EXTRA_ID)!! }

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

    var album: Album? = null
    lateinit var artist: Artist
    lateinit var songPaths: List<String>

    val binding by lazy { ActivityTagBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        showViews()

        getDataSet()
        createTagUtil()

        headerVariableSpace = resources.getDimensionPixelSize(R.dimen.tagEditorHeaderVariableSpace)

        setupViews()
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setNavigationBarDividerColorAuto()

        applyToolbar(binding.toolbar)

        supportActionBar?.setTitle(R.string.action_tag_editor)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        TapselUtils(this).loadStandardBanner(binding.banner)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tag_editor, menu);
        ToolbarContentTintHelper.tintAllIcons(menu, colorControlNormal())
        return super.onCreateOptionsMenu(menu);
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(this, binding.toolbar)
        return super.onPrepareOptionsMenu(menu)
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
        binding.nestedScrollView.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
                albumImageView().translationY = scrollY / 2f
            }
        )
    }

    private fun setupSearchButton() {
        binding.searchOnline.setOnClickListener { searchOnline() }
    }

    private fun setupAlbumImageView() {

        val bitmap = tagUtil!!.albumArt
        setAlbumImageBitmap(bitmap)

        val items = arrayOf<CharSequence>(
            getString(R.string.pick_from_local_storage),
            getString(R.string.web_search),
            getString(R.string.remove_cover)
        ).asList()
        albumImageView().setOnClickListener {
            MaterialDialog(this@AbsTagEditorActivity)
                .title(R.string.update_album_image)
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
        artistImageView().let {

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
                    .title(R.string.update_artist_image)
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
        albumImageView().let {
            if (bitmap == null) {
                album?.apply {
                    GlideLoader.with(this@AbsTagEditorActivity)
                        .load(this).into(it)
                }
            } else {
                it.setImageBitmap(bitmap)
            }
        }
    }

    protected fun setArtistImageBitmap(bitmap: Bitmap?) {
        artistImageView().let {
            if (bitmap == null) {
                GlideLoader.with(this)
                    .load(artist).into(it)
            } else {
                it.setImageBitmap(bitmap)
            }
        }
    }

    private fun setupTextInputEditTexts() {
        fillViewsWithFileTags()
        binding.song.editText?.addTextChangedListener(textWatcher)
        binding.album.editText?.addTextChangedListener(textWatcher)
        binding.artist.editText?.addTextChangedListener(textWatcher)
        binding.genre.editText?.addTextChangedListener(textWatcher)
        binding.year.editText?.addTextChangedListener(textWatcher)
        binding.track.editText?.addTextChangedListener(textWatcher)
        binding.lyrics.editText?.addTextChangedListener(textWatcher)
    }

    private fun fillViewsWithFileTags() {
        tagUtil?.let {
            binding.artistTitle.text = artist.name
            binding.artistText.text = MusicUtil.getArtistInfoString(this, artist)
            binding.song.editText?.setText(it.songTitle)
            binding.album.editText?.setText(it.albumTitle)
            binding.artist.editText?.setText(it.artistName)
            binding.genre.editText?.setText(it.genreName)
            binding.year.editText?.setText(it.songYear)
            binding.track.editText?.setText(it.trackNumber)
            binding.disc.editText?.setText(it.discNumber)
            binding.lyrics.editText?.setText(it.lyrics)
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
                }
            } catch (e: Exception) {
                e.message?.let { Log.e(TAG, it) }
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

        binding.song.editText?.let {
            if (binding.song.isVisible)
                fieldKeyValueMap[FieldKey.TITLE] = it.getSafeString()
        }
        binding.album.editText?.let {
            if (binding.album.isVisible)
                fieldKeyValueMap[FieldKey.ALBUM] = it.getSafeString()
        }
        binding.artist.editText?.let {
            if (binding.artist.isVisible)
                fieldKeyValueMap[FieldKey.ARTIST] = it.getSafeString()
        }
        binding.genre.editText?.let {
            if (binding.genre.isVisible)
                fieldKeyValueMap[FieldKey.GENRE] = it.getSafeString()
        }
        binding.year.editText?.let {
            if (binding.year.isVisible)
                fieldKeyValueMap[FieldKey.YEAR] = it.getSafeString()
        }
        binding.track.editText?.let {
            if (binding.track.isVisible)
                fieldKeyValueMap[FieldKey.TRACK] = it.getSafeString()
        }
        binding.disc.editText?.let {
            if (binding.disc.isVisible)
                fieldKeyValueMap[FieldKey.DISC_NO] = it.getSafeString()
        }
        binding.lyrics.editText?.let {
            if (binding.lyrics.isVisible)
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

    private fun getDataSet() {
        artist = createArtist()
        songPaths = createPaths()
    }


    abstract fun artistImageView(): ImageView
    abstract fun albumImageView(): ImageView
    abstract fun showViews()
    abstract fun createArtist(): Artist
    abstract fun createPaths(): List<String>
    abstract fun searchImageOnWeb()
    abstract fun searchOnline()

    companion object {
        const val EXTRA_ID = "extra_id"
        private val TAG = AbsTagEditorActivity::class.java.simpleName
        private const val REQUEST_CODE_SELECT_ALBUM_IMAGE = 1000
        private const val REQUEST_CODE_SELECT_ARTIST_IMAGE = 1001
    }
}

private fun EditText.getSafeString(): String {
    return if (text?.length ?: -1 > 0) {
        text.toString()
    } else ""
}