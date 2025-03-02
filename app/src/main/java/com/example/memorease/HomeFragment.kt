package com.example.memorease

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
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
        setupButtonListeners()
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
                        val uuid = document.getString("uuid") ?: userId // UUID alanı yoksa UID göster

                        binding.username.text = "$name $surname"
                        binding.uuidDisplay.text = uuid // UUID alanını güncelle

                        Glide.with(this)
                            .load(profileImageUrl)
                            .placeholder(R.drawable.sample_avatar)
                            .error(R.drawable.sample_avatar)
                            .transform(CircleCrop())
                            .into(binding.profileImage)

                        // UUID'yi kopyalama işlemi için butona listener ekle
                        binding.copyIcon.setOnClickListener {
                            copyUuidToClipboard(uuid)
                        }

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
    }

    /**
     * UUID'yi panoya kopyalama fonksiyonu
     */
    private fun copyUuidToClipboard(uuid: String) {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("UUID", uuid)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(requireContext(), "UUID copied to clipboard!", Toast.LENGTH_SHORT).show()
    }

    private fun setupButtonListeners() {
        binding.buttonUpload.setOnClickListener {
            val uploadFragment = UploadMemoryFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, uploadFragment)
                .addToBackStack(null)
                .commit()
        }

        binding.buttonReview.setOnClickListener {
            val userReviewFragment = UserReviewMemoryFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, userReviewFragment)
                .addToBackStack(null)
                .commit()
        }

        // Kullanıcı Çıkış Yapma Butonu
        binding.logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    /**
     * Kullanıcıyı Firebase'den Çıkış Yaptır ve Giriş Ekranına Yönlendir
     */
    private fun logoutUser() {
        auth.signOut() // Firebase'den çıkış yap

        Toast.makeText(requireContext(), "Successfully logged out!", Toast.LENGTH_SHORT).show()

        // Giriş ekranına yönlendir
        val intent = Intent(requireContext(), SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Önceki aktiviteleri temizle
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
