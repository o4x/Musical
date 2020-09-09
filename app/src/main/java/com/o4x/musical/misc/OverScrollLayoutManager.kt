package com.o4x.musical.misc

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.isRecyclerScrollable(): Boolean {
    return canScrollVertically(0) // 1 = down; -1 = up; 0 = up or down
}

abstract class VerticalScrollListener {
    abstract fun onScroll(dy: Int)
}

class OverScrollLinearLayoutManager : LinearLayoutManager {
    constructor(context: Context) : super(context)
    constructor(
        context: Context,
        @RecyclerView.Orientation orientation: Int,
        reverseLayout: Boolean,
    ) : super(context, orientation, reverseLayout)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?,
    ): Int {
        verticalScrollListener?.onScroll(dy)
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    fun setOnVerticalScrollListener(verticalScrollListener: VerticalScrollListener) {
        this.verticalScrollListener = verticalScrollListener
    }

    private var verticalScrollListener: VerticalScrollListener? = null
}

class OverScrollGridLayoutManager: GridLayoutManager {
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    constructor(context: Context?, spanCount: Int) :super(context, spanCount)

    constructor(
        context: Context?,
        spanCount: Int,
        orientation: Int,
        reverseLayout: Boolean,
    ) : super(context, spanCount, orientation, reverseLayout)


    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?,
    ): Int {
        verticalScrollListener?.onScroll(dy)
        return super.scrollVerticallyBy(dy, recycler, state)
    }

    fun setOnVerticalScrollListener(verticalScrollListener: VerticalScrollListener) {
        this.verticalScrollListener = verticalScrollListener
    }

    private var verticalScrollListener: VerticalScrollListener? = null
}