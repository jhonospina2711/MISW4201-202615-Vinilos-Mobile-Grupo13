package com.example.vinilosmobile.feature.home.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vinilosmobile.R
import com.example.vinilosmobile.coreui.models.ArtistRowUiModel
import com.example.vinilosmobile.coreui.molecules.ArtistRowAdapter
import com.example.vinilosmobile.databinding.FragmentArtistCatalogBinding
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.feature.home.artists.models.ArtistCatalogUiState
import com.example.vinilosmobile.feature.home.artists.models.ArtistFilter
import com.example.vinilosmobile.feature.home.navigation.ArtistNavigator
import com.example.vinilosmobile.feature.shared.messageRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistsFragment : Fragment() {
    private var _binding: FragmentArtistCatalogBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: ArtistCatalogViewModel by viewModels()
    private val artistAdapter = ArtistRowAdapter { key ->
        (requireActivity() as ArtistNavigator).openArtist(key)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentArtistCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.artistList.layoutManager = LinearLayoutManager(requireContext())
        binding.artistList.adapter = artistAdapter
        binding.artistFilters.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.filterMusicians -> viewModel.setFilter(ArtistFilter.MUSICIANS)
                R.id.filterBands -> viewModel.setFilter(ArtistFilter.BANDS)
                else -> viewModel.setFilter(ArtistFilter.ALL)
            }
        }
        viewModel.filter.observe(viewLifecycleOwner) { filter ->
            val id = when (filter) {
                ArtistFilter.ALL -> R.id.filterAll
                ArtistFilter.MUSICIANS -> R.id.filterMusicians
                ArtistFilter.BANDS -> R.id.filterBands
            }
            if (binding.artistFilters.checkedChipId != id) binding.artistFilters.check(id)
        }
        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: ArtistCatalogUiState) {
        binding.artistLoading.isVisible = state == ArtistCatalogUiState.Loading
        binding.artistList.isVisible = state is ArtistCatalogUiState.Content
        binding.artistMessage.isVisible = state == ArtistCatalogUiState.Empty ||
            state is ArtistCatalogUiState.Error || state == ArtistCatalogUiState.Cancelled
        when (state) {
            is ArtistCatalogUiState.Content -> artistAdapter.submitList(state.artists.map(::toRow))
            ArtistCatalogUiState.Empty -> binding.artistMessage.show(R.string.artists_empty)
            is ArtistCatalogUiState.Error -> binding.artistMessage.show(
                R.string.artists_error,
                state.kind.messageRes(),
                R.string.retry,
            ) { viewModel.reload() }
            ArtistCatalogUiState.Cancelled -> binding.artistMessage.show(
                R.string.request_cancelled,
                actionRes = R.string.retry,
            ) { viewModel.reload() }
            ArtistCatalogUiState.Loading -> Unit
        }
    }

    private fun toRow(artist: Artist): ArtistRowUiModel {
        val type = getString(
            if (artist.key.type == ArtistType.BAND) R.string.artist_type_band else R.string.artist_type_musician,
        )
        val count = resources.getQuantityString(R.plurals.artist_album_count, artist.albums.size, artist.albums.size)
        return ArtistRowUiModel(
            artist.key,
            artist.name,
            getString(R.string.artist_row_subtitle, type, count),
            artist.imageUrl,
        )
    }

    override fun onDestroyView() {
        binding.artistList.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
