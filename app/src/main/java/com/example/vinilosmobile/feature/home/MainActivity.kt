package com.example.vinilosmobile.feature.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.vinilosmobile.R
import com.example.vinilosmobile.databinding.ActivityMainBinding
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.feature.albumdetail.AlbumDetailActivity
import com.example.vinilosmobile.feature.entry.EntryActivity
import com.example.vinilosmobile.feature.home.adapters.HomeSectionPagerAdapter
import com.example.vinilosmobile.feature.home.navigation.AlbumNavigator
import com.example.vinilosmobile.feature.home.navigation.ArtistNavigator
import com.example.vinilosmobile.feature.artistdetail.ArtistDetailActivity
import com.example.vinilosmobile.domain.models.ArtistKey
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AlbumNavigator, ArtistNavigator {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        viewModel.selectedRole.observe(this) { role ->
            if (role == null) {
                startActivity(Intent(this, EntryActivity::class.java))
                finish()
            } else {
                binding.addAlbumFab.isVisible = role == UserRole.COLLECTOR
            }
        }
        binding.changeRoleButton.setOnClickListener {
            startActivity(
                Intent(this, EntryActivity::class.java)
                    .putExtra(EntryActivity.EXTRA_ALLOW_ROLE_CHANGE, true),
            )
        }
        binding.addAlbumFab.setOnClickListener {
            Toast.makeText(this, R.string.add_album_coming_soon, Toast.LENGTH_SHORT).show()
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.sectionPager.adapter = HomeSectionPagerAdapter(this)
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

    }

    override fun openAlbum(id: Long) {
        startActivity(AlbumDetailActivity.intent(this, id))
    }

    override fun openArtist(key: ArtistKey) {
        startActivity(ArtistDetailActivity.intent(this, key))
    }
}
