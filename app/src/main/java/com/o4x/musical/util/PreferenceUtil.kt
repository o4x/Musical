package com.o4x.musical.util

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.o4x.musical.*
import com.o4x.musical.extensions.getStringOrDefault
import com.o4x.musical.extensions.isDark
import com.o4x.musical.helper.SortOrder
import com.o4x.musical.model.CategoryInfo
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment
import java.io.File

object PreferenceUtil {

    const val GENERAL_THEME = "general_theme"
    const val THEME_COLOR = "theme_color"
    const val AUDIO_DUCKING = "audio_ducking"
    const val GAPLESS_PLAYBACK = "gapless_playback"
    const val EQUALIZER = "equalizer"
    const val COLORED_FOOTER = "colored_footer"
    const val LIBRARY_CATEGORIES = "library_categories"
    const val ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen"
    const val AUTO_DOWNLOAD_IMAGES_POLICY = "auto_download_images_policy"
    const val IGNORE_MEDIA = "ignore_media_store_artwork"
    const val CACHE_IMAGES = "cache_images"
    const val DELETE_CACHED_IMAGES = "delete_cached_images"
    const val DELETE_CUSTOM_IMAGES = "delete_custom_images"
    const val CLASSIC_NOTIFICATION = "classic_notification"
    const val COLORED_NOTIFICATION = "colored_notification"
    const val SMART_PLAYLIST_LIMIT = "smart_playlist_limit"
    const val LANGUAGE_NAME = "language_name"
    const val ABOUT = "about"


    const val REMEMBER_LAST_TAB = "remember_last_tab"
    const val LAST_PAGE = "last_start_page"
    const val ARTIST_SORT_ORDER = "artist_sort_order"
    const val ARTIST_SONG_SORT_ORDER = "artist_song_sort_order"
    const val ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order"
    const val ALBUM_SORT_ORDER = "album_sort_order"
    const val ALBUM_SONG_SORT_ORDER = "album_song_sort_order"
    const val SONG_SORT_ORDER = "song_sort_order"
    const val GENRE_SORT_ORDER = "genre_sort_order"
    const val PLAYLIST_SORT_ORDER = "playlist_sort_order"
    const val ALBUM_GRID_SIZE = "album_grid_size"
    const val ALBUM_GRID_SIZE_LAND = "album_grid_size_land"
    const val SONG_GRID_SIZE = "song_grid_size"
    const val SONG_GRID_SIZE_LAND = "song_grid_size_land"
    const val ARTIST_GRID_SIZE = "artist_grid_size"
    const val ARTIST_GRID_SIZE_LAND = "artist_grid_size_land"
    const val GENRE_GRID_SIZE = "genre_grid_size"
    const val GENRE_GRID_SIZE_LAND = "genre_grid_size_land"
    const val PLAYLIST_GRID_SIZE = "playlist_grid_size"
    const val PLAYLIST_GRID_SIZE_LAND = "playlist_grid_size_land"
    const val FORCE_SQUARE_ALBUM_COVER = "force_square_album_art"
    const val LAST_SLEEP_TIMER_VALUE = "last_sleep_timer_value"
    const val NEXT_SLEEP_TIMER_ELAPSED_REALTIME = "next_sleep_timer_elapsed_real_time"
    const val SLEEP_TIMER_FINISH_SONG = "sleep_timer_finish_music"
    const val LAST_CHANGELOG_VERSION = "last_changelog_version"
    const val START_DIRECTORY = "start_directory"
    const val SYNCHRONIZED_LYRICS_SHOW = "synchronized_lyrics_show"
    const val INITIALIZED_BLACKLIST = "initialized_blacklist"
    const val REMEMBER_SHUFFLE = "remember_shuffle"

    @JvmStatic
    fun isAllowedToDownloadMetadata(context: Context): Boolean {
        return when (autoDownloadImagesPolicy) {
            "always" -> true
            "only_wifi" -> {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val netInfo =
                    connectivityManager.activeNetworkInfo
                netInfo != null && netInfo.type == ConnectivityManager.TYPE_WIFI && netInfo.isConnectedOrConnecting
            }
            "never" -> false
            else -> false
        }
    }

