package com.example.vinilosmobile.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.vinilosmobile.data.local.models.AlbumEntity

@Database(entities = [AlbumEntity::class], version = 1, exportSchema = true)
abstract class VinilosDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
}
