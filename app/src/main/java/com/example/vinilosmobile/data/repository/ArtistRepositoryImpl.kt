package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.network.ArtistServiceAdapter
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.di.IoDispatcher
import com.example.vinilosmobile.domain.models.Artist
import com.example.vinilosmobile.domain.models.ArtistKey
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.repository.ArtistNotFoundException
import com.example.vinilosmobile.domain.repository.ArtistRepository
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import retrofit2.HttpException

class ArtistRepositoryImpl @Inject constructor(
    private val adapter: ArtistServiceAdapter,
    private val errorMapper: NetworkErrorMapper,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ArtistRepository {
    override fun getArtists(): Flow<DataResult<List<Artist>>> = flow {
        emit(DataResult.Loading)
        val artists = try {
            adapter.getArtists()
        } catch (exception: CancellationException) {
            if (!currentCoroutineContext().isActive) throw exception
            emit(DataResult.Cancelled)
            return@flow
        } catch (exception: Exception) {
            emit(DataResult.Error(DataError.Request(errorMapper.map(exception).kind)))
            return@flow
        }
        emit(DataResult.Success(artists))
    }.flowOn(ioDispatcher)

    override fun getArtist(key: ArtistKey): Flow<DataResult<Artist>> = flow {
        emit(DataResult.Loading)
        val artist = try {
            adapter.getArtist(key)
        } catch (exception: CancellationException) {
            if (!currentCoroutineContext().isActive) throw exception
            emit(DataResult.Cancelled)
            return@flow
        } catch (exception: ArtistNotFoundException) {
            emit(DataResult.Error(DataError.NotFound))
            return@flow
        } catch (exception: HttpException) {
            val reason = if (exception.code() == 404) DataError.NotFound
            else DataError.Request(errorMapper.map(exception).kind)
            emit(DataResult.Error(reason))
            return@flow
        } catch (exception: Exception) {
            emit(DataResult.Error(DataError.Request(errorMapper.map(exception).kind)))
            return@flow
        }
        emit(DataResult.Success(artist))
    }.flowOn(ioDispatcher)
}
