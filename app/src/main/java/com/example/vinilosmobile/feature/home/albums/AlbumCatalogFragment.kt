package com.example.vinilosmobile.feature.home.albums

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vinilosmobile.R
import com.example.vinilosmobile.coreui.molecules.AlbumCardAdapter
import com.example.vinilosmobile.coreui.models.AlbumCardUiModel
import com.example.vinilosmobile.databinding.FragmentAlbumCatalogBinding
import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.feature.home.albums.models.AlbumCatalogUiState
import com.example.vinilosmobile.feature.home.navigation.AlbumNavigator
import com.example.vinilosmobile.feature.shared.messageRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumCatalogFragment : Fragment() {
    private var _binding: FragmentAlbumCatalogBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: AlbumCatalogViewModel by viewModels()
    private val albumAdapter = AlbumCardAdapter { id -> (requireActivity() as AlbumNavigator).openAlbum(id) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.albumGrid.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.albumGrid.adapter = albumAdapter
        binding.cachedRetry.setOnClickListener { viewModel.reload() }
        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: AlbumCatalogUiState) {
        binding.catalogLoading.isVisible = state is AlbumCatalogUiState.Loading
        binding.albumGrid.isVisible = state is AlbumCatalogUiState.Content
        binding.albumCount.isVisible = state is AlbumCatalogUiState.Content
        binding.cachedNotice.isVisible = state is AlbumCatalogUiState.Content && state.source == AlbumSource.CACHE
        binding.catalogMessage.isVisible = state is AlbumCatalogUiState.Empty || state is AlbumCatalogUiState.Error
        when (state) {
            is AlbumCatalogUiState.Content -> {
                binding.albumCount.text = getString(R.string.album_count, state.albums.size)
                albumAdapter.submitList(state.albums.map(::toCardUiModel))
            }
            AlbumCatalogUiState.Empty -> {
                binding.catalogMessage.show(R.string.catalog_empty)
            }
            is AlbumCatalogUiState.Error -> {
                binding.catalogMessage.show(
                    R.string.catalog_error_title,
                    state.kind.messageRes(),
                    R.string.retry,
                ) { viewModel.reload() }
            }
            AlbumCatalogUiState.Loading -> Unit
        }
    }

    private fun toCardUiModel(album: Album): AlbumCardUiModel = AlbumCardUiModel(
        id = album.id,
        title = album.name,
        artist = album.artistNames.joinToString().ifBlank { getString(R.string.album_no_artist) },
        metadata = listOf(
            album.genre ?: getString(R.string.album_no_genre),
            album.releaseYear ?: getString(R.string.album_no_year),
        ).joinToString(" · "),
        coverUrl = album.coverUrl,
    )

    override fun onDestroyView() {
        binding.albumGrid.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
