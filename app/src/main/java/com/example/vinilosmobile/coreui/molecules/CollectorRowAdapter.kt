package com.example.vinilosmobile.coreui.molecules

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.vinilosmobile.coreui.models.CollectorRowUiModel
import com.example.vinilosmobile.databinding.VinilosItemCollectorBinding

class CollectorRowAdapter(private val onClick: (Long) -> Unit) :
    ListAdapter<CollectorRowUiModel, CollectorRowAdapter.Holder>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder = Holder(
        VinilosItemCollectorBinding.inflate(LayoutInflater.from(parent.context), parent, false),
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position), onClick)

    class Holder(private val binding: VinilosItemCollectorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CollectorRowUiModel, onClick: (Long) -> Unit) {
            binding.collectorAvatar.show(null, false)
            binding.collectorName.text = item.name
            binding.collectorSubtitle.text = item.subtitle
            binding.root.contentDescription = "${item.name}, ${item.subtitle}"
            binding.root.setOnClickListener { onClick(item.id) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<CollectorRowUiModel>() {
            override fun areItemsTheSame(oldItem: CollectorRowUiModel, newItem: CollectorRowUiModel) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: CollectorRowUiModel, newItem: CollectorRowUiModel) = oldItem == newItem
        }
    }
}
