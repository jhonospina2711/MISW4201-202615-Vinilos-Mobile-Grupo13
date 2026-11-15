package com.example.vinilosmobile.data.network

import com.example.vinilosmobile.data.mappers.toDomain
import com.example.vinilosmobile.data.network.services.ArtistApiService
import com.example.vinilosmobile.di.IoDispatcher
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.ArtistType
import com.example.vinilosmobile.domain.repository.ArtistNotFoundException
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/** Adapts the backend's separate musician and band APIs to one artist contract. */
class ArtistServiceAdapter @Inject constructor(
    private val service: ArtistApiService,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    suspend fun getArtists(): List<Artist> = withContext(ioDispatcher) {
        coroutineScope {
            val musicians = async { service.getMusicians() }
            val bands = async { service.getBands() }
            (musicians.await().mapNotNull { it.toDomain(ArtistType.MUSICIAN) } +
                bands.await().mapNotNull { it.toDomain(ArtistType.BAND) }).sortedBy { it.key.id }
        }
    }

    suspend fun getArtist(key: ArtistKey): Artist = withContext(ioDispatcher) {
        val dto = when (key.type) {
            ArtistType.MUSICIAN -> service.getMusician(key.id)
            ArtistType.BAND -> service.getBand(key.id)
        }
        val awards = if (dto.performerPrizes.isNullOrEmpty()) emptyMap() else try {
            service.getPerformerPrizes().mapNotNull { award ->
                award.id?.let { it to award }
            }.toMap()
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            emptyMap()
        }
        dto.toDomain(key.type, awards) ?: throw ArtistNotFoundException()
    }
}
