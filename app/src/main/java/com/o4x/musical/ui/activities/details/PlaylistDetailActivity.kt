package com.o4x.musical.ui.activities.details

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import butterknife.BindView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore.Companion.themeColor
import com.afollestad.materialcab.MaterialCab
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.o4x.musical.R
import com.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import com.o4x.musical.helper.menu.PlaylistMenuHelper.handleMenuClick
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.AbsCustomPlaylist
import com.o4x.musical.model.Playlist
import com.o4x.musical.ui.activities.base.AbsMusicPanelActivity
import com.o4x.musical.ui.adapter.song.OrderablePlaylistSongAdapter
import com.o4x.musical.ui.adapter.song.PlaylistSongAdapter
import com.o4x.musical.ui.adapter.song.SongAdapter
import com.o4x.musical.ui.viewmodel.PlaylistDetailsViewModel
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.PlaylistsUtil
import com.o4x.musical.util.ViewUtil
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class PlaylistDetailActivity : AbsMusicPanelActivity(), CabHolder {

    companion object {
        @JvmField
        var EXTRA_PLAYLIST = "extra_playlist"
    }

    private val viewModel by viewModel<PlaylistDetailsViewModel> {
        parametersOf(intent.extras!!.getParcelable(EXTRA_PLAYLIST))
    }

    @BindView(R.id.recycler_view)
    lateinit var recyclerView: RecyclerView
    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar
    @BindView(android.R.id.empty)
    lateinit  var empty: TextView

    private var playlist: Playlist? = null
    private var cab: MaterialCab? = null
    private var adapter: SongAdapter? = null
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setStatusBarColorAuto()
        setNavigationBarColorAuto()
        setTaskDescriptionColorAuto()
        playlist = intent.extras!!.getParcelable(EXTRA_PLAYLIST)
        setUpRecyclerView()
        setUpToolbar()
        addMusicServiceEventListener(viewModel)
        viewModel.playListSongs.observe(this, {
            adapter?.swapDataSet(it)
        })
    }

    override fun createContentView(): View? {
        return wrapSlidingMusicPanel(R.layout.activity_playlist_detail)
    }

    private fun setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(
            this,
            recyclerView as FastScrollRecyclerView?,
            themeColor(this)
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        if (playlist is AbsCustomPlaylist) {
            adapter = PlaylistSongAdapter(this, ArrayList(), R.layout.item_list, this)
            recyclerView.adapter = adapter
        } else {
            recyclerViewDragDropManager = RecyclerViewDragDropManager()
            val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
            adapter = OrderablePlaylistSongAdapter(
                this,
                ArrayList(),
                R.layout.item_list,
                this,
                { fromPosition: Int, toPosition: Int ->
                    if (PlaylistsUtil.moveItem(
                            this@PlaylistDetailActivity,
                            playlist!!.id,
                            fromPosition,
                            toPosition
                        )
                    ) {
                        val song = adapter!!.dataSet.removeAt(fromPosition)
                        adapter!!.dataSet.add(toPosition, song)
                        adapter!!.notifyItemMoved(fromPosition, toPosition)
                    }
                })
            wrappedAdapter = recyclerViewDragDropManager!!.createWrappedAdapter(adapter!!)
            recyclerView.adapter = wrappedAdapter
            recyclerView.itemAnimator = animator
            recyclerViewDragDropManager!!.attachRecyclerView(recyclerView)
        }
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    private fun setUpToolbar() {
        toolbar.setBackgroundColor(themeColor(this))
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        setToolbarTitle(playlist!!.name)
    }

    private fun setToolbarTitle(title: String) {
        supportActionBar!!.title = title
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(
            if (playlist is AbsCustomPlaylist) R.menu.menu_smart_playlist_detail else R.menu.menu_playlist_detail,
            menu
        )
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_playlist -> {
                openAndShuffleQueue(adapter!!.dataSet, true)
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return handleMenuClick(this, playlist!!, item)
    }

    override fun openCab(menu: Int, callback: MaterialCab.Callback): MaterialCab {
        if (cab != null && cab!!.isActive) cab!!.finish()
        cab = MaterialCab(this, R.id.cab_stub)
            .setMenu(menu)
            .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
            .setBackgroundColor(PhonographColorUtil.shiftBackgroundColorForLightText(themeColor(this)))
            .start(callback)
        return cab!!
    }

    override fun onBackPressed() {
        if (cab != null && cab!!.isActive) cab!!.finish() else {
            recyclerView.stopScroll()
            super.onBackPressed()
        }
    }

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        if (playlist !is AbsCustomPlaylist) {
            // Playlist deleted
            if (!PlaylistsUtil.doesPlaylistExist(this, playlist!!.id)) {
                finish()
                return
            }

            // Playlist renamed
            val playlistName = PlaylistsUtil.getNameForPlaylist(this, playlist!!.id)
            if (playlistName != playlist!!.name) {
//                playlist = PlaylistLoader.getPlaylist(this, playlist!!.id)
                setToolbarTitle(playlist!!.name)
            }
        }
    }

    private fun checkIsEmpty() {
        empty.visibility =
            if (adapter!!.itemCount == 0) View.VISIBLE else View.GONE
    }

    public override fun onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager!!.cancelDrag()
        }
        super.onPause()
    }

    override fun onDestroy() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager!!.release()
            recyclerViewDragDropManager = null
        }
        recyclerView.itemAnimator = null
        recyclerView.adapter = null
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        adapter = null
        super.onDestroy()
    }
}