package com.example.vinilosmobile.di

import android.content.Context
import androidx.room.Room
import com.example.vinilosmobile.data.local.AlbumDao
import com.example.vinilosmobile.data.local.VinilosDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): VinilosDatabase =
        Room.databaseBuilder(context, VinilosDatabase::class.java, "vinilos.db").build()

    @Provides
    fun provideAlbumDao(database: VinilosDatabase): AlbumDao = database.albumDao()
}
