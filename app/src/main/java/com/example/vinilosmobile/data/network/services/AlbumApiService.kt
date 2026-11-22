package com.example.vinilosmobile.data.network.services

import com.example.vinilosmobile.data.network.models.AlbumDto
import com.example.vinilosmobile.data.network.models.NewAlbumDto
import com.example.vinilosmobile.data.network.models.NewTrackDto
import com.example.vinilosmobile.data.network.models.TrackDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AlbumApiService {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: Long): AlbumDto

    @POST("albums")
    suspend fun createAlbum(@Body album: NewAlbumDto): AlbumDto

    @POST("albums/{id}/tracks")
    suspend fun addTrack(@Path("id") albumId: Long, @Body track: NewTrackDto): TrackDto
}
