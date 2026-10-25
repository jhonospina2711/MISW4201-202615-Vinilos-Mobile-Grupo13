package com.example.vinilosmobile.data.network.services

import com.example.vinilosmobile.data.network.models.ArtistAwardDto
import com.example.vinilosmobile.data.network.models.ArtistDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ArtistApiService {
    @GET("musicians")
    suspend fun getMusicians(): List<ArtistDto>

    @GET("bands")
    suspend fun getBands(): List<ArtistDto>

    @GET("musicians/{id}")
    suspend fun getMusician(@Path("id") id: Long): ArtistDto

    @GET("bands/{id}")
    suspend fun getBand(@Path("id") id: Long): ArtistDto

    @GET("performerprizes")
    suspend fun getPerformerPrizes(): List<ArtistAwardDto>
}
