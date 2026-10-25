package com.example.vinilosmobile.feature.home.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.vinilosmobile.feature.home.albums.AlbumCatalogFragment
import com.example.vinilosmobile.feature.home.artists.ArtistsFragment
import com.example.vinilosmobile.feature.home.collectors.CollectorsFragment

class HomeSectionPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> AlbumCatalogFragment()
        1 -> ArtistsFragment()
        2 -> CollectorsFragment()
        else -> error("Unknown section: $position")
    }
}
