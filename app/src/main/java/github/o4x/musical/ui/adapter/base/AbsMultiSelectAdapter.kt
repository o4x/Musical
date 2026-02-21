package github.o4x.musical.ui.adapter.base

import android.content.Context
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView

abstract class AbsMultiSelectAdapter<VH : RecyclerView.ViewHolder?, I>(
    context: Context,
    @MenuRes menuRes: Int
) : RecyclerView.Adapter<VH>() {


    private var menuRes: Int
    private val context: Context

    protected fun setMultiSelectMenuRes(@MenuRes menuRes: Int) {
        this.menuRes = menuRes
    }

    protected open fun getName(`object`: I): String? {
        return `object`.toString()
    }

    protected abstract fun getIdentifier(position: Int): I?
    protected abstract fun onMultipleItemAction(menuItem: MenuItem, selection: MutableList<I>)

    init {
        this.menuRes = menuRes
        this.context = context
    }
}