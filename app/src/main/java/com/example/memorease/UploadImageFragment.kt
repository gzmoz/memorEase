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
    private var isRelative: Boolean = false
    private var userUUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Eğer RelativeUploadReviewFragment'tan gelindiyse bunu anlamak için argument kullanıyoruz
        isRelative = arguments?.getBoolean("isRelative", false) ?: false
    }

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

        // Kullanıcı Bilgilerini Yükle
        fetchUploaderInfo()

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
     * Kullanıcı veya Relative Bilgisini Firebase Firestore'dan Al
     */
    private fun fetchUploaderInfo() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val sharedPreferences = requireActivity().getSharedPreferences("APP_PREFS", Activity.MODE_PRIVATE)
        val storedUUID = sharedPreferences.getString("storedUUID", null)
        val relativeEmail = sharedPreferences.getString("relativeEmail", null)

        if (isRelative && storedUUID != null && relativeEmail != null) {
            // Eğer relative olarak giriş yapıldıysa relative'in bilgilerini çek
            firestore.collection("users").document(storedUUID)
                .collection("relatives")
                .whereEqualTo("email", relativeEmail)
                .get()
                .addOnSuccessListener { relativeDocs ->
                    if (!relativeDocs.isEmpty) {
                        val relative = relativeDocs.documents[0]
                        uploadedBy = relative.getString("fullName") ?: "Unknown Relative"
                        userUUID = storedUUID
                    }
                }
                .addOnFailureListener {
                    uploadedBy = "Unknown Relative"
                    userUUID = storedUUID
                }
        } else if (currentUser != null) {
            // Eğer normal kullanıcı olarak giriş yapıldıysa
            firestore.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "Unknown"
                        val surname = document.getString("surname") ?: ""
                        uploadedBy = "$name $surname"
                        userUUID = document.getString("uuid") ?: currentUser.uid
                    }
                }
                .addOnFailureListener {
                    uploadedBy = "Unknown"
                    userUUID = currentUser.uid
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
            uploadedBy = "Unknown"
        }
    }

    /**
     * Cloudinary'e Yükleme
     */
    private fun uploadImageToCloudinary(imageUri: Uri, comment: String) {
        CloudinaryService.uploadFile(imageUri, "memories", "image", onSuccess = { imageUrl ->
            saveMemoryToFirestore(imageUrl, comment)
        }, onError = { error ->
            Toast.makeText(requireContext(), "Cloudinary Error: $error", Toast.LENGTH_LONG).show()
        })
    }

    /**
     * Firestore'a Kayıt Etme
     */
    private fun saveMemoryToFirestore(imageUrl: String, comment: String) {
        if (userUUID.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "User UUID not found!", Toast.LENGTH_LONG).show()
            return
        }

        val memoryId = UUID.randomUUID().toString()
        val memoryData = mapOf(
            "type" to "image",
            "url" to imageUrl,
            "uploadedBy" to uploadedBy, // Artık relative veya kullanıcı ismi doğru şekilde kayıt edilecek
            "description" to comment,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(userUUID!!)
            .collection("memories").document(memoryId)
            .set(memoryData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Memory successfully uploaded!", Toast.LENGTH_LONG).show()
                navigateToHomeFragment()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * Ana Sayfaya Dönüş
     */
    private fun navigateToHomeFragment() {
        val uploadMemoryFragment = UploadMemoryFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, uploadMemoryFragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Galeri İzni Kontrolü
     */
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

    /**
     * Galeri Açma
     */
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

    companion object {
        private const val GALLERY_PERMISSION_CODE = 1001
    }
}
