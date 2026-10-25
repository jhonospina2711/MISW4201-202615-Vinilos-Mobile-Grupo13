package com.example.vinilosmobile.feature.home.artists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.models.RequestFailure
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.usecase.GetArtistsUseCase
import com.example.vinilosmobile.feature.home.artists.models.ArtistCatalogUiState
import com.example.vinilosmobile.feature.home.artists.models.ArtistFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class ArtistCatalogViewModel @Inject constructor(
    private val getArtists: GetArtistsUseCase,
) : ViewModel() {
    private val _state = MutableLiveData<ArtistCatalogUiState>(ArtistCatalogUiState.Loading)
    val state: LiveData<ArtistCatalogUiState> = _state
    private val _filter = MutableLiveData(ArtistFilter.ALL)
    val filter: LiveData<ArtistFilter> = _filter
    private var allArtists: List<Artist>? = null
    private var activeRequest = false

    init { reload() }

    fun setFilter(value: ArtistFilter) {
        if (_filter.value == value) return
        _filter.value = value
        allArtists?.let(::showArtists)
    }

    fun reload() {
        if (activeRequest) return
        activeRequest = true
        viewModelScope.launch {
            try {
                getArtists().collect { result ->
                    when (result) {
                        DataResult.Loading -> _state.value = ArtistCatalogUiState.Loading
                        is DataResult.Success -> {
                            allArtists = result.value
                            showArtists(result.value)
                        }
                        is DataResult.Error -> _state.value = ArtistCatalogUiState.Error(
                            (result.reason as? DataError.Request)?.kind
                                ?: RequestFailureKind.UNKNOWN,
                        )
                        DataResult.Cancelled -> _state.value = ArtistCatalogUiState.Cancelled
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.value = ArtistCatalogUiState.Error(
                    (exception as? RequestFailure)?.kind ?: RequestFailureKind.UNKNOWN,
                )
            } finally {
                activeRequest = false
            }
        }
    }

    private fun showArtists(artists: List<Artist>) {
        val filtered = when (_filter.value) {
            ArtistFilter.MUSICIANS -> artists.filter { it.key.type == ArtistType.MUSICIAN }
            ArtistFilter.BANDS -> artists.filter { it.key.type == ArtistType.BAND }
            else -> artists
        }
        _state.value = if (filtered.isEmpty()) ArtistCatalogUiState.Empty
        else ArtistCatalogUiState.Content(filtered)
    }
}
