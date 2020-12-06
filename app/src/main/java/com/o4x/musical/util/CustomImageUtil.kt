package com.o4x.musical.util

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.blankj.utilcode.util.FileUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.o4x.musical.App.Companion.getContext
import com.o4x.musical.model.Artist
import com.o4x.musical.model.Genre
import com.o4x.musical.model.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.util.*

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
class CustomImageUtil {

    private val id: Long
    private val name: String
    private val type: Type

    constructor(id: Long, name: String, type: Type) {
        this.id = id
        this.name = name
        this.type = type
    }

    constructor(artist: Artist) {
        id = artist.id
        name = artist.name
        type = Type.ARTIST
    }

    constructor(genre: Genre) {
        id = genre.id
        name = genre.name
        type = Type.GENRE
    }

    constructor(playlist: Playlist) {
        id = playlist.id
        name = playlist.name
        type = Type.PLAYLIST
    }

    fun setCustomImage(uri: Uri?) {
        Glide.with(getContext())
            .asBitmap()
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    setCustomImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }


    fun setCustomImage(bitmap: Bitmap?) {
        if (bitmap == null) return
        GlobalScope.launch(Dispatchers.Default) {
            val file = file
            var succesful = false
            try {
                val os: OutputStream = BufferedOutputStream(FileOutputStream(file))
                succesful = ImageUtil.resizeBitmap(bitmap, 2048)
                    .compress(Bitmap.CompressFormat.JPEG, 100, os)
                os.close()
            } catch (e: IOException) {
                Toast.makeText(getContext(), e.toString(), Toast.LENGTH_LONG).show()
            }
            if (succesful) {
                // Remove cache from universal image loader for reload image
                // For glide we don't need to remove cache, it's work with Signature
                notifyChange()
            }
        }
    }


    fun resetCustomImage() {
        GlobalScope.launch {
            notifyChange()
            val file = file
            if (file.exists()) {
                // Remove caches from UIL just for optimize memory
                file.delete()
            }
        }
    }

    private fun notifyChange() {
        // trigger media store changed to force image reload
        when (type) {
            Type.ARTIST -> getContext().contentResolver
                .notifyChange(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null)
            Type.GENRE -> getContext().contentResolver
                .notifyChange(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, null)
            Type.PLAYLIST -> getContext().contentResolver
                .notifyChange(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null)
        }
    }

    fun hasCustomImage(): Boolean {
        return file.exists()
    }

    // replace everything that is not a letter or a number with _
    private val fileName: String
        get() {
            var mName = name
            // replace everything that is not a letter or a number with _
            mName = mName.replace("[^a-zA-Z0-9]".toRegex(), "_")
            return String.format(Locale.US, "%s_%d.jpeg", mName, id)
        }

    // create the folder
    val file: File
        get() {
            val dir = File(getContext().filesDir, FOLDER_NAME + type.name)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return File(dir, fileName)
        }
    val path: String
        get() = Uri.fromFile(file).toString()

    enum class Type {
        ARTIST, GENRE, PLAYLIST
    }

    companion object {
        private const val FOLDER_NAME = "/images/"

        fun deleteAll() {
            val dir = File(getContext().filesDir, FOLDER_NAME)
            FileUtils.delete(dir)
        }

    }
}