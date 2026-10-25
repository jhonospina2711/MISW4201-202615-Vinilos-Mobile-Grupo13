package com.example.vinilosmobile.feature.home.navigation

import com.example.vinilosmobile.domain.models.ArtistKey

interface ArtistNavigator {
    fun openArtist(key: ArtistKey)
}
