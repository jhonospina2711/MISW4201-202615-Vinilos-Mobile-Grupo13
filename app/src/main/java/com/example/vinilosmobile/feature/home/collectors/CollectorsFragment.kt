package com.example.vinilosmobile.feature.home.collectors

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vinilosmobile.R
import androidx.core.view.isVisible
import androidx.core.view.ViewCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vinilosmobile.coreui.models.CollectorRowUiModel
import com.example.vinilosmobile.coreui.molecules.CollectorRowAdapter
import com.example.vinilosmobile.databinding.FragmentCollectorCatalogBinding
import com.example.vinilosmobile.domain.models.CollectorSummary
import com.example.vinilosmobile.feature.home.collectors.models.CollectorCatalogUiState
import com.example.vinilosmobile.feature.home.navigation.CollectorNavigator
import com.example.vinilosmobile.feature.shared.messageRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CollectorsFragment : Fragment() {
    private var _binding: FragmentCollectorCatalogBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: CollectorCatalogViewModel by viewModels()
    private val collectorAdapter = CollectorRowAdapter { id ->
        (requireActivity() as CollectorNavigator).openCollector(id)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCollectorCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setAccessibilityHeading(binding.collectorTitle, true)
        binding.collectorTitle.contentDescription = getString(R.string.collectors_catalog_accessibility_title)
        binding.collectorList.layoutManager = LinearLayoutManager(requireContext())
        binding.collectorList.adapter = collectorAdapter
        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: CollectorCatalogUiState) {
        binding.collectorLoading.isVisible = state == CollectorCatalogUiState.Loading
        binding.collectorList.isVisible = state is CollectorCatalogUiState.Content
        binding.collectorMessage.isVisible = state == CollectorCatalogUiState.Empty ||
            state is CollectorCatalogUiState.Error || state == CollectorCatalogUiState.Cancelled
        when (state) {
            is CollectorCatalogUiState.Content -> collectorAdapter.submitList(state.collectors.map(::toRow))
            CollectorCatalogUiState.Empty -> binding.collectorMessage.show(R.string.collectors_empty)
            is CollectorCatalogUiState.Error -> binding.collectorMessage.show(
                R.string.collectors_error, state.kind.messageRes(), R.string.retry,
            ) { viewModel.reload() }
            CollectorCatalogUiState.Cancelled -> binding.collectorMessage.show(
                R.string.request_cancelled, actionRes = R.string.retry,
            ) { viewModel.reload() }
            CollectorCatalogUiState.Loading -> Unit
        }
    }

    private fun toRow(collector: CollectorSummary): CollectorRowUiModel {
        val albums = resources.getQuantityString(R.plurals.artist_album_count, collector.albumCount, collector.albumCount)
        val favorites = resources.getQuantityString(
            R.plurals.collector_favorite_count, collector.favoriteCount, collector.favoriteCount,
        )
        return CollectorRowUiModel(collector.id, collector.name, "$albums · $favorites")
    }

    override fun onDestroyView() {
        binding.collectorList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
