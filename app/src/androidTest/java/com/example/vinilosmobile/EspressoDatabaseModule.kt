package com.example.vinilosmobile

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
object EspressoDatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): VinilosDatabase =
        Room.inMemoryDatabaseBuilder(context, VinilosDatabase::class.java).build()

    @Provides fun albumDao(database: VinilosDatabase): AlbumDao = database.albumDao()
}
