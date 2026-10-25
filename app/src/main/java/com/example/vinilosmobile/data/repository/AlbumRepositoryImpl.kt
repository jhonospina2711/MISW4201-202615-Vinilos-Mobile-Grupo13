package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.local.AlbumDao
import com.example.vinilosmobile.data.mappers.toDomain
import com.example.vinilosmobile.data.mappers.toEntity
import com.example.vinilosmobile.data.network.errors.NetworkErrorMapper
import com.example.vinilosmobile.data.network.services.AlbumApiService
import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException

class AlbumRepositoryImpl @Inject constructor(
    private val service: AlbumApiService,
    private val dao: AlbumDao,
    private val gson: Gson,
    private val errorMapper: NetworkErrorMapper,
) : AlbumRepository {
    override fun getAlbums(): Flow<AlbumCatalog> = flow {
        val cached = try {
            dao.getAll().map { it.toDomain(gson) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            emptyList()
        }
        if (cached.isNotEmpty()) emit(AlbumCatalog(cached, AlbumSource.CACHE))

        val albums = try {
            service.getAlbums().mapNotNull { it.toDomain() }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            if (cached.isEmpty()) throw errorMapper.map(exception)
            null
        }
        if (albums != null) {
            try {
                dao.replaceAll(albums.mapIndexed { index, album -> album.toEntity(index, true, gson) })
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // Fresh remote data is still usable when the local cache cannot be updated.
            }
            emit(AlbumCatalog(albums, AlbumSource.REMOTE))
        }
    }

    override fun getAlbum(id: Long): Flow<Album> = flow {
        val cached = try {
            dao.getById(id)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            null
        }
        cached?.toDomain(gson)?.let { emit(it) }

        val album = try {
            service.getAlbum(id).toDomain() ?: throw AlbumNotFoundException()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: AlbumNotFoundException) {
            throw exception
        } catch (exception: HttpException) {
            if (exception.code() == 404) throw AlbumNotFoundException()
            if (cached == null) throw errorMapper.map(exception)
            null
        } catch (exception: Exception) {
            if (cached == null) throw errorMapper.map(exception)
            null
        }
        if (album != null) {
            try {
                dao.upsert(album.toEntity(cached?.position ?: Int.MAX_VALUE, cached?.inCatalog ?: false, gson))
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                // A failed cache write must not hide a successfully fetched album.
            }
            emit(album)
        }
    }
}
