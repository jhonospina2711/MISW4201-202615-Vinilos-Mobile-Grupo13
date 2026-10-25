package com.example.vinilosmobile.feature.albumdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.vinilosmobile.R
import com.example.vinilosmobile.coreui.models.DetailRowUiModel
import com.example.vinilosmobile.coreui.molecules.DetailRowAdapter
import com.example.vinilosmobile.databinding.ActivityAlbumDetailBinding
import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.feature.albumdetail.models.AlbumDetailUiState
import com.example.vinilosmobile.feature.shared.DateTextFormatter
import com.example.vinilosmobile.feature.shared.messageRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlbumDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlbumDetailBinding
    private val viewModel: AlbumDetailViewModel by viewModels()
    private val trackAdapter = DetailRowAdapter()
    private val commentAdapter = DetailRowAdapter()
    private val albumId: Long by lazy { intent.getLongExtra(EXTRA_ALBUM_ID, -1L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAlbumDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        binding.detailToolbar.setNavigationOnClickListener { finish() }
        binding.trackList.layoutManager = LinearLayoutManager(this)
        binding.trackList.adapter = trackAdapter
        binding.trackList.isNestedScrollingEnabled = false
        binding.commentList.layoutManager = LinearLayoutManager(this)
        binding.commentList.adapter = commentAdapter
        binding.commentList.isNestedScrollingEnabled = false
        binding.addTrackFab.setOnClickListener {
            Toast.makeText(this, R.string.add_track_coming_soon, Toast.LENGTH_SHORT).show()
        }
        viewModel.selectedRole.observe(this) { role ->
            binding.addTrackFab.isVisible = role == UserRole.COLLECTOR
        }
        viewModel.state.observe(this, ::render)
        viewModel.load(albumId)
    }

    private fun render(state: AlbumDetailUiState) {
        binding.detailLoading.isVisible = state == AlbumDetailUiState.Loading
        binding.detailContent.isVisible = state is AlbumDetailUiState.Content
        binding.detailMessage.isVisible = state == AlbumDetailUiState.NotFound ||
            state is AlbumDetailUiState.Error || state == AlbumDetailUiState.Cancelled
        when (state) {
            is AlbumDetailUiState.Content -> showAlbum(state.album)
            AlbumDetailUiState.NotFound -> binding.detailMessage.show(
                R.string.album_not_found,
                actionRes = R.string.back,
            ) { finish() }
            is AlbumDetailUiState.Error -> binding.detailMessage.show(
                R.string.album_detail_error,
                state.kind.messageRes(),
                R.string.retry,
            ) { viewModel.load(albumId) }
            AlbumDetailUiState.Cancelled -> binding.detailMessage.show(
                R.string.request_cancelled,
                actionRes = R.string.retry,
            ) { viewModel.load(albumId) }
            AlbumDetailUiState.Loading -> Unit
        }
    }

    private fun showAlbum(album: Album) {
        binding.detailName.text = album.name
        binding.detailArtist.text = album.artistNames.joinToString().ifBlank { getString(R.string.album_no_artist) }
        binding.genreChip.isVisible = !album.genre.isNullOrBlank()
        binding.genreChip.text = album.genre
        binding.labelChip.isVisible = !album.recordLabel.isNullOrBlank()
        binding.labelChip.text = album.recordLabel
        binding.detailDate.text = DateTextFormatter.format(album.releaseDate)
            ?: album.releaseYear ?: getString(R.string.not_available)
        binding.detailLabel.text = album.recordLabel ?: getString(R.string.not_available)
        binding.detailDescription.text = album.description ?: getString(R.string.not_available)
        binding.detailCover.contentDescription = getString(R.string.album_image_description, album.name)
        Glide.with(this).load(album.coverUrl).centerCrop()
            .placeholder(R.drawable.ic_album_placeholder)
            .error(R.drawable.ic_album_placeholder)
            .into(binding.detailCover)

        val tracks = album.tracks.orEmpty()
        binding.tracksHeader.show(R.string.album_tracks, tracks.size)
        trackAdapter.submitList(tracks.mapIndexed { index, track ->
            DetailRowUiModel(track.id, (index + 1).toString(), track.name, trailing = track.duration)
        })
        binding.tracksEmpty.isVisible = tracks.isEmpty()
        binding.tracksEmpty.setText(
            if (album.tracks == null) R.string.related_data_unavailable else R.string.album_no_tracks,
        )

        val comments = album.comments.orEmpty()
        binding.commentsHeader.show(R.string.album_comments, comments.size)
        commentAdapter.submitList(comments.map { comment ->
            DetailRowUiModel(
                id = comment.id,
                title = comment.rating?.let { getString(R.string.album_comment_rating, it) }
                    ?: getString(R.string.album_comment),
                subtitle = comment.description,
            )
        })
        binding.commentsEmpty.isVisible = comments.isEmpty()
        binding.commentsEmpty.setText(
            if (album.comments == null) R.string.related_data_unavailable else R.string.album_no_comments,
        )
    }

    companion object {
        private const val EXTRA_ALBUM_ID = "album_id"

        fun intent(context: Context, id: Long): Intent =
            Intent(context, AlbumDetailActivity::class.java).putExtra(EXTRA_ALBUM_ID, id)
    }
}
