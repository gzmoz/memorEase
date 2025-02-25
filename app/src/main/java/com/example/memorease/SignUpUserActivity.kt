package com.example.memorease

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.cloudinary.android.MediaManager
import com.example.memorease.databinding.ActivitySignUpUserBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class SignUpUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpUserBinding
    private var selectedImageUri: Uri? = null
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cloudinary Yapılandırması (Her Activity içinde)
        val config = mapOf(
            "cloud_name" to "dqkgrjl0b",
            "api_key" to "713947242619374",
            "api_secret" to "3QkSfPPV2bC9cHOpO6iP_x4vUnc"
        )
        MediaManager.init(this, config)

        binding.imageView2.setOnClickListener {
            requestGalleryPermission()
        }

        binding.signUpButton.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val surname = binding.surnameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            // Zorunlu alanların doluluğunu kontrol et
            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Fotoğraf opsiyonel, boş bırakılabilir
            if (selectedImageUri == null) {
                val defaultImageUrl = "https://res.cloudinary.com/dqkgrjl0b/image/upload/v1680000000/memorease/default_avatar.png"
                registerUserWithFirebaseAuth(name, surname, email, password, defaultImageUrl)
            } else {
                uploadImageToCloudinary(selectedImageUri)
            }
        }
    }

    private fun requestGalleryPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            GALLERY_PERMISSION_CODE
        )
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.addprofilephoto)
                        .transform(CircleCrop())
                        .into(binding.imageView2)
                }
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GALLERY_PERMISSION_CODE) {
            openGallery()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

/*    private fun uploadImageToCloudinary(imageUri: Uri?) {
        imageUri?.let { uri ->
            CloudinaryService.uploadImage(uri, onSuccess = { imageUrl ->
                Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_LONG).show()
                val name = binding.nameInput.text.toString().trim()
                val surname = binding.surnameInput.text.toString().trim()
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()
                registerUserWithFirebaseAuth(name, surname, email, password, imageUrl)
            }, onError = { error ->
                Toast.makeText(this, "Cloudinary Installation Error: $error", Toast.LENGTH_LONG).show()
            })
        }
    }*/

    private fun uploadImageToCloudinary(imageUri: Uri?) {
        imageUri?.let { uri ->
            CloudinaryService.uploadImage(uri, "profile_photos", onSuccess = { imageUrl ->
                Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_LONG).show()
                val name = binding.nameInput.text.toString().trim()
                val surname = binding.surnameInput.text.toString().trim()
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()
                registerUserWithFirebaseAuth(name, surname, email, password, imageUrl)
            }, onError = { error ->
                Toast.makeText(this, "Cloudinary Installation Error: $error", Toast.LENGTH_LONG).show()
            })
        }
    }


    private fun registerUserWithFirebaseAuth(
        name: String,
        surname: String,
        email: String,
        password: String,
        profileImageUrl: String
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid ?: UUID.randomUUID().toString()
                saveUserToFirestore(userId, name, surname, email, profileImageUrl)
            } else {
                Toast.makeText(this, "Registration error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveUserToFirestore(
        userId: String,
        name: String,
        surname: String,
        email: String,
        profileImageUrl: String
    ) {
        val userProfile = UserProfile(
            name = name,
            surname = surname,
            email = email,
            profileImageUrl = profileImageUrl,
            uuid = userId
        )

        firestore.collection("users").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                Toast.makeText(this, "User registered successfully.", Toast.LENGTH_LONG).show()
                val intent = Intent(this, ActionOrientedActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        private const val GALLERY_PERMISSION_CODE = 1001
    }
}
