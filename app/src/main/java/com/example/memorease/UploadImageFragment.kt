package com.example.memorease

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.example.memorease.databinding.FragmentUploadImageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class UploadImageFragment : Fragment() {

    private var _binding: FragmentUploadImageBinding? = null
    private val binding get() = _binding!!

    private var selectedImageUri: Uri? = null
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var uploadedBy: String = "Unknown"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadImageBinding.inflate(inflater, container, false)

        // Cloudinary Yapılandırması
        val config = mapOf(
            "cloud_name" to "dqkgrjl0b",
            "api_key" to "713947242619374",
            "api_secret" to "3QkSfPPV2bC9cHOpO6iP_x4vUnc"
        )
        MediaManager.init(requireContext(), config)

        // Kullanıcı Bilgilerini Yükle
        fetchUploaderName()

        binding.addImageButton.setOnClickListener {
            checkAndRequestGalleryPermission()
        }

        binding.uploadButton.setOnClickListener {
            val comment = binding.commentBox.text.toString().trim()

            if (selectedImageUri == null) {
                Toast.makeText(requireContext(), "Please select an image.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (comment.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadImageToCloudinary(selectedImageUri!!, comment)
        }

        return binding.root
    }

    /**
     * Kullanıcı Bilgisini Firebase Firestore'dan Al
     */
    private fun fetchUploaderName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Kullanıcı kendisi mi yoksa bir relative mi?
                    val name = document.getString("name") ?: "Unknown"
                    val surname = document.getString("surname") ?: ""
                    uploadedBy = "$name $surname" // Hasta ismi

                } else {
                    // Eğer kullanıcı bir relative ise
                    firestore.collectionGroup("relatives")
                        .whereEqualTo("email", FirebaseAuth.getInstance().currentUser?.email)
                        .get()
                        .addOnSuccessListener { relativeDocs ->
                            if (!relativeDocs.isEmpty) {
                                val relative = relativeDocs.documents[0]
                                uploadedBy = relative.getString("fullName") ?: "Unknown Relative"
                            }
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching user info: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun checkAndRequestGalleryPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            requestPermissions(arrayOf(permission), GALLERY_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GALLERY_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(requireContext(), "Permission denied. Please allow access to gallery.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(binding.addImageButton)
                }
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun uploadImageToCloudinary(imageUri: Uri, comment: String) {
        CloudinaryService.uploadImage(imageUri, "memories", onSuccess = { imageUrl ->
            saveMemoryToFirestore(imageUrl, comment)
        }, onError = { error ->
            Toast.makeText(requireContext(), "Cloudinary Error: $error", Toast.LENGTH_LONG).show()
        })
    }

    private fun saveMemoryToFirestore(imageUrl: String, comment: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val memoryId = UUID.randomUUID().toString()

        val memoryData = mapOf(
            "type" to "image",
            "url" to imageUrl,
            "uploadedBy" to uploadedBy, // Dinamik isim atanıyor
            "description" to comment,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(userId)
            .collection("memories").document(memoryId)
            .set(memoryData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Memory successfully uploaded!", Toast.LENGTH_LONG).show()
                // Kullanıcıyı HomeFragment'a yönlendir
                navigateToHomeFragment()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * HomeFragment'a yönlendirme fonksiyonu
     */
    private fun navigateToHomeFragment() {
        val homeFragment = HomeFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, homeFragment) // Ana container id'sini kullan
            .addToBackStack(null) // Geri butonunda doğru çalışması için backstack ekle
            .commit()
    }

    companion object {
        private const val GALLERY_PERMISSION_CODE = 1001
    }
}
