package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.local.AlbumDao
import com.example.vinilosmobile.data.mappers.toDomain
import com.example.vinilosmobile.data.mappers.toEntity
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.data.network.services.AlbumApiService
import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.domain.models.DataError
import com.example.vinilosmobile.domain.models.DataResult
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import retrofit2.HttpException

class AlbumRepositoryImpl @Inject constructor(
    private val service: AlbumApiService,
    private val dao: AlbumDao,
    private val gson: Gson,
    private val errorMapper: NetworkErrorMapper,
) : AlbumRepository {
    override fun getAlbums(): Flow<DataResult<AlbumCatalog>> = flow {
        emit(DataResult.Loading)
        val cached = try {
            dao.getAll().map { it.toDomain(gson) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            emptyList()
        }
        if (cached.isNotEmpty()) emit(DataResult.Success(AlbumCatalog(cached, AlbumSource.CACHE)))

        val albums = try {
            service.getAlbums().mapNotNull { it.toDomain() }
        } catch (exception: CancellationException) {
            if (!currentCoroutineContext().isActive) throw exception
            emit(DataResult.Cancelled)
            return@flow
        } catch (exception: Exception) {
            emit(DataResult.Error(DataError.Request(errorMapper.map(exception).kind)))
            return@flow
        }
        try {
            dao.replaceAll(albums.mapIndexed { index, album -> album.toEntity(index, true, gson) })
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // Fresh remote data is still usable when the local cache cannot be updated.
        }
        emit(DataResult.Success(AlbumCatalog(albums, AlbumSource.REMOTE)))
    }

    override fun getAlbum(id: Long): Flow<DataResult<Album>> = flow {
        emit(DataResult.Loading)
        val cached = try {
            dao.getById(id)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }
        val cachedAlbum = try {
            cached?.toDomain(gson)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }
        cachedAlbum?.let { emit(DataResult.Success(it)) }

        val album = try {
            service.getAlbum(id).toDomain() ?: throw AlbumNotFoundException()
        } catch (exception: CancellationException) {
            if (!currentCoroutineContext().isActive) throw exception
            emit(DataResult.Cancelled)
            return@flow
        } catch (exception: AlbumNotFoundException) {
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
        try {
            dao.upsert(album.toEntity(cached?.position ?: Int.MAX_VALUE, cached?.inCatalog ?: false, gson))
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // A failed cache write must not hide a successfully fetched album.
        }
        emit(DataResult.Success(album))
    }
}
