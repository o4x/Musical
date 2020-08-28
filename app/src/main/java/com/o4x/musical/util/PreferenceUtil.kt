package com.o4x.musical.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.annotation.StyleRes
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.o4x.musical.extensions.getStringOrDefault
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.o4x.musical.App
import com.o4x.musical.R
import com.o4x.musical.helper.SortOrder
import com.o4x.musical.model.CategoryInfo
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment
import com.o4x.musical.ui.fragments.player.AlbumCoverStyle
import com.o4x.musical.ui.fragments.player.NowPlayingScreen
import com.o4x.musical.util.theme.ThemeMode
import java.io.File
import java.util.*

object PreferenceUtil {
    const val GENERAL_THEME = "general_theme"
    const val REMEMBER_LAST_TAB = "remember_last_tab"
    const val LAST_PAGE = "last_start_page"
    const val LAST_MUSIC_CHOOSER = "last_music_chooser"
    const val NOW_PLAYING_SCREEN_ID = "now_playing_screen_id"
    const val ARTIST_SORT_ORDER = "artist_sort_order"
    const val ARTIST_SONG_SORT_ORDER = "artist_song_sort_order"
    const val ARTIST_ALBUM_SORT_ORDER = "artist_album_sort_order"
    const val ALBUM_SORT_ORDER = "album_sort_order"
    const val ALBUM_SONG_SORT_ORDER = "album_song_sort_order"
    const val SONG_SORT_ORDER = "song_sort_order"
    const val GENRE_SORT_ORDER = "genre_sort_order"
    const val ALBUM_GRID_SIZE = "album_grid_size"
    const val ALBUM_GRID_SIZE_LAND = "album_grid_size_land"
    const val SONG_GRID_SIZE = "song_grid_size"
    const val SONG_GRID_SIZE_LAND = "song_grid_size_land"
    const val ARTIST_GRID_SIZE = "artist_grid_size"
    const val ARTIST_GRID_SIZE_LAND = "artist_grid_size_land"
    const val ALBUM_COLORED_FOOTERS = "album_colored_footers"
    const val SONG_COLORED_FOOTERS = "song_colored_footers"
    const val ARTIST_COLORED_FOOTERS = "artist_colored_footers"
    const val ALBUM_ARTIST_COLORED_FOOTERS = "album_artist_colored_footers"
    const val FORCE_SQUARE_ALBUM_COVER = "force_square_album_art"
    const val COLORED_NOTIFICATION = "colored_notification"
    const val CLASSIC_NOTIFICATION = "classic_notification"
    const val COLORED_APP_SHORTCUTS = "colored_app_shortcuts"
    const val AUDIO_DUCKING = "audio_ducking"
    const val GAPLESS_PLAYBACK = "gapless_playback"
    const val LAST_ADDED_CUTOFF = "last_added_interval"
    const val ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen"
    const val BLURRED_ALBUM_ART = "blurred_album_art"
    const val LAST_SLEEP_TIMER_VALUE = "last_sleep_timer_value"
    const val NEXT_SLEEP_TIMER_ELAPSED_REALTIME = "next_sleep_timer_elapsed_real_time"
    const val SLEEP_TIMER_FINISH_SONG = "sleep_timer_finish_music"
    const val LAST_CHANGELOG_VERSION = "last_changelog_version"
    const val INTRO_SHOWN = "intro_shown"
    const val AUTO_DOWNLOAD_IMAGES_POLICY = "auto_download_images_policy"
    const val START_DIRECTORY = "start_directory"
    const val SYNCHRONIZED_LYRICS_SHOW = "synchronized_lyrics_show"
    const val INITIALIZED_BLACKLIST = "initialized_blacklist"
    const val LIBRARY_CATEGORIES = "library_categories"
    const val ALBUM_COVER_STYLE = "album_cover_style_id"
    const val CIRCULAR_ALBUM_ART = "circular_album_art"
    const val CAROUSEL_EFFECT = "carousel_effect"
    const val DESATURATED_COLOR = "desaturated_color"
    const val BLACK_THEME = "black_theme"
    private const val REMEMBER_SHUFFLE = "remember_shuffle"

    @JvmStatic
    fun isAllowedToDownloadMetadata(context: Context): Boolean {
        return when (autoDownloadImagesPolicy()) {
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
            else -> R.style.Theme_Musical_Light
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
    @get:StyleRes
    val generalTheme: Int
        get() = themeResFromPrefValue(
            sharedPreferences.getString(
                GENERAL_THEME,
                "light"
            )
        )

    @JvmStatic
    fun setGeneralTheme(theme: String?) {
        val editor = sharedPreferences.edit()
        editor.putString(GENERAL_THEME, theme)
        editor.apply()
    }

    @JvmStatic
    fun rememberLastTab(): Boolean {
        return sharedPreferences.getBoolean(REMEMBER_LAST_TAB, true)
    }

    @JvmStatic
    var lastPage: Int
        get() = sharedPreferences.getInt(LAST_PAGE, 0)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(LAST_PAGE, value)
            editor.apply()
        }

    @JvmStatic
    var lastMusicChooser: Int
        get() = sharedPreferences.getInt(
            LAST_MUSIC_CHOOSER,
            R.id.nav_home
        )
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(LAST_MUSIC_CHOOSER, value)
            editor.apply()
        }

