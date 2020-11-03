package com.o4x.musical.ui.fragments.mainactivity.datails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.ThemeStore
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.o4x.musical.R
import com.o4x.musical.extensions.showToast
import com.o4x.musical.extensions.startImagePicker
import com.o4x.musical.helper.MusicPlayerRemote
import com.o4x.musical.helper.menu.PlaylistMenuHelper
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.model.AbsCustomPlaylist
import com.o4x.musical.model.Playlist
import com.o4x.musical.ui.adapter.song.OrderablePlaylistSongAdapter
import com.o4x.musical.ui.adapter.song.PlaylistSongAdapter
import com.o4x.musical.ui.fragments.mainactivity.AbsPopupFragment
import com.o4x.musical.ui.viewmodel.PlaylistDetailsViewModel
import com.o4x.musical.util.CustomImageUtil
import com.o4x.musical.util.PlaylistsUtil
import com.o4x.musical.util.ViewUtil
import kotlinx.android.synthetic.main.fragment_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class PlaylistDetailFragment : AbsDetailFragment<Playlist, PlaylistSongAdapter>() {

    private val viewModel by viewModel<PlaylistDetailsViewModel> {
        parametersOf(requireArguments().getParcelable(EXTRA))
    }


    private var recyclerViewDragDropManager: RecyclerViewDragDropManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.addMusicServiceEventListener(viewModel)

        setUpRecyclerView()
        viewModel.playListSongs.observe(viewLifecycleOwner, {
            adapter?.swapDataSet(it)
        })
        libraryViewModel.getQueue().observe(viewLifecycleOwner, {
            adapter?.notifyDataSetChanged()
        })
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(data?.name)
    }

    override fun setUpRecyclerView() {
        super.setUpRecyclerView()
        if (data is AbsCustomPlaylist) {
            adapter = PlaylistSongAdapter(mainActivity, ArrayList(), R.layout.item_list, mainActivity)
            recycler_view.adapter = adapter
        } else {
            recyclerViewDragDropManager = RecyclerViewDragDropManager()
            val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
            adapter = OrderablePlaylistSongAdapter(
                mainActivity,
                ArrayList(),
                R.layout.item_list,
                mainActivity
            ) { fromPosition: Int, toPosition: Int ->
                if (PlaylistsUtil.moveItem(
                        requireContext(),
                        data!!.id,
                        fromPosition,
                        toPosition
                    )
                ) {
                    val song = adapter!!.dataSet.removeAt(fromPosition)
                    adapter!!.dataSet.add(toPosition, song)
                    adapter!!.notifyItemMoved(fromPosition, toPosition)
                }
            }
            wrappedAdapter = recyclerViewDragDropManager!!.createWrappedAdapter(adapter!!)
            recycler_view.adapter = wrappedAdapter
            recycler_view.itemAnimator = animator
            recyclerViewDragDropManager!!.attachRecyclerView(recycler_view)
        }
        adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(
            if (data is AbsCustomPlaylist) R.menu.menu_smart_playlist_detail else R.menu.menu_playlist_detail,
            menu
        )
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_playlist -> {
                MusicPlayerRemote.openAndShuffleQueue(adapter!!.dataSet, true)
                return true
            }
            R.id.action_set_image -> {
                startImagePicker(REQUEST_CODE_SELECT_IMAGE)
                return true
            }
            R.id.action_reset_image -> {
                showToast(resources.getString(R.string.updating))
                CustomImageUtil(data).resetCustomImage()
                return true
            }
        }
        return PlaylistMenuHelper.handleMenuClick(mainActivity, data!!, item)
    }

    override fun onMediaStoreChanged() {
        super.onMediaStoreChanged()
        if (data !is AbsCustomPlaylist) {
            // Playlist deleted
            if (!PlaylistsUtil.doesPlaylistExist(requireContext(), data!!.id)) {
                navController().popBackStack()
                return
            }

            // Playlist renamed
            val playlistName = PlaylistsUtil.getNameForPlaylist(requireContext(), data!!.id)
            if (playlistName != data!!.name) {
                libraryViewModel.playlist(data!!.id).observe(viewLifecycleOwner, {
                    data = it
                    setToolbarTitle(data!!.name)
                })
            }
        }
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
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE_SELECT_IMAGE -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let {
                    CustomImageUtil(this.data).setCustomImage(it)
                }
            }
        }
    }
}