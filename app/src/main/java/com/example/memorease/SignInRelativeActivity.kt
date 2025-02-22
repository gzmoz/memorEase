package com.example.memorease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.memorease.databinding.ActivitySignInRelativeBinding
import com.google.firebase.firestore.FirebaseFirestore

class SignInRelativeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInRelativeBinding
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInRelativeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val enteredUUID = binding.uuidDisplay.text.toString().trim()

            if (email.isEmpty() || enteredUUID.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            validateRelativeSignIn(email, enteredUUID)
        }
    }

    private fun validateRelativeSignIn(email: String, enteredUUID: String) {
        // Önce kullanıcı UUID'ye göre ana kullanıcıyı bul
        firestore.collection("users")
            .whereEqualTo("uuid", enteredUUID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "Invalid UUID. Please check and try again.", Toast.LENGTH_LONG).show()
                } else {
                    val userId = querySnapshot.documents[0].id
                    validateRelativeEmail(userId, email)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun validateRelativeEmail(userId: String, email: String) {
        // UUID'ye bağlı relatives koleksiyonunda e-mail doğrulaması yap
        firestore.collection("users").document(userId)
            .collection("relatives")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "Invalid email or no matching relative found.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Relative successfully signed in!", Toast.LENGTH_LONG).show()
                    navigateToRelativeMainScreen()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun navigateToRelativeMainScreen() {
        val intent = Intent(this, RelativeMainScreen::class.java)
        startActivity(intent)
        finish()
    }
}
