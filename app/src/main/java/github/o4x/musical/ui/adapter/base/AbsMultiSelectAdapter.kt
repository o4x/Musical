package github.o4x.musical.ui.adapter.base

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import github.o4x.musical.R
import github.o4x.musical.interfaces.CabCallback
import github.o4x.musical.interfaces.CabHolder
import java.util.*

abstract class AbsMultiSelectAdapter<VH : RecyclerView.ViewHolder?, I>(
    context: Context,
    private val cabHolder: CabHolder?,
    @MenuRes menuRes: Int
) : RecyclerView.Adapter<VH>(), CabCallback {

    private var cab: AttachedCab? = null
    private val checked: MutableList<I>
    private var menuRes: Int
    private val context: Context

    protected fun setMultiSelectMenuRes(@MenuRes menuRes: Int) {
        this.menuRes = menuRes
    }

    protected fun toggleChecked(position: Int): Boolean {
        if (cabHolder != null) {
            val identifier = getIdentifier(position) ?: return false
            if (!checked.remove(identifier)) checked.add(identifier)
            notifyItemChanged(position)
            updateCab()
            return true
        }
        return false
    }

    private fun checkAll() {
        if (cabHolder != null) {
            checked.clear()
            for (i in 0 until itemCount) {
                val identifier = getIdentifier(i)
                if (identifier != null) {
                    checked.add(identifier)
                }
            }
            notifyDataSetChanged()
            updateCab()
        }
    }

    private fun updateCab() {
        if (cabHolder != null) {
            if (cab == null || !cab.isActive()) {
                cab = cabHolder.openCab(menuRes, this)
            }
            val size = checked.size
            when {
                size <= 0 -> cab.destroy()
                size == 1 -> cab?.title(literal = getName(checked[0]))
                else -> cab?.title(literal = context.getString(R.string.x_selected, size))
            }
        }
    }

    private fun clearChecked() {
        checked.clear()
        notifyDataSetChanged()
    }

    protected fun isChecked(identifier: I): Boolean {
        return checked.contains(identifier)
    }

    protected val isInQuickSelectMode: Boolean
        get() = cab != null && cab.isActive()

    override fun onCreate(attachedCab: AttachedCab, menu: Menu) {}
    override fun onSelection(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.action_multi_select_adapter_check_all) {
            checkAll()
        } else {
            onMultipleItemAction(menuItem, ArrayList(checked))
            cab.destroy()
            clearChecked()
        }
        return true
    }

    override fun onDestroy(attachedCab: AttachedCab): Boolean {
        clearChecked()
        return true
    }

    protected open fun getName(`object`: I): String? {
        return `object`.toString()
    }

    protected abstract fun getIdentifier(position: Int): I?
    protected abstract fun onMultipleItemAction(menuItem: MenuItem, selection: MutableList<I>)

    init {
        checked = ArrayList()
        this.menuRes = menuRes
        this.context = context
    }
}