package com.example.memorease

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                GALLERY_PERMISSION_CODE
            )
        }
    }

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    val mimeType = contentResolver.getType(uri)
                    if (mimeType?.startsWith("image") == true) {
                        Glide.with(this)
                            .load(uri)
                            .placeholder(R.drawable.addprofilephoto)
                            .transform(CircleCrop())
                            .into(binding.imageView2)
                    } else {
                        Toast.makeText(this, "Please select a valid image file.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        selectImageLauncher.launch(intent)
    }

    private fun uploadImageToCloudinary(imageUri: Uri?) {
        imageUri?.let { uri ->
            CloudinaryService.uploadFile(uri, "profile_photos", "image", onSuccess = { imageUrl ->
                Toast.makeText(this, "Photo uploaded successfully!", Toast.LENGTH_LONG).show()
                val name = binding.nameInput.text.toString().trim()
                val surname = binding.surnameInput.text.toString().trim()
                val email = binding.emailInput.text.toString().trim()
                val password = binding.passwordInput.text.toString().trim()
                registerUserWithFirebaseAuth(name, surname, email, password, imageUrl)
            }, onError = { error ->
                Toast.makeText(this, "Cloudinary Upload Error: $error", Toast.LENGTH_LONG).show()
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
        val userProfile = mapOf(
            "name" to name,
            "surname" to surname,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "uuid" to userId,
            "score" to 0 // Kullanıcının başlangıç skoru 0 olarak kaydediliyor
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
