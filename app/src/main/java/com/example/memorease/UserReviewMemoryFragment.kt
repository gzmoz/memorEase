package com.example.memorease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.memorease.adapters.MemoryAdapter
import com.example.memorease.databinding.FragmentUserReviewMemoryBinding
import com.example.memorease.models.Memory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserReviewMemoryFragment : Fragment() {

    private var _binding: FragmentUserReviewMemoryBinding? = null
    private val binding get() = _binding!!

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val memories = mutableListOf<Memory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserReviewMemoryBinding.inflate(inflater, container, false)
        setupRecyclerView()
        fetchMemoriesFromFirestore()
        return binding.root
    }

    private fun setupRecyclerView() {
        val adapter = MemoryAdapter(requireContext(), memories)
        binding.recyclerViewMemories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMemories.adapter = adapter
    }

    private fun fetchMemoriesFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("memories")
            .get()
            .addOnSuccessListener { documents ->
                memories.clear()
                for (document in documents) {
                    val memory = document.toObject(Memory::class.java)
                    memories.add(memory)
                }
                binding.recyclerViewMemories.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load memories: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
