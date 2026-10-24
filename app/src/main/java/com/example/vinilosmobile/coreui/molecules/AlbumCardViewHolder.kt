package com.example.vinilosmobile.coreui.molecules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vinilosmobile.R
import com.example.vinilosmobile.coreui.models.AlbumCardUiModel
import com.example.vinilosmobile.databinding.VinilosItemAlbumBinding

class AlbumCardViewHolder private constructor(
    private val binding: VinilosItemAlbumBinding,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(album: AlbumCardUiModel, position: Int, onClick: (Long) -> Unit) {
        val context = binding.root.context
        val placeholderColors = intArrayOf(
            R.color.vinilos_cover_one,
            R.color.vinilos_cover_two,
            R.color.vinilos_cover_three,
            R.color.vinilos_cover_four,
        )
        binding.albumCoverCard.setCardBackgroundColor(
            ContextCompat.getColor(context, placeholderColors[position % placeholderColors.size]),
        )
        binding.albumTitle.text = album.title
        binding.albumArtist.text = album.artist
        binding.albumMetadata.text = album.metadata
        binding.root.contentDescription = listOf(album.title, album.artist, album.metadata).joinToString(", ")
        binding.root.setOnClickListener { onClick(album.id) }

        Glide.with(binding.root).clear(binding.albumCover)
        binding.albumCover.setImageResource(R.drawable.ic_album_placeholder)
        if (!album.coverUrl.isNullOrBlank()) {
            Glide.with(binding.root)
                .load(album.coverUrl)
                .centerCrop()
                .placeholder(R.drawable.ic_album_placeholder)
                .error(R.drawable.ic_album_placeholder)
                .into(binding.albumCover)
        }
    }

    companion object {
        fun create(parent: ViewGroup): AlbumCardViewHolder = AlbumCardViewHolder(
            VinilosItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        )
    }
}
