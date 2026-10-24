package com.example.vinilosmobile

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.vinilosmobile.databinding.ActivityMainBinding
import com.example.vinilosmobile.feature.albums.catalog.AlbumCatalogFragment
import com.example.vinilosmobile.feature.albums.detail.AlbumDetailFragment
import com.example.vinilosmobile.feature.artists.ArtistsFragment
import com.example.vinilosmobile.feature.collectors.CollectorsFragment
import kotlin.math.abs
import dagger.hilt.android.AndroidEntryPoint

interface AlbumNavigator {
    fun openAlbum(id: Long)
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AlbumNavigator {

    private lateinit var binding: ActivityMainBinding
    private lateinit var detailBackCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        detailBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = closeAlbumDetail()
        }
        onBackPressedDispatcher.addCallback(this, detailBackCallback)

        binding.sectionPager.adapter = SectionPagerAdapter(this)
        binding.sectionPager.offscreenPageLimit = 2
        binding.sectionPager.setPageTransformer { page, position ->
            page.alpha = 1f - 0.15f * abs(position).coerceAtMost(1f)
        }
        binding.sectionPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val itemId = when (position) {
                    0 -> R.id.nav_albums
                    1 -> R.id.nav_artists
                    else -> R.id.nav_collectors
                }
                binding.bottomNavigation.menu.findItem(itemId).isChecked = true
            }
        })
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val page = when (item.itemId) {
                R.id.nav_albums -> 0
                R.id.nav_artists -> 1
                R.id.nav_collectors -> 2
                else -> return@setOnItemSelectedListener false
            }
            binding.sectionPager.setCurrentItem(page, true)
            true
        }

        val detailIsOpen = supportFragmentManager.findFragmentByTag(DETAIL_TAG) != null
        binding.tabsContent.isVisible = !detailIsOpen
        binding.detailContainer.isVisible = detailIsOpen
        detailBackCallback.isEnabled = detailIsOpen
    }

    override fun openAlbum(id: Long) {
        binding.tabsContent.visibility = View.GONE
        binding.detailContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.detailContainer, AlbumDetailFragment.newInstance(id), DETAIL_TAG)
            .commitNow()
        detailBackCallback.isEnabled = true
    }

    private fun closeAlbumDetail() {
        supportFragmentManager.findFragmentByTag(DETAIL_TAG)?.let { fragment ->
            supportFragmentManager.beginTransaction().remove(fragment).commitNow()
        }
        binding.detailContainer.visibility = View.GONE
        binding.tabsContent.visibility = View.VISIBLE
        detailBackCallback.isEnabled = false
    }

    private class SectionPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> AlbumCatalogFragment()
            1 -> ArtistsFragment()
            2 -> CollectorsFragment()
            else -> error("Unknown section: $position")
        }
    }

    private companion object {
        const val DETAIL_TAG = "album_detail"
    }
}
