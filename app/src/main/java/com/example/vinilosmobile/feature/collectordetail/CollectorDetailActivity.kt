package com.example.vinilosmobile.feature.collectordetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vinilosmobile.R
import com.example.vinilosmobile.coreui.models.AlbumCardUiModel
import com.example.vinilosmobile.coreui.models.ArtistRowUiModel
import com.example.vinilosmobile.coreui.models.DetailRowUiModel
import com.example.vinilosmobile.coreui.molecules.AlbumCardAdapter
import com.example.vinilosmobile.coreui.molecules.ArtistRowAdapter
import com.example.vinilosmobile.coreui.molecules.DetailRowAdapter
import com.example.vinilosmobile.databinding.ActivityCollectorDetailBinding
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.CollectorAlbum
import com.example.vinilosmobile.domain.models.CollectorDetail
import com.example.vinilosmobile.feature.albumdetail.AlbumDetailActivity
import com.example.vinilosmobile.feature.artistdetail.ArtistDetailActivity
import com.example.vinilosmobile.feature.collectordetail.models.CollectorDetailUiState
import com.example.vinilosmobile.feature.shared.messageRes
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class CollectorDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCollectorDetailBinding
    private val viewModel: CollectorDetailViewModel by viewModels()
    private val favoritesAdapter = ArtistRowAdapter { key -> startActivity(ArtistDetailActivity.intent(this, key)) }
    private val albumsAdapter = AlbumCardAdapter { id -> startActivity(AlbumDetailActivity.intent(this, id)) }
    private val commentsAdapter = DetailRowAdapter()
    /**
     * Un solo formateador para toda la pantalla. Antes se creaba uno por cada
     * álbum de la colección; `NumberFormat.getCurrencyInstance` es una de las
     * fábricas más costosas de `java.text` y se llamaba en cada elemento.
     */
    private val currencyFormat: NumberFormat =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
    private val collectorId: Long by lazy { intent.getLongExtra(EXTRA_COLLECTOR_ID, -1L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCollectorDetailBinding.inflate(layoutInflater)
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
        binding.favoritesList.layoutManager = LinearLayoutManager(this)
        binding.favoritesList.adapter = favoritesAdapter
        binding.favoritesList.isNestedScrollingEnabled = false
        binding.collectionList.layoutManager = GridLayoutManager(this, 2)
        binding.collectionList.adapter = albumsAdapter
        binding.collectionList.isNestedScrollingEnabled = false
        binding.commentList.layoutManager = LinearLayoutManager(this)
        binding.commentList.adapter = commentsAdapter
        binding.commentList.isNestedScrollingEnabled = false
        viewModel.state.observe(this, ::render)
        viewModel.load(collectorId)
    }

    private fun render(state: CollectorDetailUiState) {
        binding.detailLoading.isVisible = state == CollectorDetailUiState.Loading
        binding.detailContent.isVisible = state is CollectorDetailUiState.Content
        binding.detailMessage.isVisible = state == CollectorDetailUiState.NotFound ||
            state is CollectorDetailUiState.Error || state == CollectorDetailUiState.Cancelled
        when (state) {
            is CollectorDetailUiState.Content -> showCollector(state.collector)
            CollectorDetailUiState.NotFound -> binding.detailMessage.show(
                R.string.collector_not_found, actionRes = R.string.back,
            ) { finish() }
            is CollectorDetailUiState.Error -> binding.detailMessage.show(
                R.string.collector_detail_error, state.kind.messageRes(), R.string.retry,
            ) { viewModel.load(collectorId) }
            CollectorDetailUiState.Cancelled -> binding.detailMessage.show(
                R.string.request_cancelled, actionRes = R.string.retry,
            ) { viewModel.load(collectorId) }
            CollectorDetailUiState.Loading -> Unit
        }
    }

    private fun showCollector(collector: CollectorDetail) {
        binding.collectorAvatar.show(null, false)
        binding.collectorAvatar.contentDescription = getString(R.string.collector_image_description, collector.name)
        binding.collectorName.text = collector.name
        binding.collectorPhone.text = collector.telephone ?: getString(R.string.not_available)
        binding.collectorEmail.text = collector.email ?: getString(R.string.not_available)

        binding.favoritesHeader.show(R.string.collector_favorites, collector.favorites.size)
        favoritesAdapter.submitList(collector.favorites.map { favorite ->
            ArtistRowUiModel(
                favorite.key, favorite.name,
                getString(if (favorite.key.type == ArtistType.BAND) R.string.artist_type_band else R.string.artist_type_musician),
                favorite.imageUrl,
            )
        })
        binding.favoritesEmpty.isVisible = collector.favorites.isEmpty()

        val albums = collector.albums
        binding.collectionHeader.show(R.string.collector_collection, albums?.size ?: 0)
        albumsAdapter.submitList(albums.orEmpty().map(::toAlbumCard))
        binding.collectionEmpty.isVisible = albums.isNullOrEmpty()
        binding.collectionEmpty.setText(
            if (albums == null) R.string.collector_albums_unavailable else R.string.collector_no_albums,
        )

        binding.commentsHeader.show(R.string.album_comments, collector.comments.size)
        commentsAdapter.submitList(collector.comments.map { comment ->
            DetailRowUiModel(
                comment.id,
                comment.rating?.let { getString(R.string.album_comment_rating, it) } ?: getString(R.string.album_comment),
                comment.description,
            )
        })
        binding.commentsEmpty.isVisible = collector.comments.isEmpty()
    }

    private fun toAlbumCard(item: CollectorAlbum): AlbumCardUiModel {
        val price = item.price?.let { currencyFormat.format(it) }
            ?: getString(R.string.not_available)
        val status = when (item.status) {
            "Active" -> getString(R.string.collector_album_active)
            "Inactive" -> getString(R.string.collector_album_inactive)
            else -> getString(R.string.not_available)
        }
        return AlbumCardUiModel(item.album.id, item.album.name, price, status, item.album.coverUrl)
    }

    companion object {
        private const val EXTRA_COLLECTOR_ID = "collector_id"

        fun intent(context: Context, id: Long): Intent =
            Intent(context, CollectorDetailActivity::class.java).putExtra(EXTRA_COLLECTOR_ID, id)
    }
}
