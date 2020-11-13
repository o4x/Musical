package com.o4x.musical.ui.fragments.mainactivity.datails

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import code.name.monkey.appthemehelper.extensions.accentColor
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils
import com.o4x.musical.R
import com.o4x.musical.misc.OverScrollLinearLayoutManager
import com.o4x.musical.ui.fragments.mainactivity.AbsPopupFragment
import com.o4x.musical.util.ViewUtil
import kotlinx.android.synthetic.main.fragment_detail.*

open class AbsDetailFragment<T, A: RecyclerView.Adapter<*>> : AbsPopupFragment(R.layout.fragment_detail) {

    companion object {
        const val EXTRA = "extra"
        const val REQUEST_CODE_SELECT_IMAGE = 1400
    }

    var data: T? = null

    var adapter: A? = null
    var wrappedAdapter: RecyclerView.Adapter<*>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        data = requireArguments().getParcelable(EXTRA)

        setUpRecyclerView()
    }

    open fun setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(
            requireContext(),
            recycler_view,
            accentColor()
        )
        recycler_view.layoutManager = OverScrollLinearLayoutManager(requireContext())
        recycler_view.addAppbarListener()
    }

    fun checkIsEmpty() {
        empty.visibility =
            if (adapter!!.itemCount == 0) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        recycler_view.adapter = null
        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter)
            wrappedAdapter = null
        }
        adapter = null
        super.onDestroyView()
    }
}