package com.example.vinilosmobile.coreui.molecules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vinilosmobile.coreui.models.ArtistRowUiModel
import com.example.vinilosmobile.databinding.VinilosItemArtistBinding
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType

class ArtistRowAdapter(private val onClick: (ArtistKey) -> Unit) :
    ListAdapter<ArtistRowUiModel, ArtistRowAdapter.Holder>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        VinilosItemArtistBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position), onClick)

    class Holder(private val binding: VinilosItemArtistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ArtistRowUiModel, onClick: (ArtistKey) -> Unit) {
            binding.artistAvatar.show(item.imageUrl, item.key.type == ArtistType.BAND)
            binding.artistName.text = item.title
            binding.artistSubtitle.text = item.subtitle
            binding.root.contentDescription = listOf(item.title, item.subtitle).joinToString(", ")
            binding.root.setOnClickListener { onClick(item.key) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<ArtistRowUiModel>() {
            override fun areItemsTheSame(oldItem: ArtistRowUiModel, newItem: ArtistRowUiModel) =
                oldItem.key == newItem.key

            override fun areContentsTheSame(oldItem: ArtistRowUiModel, newItem: ArtistRowUiModel) =
                oldItem == newItem
        }
    }
}
