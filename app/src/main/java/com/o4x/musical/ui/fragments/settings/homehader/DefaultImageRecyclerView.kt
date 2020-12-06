package com.o4x.musical.ui.fragments.settings.homehader

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.o4x.musical.R
import com.o4x.musical.databinding.ItemImageBinding
import com.o4x.musical.imageloader.glide.module.GlideApp
import com.o4x.musical.prefs.HomeHeaderPref

val defaultImages = arrayOf(
    R.drawable.gram_0
)

class DefaultImageRecyclerView : RecyclerView.Adapter<DefaultImageRecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemImageBinding
            .inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        GlideApp.with(holder.binding.root.context)
            .load(defaultImages[position])
            .into(holder.binding.image)

        holder.binding.image.setOnClickListener {
            HomeHeaderPref.defaultImageIndex = position
        }
    }

    override fun getItemCount(): Int = defaultImages.size


    class ViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}