package com.example.vinilosmobile.data.network.services

import com.example.vinilosmobile.data.network.models.AlbumDto

import retrofit2.http.GET
import retrofit2.http.Path

interface AlbumApiService {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: Long): AlbumDto
}
