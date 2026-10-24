package com.example.vinilosmobile.feature.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.example.vinilosmobile.R
import com.example.vinilosmobile.databinding.ActivityMainBinding
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.feature.albumdetail.AlbumDetailFragment
import com.example.vinilosmobile.feature.entry.EntryActivity
import com.example.vinilosmobile.feature.home.adapters.HomeSectionPagerAdapter
import com.example.vinilosmobile.feature.home.navigation.AlbumNavigator
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), AlbumNavigator {

    private lateinit var binding: ActivityMainBinding
    private lateinit var detailBackCallback: OnBackPressedCallback
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
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

        detailBackCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = closeAlbumDetail()
        }
        onBackPressedDispatcher.addCallback(this, detailBackCallback)

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

    private companion object {
        const val DETAIL_TAG = "album_detail"
    }
}
