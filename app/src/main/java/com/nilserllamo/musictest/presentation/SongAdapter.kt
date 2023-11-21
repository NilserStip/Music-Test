package com.nilserllamo.musictest.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nilserllamo.musictest.R
import com.nilserllamo.musictest.databinding.ItemSongBinding
import com.nilserllamo.musictest.presentation.model.Song

class SongAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var data = ArrayList<Song>()
    private var selected = -1

    class ViewHolder(val binding: ItemSongBinding) :
        RecyclerView.ViewHolder(binding.root)

    interface OnItemClickListener {
        fun onItemClick(song: Song)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        return ViewHolder(
            ItemSongBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
            )
        )
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = data[position]

        holder.binding.apply {
            root.setBackgroundColor(Color.WHITE)
            Glide.with(context).load(getDrawable(song.name)).centerCrop()
                .placeholder(R.drawable.loading).into(imageSong)
            textviewTitleSong.text = song.title

            if (selected == position)
                root.setBackgroundColor(Color.LTGRAY)

            root.setOnClickListener {
                if (selected == position) {
                    selected = -1
                    root.setBackgroundColor(Color.WHITE)
                } else {
                    selected = holder.adapterPosition
                    notifyDataSetChanged()
                }

                listener.onItemClick(song)
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun getDrawable(name: String): Int {
        val res: Resources = context.resources
        return res.getIdentifier(name, "drawable", context.packageName)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(array: ArrayList<Song>) {
        data = ArrayList(array)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSelected(position: Int) {
        selected = position
        notifyDataSetChanged()
    }

    override fun getItemCount() = data.size

}