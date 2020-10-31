package com.o4x.musical.ui.fragments.mainactivity.folders

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.PopupMenu
import android.widget.Toast
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import code.name.monkey.appthemehelper.common.ATHToolbarActivity
import code.name.monkey.appthemehelper.util.ToolbarContentTintHelper
import com.afollestad.materialcab.MaterialCab
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.o4x.musical.R
import com.o4x.musical.extensions.surfaceColor
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.menu.SongMenuHelper
import com.o4x.musical.helper.menu.SongsMenuHelper
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.interfaces.LoaderIds
import com.o4x.musical.misc.DialogAsyncTask
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.misc.WrappedAsyncTaskLoader
import com.o4x.musical.model.Song
import com.o4x.musical.ui.adapter.SongFileAdapter
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment.ArrayListPathsAsyncTask.OnPathsListedCallback
import com.o4x.musical.ui.fragments.mainactivity.folders.FoldersFragment.ListSongsAsyncTask.OnSongsListedCallback
import com.o4x.musical.util.FileUtil
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.PreferenceUtil.startDirectory
import com.o4x.musical.util.ViewUtil
import com.o4x.musical.util.scanPaths
import com.o4x.musical.views.BreadCrumbLayout.Crumb
import com.o4x.musical.views.BreadCrumbLayout.SelectionCallback
import kotlinx.android.synthetic.main.fragment_folder.*
import java.io.File
import java.io.FileFilter
import java.lang.ref.WeakReference
import java.util.*

