package com.example.memorease

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.memorease.databinding.FragmentRelativeUploadReviewMemoryBinding
import com.example.memorease.databinding.FragmentUploadMemoryBinding


class RelativeUploadReviewMemoryFragment : Fragment() {


    private var _binding: FragmentRelativeUploadReviewMemoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRelativeUploadReviewMemoryBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.relativeUploadMemoryButton.setOnClickListener {
            val uploadMemoryFragment = UploadMemoryFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, uploadMemoryFragment)
                .addToBackStack(null)
                .commit()
        }

    }


}