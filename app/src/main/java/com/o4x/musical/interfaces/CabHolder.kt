package com.o4x.musical.interfaces

import android.view.Menu
import android.view.MenuItem
import com.afollestad.materialcab.attached.AttachedCab

interface CabHolder {
    fun openCab(menuRes: Int, callback: CabCallback): AttachedCab
}

interface CabCallback {
    fun onCreate(attachedCab: AttachedCab, menu: Menu)
    fun onSelection(menuItem: MenuItem): Boolean
    fun onDestroy(attachedCab: AttachedCab): Boolean
}