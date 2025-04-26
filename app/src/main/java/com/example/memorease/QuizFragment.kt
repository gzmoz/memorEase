package com.example.memorease

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.memorease.databinding.FragmentQuizBinding
//import com.example.memorease.ui.QuestionsFragment


class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.attemptQuizButton.setOnClickListener{
            val questionsFragment = QuestionsFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, questionsFragment)
                .addToBackStack(null)
                .commit()
        }
    }


}