    @JvmStatic
    var albumCoverStyle: AlbumCoverStyle
        get() {
            val id: Int = sharedPreferences.getInt(ALBUM_COVER_STYLE, 0)
            for (albumCoverStyle in AlbumCoverStyle.values()) {
                if (albumCoverStyle.id == id) {
                    return albumCoverStyle
                }
            }
            return AlbumCoverStyle.Card
        }
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putInt(ALBUM_COVER_STYLE, value.id)
            editor.apply()
        }

    @JvmStatic
    var isDesaturatedColor
        get() = sharedPreferences.getBoolean(
            DESATURATED_COLOR, false
        )
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(DESATURATED_COLOR, value)
            editor.apply()
        }


    @JvmStatic
    @set:SuppressLint("CommitPrefEdits")
    var nowPlayingScreen: NowPlayingScreen
        get() {
            val id = sharedPreferences.getInt(NOW_PLAYING_SCREEN_ID, 0)
            for (nowPlayingScreen in NowPlayingScreen.values()) {
                if (nowPlayingScreen.id == id) return nowPlayingScreen
            }
            return NowPlayingScreen.CARD
        }
        set(nowPlayingScreen) {
            val editor = sharedPreferences.edit()
            editor.putInt(NOW_PLAYING_SCREEN_ID, nowPlayingScreen.id)
            editor.apply()
        }

    @JvmStatic
    var isClassicNotification
        get() = sharedPreferences.getBoolean(CLASSIC_NOTIFICATION, false)
        set(value) = sharedPreferences.edit { putBoolean(CLASSIC_NOTIFICATION, value) }

    @JvmStatic
    var isColoredNotification
        get() = sharedPreferences.getBoolean(
            COLORED_NOTIFICATION, true
        )
        set(value) = sharedPreferences.edit {
            putBoolean(COLORED_NOTIFICATION, value)
        }

    @JvmStatic
    var isColoredAppShortcuts
        get() = sharedPreferences.getBoolean(
            COLORED_APP_SHORTCUTS, true
        )
        set(value) = sharedPreferences.edit {
            putBoolean(COLORED_APP_SHORTCUTS, value)
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
    fun blurredAlbumArt(): Boolean {
        return sharedPreferences.getBoolean(BLURRED_ALBUM_ART, false)
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
    var songSortOrder: String?
        get() = sharedPreferences.getString(
            SONG_SORT_ORDER,
            SortOrder.SongSortOrder.SONG_A_Z
        )
        set(sortOrder) {
            val editor = sharedPreferences.edit()
            editor.putString(SONG_SORT_ORDER, sortOrder)
            editor.commit()
        }

    @JvmStatic
    val genreSortOrder: String?
        get() = sharedPreferences.getString(
            GENRE_SORT_ORDER,
            SortOrder.GenreSortOrder.GENRE_A_Z
        )

    @JvmStatic
    val lastAddedCutoff: Long
        get() {
            val calendarUtil = CalendarUtil()
            val interval: Long
            interval = when (sharedPreferences.getString(LAST_ADDED_CUTOFF, "")) {
                "today" -> calendarUtil.elapsedToday
                "this_week" -> calendarUtil.elapsedWeek
                "past_seven_days" -> calendarUtil.getElapsedDays(7)
                "past_three_months" -> calendarUtil.getElapsedMonths(3)
                "this_year" -> calendarUtil.elapsedYear
                "this_month" -> calendarUtil.elapsedMonth
                else -> calendarUtil.elapsedMonth
            }
            return (System.currentTimeMillis() - interval) / 1000
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

    @JvmStatic
    var sleepTimerFinishMusic: Boolean
        get() = sharedPreferences.getBoolean(SLEEP_TIMER_FINISH_SONG, false)
        set(value) {
            val editor = sharedPreferences.edit()
            editor.putBoolean(SLEEP_TIMER_FINISH_SONG, value)
            editor.apply()
        }

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
    fun setAlbumColoredFooters(value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ALBUM_COLORED_FOOTERS, value)
        editor.apply()
    }

    @JvmStatic
    fun albumColoredFooters(): Boolean {
        return sharedPreferences.getBoolean(ALBUM_COLORED_FOOTERS, true)
    }

    @JvmStatic
    fun setAlbumArtistColoredFooters(value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ALBUM_ARTIST_COLORED_FOOTERS, value)
        editor.apply()
    }

    @JvmStatic
    fun albumArtistColoredFooters(): Boolean {
        return sharedPreferences.getBoolean(ALBUM_ARTIST_COLORED_FOOTERS, true)
    }

    @JvmStatic
    fun setSongColoredFooters(value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(SONG_COLORED_FOOTERS, value)
        editor.apply()
    }

    @JvmStatic
    fun songColoredFooters(): Boolean {
        return sharedPreferences.getBoolean(SONG_COLORED_FOOTERS, true)
    }

    @JvmStatic
    fun setArtistColoredFooters(value: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ARTIST_COLORED_FOOTERS, value)
        editor.apply()
    }

    @JvmStatic
    fun artistColoredFooters(): Boolean {
        return sharedPreferences.getBoolean(ARTIST_COLORED_FOOTERS, true)
    }

    @JvmStatic
    fun setLastChangeLogVersion(version: Int) {
        sharedPreferences.edit().putInt(LAST_CHANGELOG_VERSION, version).apply()
    }

    @JvmStatic
    val lastChangelogVersion: Int
        get() = sharedPreferences.getInt(LAST_CHANGELOG_VERSION, -1)

    @JvmStatic
    @SuppressLint("CommitPrefEdits")
    fun setIntroShown() {
        // don't use apply here
        sharedPreferences.edit().putBoolean(INTRO_SHOWN, true).apply()
    }

    @JvmStatic
    fun introShown(): Boolean {
        return sharedPreferences.getBoolean(INTRO_SHOWN, false)
    }

    @JvmStatic
    fun rememberShuffle(): Boolean {
        return sharedPreferences.getBoolean(REMEMBER_SHUFFLE, true)
    }

    @JvmStatic
    fun autoDownloadImagesPolicy(): String? {
        return sharedPreferences.getString(
            AUTO_DOWNLOAD_IMAGES_POLICY,
            "only_wifi"
        )
    }

    @JvmStatic
    var startDirectory: File?
        get() = File(
            sharedPreferences.getString(
                START_DIRECTORY,
                FoldersFragment.getDefaultStartDirectory().path
            )
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

    @JvmStatic
    fun setInitializedBlacklist() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(INITIALIZED_BLACKLIST, true)
        editor.apply()
    }

    @JvmStatic
    fun initializedBlacklist(): Boolean {
        return sharedPreferences.getBoolean(INITIALIZED_BLACKLIST, false)
    }

    private val isBlackMode
        get() = sharedPreferences.getBoolean(
            BLACK_THEME, false
        )

    @JvmStatic
    var libraryCategoryInfos: List<CategoryInfo?>?
        get() {
            val data =
                sharedPreferences.getString(LIBRARY_CATEGORIES, null)
            if (data != null) {
                val gson = Gson()
                val collectionType =
                    object : TypeToken<List<CategoryInfo?>?>() {}.type
                try {
                    return gson.fromJson<List<CategoryInfo>>(data, collectionType)
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                }
            }
            return defaultLibraryCategoryInfos
        }
        set(categories) {
            val gson = Gson()
            val collectionType =
                object : TypeToken<List<CategoryInfo?>?>() {}.type
            val editor = sharedPreferences.edit()
            editor.putString(
                LIBRARY_CATEGORIES,
                gson.toJson(categories, collectionType)
            )
            editor.apply()
        }

    @JvmStatic
    val defaultLibraryCategoryInfos: List<CategoryInfo>
        get() {
            val defaultCategoryInfos: MutableList<CategoryInfo> =
                ArrayList(5)
            defaultCategoryInfos.add(CategoryInfo(CategoryInfo.Category.SONGS, true))
            defaultCategoryInfos.add(CategoryInfo(CategoryInfo.Category.ALBUMS, true))
            defaultCategoryInfos.add(CategoryInfo(CategoryInfo.Category.ARTISTS, true))
            defaultCategoryInfos.add(CategoryInfo(CategoryInfo.Category.GENRES, true))
            defaultCategoryInfos.add(CategoryInfo(CategoryInfo.Category.PLAYLISTS, true))
            return defaultCategoryInfos
        }


    @JvmStatic
    fun getGeneralThemeValue(isSystemDark: Boolean): ThemeMode {
        val themeMode: String =
            sharedPreferences.getStringOrDefault(GENERAL_THEME, "auto")
        return if (isBlackMode && isSystemDark) {
            ThemeMode.BLACK
        } else {
            if (isBlackMode && themeMode == "dark") {
                ThemeMode.BLACK
            } else {
                when (themeMode) {
                    "light" -> ThemeMode.LIGHT
                    "dark" -> ThemeMode.DARK
                    "auto" -> ThemeMode.AUTO
                    else -> ThemeMode.AUTO
                }
            }
        }
    }

}