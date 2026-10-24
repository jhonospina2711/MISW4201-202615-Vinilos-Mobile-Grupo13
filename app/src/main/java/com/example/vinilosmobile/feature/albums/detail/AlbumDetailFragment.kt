package com.example.vinilosmobile.feature.albums.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.vinilosmobile.R
import com.example.vinilosmobile.databinding.FragmentAlbumDetailBinding
import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.feature.albums.detail.models.AlbumDetailUiState
import com.example.vinilosmobile.feature.shared.messageRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumDetailFragment : Fragment() {
    private var _binding: FragmentAlbumDetailBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val albumId: Long get() = requireArguments().getLong(ARG_ID)
    private val viewModel: AlbumDetailViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAlbumDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.detailToolbar.setNavigationOnClickListener { requireActivity().onBackPressedDispatcher.onBackPressed() }
        viewModel.state.observe(viewLifecycleOwner, ::render)
        if (viewModel.state.value == AlbumDetailUiState.Loading) viewModel.load(albumId)
    }

    private fun render(state: AlbumDetailUiState) {
        binding.detailLoading.isVisible = state is AlbumDetailUiState.Loading
        binding.detailContent.isVisible = state is AlbumDetailUiState.Content
        binding.detailMessage.isVisible = state == AlbumDetailUiState.NotFound || state is AlbumDetailUiState.Error
        when (state) {
            is AlbumDetailUiState.Content -> showAlbum(state.album)
            AlbumDetailUiState.NotFound -> {
                binding.detailMessage.show(R.string.album_not_found, actionRes = R.string.back) {
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
            is AlbumDetailUiState.Error -> {
                binding.detailMessage.show(R.string.album_detail_error, state.kind.messageRes(), R.string.retry) {
                    viewModel.load(albumId)
                }
            }
            AlbumDetailUiState.Loading -> Unit
        }
    }

    private fun showAlbum(album: Album) {
        binding.detailName.text = album.name
        binding.detailArtist.text = album.artistNames.joinToString().ifBlank { getString(R.string.album_no_artist) }
        binding.detailMetadata.text = listOf(
            album.genre ?: getString(R.string.album_no_genre),
            album.releaseYear ?: getString(R.string.album_no_year),
        ).joinToString(" · ")
        binding.detailLabel.text = album.recordLabel.orEmpty()
        binding.detailDescription.text = album.description.orEmpty()
        Glide.with(this)
            .load(album.coverUrl)
            .centerCrop()
            .placeholder(R.drawable.ic_album_placeholder)
            .error(R.drawable.ic_album_placeholder)
            .into(binding.detailCover)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_ID = "album_id"
        fun newInstance(id: Long): AlbumDetailFragment = AlbumDetailFragment().apply {
            arguments = Bundle().apply { putLong(ARG_ID, id) }
        }
    }
}
