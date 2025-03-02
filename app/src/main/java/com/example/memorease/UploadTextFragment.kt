package com.example.memorease

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.memorease.databinding.FragmentUploadTextBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class UploadTextFragment : Fragment() {

    private var _binding: FragmentUploadTextBinding? = null
    private val binding get() = _binding!!

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var uploadedBy: String = "Unknown"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadTextBinding.inflate(inflater, container, false)

        // Kullanıcı Bilgilerini Yükle
        fetchUploaderName()

        binding.uploadButton.setOnClickListener {
            val textContent = binding.commentBox.text.toString().trim()

            if (textContent.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a text for the memory.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveTextMemoryToFirestore(textContent)
        }

        return binding.root
    }

    /**
     * Kullanıcı Bilgisini Firestore'dan Al
     */
    private fun fetchUploaderName() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Unknown"
                    val surname = document.getString("surname") ?: ""
                    uploadedBy = "$name $surname"
                } else {
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

    /**
     * Firestore'a Metin Anısını Kaydet
     */
    private fun saveTextMemoryToFirestore(textContent: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val memoryId = UUID.randomUUID().toString()

        val memoryData = mapOf(
            "type" to "text",
            "text" to textContent,
            "uploadedBy" to uploadedBy,
            "description" to textContent,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(userId)
            .collection("memories").document(memoryId)
            .set(memoryData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Text memory successfully uploaded!", Toast.LENGTH_LONG).show()
                navigateToHomeFragment()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Firestore Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    /**
     * HomeFragment'a Yönlendirme
     */
    private fun navigateToHomeFragment() {
        val uploadMemoryFragment = UploadMemoryFragment()
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, uploadMemoryFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
        }
}