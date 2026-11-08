package com.example.vinilosmobile.domain.models

enum class AlbumField {
    NAME,
    COVER_URL,
    RELEASE_DATE,
    GENRE,
    RECORD_LABEL,
    DESCRIPTION,
}

enum class AlbumFieldError {
    REQUIRED,
    INVALID_URL,
    INVALID_DATE,
    INVALID_OPTION,
}
