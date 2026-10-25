package com.example.vinilosmobile

import com.example.vinilosmobile.data.network.services.AlbumApiService
import com.example.vinilosmobile.data.network.services.ArtistApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object EspressoNetworkModule {
    @Provides @Singleton fun gson(): Gson = Gson()

    @Provides @Singleton fun client(fixtures: FixtureApiInterceptor): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(fixtures).build()

    @Provides @Singleton fun retrofit(client: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://vinilos.espresso.invalid/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides fun albums(retrofit: Retrofit): AlbumApiService = retrofit.create(AlbumApiService::class.java)
    @Provides fun artists(retrofit: Retrofit): ArtistApiService = retrofit.create(ArtistApiService::class.java)
}
