package com.example.vinilosmobile.feature.albumcreate

import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.widget.doAfterTextChanged
import com.example.vinilosmobile.R
import com.example.vinilosmobile.databinding.ActivityCreateAlbumBinding
import com.example.vinilosmobile.domain.models.AlbumField
import com.example.vinilosmobile.domain.models.AlbumFieldError
import com.example.vinilosmobile.domain.models.AlbumOptions
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.feature.albumcreate.models.AlbumForm
import com.example.vinilosmobile.feature.albumcreate.models.CreateAlbumUiState
import com.example.vinilosmobile.feature.shared.DateTextFormatter
import com.example.vinilosmobile.feature.shared.messageRes
import com.google.android.material.color.MaterialColors
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateAlbumActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAlbumBinding
    private val viewModel: CreateAlbumViewModel by viewModels()
    private lateinit var fieldLayouts: Map<AlbumField, TextInputLayout>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCreateAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowInsetsControllerCompat(window, binding.root).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, R.anim.fade_out)
        }

        fieldLayouts = mapOf(
            AlbumField.NAME to binding.nameLayout,
            AlbumField.COVER_URL to binding.coverLayout,
            AlbumField.RELEASE_DATE to binding.releaseDateLayout,
            AlbumField.GENRE to binding.genreLayout,
            AlbumField.RECORD_LABEL to binding.recordLabelLayout,
            AlbumField.DESCRIPTION to binding.descriptionLayout,
        )
        fieldLayouts.values.forEach { layout ->
            layout.editText?.doAfterTextChanged { layout.showError(null) }
        }
        binding.genreInput.setSimpleItems(AlbumOptions.GENRES.toTypedArray())
        binding.recordLabelInput.setSimpleItems(AlbumOptions.RECORD_LABELS.toTypedArray())
        binding.releaseDateLayout.setEndIconOnClickListener { showDatePicker() }
        datePicker()?.addOnPositiveButtonClickListener(::onDatePicked)

        binding.createToolbar.setNavigationOnClickListener { finish() }
        binding.cancelButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { viewModel.save(readForm()) }
        viewModel.state.observe(this, ::render)
    }

    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, R.anim.fade_out)
        }
    }

    private fun readForm() = AlbumForm(
        name = binding.nameInput.text.toString(),
        coverUrl = binding.coverInput.text.toString(),
        releaseDate = binding.releaseDateInput.text.toString(),
        genre = binding.genreInput.text.toString(),
        recordLabel = binding.recordLabelInput.text.toString(),
        description = binding.descriptionInput.text.toString(),
    )

    private fun render(state: CreateAlbumUiState) {
        val saving = state == CreateAlbumUiState.Saving
        fieldLayouts.values.forEach { it.isEnabled = !saving }
        binding.cancelButton.isEnabled = !saving
        binding.saveButton.isEnabled = !saving
        binding.saveButton.setText(if (saving) R.string.saving_album else R.string.save_album)
        when (state) {
            CreateAlbumUiState.Editing -> showMessage(getString(R.string.create_album_required), isError = false)
            CreateAlbumUiState.Saving -> Unit
            is CreateAlbumUiState.Invalid -> showFieldErrors(state.errors)
            is CreateAlbumUiState.Failed -> showMessage(failureMessage(state.kind), isError = true)
            is CreateAlbumUiState.Saved -> {
                setResult(RESULT_OK, CreateAlbumContract.resultIntent(CreatedAlbum(state.albumId, state.albumName)))
                finish()
            }
        }
    }

    private fun showFieldErrors(errors: Map<AlbumField, AlbumFieldError>) {
        fieldLayouts.forEach { (field, layout) -> layout.showError(errors[field]?.let { getString(it.messageRes()) }) }
        showMessage(getString(R.string.create_album_check_fields), isError = true)
        AlbumField.entries.firstOrNull { it in errors }?.let { fieldLayouts.getValue(it).requestFocus() }
    }

    private fun failureMessage(kind: RequestFailureKind): String =
        if (kind == RequestFailureKind.UNKNOWN) getString(R.string.create_album_failed_unknown)
        else getString(R.string.create_album_failed, getString(kind.messageRes()))

    private fun showMessage(text: String, isError: Boolean) {
        val color = if (isError) androidx.appcompat.R.attr.colorError
        else com.google.android.material.R.attr.colorOnSurfaceVariant
        binding.formMessage.text = text
        binding.formMessage.setTextColor(MaterialColors.getColor(binding.formMessage, color))
    }

    private fun showDatePicker() {
        if (datePicker() != null) return
        val builder = MaterialDatePicker.Builder.datePicker().setTitleText(R.string.album_release_date)
        DateTextFormatter.utcMillisFromInput(binding.releaseDateInput.text.toString())?.let(builder::setSelection)
        builder.build().apply {
            addOnPositiveButtonClickListener(::onDatePicked)
            show(supportFragmentManager, DATE_PICKER_TAG)
        }
    }

    // After a rotation the picker comes back on its own but without the listener.
    @Suppress("UNCHECKED_CAST")
    private fun datePicker(): MaterialDatePicker<Long>? =
        supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<Long>

    private fun onDatePicked(utcMillis: Long) {
        binding.releaseDateInput.setText(DateTextFormatter.inputFromUtcMillis(utcMillis))
    }

    private fun TextInputLayout.showError(message: String?) {
        error = message
        isErrorEnabled = message != null
    }

    private companion object {
        const val DATE_PICKER_TAG = "release_date_picker"
    }
}
