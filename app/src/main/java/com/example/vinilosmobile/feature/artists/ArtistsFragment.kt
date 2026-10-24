package com.example.vinilosmobile.feature.artists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.vinilosmobile.R
import com.example.vinilosmobile.databinding.FragmentPendingSectionBinding

class ArtistsFragment : Fragment() {
    private var _binding: FragmentPendingSectionBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPendingSectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.sectionTitle.setText(R.string.nav_artists)
        binding.sectionMessage.setText(R.string.artists_pending)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
