package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.network.ArtistServiceAdapter
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.repository.ArtistNotFoundException
import com.example.vinilosmobile.domain.repository.ArtistRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class ArtistRepositoryImpl @Inject constructor(
    private val adapter: ArtistServiceAdapter,
    private val errorMapper: NetworkErrorMapper,
) : ArtistRepository {
    override suspend fun getArtists(): List<Artist> = try {
        adapter.getArtists()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        throw errorMapper.map(exception)
    }

    override suspend fun getArtist(key: ArtistKey): Artist = try {
        adapter.getArtist(key)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: ArtistNotFoundException) {
        throw exception
    } catch (exception: HttpException) {
        if (exception.code() == 404) throw ArtistNotFoundException()
        throw errorMapper.map(exception)
    } catch (exception: Exception) {
        throw errorMapper.map(exception)
    }
}
