package com.example.vinilosmobile.feature.albumcreate

import androidx.annotation.StringRes
import com.example.vinilosmobile.R
import com.example.vinilosmobile.domain.models.AlbumFieldError

@StringRes
fun AlbumFieldError.messageRes(): Int = when (this) {
    AlbumFieldError.REQUIRED -> R.string.field_required
    AlbumFieldError.INVALID_URL -> R.string.field_invalid_url
    AlbumFieldError.INVALID_DATE -> R.string.field_invalid_date
    AlbumFieldError.INVALID_OPTION -> R.string.field_invalid_option
}
