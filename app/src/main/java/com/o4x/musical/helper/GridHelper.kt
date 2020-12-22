package com.o4x.musical.helper

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.o4x.musical.R

object GridHelper {

    fun gridLayoutManager(context: Context): GridLayoutManager {
        val size = context.homeGridSize()
        return object : GridLayoutManager(context, size) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.width = (width / size) - (lp.marginStart * 2 /* for left and right */)
                lp.height = (lp.width * 1.5).toInt()
                return super.checkLayoutParams(lp)
            }
        }
    }

    fun linearLayoutManager(context: Context): LinearLayoutManager {
        val size = context.homeGridSize()
        return object : LinearLayoutManager(context, HORIZONTAL, false) {
            override fun checkLayoutParams(lp: RecyclerView.LayoutParams): Boolean {
                lp.width = (width / size) - (lp.marginStart * 2 /* for left and right */)
                lp.height = (lp.width * 1.5).toInt()
                return super.checkLayoutParams(lp)
            }
        }
    }
}

fun Context.homeGridSize(): Int
        = resources.getInteger(R.integer.home_grid_columns)