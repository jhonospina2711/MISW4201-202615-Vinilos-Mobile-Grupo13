package com.example.vinilosmobile.domain.models

enum class ArtistType(val value: String) {
    MUSICIAN("musician"),
    BAND("band");

    companion object {
        fun fromValue(value: String?): ArtistType? = entries.firstOrNull { it.value == value }
    }
}