class FoldersFragment : AbsMainActivityFragment(R.layout.fragment_folder), CabHolder, SelectionCallback,
    SongFileAdapter.Callbacks, LoaderManager.LoaderCallbacks<List<File>> {

    private var adapter: SongFileAdapter? = null
    private var cab: MaterialCab? = null

    fun setCrumb(crumb: Crumb?, addToHistory: Boolean) {
        if (crumb == null) return
        saveScrollPosition()
        mainActivity.bread_crumbs.setActiveOrAdd(crumb, false)
        if (addToHistory) {
            mainActivity.bread_crumbs.addHistory(crumb)
        }
        loaderManager.restartLoader(LOADER_ID, null, this)
    }

    private fun saveScrollPosition() {
        val crumb = activeCrumb
        if (crumb != null) {
            crumb.scrollPosition =
                (recycler_view!!.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition()
        }
    }

    private val activeCrumb: Crumb?
        private get() = if (mainActivity.bread_crumbs.size() > 0) mainActivity.bread_crumbs.getCrumb(
            mainActivity.bread_crumbs.activeIndex) else null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(CRUMBS, mainActivity.bread_crumbs.stateWrapper)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState == null) {
            setCrumb(Crumb(FileUtil.safeGetCanonicalFile(startDirectory)), true)
        } else {
            mainActivity.bread_crumbs.restoreFromStateWrapper(savedInstanceState.getParcelable(CRUMBS))
            loaderManager.initLoader(LOADER_ID, null, this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAppbarColor()
        setUpBreadCrumbs()
        setUpRecyclerView()
        setUpAdapter()
    }

    override fun onReloadSubToolbar() {
        super.onReloadSubToolbar()
        mainActivity.bread_crumbs.visibility = View.VISIBLE
    }

    private fun setUpAppbarColor() {
        val primaryColor = surfaceColor()
        mainActivity.bread_crumbs.setBackgroundColor(primaryColor)
        mainActivity.bread_crumbs.setActivatedContentColor(ToolbarContentTintHelper.toolbarTitleColor(mainActivity,
            primaryColor))
        mainActivity.bread_crumbs.setDeactivatedContentColor(ToolbarContentTintHelper.toolbarSubtitleColor(
            mainActivity, primaryColor))
    }

    private fun setUpBreadCrumbs() {
        mainActivity.bread_crumbs.setCallback(this)
    }

    private fun setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(activity, recycler_view, themeColor(
            mainActivity))
        recycler_view.layoutManager = OverScrollLinearLayoutManager(requireContext())
        recycler_view.addAppbarListener()
    }

    private fun setUpAdapter() {
        adapter = SongFileAdapter(mainActivity, LinkedList(), R.layout.item_list, this, this)
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
        recycler_view!!.adapter = adapter
        checkIsEmpty()
    }

    override fun onPause() {
        super.onPause()
        saveScrollPosition()
    }

    override fun handleBackPress(): Boolean {
        if (cab != null && cab!!.isActive) {
            cab!!.finish()
            return true
        }
        if (mainActivity.bread_crumbs.popHistory()) {
            setCrumb(mainActivity.bread_crumbs.lastHistory(), false)
            return true
        }
        return false
    }

    override fun openCab(menuRes: Int, callback: MaterialCab.Callback): MaterialCab {
        if (cab != null && cab!!.isActive) cab!!.finish()
        cab = MaterialCab(mainActivity, R.id.cab_stub)
            .setMenu(menuRes)
            .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
            .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(themeColor(
                mainActivity)))
            .start(callback)
        return cab!!
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_folders, menu)
    }

    override fun onCrumbSelection(crumb: Crumb, index: Int) {
        setCrumb(crumb, true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_go_to_start_directory -> {
                setCrumb(Crumb(FileUtil.safeGetCanonicalFile(startDirectory)), true)
                return true
            }
            R.id.action_scan -> {
                val crumb = activeCrumb
                if (crumb != null) {
                    ArrayListPathsAsyncTask(activity,
                        object : OnPathsListedCallback {
                            override fun onPathsListed(paths: Array<String>) {
                                scanPaths(paths)
                            }
                        }).execute(
                        ArrayListPathsAsyncTask.LoadingInfo(crumb.file, AUDIO_FILE_FILTER))
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onFileSelected(file: File) {
        val canonicalFile =
            FileUtil.safeGetCanonicalFile(file) // important as we compare the path value later
        if (canonicalFile.isDirectory) {
            setCrumb(Crumb(canonicalFile), true)
        } else {
            val fileFilter = FileFilter { pathname: File ->
                !pathname.isDirectory && AUDIO_FILE_FILTER.accept(pathname)
            }
            ListSongsAsyncTask(activity,
                null,
                object : OnSongsListedCallback {
                    override fun onSongsListed(songs: List<Song?>, extra: Any?) {
                        var startIndex = -1
                        for (i in songs.indices) {
                            if (canonicalFile.path == songs[i]!!.data) {
                                startIndex = i
                                break
                            }
                        }
                        if (startIndex > -1) {
                            MusicPlayerRemote.openQueue(songs as List<Song>, startIndex, true)
                        } else {
                            Snackbar.make(coordinator_layout!!,
                                Html.fromHtml(String.format(getString(R.string.not_listed_in_media_store),
                                    canonicalFile.name)),
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.action_scan) {
                                    scanPaths(arrayOf(canonicalFile.path))
                                }
                                .setActionTextColor(themeColor(mainActivity))
                                .show()
                        }
                    }
                }).execute(ListSongsAsyncTask.LoadingInfo(toList(canonicalFile.parentFile),
                fileFilter,
                fileComparator))
        }
    }

    override fun onMultipleItemAction(item: MenuItem, files: List<File>) {
        val itemId = item.itemId
        ListSongsAsyncTask(activity,
            null,
            object : OnSongsListedCallback {
                override fun onSongsListed(songs: List<Song?>, extra: Any?) {
                    if (songs.isNotEmpty()) {
                        SongsMenuHelper.handleMenuClick(mainActivity, songs, itemId)
                    }
                    if (songs.size != files.size) {
                        Snackbar.make(coordinator_layout!!,
                            R.string.some_files_are_not_listed_in_the_media_store,
                            Snackbar.LENGTH_LONG)
                            .setAction(R.string.action_scan) { v: View? ->
                                val paths = arrayOf<String>()
                                for (i in files.indices) {
                                    paths[i] = FileUtil.safeGetCanonicalPath(files[i])
                                }
                                scanPaths(paths)
                            }
                            .setActionTextColor(themeColor(mainActivity))
                            .show()
                    }
                }
            }).execute(ListSongsAsyncTask.LoadingInfo(files, AUDIO_FILE_FILTER, fileComparator))
    }

    private fun toList(file: File): List<File> {
        val files: MutableList<File> = ArrayList(1)
        files.add(file)
        return files
    }

    private var fileComparator = Comparator { lhs: File, rhs: File ->
        if (lhs.isDirectory && !rhs.isDirectory) {
            return@Comparator -1
        } else if (!lhs.isDirectory && rhs.isDirectory) {
            return@Comparator 1
        } else {
            return@Comparator lhs.name.compareTo(rhs.name, ignoreCase = true)
        }
    }

    override fun onFileMenuClicked(file: File, view: View) {
        val popupMenu = PopupMenu(activity, view)
        if (file.isDirectory) {
            popupMenu.inflate(R.menu.menu_item_directory)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                val itemId = item.itemId
                when (itemId) {
                    R.id.action_play_next, R.id.action_add_to_current_playing, R.id.action_add_to_playlist, R.id.action_delete_from_device -> {
                        ListSongsAsyncTask(activity,
                            null,
                            object : OnSongsListedCallback {
                                override fun onSongsListed(songs: List<Song?>, extra: Any?) {
                                    if (!songs.isEmpty()) {
                                        SongsMenuHelper.handleMenuClick(mainActivity, songs, itemId)
                                    }
                                }
                            }).execute(ListSongsAsyncTask.LoadingInfo(toList(file),
                            AUDIO_FILE_FILTER,
                            fileComparator))
                        return@setOnMenuItemClickListener true
                    }
                    R.id.action_set_as_start_directory -> {
                        startDirectory = file
                        Toast.makeText(activity,
                            String.format(getString(R.string.new_start_directory), file.path),
                            Toast.LENGTH_SHORT).show()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.action_scan -> {
                        ArrayListPathsAsyncTask(activity,
                            object : OnPathsListedCallback {
                                override fun onPathsListed(paths: Array<String>) {
                                    scanPaths(paths)
                                }
                            }).execute(
                            ArrayListPathsAsyncTask.LoadingInfo(file, AUDIO_FILE_FILTER))
                        return@setOnMenuItemClickListener true
                    }
                }
                false
            }
        } else {
            popupMenu.inflate(R.menu.menu_item_file)
            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (val itemId = item.itemId) {
                    R.id.action_play_next, R.id.action_add_to_current_playing, R.id.action_add_to_playlist, R.id.action_go_to_album, R.id.action_go_to_artist, R.id.action_share, R.id.action_tag_editor, R.id.action_details, R.id.action_delete_from_device -> {
                        ListSongsAsyncTask(activity,
                            null,
                            object : OnSongsListedCallback {
                                override fun onSongsListed(songs: List<Song?>, extra: Any?) {
                                    if (songs.isNotEmpty()) {
                                        SongMenuHelper.handleMenuClick(mainActivity,
                                            songs[0]!!,
                                            itemId)
                                    } else {
                                        Snackbar.make(coordinator_layout!!,
                                            Html.fromHtml(String.format(getString(R.string.not_listed_in_media_store),
                                                file.name)),
                                            Snackbar.LENGTH_LONG)
                                            .setAction(R.string.action_scan) {
                                                scanPaths(arrayOf(FileUtil.safeGetCanonicalPath(file)))
                                            }
                                            .setActionTextColor(themeColor(mainActivity))
                                            .show()
                                    }
                                }
                            }).execute(ListSongsAsyncTask.LoadingInfo(toList(file),
                            AUDIO_FILE_FILTER,
                            fileComparator))
                        return@setOnMenuItemClickListener true
                    }
                    R.id.action_scan -> {
                        scanPaths(arrayOf(FileUtil.safeGetCanonicalPath(file)))
                        return@setOnMenuItemClickListener true
                    }
                }
                false
            }
        }
        popupMenu.show()
    }

    private fun checkIsEmpty() {
        if (empty != null) {
            empty!!.visibility =
                if (adapter == null || adapter!!.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun updateAdapter(files: List<File>) {
        adapter!!.swapDataSet(files)
        val crumb = activeCrumb
        if (crumb != null && recycler_view != null) {
            (recycler_view!!.layoutManager as LinearLayoutManager?)!!.scrollToPositionWithOffset(
                crumb.scrollPosition,
                0)
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<File>> {
        return AsyncFileLoader(this) as Loader<List<File>>
    }

    override fun onLoadFinished(loader: Loader<List<File>>, data: List<File>) {
        updateAdapter(data)
    }

    override fun onLoaderReset(loader: Loader<List<File>>) {
        updateAdapter(LinkedList())
    }

    private class AsyncFileLoader(foldersFragment: FoldersFragment) :
        WrappedAsyncTaskLoader<List<File?>?>(foldersFragment.activity) {
        private val fragmentWeakReference: WeakReference<FoldersFragment>
        override fun loadInBackground(): List<File>? {
            val foldersFragment = fragmentWeakReference.get()
            var directory: File? = null
            if (foldersFragment != null) {
                val crumb = foldersFragment.activeCrumb
                if (crumb != null) {
                    directory = crumb.file
                }
            }
            return if (directory != null) {
                val files = FileUtil.listFiles(directory, AUDIO_FILE_FILTER)
                Collections.sort(files, foldersFragment!!.fileComparator)
                files
            } else {
                LinkedList()
            }
        }

        init {
            fragmentWeakReference = WeakReference(foldersFragment)
        }
    }

    private class ListSongsAsyncTask(
        context: Context?,
        private val extra: Any?,
        callback: OnSongsListedCallback
    ) : ListingFilesDialogAsyncTask<ListSongsAsyncTask.LoadingInfo?, Void?, List<Song?>?>(context,
        500) {
        private val contextWeakReference: WeakReference<Context?>
        private val callbackWeakReference: WeakReference<OnSongsListedCallback>
        override fun onPreExecute() {
            super.onPreExecute()
            checkCallbackReference()
            checkContextReference()
        }

        protected override fun doInBackground(vararg params: LoadingInfo?): List<Song?>? {
            return try {
                val info = params[0]
                val files = FileUtil.listFilesDeep(info!!.files, info.fileFilter)
                if (isCancelled || checkContextReference() == null || checkCallbackReference() == null) return null
                Collections.sort(files, info.fileComparator)
                val context = checkContextReference()
                if (isCancelled || context == null || checkCallbackReference() == null) null else FileUtil.matchFilesWithMediaStore(
                    context,
                    files)
            } catch (e: Exception) {
                e.printStackTrace()
                cancel(false)
                null
            }
        }

        protected override fun onPostExecute(songs: List<Song?>?) {
            super.onPostExecute(songs)
            val callback = checkCallbackReference()
            if (songs != null && callback != null) callback.onSongsListed(songs, extra)
        }

        private fun checkContextReference(): Context? {
            val context = contextWeakReference.get()
            if (context == null) {
                cancel(false)
            }
            return context
        }

        private fun checkCallbackReference(): OnSongsListedCallback? {
            val callback = callbackWeakReference.get()
            if (callback == null) {
                cancel(false)
            }
            return callback
        }

        class LoadingInfo(
            val files: List<File>,
            val fileFilter: FileFilter,
            val fileComparator: Comparator<File>
        )

        interface OnSongsListedCallback {
            fun onSongsListed(songs: List<Song?>, extra: Any?)
        }

        init {
            contextWeakReference = WeakReference(context)
            callbackWeakReference = WeakReference(callback)
        }
    }

    class ArrayListPathsAsyncTask(context: Context?, callback: OnPathsListedCallback) :
        ListingFilesDialogAsyncTask<ArrayListPathsAsyncTask.LoadingInfo?, String?, Array<String>?>(
            context,
            500) {
        private val onPathsListedCallbackWeakReference: WeakReference<OnPathsListedCallback>
        override fun onPreExecute() {
            super.onPreExecute()
            checkCallbackReference()
        }

        protected override fun doInBackground(vararg params: LoadingInfo?): Array<String>? {
            return try {
                if (isCancelled || checkCallbackReference() == null) return null
                val info = params[0]
                val paths: Array<String>
                if (info!!.file.isDirectory) {
                    val files = FileUtil.listFilesDeep(info.file, info.fileFilter)
                    if (isCancelled || checkCallbackReference() == null) return null
                    paths = arrayOf()
                    for (i in files.indices) {
                        val f = files[i]
                        paths[i] = FileUtil.safeGetCanonicalPath(f)
                        if (isCancelled || checkCallbackReference() == null) return null
                    }
                } else {
                    paths = arrayOf()
                    paths[0] = FileUtil.safeGetCanonicalPath(info.file)
                }
                paths
            } catch (e: Exception) {
                e.printStackTrace()
                cancel(false)
                null
            }
        }

        override fun onPostExecute(paths: Array<String>?) {
            super.onPostExecute(paths)
            val callback = checkCallbackReference()
            if (callback != null && paths != null) {
                callback.onPathsListed(paths)
            }
        }

        private fun checkCallbackReference(): OnPathsListedCallback? {
            val callback = onPathsListedCallbackWeakReference.get()
            if (callback == null) {
                cancel(false)
            }
            return callback
        }

        class LoadingInfo(val file: File, val fileFilter: FileFilter)
        interface OnPathsListedCallback {
            fun onPathsListed(paths: Array<String>)
        }

        init {
            onPathsListedCallbackWeakReference = WeakReference(callback)
        }
    }

    abstract class ListingFilesDialogAsyncTask<Params, Progress, Result> :
        DialogAsyncTask<Params, Progress, Result> {
        constructor(context: Context?) : super(context) {}
        constructor(context: Context?, showDelay: Int) : super(context, showDelay) {}

        override fun createDialog(context: Context): Dialog {
            return MaterialDialog(context)
                .title(R.string.listing_files)
                .negativeButton(android.R.string.cancel) {
                    cancel(false)
                }
        }
    }

    companion object {
        private const val LOADER_ID = LoaderIds.FOLDERS_FRAGMENT
        protected const val PATH = "path"
        protected const val CRUMBS = "crumbs"
        @JvmField
        val AUDIO_FILE_FILTER = FileFilter { file: File ->
            !file.isHidden && (file.isDirectory ||
                    FileUtil.fileIsMimeType(file, "audio/*", MimeTypeMap.getSingleton()) ||
                    FileUtil.fileIsMimeType(file, "application/ogg", MimeTypeMap.getSingleton()))
        }

        // root
        val defaultStartDirectory: File
            get() {
                val musicDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                val startFolder: File
                startFolder = if (musicDir.exists() && musicDir.isDirectory) {
                    musicDir
                } else {
                    val externalStorage = Environment.getExternalStorageDirectory()
                    if (externalStorage.exists() && externalStorage.isDirectory) {
                        externalStorage
                    } else {
                        File("/") // root
                    }
                }
                return startFolder
            }
    }
}