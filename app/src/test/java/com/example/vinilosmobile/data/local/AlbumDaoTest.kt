package com.example.vinilosmobile.data.local

import androidx.room.Room
import com.example.vinilosmobile.data.local.models.AlbumEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AlbumDaoTest {
    private lateinit var database: VinilosDatabase
    private lateinit var dao: AlbumDao

    @Before fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            VinilosDatabase::class.java,
        ).build()
        dao = database.albumDao()
    }

    @After fun tearDown() {
        database.close()
    }

    @Test fun catalogQueryExcludesDetailOnlyRowsAndKeepsPosition() = runBlocking {
        dao.insertAll(listOf(
            entity(3, 1, true),
            entity(9, Int.MAX_VALUE, false),
            entity(2, 0, true),
        ))

        assertEquals(listOf(2L, 3L), dao.getAll().map { it.id })
        assertEquals(9L, dao.getById(9)?.id)
    }

    @Test fun replaceAllClearsStaleAlbumsAndStoresNewCatalog() = runBlocking {
        dao.insertAll(listOf(entity(1, 0, true), entity(2, 1, true)))

        dao.replaceAll(listOf(entity(4, 0, true)))

        assertEquals(listOf(4L), dao.getAll().map { it.id })
        assertNull(dao.getById(1))
        assertNull(dao.getById(2))
    }

    @Test fun upsertUpdatesExistingAlbumByPrimaryKey() = runBlocking {
        dao.upsert(entity(1, 0, true))

        dao.upsert(entity(1, 0, true).copy(name = "Actualizado"))

        assertEquals("Actualizado", dao.getById(1)?.name)
        assertEquals(1, dao.getAll().size)
    }

    private fun entity(id: Long, position: Int, inCatalog: Boolean) = AlbumEntity(
        id = id,
        position = position,
        inCatalog = inCatalog,
        name = "Álbum $id",
        artistNamesJson = "[]",
        genre = null,
        releaseYear = null,
        coverUrl = null,
        recordLabel = null,
        description = null,
    )
}
