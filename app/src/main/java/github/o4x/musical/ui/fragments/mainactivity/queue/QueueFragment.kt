package github.o4x.musical.ui.fragments.mainactivity.queue

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import github.o4x.musical.R
import github.o4x.musical.databinding.FragmentQueueBinding
import github.o4x.musical.helper.MusicPlayerRemote
import github.o4x.musical.misc.OverScrollLinearLayoutManager
import github.o4x.musical.prefs.HomeHeaderPref
import github.o4x.musical.ui.activities.MusicPickerActivity
import github.o4x.musical.ui.adapter.song.PlayingQueueAdapter
import github.o4x.musical.ui.dialogs.CreatePlaylistDialog
import github.o4x.musical.ui.fragments.mainactivity.AbsQueueFragment
import github.o4x.musical.ui.fragments.mainactivity.home.HomeFragment.Companion.REQUEST_CODE_SELECT_SONG
import github.o4x.musical.util.ViewInsetsUtils.applyAppBarPadding
import github.o4x.musical.util.ViewInsetsUtils.applySystemBarsPadding
import github.o4x.musical.util.ViewUtil
import github.o4x.musical.util.accentColor

class QueueFragment : AbsQueueFragment(R.layout.fragment_queue), MenuProvider {

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private lateinit var recyclerViewDragDropManager: RecyclerViewDragDropManager

    private var _binding: FragmentQueueBinding? = null
    private val binding get() = _binding!!

    override val queueRecyclerView: RecyclerView
        get() = binding.queueRecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQueueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainActivity.setSupportActionBar(binding.toolbar)
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.appbar.applySystemBarsPadding(applyTop = true)
        binding.queueRecyclerView.applyAppBarPadding()

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        setUpViews()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.menu_queue, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_shuffle_all -> libraryViewModel.shuffleSongs()
            R.id.action_new_playlist -> CreatePlaylistDialog.create().show(childFragmentManager, "CREATE_PLAYLIST")
            R.id.action_search -> mainActivity.openSearch()
        }
        return true
    }

    private fun setUpViews() {
        binding.queueRecyclerView.apply {
            ViewUtil.setUpFastScrollRecyclerViewColor(
                requireContext(),
                this,
                accentColor())
        }
        checkIsEmpty()
    }

    override fun initQueueView() {
        recyclerViewDragDropManager = RecyclerViewDragDropManager()
        val animator: GeneralItemAnimator = RefactoredDefaultItemAnimator()
        queueAdapter = PlayingQueueAdapter(
            mainActivity,
            MusicPlayerRemote.playingQueue,
            MusicPlayerRemote.position,
            R.layout.item_list)
        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(queueAdapter)
        queueLayoutManager = OverScrollLinearLayoutManager(requireContext())
        binding.queueRecyclerView.layoutManager = queueLayoutManager
        binding.queueRecyclerView.adapter = wrappedAdapter
        binding.queueRecyclerView.itemAnimator = animator
        recyclerViewDragDropManager.attachRecyclerView(binding.queueRecyclerView)

        playerViewModel.queue.observe(viewLifecycleOwner, {
            checkIsEmpty()
        })
    }

    private fun checkIsEmpty() {
        binding.empty.visibility =
            if (queueAdapter.itemCount == 0) View.VISIBLE else View.GONE
    }
}