    @JvmStatic
    @StyleRes
    fun themeResFromPrefValue(themePrefValue: String?): Int {
        return when (themePrefValue) {
            "dark" -> R.style.Theme_Musical
            "black" -> R.style.Theme_Musical_Black
            "light" -> R.style.Theme_Musical_Light
            else -> R.style.Theme_Musical
        }
    }


    private val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.getContext())

    @JvmStatic
    fun registerOnSharedPreferenceChangedListener(sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

    @JvmStatic
    fun unregisterOnSharedPreferenceChangedListener(sharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }


    @JvmStatic
    val defaultCategories = listOf(
        CategoryInfo(CategoryInfo.Category.SONGS, true),
        CategoryInfo(CategoryInfo.Category.ALBUMS, true),
        CategoryInfo(CategoryInfo.Category.ARTISTS, true),
        CategoryInfo(CategoryInfo.Category.GENRES, true),
        CategoryInfo(CategoryInfo.Category.PLAYLISTS, true)
    )

    @JvmStatic
    var libraryCategory: List<CategoryInfo>
        get() {
            val gson = Gson()
            val collectionType = object : TypeToken<List<CategoryInfo>>() {}.type

            val data = sharedPreferences.getStringOrDefault(
                LIBRARY_CATEGORIES,
                gson.toJson(this.defaultCategories, collectionType)
            )
            return try {
                Gson().fromJson(data, collectionType)
            } catch (e: JsonSyntaxException) {
                e.printStackTrace()
                return this.defaultCategories
            }
        }
        set(value) {
            val collectionType = object : TypeToken<List<CategoryInfo?>?>() {}.type
            sharedPreferences.edit {
                putString(LIBRARY_CATEGORIES, Gson().toJson(value, collectionType))
            }
        }

    @JvmStatic
    fun rememberLastTab(): Boolean {
        return sharedPreferences.getBoolean(REMEMBER_LAST_TAB, true)
    }

    val isScreenOnEnabled get() = sharedPreferences.getBoolean(KEEP_SCREEN_ON, false)

    val languageCode get() = sharedPreferences.getString(LANGUAGE_NAME, "auto")

    @JvmStatic
    var lastPage: Int
        get() = sharedPreferences.getInt(LAST_PAGE, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(LAST_PAGE, value)
            editor.apply()
        }

    @JvmStatic
    var isClassicNotification
        get() = sharedPreferences.getBoolean(CLASSIC_NOTIFICATION, false)
        set(value) = sharedPreferences.edit { putBoolean(CLASSIC_NOTIFICATION, value) }

    @JvmStatic
    var smartPlaylistLimit
        get() = sharedPreferences.getInt(SMART_PLAYLIST_LIMIT, 100)
        set(value) = sharedPreferences.edit { putInt(SMART_PLAYLIST_LIMIT, value) }

    @JvmStatic
    val isColoredFooter
        get() = sharedPreferences.getBoolean(
            COLORED_FOOTER, false
        )

    @JvmStatic
    var isColoredNotification
        get() = sharedPreferences.getBoolean(
            COLORED_NOTIFICATION, true
        )
        set(value) = sharedPreferences.edit {
            putBoolean(COLORED_NOTIFICATION, value)
        }

    @JvmStatic
    fun gaplessPlayback(): Boolean {
        return sharedPreferences.getBoolean(GAPLESS_PLAYBACK, false)
    }

    @JvmStatic
    fun audioDucking(): Boolean {
        return sharedPreferences.getBoolean(AUDIO_DUCKING, true)
    }

    @JvmStatic
    fun albumArtOnLockscreen(): Boolean {
        return sharedPreferences.getBoolean(ALBUM_ART_ON_LOCKSCREEN, true)
    }

    @JvmStatic
    var artistSortOrder: String?
        get() = sharedPreferences.getString(
            ARTIST_SORT_ORDER,
            SortOrder.ArtistSortOrder.ARTIST_A_Z
        )
        set(sortOrder) {
            val editor = sharedPreferences.edit()
            editor.putString(ARTIST_SORT_ORDER, sortOrder)
            editor.apply()
        }

    @JvmStatic
    val artistSongSortOrder: String?
        get() = sharedPreferences.getString(
            ARTIST_SONG_SORT_ORDER,
            SortOrder.ArtistSongSortOrder.SONG_A_Z
        )

    @JvmStatic
    val artistAlbumSortOrder: String?
        get() = sharedPreferences.getString(
            ARTIST_ALBUM_SORT_ORDER,
            SortOrder.ArtistAlbumSortOrder.ALBUM_YEAR
        )

    val autoDownloadImagesPolicy
        get() = sharedPreferences.getStringOrDefault(
            AUTO_DOWNLOAD_IMAGES_POLICY,
            "only_wifi"
        )

    @JvmStatic
    var albumSortOrder: String?
        get() = sharedPreferences.getString(
            ALBUM_SORT_ORDER,
            SortOrder.AlbumSortOrder.ALBUM_A_Z
        )
        set(sortOrder) {
            val editor = sharedPreferences.edit()
            editor.putString(ALBUM_SORT_ORDER, sortOrder)
            editor.apply()
        }

    @JvmStatic
    val albumSongSortOrder: String?
        get() = sharedPreferences.getString(
            ALBUM_SONG_SORT_ORDER,
            SortOrder.AlbumSongSortOrder.SONG_TRACK_LIST
        )

    @JvmStatic
    var songSortOrder
        get() = sharedPreferences.getStringOrDefault(
            SONG_SORT_ORDER,
            SortOrder.SongSortOrder.SONG_A_Z
        )
        set(value) = sharedPreferences.edit {
            putString(SONG_SORT_ORDER, value)
        }

    @JvmStatic
    var genreSortOrder: String?
        get() = sharedPreferences.getString(
            GENRE_SORT_ORDER,
            SortOrder.GenreSortOrder.GENRE_A_Z
        )
        set(sortOrder) {
            val editor = sharedPreferences.edit()
            editor.putString(GENRE_SORT_ORDER, sortOrder)
            editor.apply()
        }

    @JvmStatic
    var playlistSortOrder: String?
        get() = sharedPreferences.getString(
            PLAYLIST_SORT_ORDER,
            SortOrder.PlaylistSortOrder.PLAYLIST_A_Z
        )
        set(sortOrder) {
            val editor = sharedPreferences.edit()
            editor.putString(GENRE_SORT_ORDER, sortOrder)
            editor.apply()
        }

    @JvmStatic
    var lastSleepTimerValue: Int
        get() = sharedPreferences.getInt(LAST_SLEEP_TIMER_VALUE, 30)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(LAST_SLEEP_TIMER_VALUE, value)
            editor.apply()
        }

    @JvmStatic
    val nextSleepTimerElapsedRealTime: Long
        get() = sharedPreferences.getLong(NEXT_SLEEP_TIMER_ELAPSED_REALTIME, -1)

    @JvmStatic
    fun setNextSleepTimerElapsedRealtime(value: Long) {
        val editor = sharedPreferences.edit()
        editor.putLong(NEXT_SLEEP_TIMER_ELAPSED_REALTIME, value)
        editor.apply()
    }

    val filterLength get() = sharedPreferences.getInt(FILTER_SONG, 20)

    @JvmStatic
    var sleepTimerFinishMusic: Boolean
        get() = sharedPreferences.getBoolean(SLEEP_TIMER_FINISH_SONG, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SLEEP_TIMER_FINISH_SONG, value)
            editor.apply()
        }

    val isShuffleModeOn get() = sharedPreferences.getBoolean(TOGGLE_SHUFFLE, false)

    var albumArtistsOnly
        get() = sharedPreferences.getBoolean(
            ALBUM_ARTISTS_ONLY,
            false
        )
        set(value) = sharedPreferences.edit { putBoolean(ALBUM_ARTISTS_ONLY, value) }

    @JvmStatic
    fun setAlbumGridSize(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(ALBUM_GRID_SIZE, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getAlbumGridSize(context: Context): Int {
        return sharedPreferences.getInt(
            ALBUM_GRID_SIZE,
            context.resources.getInteger(R.integer.default_grid_columns)
        )
    }

    @JvmStatic
    fun setSongGridSize(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(SONG_GRID_SIZE, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getSongGridSize(context: Context): Int {
        return sharedPreferences.getInt(
            SONG_GRID_SIZE,
            context.resources.getInteger(R.integer.default_list_columns)
        )
    }

    @JvmStatic
    fun setArtistGridSize(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(ARTIST_GRID_SIZE, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getArtistGridSize(context: Context): Int {
        return sharedPreferences.getInt(
            ARTIST_GRID_SIZE,
            context.resources.getInteger(R.integer.default_list_columns)
        )
    }

    @JvmStatic
    fun setGenreGridSize(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(GENRE_GRID_SIZE, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getGenreGridSize(context: Context): Int {
        return sharedPreferences.getInt(
            GENRE_GRID_SIZE,
            context.resources.getInteger(R.integer.default_list_columns)
        )
    }

    @JvmStatic
    fun setPlaylistGridSize(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PLAYLIST_GRID_SIZE, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getPlaylistGridSize(context: Context): Int {
        return sharedPreferences.getInt(
            PLAYLIST_GRID_SIZE,
            context.resources.getInteger(R.integer.default_list_columns)
        )
    }

    @JvmStatic
    fun setAlbumGridSizeLand(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(ALBUM_GRID_SIZE_LAND, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getAlbumGridSizeLand(context: Context): Int {
        return sharedPreferences.getInt(
            ALBUM_GRID_SIZE_LAND,
            context.resources.getInteger(R.integer.default_grid_columns_land)
        )
    }

    @JvmStatic
    fun setSongGridSizeLand(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(SONG_GRID_SIZE_LAND, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getSongGridSizeLand(context: Context): Int {
        return sharedPreferences.getInt(
            SONG_GRID_SIZE_LAND,
            context.resources.getInteger(R.integer.default_list_columns_land)
        )
    }

    @JvmStatic
    fun setArtistGridSizeLand(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(ARTIST_GRID_SIZE_LAND, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getArtistGridSizeLand(context: Context): Int {
        return sharedPreferences.getInt(
            ARTIST_GRID_SIZE_LAND,
            context.resources.getInteger(R.integer.default_list_columns_land)
        )
    }

    @JvmStatic
    fun setGenreGridSizeLand(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(GENRE_GRID_SIZE_LAND, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getGenreGridSizeLand(context: Context): Int {
        return sharedPreferences.getInt(
            GENRE_GRID_SIZE_LAND,
            context.resources.getInteger(R.integer.default_list_columns_land)
        )
    }

    @JvmStatic
    fun setPlaylistGridSizeLand(gridSize: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt(PLAYLIST_GRID_SIZE_LAND, gridSize)
        editor.apply()
    }

    @JvmStatic
    fun getPlaylistGridSizeLand(context: Context): Int {
        return sharedPreferences.getInt(
            PLAYLIST_GRID_SIZE_LAND,
            context.resources.getInteger(R.integer.default_list_columns_land)
        )
    }


    @JvmStatic
    fun setLastChangeLogVersion(version: Int) {
        sharedPreferences.edit().putInt(LAST_CHANGELOG_VERSION, version).apply()
    }

    @JvmStatic
    val lastChangelogVersion: Int
        get() = sharedPreferences.getInt(LAST_CHANGELOG_VERSION, -1)

    @JvmStatic
    fun rememberShuffle(): Boolean {
        return sharedPreferences.getBoolean(REMEMBER_SHUFFLE, true)
    }

    @JvmStatic
    fun isCacheImages(): Boolean {
        return sharedPreferences.getBoolean(
            CACHE_IMAGES,
            true
        )
    }


    @JvmStatic
    fun isIgnoreMediaStore(): Boolean {
        return sharedPreferences.getBoolean(
            IGNORE_MEDIA,
            true
        )
    }

    @JvmStatic
    var startDirectory: File
        get() = File(
            sharedPreferences.getString(
                START_DIRECTORY,
                FoldersFragment.defaultStartDirectory.path
            )!!
        )
        set(file) {
            val editor = sharedPreferences.edit()
            editor.putString(
                START_DIRECTORY,
                FileUtil.safeGetCanonicalPath(file)
            )
            editor.apply()
        }

    @JvmStatic
    fun synchronizedLyricsShow(): Boolean {
        return sharedPreferences.getBoolean(SYNCHRONIZED_LYRICS_SHOW, true)
    }

    var isInitializedBlacklist
        get() = sharedPreferences.getBoolean(
            INITIALIZED_BLACKLIST, false
        )
        set(value) = sharedPreferences.edit {
            putBoolean(INITIALIZED_BLACKLIST, value)
        }



    fun getGeneralThemeRes(): Int {
        val themeMode: String =
            sharedPreferences.getStringOrDefault(GENERAL_THEME, "auto")
        return when (themeMode) {
            "light" -> R.style.Theme_Musical_Light
            "dark" -> R.style.Theme_Musical_Base
            "black" -> R.style.Theme_Musical_Black
            else -> R.style.Theme_Musical_Black
        }
    }

    fun getThemeColorRes(): Int {
        val themeColor: String =
            sharedPreferences.getStringOrDefault(THEME_COLOR, "purple")
        return when (themeColor) {
            "red" -> if (isDarkMode) R.style.ThemeColorRed else R.style.ThemeColorRedLight
            "deep_purple" -> if (isDarkMode) R.style.ThemeColorDeepPurple else R.style.ThemeColorDeepPurpleLight
            "light_blue" -> if (isDarkMode) R.style.ThemeColorLightBlue else R.style.ThemeColorLightBlueLight
            "green" -> if (isDarkMode) R.style.ThemeColorGreen else R.style.ThemeColorGreenLight
            "yellow" -> if (isDarkMode) R.style.ThemeColorYellow else R.style.ThemeColorYellowLight
            "deep_orange" -> if (isDarkMode) R.style.ThemeColorDeepOrange else R.style.ThemeColorDeepOrangeLight
            "blue_grey" -> if (isDarkMode) R.style.ThemeColorBlueGrey else R.style.ThemeColorBlueGreyLight
            "pink" -> if (isDarkMode) R.style.ThemeColorPink else R.style.ThemeColorPinkLight
            "indigo" -> if (isDarkMode) R.style.ThemeColorIndigo else R.style.ThemeColorIndigoLight
            "cyan" -> if (isDarkMode) R.style.ThemeColorCyan else R.style.ThemeColorCyanLight
            "light_green" -> if (isDarkMode) R.style.ThemeColorLightGreen else R.style.ThemeColorLightGreenLight
            "amber" -> if (isDarkMode) R.style.ThemeColorAmber else R.style.ThemeColorAmberLight
            "purple" -> if (isDarkMode) R.style.ThemeColorPurple else R.style.ThemeColorPurpleLight
            "blue" -> if (isDarkMode) R.style.ThemeColorBlue else R.style.ThemeColorBlueLight
            "teal" -> if (isDarkMode) R.style.ThemeColorTeal else R.style.ThemeColorTealLight
            "lime" -> if (isDarkMode) R.style.ThemeColorLime else R.style.ThemeColorLimeLight
            "orange" -> if (isDarkMode) R.style.ThemeColorOrange else R.style.ThemeColorOrangeLight
            else -> if (isDarkMode) R.style.ThemeColorGrey else R.style.ThemeColorGreyLight
        }
    }

    val nightMode: Int
        get() {
            return if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        }

    val isDarkMode: Boolean
        get() {
            if (getGeneralThemeRes() == R.style.Theme_Musical_Light) {
                return false
            }
            return true;
        }
}