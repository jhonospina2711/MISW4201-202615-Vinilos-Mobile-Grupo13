package com.example.vinilosmobile.feature.artistdetail

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
import com.example.vinilosmobile.coreui.models.ArtistRowUiModel
import com.example.vinilosmobile.coreui.models.DetailRowUiModel
import com.example.vinilosmobile.coreui.models.toCardUiModel
import com.example.vinilosmobile.coreui.molecules.AlbumCardAdapter
import com.example.vinilosmobile.coreui.molecules.ArtistRowAdapter
import com.example.vinilosmobile.coreui.molecules.DetailRowAdapter
import com.example.vinilosmobile.databinding.ActivityArtistDetailBinding
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.feature.albumdetail.AlbumDetailActivity
import com.example.vinilosmobile.feature.artistdetail.models.ArtistDetailUiState
import com.example.vinilosmobile.feature.shared.DateTextFormatter
import com.example.vinilosmobile.feature.shared.messageRes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArtistDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArtistDetailBinding
    private val viewModel: ArtistDetailViewModel by viewModels()
    private val albumAdapter = AlbumCardAdapter { id -> startActivity(AlbumDetailActivity.intent(this, id)) }
    private val awardAdapter = DetailRowAdapter()
    private val memberAdapter = ArtistRowAdapter { key -> startActivity(intent(this, key)) }
    private val artistKey: ArtistKey? by lazy {
        val id = intent.getLongExtra(EXTRA_ARTIST_ID, -1L)
        ArtistType.fromValue(intent.getStringExtra(EXTRA_ARTIST_TYPE))?.let { ArtistKey(id, it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityArtistDetailBinding.inflate(layoutInflater)
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
        binding.artistAlbumList.layoutManager = GridLayoutManager(this, 2)
        binding.artistAlbumList.adapter = albumAdapter
        binding.artistAlbumList.isNestedScrollingEnabled = false
        binding.awardList.layoutManager = LinearLayoutManager(this)
        binding.awardList.adapter = awardAdapter
        binding.awardList.isNestedScrollingEnabled = false
        binding.memberList.layoutManager = LinearLayoutManager(this)
        binding.memberList.adapter = memberAdapter
        binding.memberList.isNestedScrollingEnabled = false
        viewModel.state.observe(this, ::render)
        viewModel.load(artistKey)
    }

    private fun render(state: ArtistDetailUiState) {
        binding.detailLoading.isVisible = state == ArtistDetailUiState.Loading
        binding.detailContent.isVisible = state is ArtistDetailUiState.Content
        binding.detailMessage.isVisible = state == ArtistDetailUiState.NotFound || state is ArtistDetailUiState.Error
        when (state) {
            is ArtistDetailUiState.Content -> showArtist(state.artist)
            ArtistDetailUiState.NotFound -> binding.detailMessage.show(
                R.string.artist_not_found,
                actionRes = R.string.back,
            ) { finish() }
            is ArtistDetailUiState.Error -> binding.detailMessage.show(
                R.string.artist_detail_error,
                state.kind.messageRes(),
                R.string.retry,
            ) { viewModel.load(artistKey) }
            ArtistDetailUiState.Loading -> Unit
        }
    }

    private fun showArtist(artist: Artist) {
        val isBand = artist.key.type == ArtistType.BAND
        binding.artistAvatar.show(artist.imageUrl, isBand)
        binding.artistAvatar.contentDescription = getString(R.string.artist_image_description, artist.name)
        binding.artistName.text = artist.name
        binding.artistTypeChip.setText(if (isBand) R.string.artist_type_band else R.string.artist_type_musician)
        binding.artistDateLabel.setText(if (isBand) R.string.artist_creation_date else R.string.artist_birth_date)
        binding.artistDate.text = DateTextFormatter.format(artist.date) ?: getString(R.string.not_available)
        binding.artistAlbumCount.text = artist.albums.size.toString()
        binding.artistDescription.text = artist.description ?: getString(R.string.not_available)

        binding.albumsHeader.show(R.string.nav_albums, artist.albums.size)
        albumAdapter.submitList(artist.albums.map { it.toCardUiModel(this) })
        binding.albumsEmpty.isVisible = artist.albums.isEmpty()

        binding.awardsHeader.show(R.string.artist_awards, artist.awards.size)
        awardAdapter.submitList(artist.awards.map { award ->
            DetailRowUiModel(
                id = award.id,
                title = award.name ?: getString(R.string.artist_award_unknown),
                subtitle = DateTextFormatter.format(award.date),
            )
        })
        binding.awardsEmpty.isVisible = artist.awards.isEmpty()

        binding.membersHeader.isVisible = isBand
        binding.memberList.isVisible = isBand && artist.members.isNotEmpty()
        binding.membersEmpty.isVisible = isBand && artist.members.isEmpty()
        if (isBand) {
            binding.membersHeader.show(R.string.artist_members, artist.members.size)
            memberAdapter.submitList(artist.members.map { member ->
                ArtistRowUiModel(
                    ArtistKey(member.id, ArtistType.MUSICIAN),
                    member.name,
                    getString(R.string.artist_type_musician),
                    member.imageUrl,
                )
            })
        } else {
            memberAdapter.submitList(emptyList())
        }
    }

    companion object {
        private const val EXTRA_ARTIST_ID = "artist_id"
        private const val EXTRA_ARTIST_TYPE = "artist_type"

        fun intent(context: Context, key: ArtistKey): Intent =
            Intent(context, ArtistDetailActivity::class.java)
                .putExtra(EXTRA_ARTIST_ID, key.id)
                .putExtra(EXTRA_ARTIST_TYPE, key.type.value)
    }
}
