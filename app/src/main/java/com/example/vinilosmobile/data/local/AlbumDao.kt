package com.example.vinilosmobile.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.vinilosmobile.data.local.models.AlbumEntity

@Dao
abstract class AlbumDao {
    @Query("SELECT * FROM albums WHERE inCatalog = 1 ORDER BY position ASC")
    abstract suspend fun getAll(): List<AlbumEntity>

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    abstract suspend fun getById(id: Long): AlbumEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(album: AlbumEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    abstract suspend fun clear()

    @Transaction
    open suspend fun replaceAll(albums: List<AlbumEntity>) {
        clear()
        insertAll(albums)
    }
}
