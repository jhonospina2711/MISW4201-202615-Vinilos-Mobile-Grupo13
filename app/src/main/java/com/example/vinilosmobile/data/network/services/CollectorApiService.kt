package com.example.vinilosmobile.data.network.services

import com.example.vinilosmobile.data.network.models.CollectorAlbumDto
import com.example.vinilosmobile.data.network.models.CollectorDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CollectorApiService {
    @GET("collectors")
    suspend fun getCollectors(): List<CollectorDto>

    @GET("collectors/{id}")
    suspend fun getCollector(@Path("id") id: Long): CollectorDto

    @GET("collectors/{id}/albums")
    suspend fun getCollectorAlbums(@Path("id") id: Long): List<CollectorAlbumDto>
}
