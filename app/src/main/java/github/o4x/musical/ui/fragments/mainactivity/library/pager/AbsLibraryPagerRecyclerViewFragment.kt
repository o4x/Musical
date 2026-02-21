package github.o4x.musical.ui.fragments.mainactivity.library.pager

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import github.o4x.musical.R
import github.o4x.musical.databinding.FragmentLibraryRecyclerViewBinding
import github.o4x.musical.util.ViewUtil
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import github.o4x.musical.util.accentColor

abstract class AbsLibraryPagerRecyclerViewFragment<A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager?> :
    AbsLibraryPagerFragment(R.layout.fragment_library_recycler_view) {

    // View Binding Reference
    private var _binding: FragmentLibraryRecyclerViewBinding? = null
    // This property is only valid between onViewCreated and onDestroyView.
    private val binding get() = _binding!!

    protected var adapter: A? = null
        private set
    protected var layoutManager: LM? = null
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Initialize View Binding
        _binding = FragmentLibraryRecyclerViewBinding.bind(view)

        super.onViewCreated(view, savedInstanceState)
        initLayoutManager()
        initAdapter()
        setUpRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up binding to prevent memory leaks
        _binding = null
    }

    private fun setUpRecyclerView() {
        // Note: ViewBinding converts snake_case IDs (recycler_view) to camelCase (recyclerView)
        if (binding.recyclerView is FastScrollRecyclerView) {
            ViewUtil.setUpFastScrollRecyclerViewColor(
                serviceActivity,
                binding.recyclerView as FastScrollRecyclerView,
                accentColor()
            )
        }
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
    }

    protected fun invalidateLayoutManager() {
        initLayoutManager()
        // Check if binding is valid before accessing (in case called async)
        if (_binding != null) {
            binding.recyclerView.layoutManager = layoutManager
        }
    }

    protected fun invalidateAdapter() {
        initAdapter()
        checkIsEmpty()
        if (_binding != null) {
            binding.recyclerView.adapter = adapter
        }
    }

    private fun initAdapter() {
        adapter = createAdapter()
        adapter!!.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkIsEmpty()
            }
        })
    }

    private fun initLayoutManager() {
        layoutManager = createLayoutManager()
    }

    private fun checkIsEmpty() {
        // Ensure binding is alive before accessing views
        if (_binding != null) {
            binding.empty.setText(emptyMessage)
            binding.empty.visibility =
                if (adapter == null || adapter!!.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    @get:StringRes
    protected open val emptyMessage: Int
        get() = R.string.empty

    // Expose the RecyclerView safely via binding
    val recyclerView: RecyclerView?
        get() = _binding?.recyclerView

    protected abstract fun createLayoutManager(): LM
    protected abstract fun createAdapter(): A
}
