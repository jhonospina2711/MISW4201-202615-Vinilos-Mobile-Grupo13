package com.example.vinilosmobile.coreui.molecules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vinilosmobile.coreui.models.DetailRowUiModel
import com.example.vinilosmobile.databinding.VinilosItemDetailRowBinding

class DetailRowAdapter : ListAdapter<DetailRowUiModel, DetailRowAdapter.Holder>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        VinilosItemDetailRowBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    class Holder(private val binding: VinilosItemDetailRowBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DetailRowUiModel) {
            binding.rowLeading.isVisible = item.leading != null
            binding.rowLeading.text = item.leading
            binding.rowTitle.text = item.title
            binding.rowSubtitle.isVisible = !item.subtitle.isNullOrBlank()
            binding.rowSubtitle.text = item.subtitle
            binding.rowTrailing.isVisible = !item.trailing.isNullOrBlank()
            binding.rowTrailing.text = item.trailing
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<DetailRowUiModel>() {
            override fun areItemsTheSame(oldItem: DetailRowUiModel, newItem: DetailRowUiModel) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: DetailRowUiModel, newItem: DetailRowUiModel) =
                oldItem == newItem
        }
    }
}
