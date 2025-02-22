package com.example.memorease

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.memorease.databinding.ActivitySignUpRelativeBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class SignUpRelativeActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpRelativeBinding
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpRelativeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signUpButton.setOnClickListener {
            val fullName = binding.fullNameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val enteredUUID = binding.uuidDisplay2.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || enteredUUID.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            validateUUIDAndSaveRelative(fullName, email, enteredUUID)
        }
    }

    private fun validateUUIDAndSaveRelative(fullName: String, email: String, enteredUUID: String) {
        firestore.collection("users")
            .whereEqualTo("uuid", enteredUUID)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    Toast.makeText(this, "Invalid UUID. Please check and try again.", Toast.LENGTH_LONG).show()
                } else {
                    for (document in querySnapshot.documents) {
                        val userId = document.id
                        saveRelativeToUser(userId, fullName, email)
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveRelativeToUser(userId: String, fullName: String, email: String) {
        val relativeId = UUID.randomUUID().toString()
        val relativeData = mapOf(
            "fullName" to fullName,
            "email" to email,
            "relativeId" to relativeId
        )

        firestore.collection("users").document(userId)
            .collection("relatives").document(relativeId)
            .set(relativeData)
            .addOnSuccessListener {
                Toast.makeText(this, "Relative successfully registered!", Toast.LENGTH_LONG).show()
                val intent = Intent(this, RelativeMainScreen::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
