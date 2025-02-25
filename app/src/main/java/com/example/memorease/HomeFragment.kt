package com.example.memorease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.memorease.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        fetchUserData()
        return binding.root
    }

    private fun fetchUserData() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: ""
                        val surname = document.getString("surname") ?: ""
                        val profileImageUrl = document.getString("profileImageUrl") ?: ""

                        // Kullanıcı adını ve soyadını birleştir
                        binding.username.text = "$name $surname"

                        // Glide ile profil resmini yükle
                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.sample_avatar) // Yüklenirken gösterilecek resim
                            .error(R.drawable.sample_avatar) // Hata durumunda gösterilecek resim
                            .transform(CircleCrop()) // Yuvarlak yapmak için
                            .into(binding.profileImage)
                    } else {
                        Toast.makeText(requireContext(), "User data not found in Firestore.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching user data: ${e.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_LONG).show()
        }


        binding.buttonUpload.setOnClickListener(){
            val uploadFragment = UploadMemoryFragment()

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, uploadFragment)
                .addToBackStack(null) // Geri tuşuna basıldığında önceki fragmente dönmek için
                .commit()

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
