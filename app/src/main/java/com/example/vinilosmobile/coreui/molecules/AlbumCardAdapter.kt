package com.example.vinilosmobile.coreui.molecules

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.vinilosmobile.coreui.models.AlbumCardUiModel

class AlbumCardAdapter(private val onAlbumClick: (Long) -> Unit) :
    ListAdapter<AlbumCardUiModel, AlbumCardViewHolder>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumCardViewHolder =
        AlbumCardViewHolder.create(parent)

    override fun onBindViewHolder(holder: AlbumCardViewHolder, position: Int) {
        holder.bind(getItem(position), position, onAlbumClick)
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<AlbumCardUiModel>() {
            override fun areItemsTheSame(oldItem: AlbumCardUiModel, newItem: AlbumCardUiModel): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: AlbumCardUiModel, newItem: AlbumCardUiModel): Boolean =
                oldItem == newItem
        }
    }
}
