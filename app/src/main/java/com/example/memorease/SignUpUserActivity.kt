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

class SignUpUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpUserBinding
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cloudinary yapılandırması
        val config = mapOf(
            "cloud_name" to "dqkgrjl0b",
            "api_key" to "713947242619374",
            "api_secret" to "3QkSfPPV2bC9cHOpO6iP_x4vUnc"
        )
        MediaManager.init(this, config)

        // Fotoğraf seçme butonu
        binding.imageView2.setOnClickListener {
            requestGalleryPermission()
        }

        // Kayıt butonu
        binding.signUpButton.setOnClickListener {
            selectedImageUri?.let { uri ->
                uploadImageToCloudinary(uri)
            } ?: Toast.makeText(this, "Lütfen bir fotoğraf seçin!", Toast.LENGTH_SHORT).show()
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
                    // Glide ile yuvarlak şekilde görseli yükle
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.addprofilephoto) // Varsayılan resim
                        .transform(CircleCrop()) // Yuvarlak yapmak için CircleCrop kullan
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

    private fun uploadImageToCloudinary(imageUri: Uri) {
        CloudinaryService.uploadImage(imageUri, onSuccess = { imageUrl ->
            Toast.makeText(this, "Fotoğraf yüklendi: $imageUrl", Toast.LENGTH_LONG).show()
            val intent = Intent(this, ActionOrientedActivity::class.java)
            startActivity(intent)
        }, onError = { error ->
            Toast.makeText(this, "Yükleme hatası: $error", Toast.LENGTH_LONG).show()
        })
    }

    companion object {
        private const val GALLERY_PERMISSION_CODE = 1001
    }
}
