package github.o4x.musical.ui.fragments.mainactivity.queue

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.RecyclerView
import com.o4x.appthemehelper.extensions.accentColor
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import github.o4x.musical.R
import github.o4x.musical.databinding.FragmentQueueBinding
import github.o4x.musical.helper.MusicPlayerRemote
import github.o4x.musical.misc.OverScrollLinearLayoutManager
import github.o4x.musical.ui.adapter.song.PlayingQueueAdapter
import github.o4x.musical.ui.dialogs.CreatePlaylistDialog
import github.o4x.musical.ui.fragments.mainactivity.AbsQueueFragment
import github.o4x.musical.util.ViewUtil

class QueueFragment : AbsQueueFragment(R.layout.fragment_queue) {

    private lateinit var wrappedAdapter: RecyclerView.Adapter<*>
    private lateinit var recyclerViewDragDropManager: RecyclerViewDragDropManager

    private var _binding: FragmentQueueBinding? = null
    private val binding get() = _binding!!

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
        setUpViews()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_queue, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_shuffle_all -> {
                libraryViewModel.shuffleSongs()
                return true
            }
            R.id.action_new_playlist -> {
                CreatePlaylistDialog.create().show(childFragmentManager, "CREATE_PLAYLIST")
                return true
            }
            R.id.action_search -> {
                mainActivity.openSearch()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setUpViews() {
        binding.queueRecyclerView.apply {
            addAppbarListener()
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
            R.layout.item_list,
            null)
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