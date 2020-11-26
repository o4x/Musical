package com.o4x.musical.ui.fragments.mainactivity.datails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.extensions.accentColor
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.o4x.musical.R
import com.o4x.musical.databinding.FragmentDetailBinding
import com.o4x.musical.databinding.FragmentSearchBinding
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.ui.fragments.mainactivity.AbsPopupFragment
import com.o4x.musical.util.ViewUtil

open class AbsDetailFragment<T, A: RecyclerView.Adapter<*>> : AbsPopupFragment(R.layout.fragment_detail) {

    companion object {
        const val EXTRA = "extra"
        const val REQUEST_CODE_SELECT_IMAGE = 1400
    }

    var data: T? = null

    var adapter: A? = null
    var wrappedAdapter: RecyclerView.Adapter<*>? = null

    private var _binding: FragmentDetailBinding? = null
    protected val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data = requireArguments().getParcelable(EXTRA)

        setUpRecyclerView()
    }

    open fun setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(
            requireContext(),
            binding.recyclerView,
            accentColor()
        )
        binding.recyclerView.layoutManager = OverScrollLinearLayoutManager(requireContext())
        binding.recyclerView.addAppbarListener()
    }

    fun checkIsEmpty() {
        binding.empty.visibility =
            if (adapter!!.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        adapter = null
        _binding = null
        super.onDestroyView()
    }
}