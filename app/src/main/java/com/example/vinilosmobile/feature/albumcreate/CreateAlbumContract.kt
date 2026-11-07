package com.example.vinilosmobile.feature.albumcreate

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

data class CreatedAlbum(val id: Long, val name: String)

class CreateAlbumContract : ActivityResultContract<Unit, CreatedAlbum?>() {
    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(context, CreateAlbumActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): CreatedAlbum? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null
        val id = intent.getLongExtra(EXTRA_ALBUM_ID, -1L)
        if (id < 0) return null
        return CreatedAlbum(id, intent.getStringExtra(EXTRA_ALBUM_NAME).orEmpty())
    }

    companion object {
        fun resultIntent(album: CreatedAlbum): Intent = Intent()
            .putExtra(EXTRA_ALBUM_ID, album.id)
            .putExtra(EXTRA_ALBUM_NAME, album.name)

        private const val EXTRA_ALBUM_ID = "created_album_id"
        private const val EXTRA_ALBUM_NAME = "created_album_name"
    }
}
