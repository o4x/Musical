package com.o4x.musical.ui.fragments.mainactivity.datails

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.o4x.musical.R
import com.o4x.musical.extensions.showToast
import com.o4x.musical.extensions.startImagePicker
import com.o4x.musical.helper.MusicPlayerRemote.openAndShuffleQueue
import com.o4x.musical.model.Genre
import com.o4x.musical.ui.adapter.song.SongAdapter
import com.o4x.musical.ui.viewmodel.GenreDetailsViewModel
import com.o4x.musical.util.CustomImageUtil
import kotlinx.android.synthetic.main.fragment_detail.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.util.*

class GenreDetailFragment : AbsDetailFragment<Genre, SongAdapter>() {

    private val viewModel: GenreDetailsViewModel by viewModel {
        parametersOf(requireArguments().getParcelable(EXTRA))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.addMusicServiceEventListener(viewModel)

        viewModel.getSongs().observe(viewLifecycleOwner, {
            adapter?.swapDataSet(it)
        })
    }

    override fun onResume() {
        super.onResume()
        setToolbarTitle(data?.name)
    }

    override fun setUpRecyclerView() {
        super.setUpRecyclerView()
        adapter = SongAdapter(mainActivity, ArrayList(), R.layout.item_list, mainActivity)
        recycler_view.adapter = adapter
        adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_genre_detail, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_genre -> {
                openAndShuffleQueue(adapter!!.dataSet, true)
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
        return super.onOptionsItemSelected(item)
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