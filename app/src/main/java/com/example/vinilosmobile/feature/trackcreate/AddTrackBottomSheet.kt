package com.example.vinilosmobile.feature.trackcreate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.example.vinilosmobile.R
import com.example.vinilosmobile.databinding.VinilosSheetAddTrackBinding
import com.example.vinilosmobile.domain.models.RequestFailureKind
import com.example.vinilosmobile.domain.models.Track
import com.example.vinilosmobile.domain.models.TrackField
import com.example.vinilosmobile.domain.models.TrackFieldError
import com.example.vinilosmobile.feature.shared.messageRes
import com.example.vinilosmobile.feature.trackcreate.models.CreateTrackUiState
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.color.MaterialColors
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddTrackBottomSheet : BottomSheetDialogFragment() {
    private var _binding: VinilosSheetAddTrackBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: CreateTrackViewModel by viewModels()
    private val albumId get() = requireArguments().getLong(ARG_ALBUM_ID)
    private val albumName get() = requireArguments().getString(ARG_ALBUM_NAME).orEmpty()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = VinilosSheetAddTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ViewCompat.setAccessibilityHeading(binding.sheetTitle, true)
        binding.albumName.text = albumName
        binding.nameInput.doAfterTextChanged { binding.nameLayout.showError(null) }
        binding.durationInput.doAfterTextChanged { binding.durationLayout.showError(null) }
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.saveButton.setOnClickListener {
            viewModel.save(albumId, binding.nameInput.text.toString(), binding.durationInput.text.toString())
        }
        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: CreateTrackUiState) {
        val saving = state == CreateTrackUiState.Saving
        isCancelable = !saving
        dialog?.setCanceledOnTouchOutside(!saving)
        binding.nameLayout.isEnabled = !saving
        binding.durationLayout.isEnabled = !saving
        binding.saveButton.isEnabled = !saving
        binding.cancelButton.isEnabled = !saving
        binding.saveButton.setText(if (saving) R.string.saving_track else R.string.track_add_action)
        when (state) {
            CreateTrackUiState.Editing, CreateTrackUiState.Saving -> Unit
            is CreateTrackUiState.Invalid -> {
                binding.nameLayout.showError(state.errors[TrackField.NAME]?.message())
                binding.durationLayout.showError(state.errors[TrackField.DURATION]?.message())
                binding.formMessage.setText(R.string.create_track_check_fields)
                binding.formMessage.visibility = View.VISIBLE
                if (TrackField.NAME in state.errors) binding.nameInput.requestFocus()
                else binding.durationInput.requestFocus()
            }
            is CreateTrackUiState.Failed -> showFailure(state.kind)
            CreateTrackUiState.AlbumNotFound -> {
                binding.formMessage.setText(R.string.album_not_found)
                binding.formMessage.visibility = View.VISIBLE
            }
            is CreateTrackUiState.Saved -> {
                parentFragmentManager.setFragmentResult(RESULT_KEY, bundleOf(
                    RESULT_TRACK_ID to state.track.id,
                    RESULT_TRACK_NAME to state.track.name,
                    RESULT_TRACK_DURATION to state.track.duration,
                ))
                dismiss()
            }
        }
    }

    private fun showFailure(kind: RequestFailureKind) {
        binding.formMessage.text = getString(R.string.create_track_failed, getString(kind.messageRes()))
        binding.formMessage.setTextColor(MaterialColors.getColor(binding.formMessage, androidx.appcompat.R.attr.colorError))
        binding.formMessage.visibility = View.VISIBLE
    }

    private fun TrackFieldError.message(): String = getString(when (this) {
        TrackFieldError.REQUIRED -> R.string.field_required
        TrackFieldError.INVALID_DURATION -> R.string.track_duration_invalid
    })

    private fun TextInputLayout.showError(message: String?) {
        error = message
        isErrorEnabled = message != null
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "add_track_sheet"
        const val RESULT_KEY = "added_track"
        private const val ARG_ALBUM_ID = "album_id"
        private const val ARG_ALBUM_NAME = "album_name"
        private const val RESULT_TRACK_ID = "track_id"
        private const val RESULT_TRACK_NAME = "track_name"
        private const val RESULT_TRACK_DURATION = "track_duration"

        fun newInstance(albumId: Long, albumName: String): AddTrackBottomSheet = AddTrackBottomSheet().apply {
            arguments = bundleOf(ARG_ALBUM_ID to albumId, ARG_ALBUM_NAME to albumName)
        }

        fun trackFromResult(result: Bundle): Track? {
            val id = result.getLong(RESULT_TRACK_ID, -1)
            val name = result.getString(RESULT_TRACK_NAME).orEmpty()
            if (id <= 0 || name.isBlank()) return null
            return Track(id, name, result.getString(RESULT_TRACK_DURATION).orEmpty())
        }
    }
}
