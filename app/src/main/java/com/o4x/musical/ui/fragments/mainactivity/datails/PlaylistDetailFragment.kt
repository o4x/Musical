package com.o4x.musical.ui.fragments.mainactivity.datails

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import code.name.monkey.appthemehelper.ThemeStore
import com.afollestad.materialcab.MaterialCab
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.o4x.musical.R
import com.o4x.musical.interfaces.CabHolder
import com.o4x.musical.model.AbsCustomPlaylist
import com.o4x.musical.model.Playlist
import com.o4x.musical.ui.adapter.song.OrderablePlaylistSongAdapter
import com.o4x.musical.ui.adapter.song.PlaylistSongAdapter
import com.o4x.musical.ui.adapter.song.SongAdapter
import com.o4x.musical.ui.fragments.mainactivity.AbsMainActivityFragment
import com.o4x.musical.ui.viewmodel.PlaylistDetailsViewModel
import com.o4x.musical.util.PhonographColorUtil
import com.o4x.musical.util.PlaylistsUtil
import com.o4x.musical.util.ViewUtil
import kotlinx.android.synthetic.main.fragment_detail_playlist.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class PlaylistDetailFragment : AbsMainActivityFragment(R.layout.fragment_detail_playlist),
    CabHolder {

    companion object {
        @JvmField
        var EXTRA_PLAYLIST = "extra_playlist"
    }

    private val viewModel by viewModel<PlaylistDetailsViewModel> {
        parametersOf(requireArguments().getParcelable(EXTRA_PLAYLIST))
    }

    private var playlist: Playlist? = null
    private var cab: MaterialCab? = null
    private var adapter: SongAdapter? = null
    private var wrappedAdapter: RecyclerView.Adapter<*>? = null
    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(view)
        playlist = requireArguments().getParcelable(EXTRA_PLAYLIST)
        setUpRecyclerView()
        mainActivity.addMusicServiceEventListener(viewModel)
        viewModel.playListSongs.observe(viewLifecycleOwner, {
            adapter?.swapDataSet(it)
        })
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(playlist!!.name)
    }

    private fun setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(
            requireContext(),
            recycler_view,
            ThemeStore.themeColor(requireContext())
        )
        recycler_view.layoutManager = LinearLayoutManager(requireContext())
        if (playlist is AbsCustomPlaylist) {
            adapter = PlaylistSongAdapter(mainActivity, ArrayList(), R.layout.item_list, this)
            recycler_view.adapter = adapter
        } else {
            recyclerViewDragDropManager = RecyclerViewDragDropManager()
            val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
            adapter = OrderablePlaylistSongAdapter(
                mainActivity,
                ArrayList(),
                R.layout.item_list,
                this,
                { fromPosition: Int, toPosition: Int ->
                    if (PlaylistsUtil.moveItem(
                            requireContext(),
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
            recycler_view.adapter = wrappedAdapter
            recycler_view.itemAnimator = animator
            recyclerViewDragDropManager!!.attachRecyclerView(recycler_view)
        }
        adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        menuInflater.inflate(
//            if (playlist is AbsCustomPlaylist) R.menu.menu_smart_playlist_detail else R.menu.menu_playlist_detail,
//            menu
//        )
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_shuffle_playlist -> {
//                MusicPlayerRemote.openAndShuffleQueue(adapter!!.dataSet, true)
//                return true
//            }
//            android.R.id.home -> {
//                handleBackPress()
//                return true
//            }
//        }
//        return PlaylistMenuHelper.handleMenuClick(this, playlist!!, item)
//    }

    override fun openCab(menu: Int, callback: MaterialCab.Callback): MaterialCab {
        if (cab != null && cab!!.isActive) cab!!.finish()
        cab = MaterialCab(mainActivity, R.id.cab_stub)
            .setMenu(menu)
            .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
            .setBackgroundColor(
                PhonographColorUtil.shiftBackgroundColorForLightText(
                    ThemeStore.themeColor(
                        requireContext()
                    )
                )
            )
            .start(callback)
        return cab!!
    }

    override fun handleBackPress(): Boolean {
        return if (cab != null && cab!!.isActive) {
            cab!!.finish()
            true
        } else {
            recycler_view.stopScroll()
            super.handleBackPress()
        }
    }

    //    override fun onMediaStoreChanged() {
//        super.onMediaStoreChanged()
//        if (playlist !is AbsCustomPlaylist) {
//            // Playlist deleted
//            if (!PlaylistsUtil.doesPlaylistExist(this, playlist!!.id)) {
//                finish()
//                return
//            }
//
//            // Playlist renamed
//            val playlistName = PlaylistsUtil.getNameForPlaylist(this, playlist!!.id)
//            if (playlistName != playlist!!.name) {
////                playlist = PlaylistLoader.getPlaylist(this, playlist!!.id)
//                setToolbarTitle(playlist!!.name)
//            }
//        }
//    }

    private fun checkIsEmpty() {
        empty.visibility =
            if (adapter!!.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager!!.cancelDrag()
        }
        super.onPause()
    }

    override fun onDestroyView() {
        mainActivity.removeMusicServiceEventListener(viewModel)
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager!!.release()
            recyclerViewDragDropManager = null
        }
        recycler_view.itemAnimator = null
        recycler_view.adapter = null
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        adapter = null
        super.onDestroyView()
    }
}