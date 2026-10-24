package com.example.vinilosmobile.feature.entry

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.example.vinilosmobile.R
import com.example.vinilosmobile.databinding.ActivityEntryBinding
import com.example.vinilosmobile.domain.models.UserRole
import com.example.vinilosmobile.feature.entry.models.EntryUiState
import com.example.vinilosmobile.feature.home.MainActivity
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEntryBinding
    private val viewModel: EntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        binding.visitorOption.bind(
            R.string.entry_visitor,
            R.string.entry_visitor_description,
            R.drawable.ic_artist,
        ) { viewModel.choose(UserRole.VISITOR) }
        binding.collectorOption.bind(
            R.string.entry_collector,
            R.string.entry_collector_description,
            R.drawable.ic_collectors,
        ) { viewModel.choose(UserRole.COLLECTOR) }
        binding.readError.show(
            R.string.entry_read_error_title,
            R.string.entry_read_error_body,
            R.string.retry,
        ) { viewModel.readSavedRole() }

        viewModel.state.observe(this) { state -> render(state) }
        viewModel.initialize(intent.getBooleanExtra(EXTRA_ALLOW_ROLE_CHANGE, false))
    }

    private fun render(state: EntryUiState) {
        binding.entryProgress.isVisible = state == EntryUiState.Loading || state == EntryUiState.Saving
        binding.roleChoices.isVisible = state is EntryUiState.Choosing ||
            state == EntryUiState.Saving || state is EntryUiState.SaveError
        binding.readError.isVisible = state == EntryUiState.ReadError
        val enabled = state is EntryUiState.Choosing || state is EntryUiState.SaveError
        binding.visitorOption.isEnabled = enabled
        binding.collectorOption.isEnabled = enabled
        val currentRole = when (state) {
            is EntryUiState.Choosing -> state.currentRole
            is EntryUiState.SaveError -> state.currentRole
            else -> null
        }
        binding.visitorOption.setCurrentRole(currentRole == UserRole.VISITOR)
        binding.collectorOption.setCurrentRole(currentRole == UserRole.COLLECTOR)

        when (state) {
            is EntryUiState.SaveError -> Snackbar.make(
                binding.root,
                R.string.entry_save_error,
                Snackbar.LENGTH_LONG,
            ).show()
            EntryUiState.NavigateHome -> {
                startActivity(
                    Intent(this, MainActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP,
                    ),
                )
                finish()
            }
            else -> Unit
        }
    }

    companion object {
        const val EXTRA_ALLOW_ROLE_CHANGE = "com.example.vinilosmobile.ALLOW_ROLE_CHANGE"
    }
}
