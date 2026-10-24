package com.example.vinilosmobile.data.remote

import com.example.vinilosmobile.data.remote.models.AlbumDto

import retrofit2.http.GET
import retrofit2.http.Path

interface AlbumApiService {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: Long): AlbumDto
}
