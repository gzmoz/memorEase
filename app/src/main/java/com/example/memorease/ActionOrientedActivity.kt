package com.example.memorease

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.memorease.databinding.ActivityActionOrientedBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ActionOrientedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActionOrientedBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityActionOrientedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Kullanıcı UUID'sini Firestore'dan getir ve göster
        fetchUserUUID()

        binding.startButton.setOnClickListener() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Copy buttona tıklayınca UUID'yi panoya kopyala
        binding.copyIcon.setOnClickListener {
            val uuidText = binding.uuidDisplay.text.toString()
            if (uuidText.isNotEmpty() && uuidText != "UUID not found" && uuidText != "No User ID") {
                copyToClipboard(uuidText)
            } else {
                Toast.makeText(this, "UUID is not available to copy.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchUserUUID() {
        val userId = auth.currentUser?.uid

        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val uuid = document.getString("uuid") ?: "UUID not found"
                        binding.uuidDisplay.text = uuid
                    } else {
                        binding.uuidDisplay.text = "User data not found in Firestore."
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error fetching UUID: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.uuidDisplay.text = "Error fetching UUID"
                }
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_LONG).show()
            binding.uuidDisplay.text = "No User ID"
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("UUID", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "UUID copied to clipboard!", Toast.LENGTH_SHORT).show()
    }
}
