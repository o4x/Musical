package com.o4x.musical.ui.fragments.mainactivity.library.pager

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.o4x.appthemehelper.extensions.accentColor
import com.o4x.musical.R
import com.o4x.musical.util.ViewUtil
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView
import kotlinx.android.synthetic.main.fragment_library_recycler_view.*

abstract class AbsLibraryPagerRecyclerViewFragment<A : RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager?> :
    AbsLibraryPagerFragment(R.layout.fragment_library_recycler_view) {

    protected var adapter: A? = null
        private set
    protected var layoutManager: LM? = null
        private set

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLayoutManager()
        initAdapter()
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        if (recycler_view is FastScrollRecyclerView) {
            ViewUtil.setUpFastScrollRecyclerViewColor(serviceActivity,
                recycler_view,
                accentColor())
        }
        recycler_view!!.layoutManager = layoutManager
        recycler_view!!.adapter = adapter
        libraryFragment.setAppbarListener(recycler_view)

    }

    protected fun invalidateLayoutManager() {
        initLayoutManager()
        recycler_view!!.layoutManager = layoutManager
    }

    protected fun invalidateAdapter() {
        initAdapter()
        checkIsEmpty()
        recycler_view!!.adapter = adapter
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
        if (empty != null) {
            empty!!.setText(emptyMessage)
            empty!!.visibility =
                if (adapter == null || adapter!!.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    @get:StringRes
    protected open val emptyMessage: Int
        get() = R.string.empty

    val recyclerView: RecyclerView?
        get() = recycler_view

    protected abstract fun createLayoutManager(): LM
    protected abstract fun createAdapter(): A
}