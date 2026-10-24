package com.example.vinilosmobile.data.repository

import com.example.vinilosmobile.data.local.AlbumDao
import com.example.vinilosmobile.data.mappers.toDomain
import com.example.vinilosmobile.data.mappers.toEntity
import com.example.vinilosmobile.data.remote.AlbumApiService
import com.example.vinilosmobile.domain.models.Album
import com.example.vinilosmobile.domain.models.AlbumCatalog
import com.example.vinilosmobile.domain.models.AlbumSource
import com.example.vinilosmobile.domain.repository.AlbumNotFoundException
import com.example.vinilosmobile.domain.repository.AlbumRepository
import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException

class AlbumRepositoryImpl @Inject constructor(
    private val service: AlbumApiService,
    private val dao: AlbumDao,
    private val gson: Gson,
) : AlbumRepository {
    override suspend fun getAlbums(): AlbumCatalog = try {
        val albums = service.getAlbums().mapNotNull { it.toDomain() }
        dao.replaceAll(albums.mapIndexed { index, album -> album.toEntity(index, true, gson) })
        AlbumCatalog(albums, AlbumSource.REMOTE)
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        val cached = dao.getAll().map { it.toDomain(gson) }
        if (cached.isEmpty()) throw exception
        AlbumCatalog(cached, AlbumSource.CACHE)
    }

    override suspend fun getAlbum(id: Long): Album = try {
        val album = service.getAlbum(id).toDomain() ?: throw AlbumNotFoundException()
        val cached = dao.getById(id)
        dao.upsert(album.toEntity(cached?.position ?: Int.MAX_VALUE, cached?.inCatalog ?: false, gson))
        album
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: AlbumNotFoundException) {
        throw exception
    } catch (exception: HttpException) {
        if (exception.code() == 404) throw AlbumNotFoundException()
        dao.getById(id)?.toDomain(gson) ?: throw exception
    } catch (exception: Exception) {
        dao.getById(id)?.toDomain(gson) ?: throw exception
    }
}
