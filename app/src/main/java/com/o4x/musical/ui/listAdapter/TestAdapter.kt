package com.o4x.musical.ui.listAdapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SectionIndexer
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import com.o4x.musical.R
import com.o4x.musical.imageloader.glide.loader.GlideLoader.Companion.with
import com.o4x.musical.model.Song

class TestAdapter(
    val activity: AppCompatActivity,
    var dataSet: MutableList<Song>,
    @LayoutRes var itemLayoutRes: Int
) : BaseAdapter(), SectionIndexer {

    fun swapData(dataSet: List<Song>) {
        this.dataSet = dataSet.toMutableList()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return dataSet.size
    }

    override fun getItem(position: Int): Song {
        return dataSet[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        if (convertView == null) {
            convertView = activity.layoutInflater.inflate(itemLayoutRes, parent, false)
        }
        with(activity).load(getItem(position)).into(
            convertView?.findViewById(R.id.image) as ImageView
        )
        return convertView
    }

    override fun getSections(): Array<Any> {
        return dataSet.map {
            it.title[0]
        }.toTypedArray()
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return sectionIndex
    }

    override fun getSectionForPosition(position: Int): Int {
        return position
    }
}