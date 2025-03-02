package com.example.memorease

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.memorease.databinding.FragmentUploadVoiceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class UploadVoiceFragment : Fragment() {

    private var _binding: FragmentUploadVoiceBinding? = null
    private val binding get() = _binding!!

    private var selectedVoiceUri: Uri? = null
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var mediaPlayer: MediaPlayer? = null

    private var uploadedBy: String = "Unknown"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUploadVoiceBinding.inflate(inflater, container, false)

        // Cloudinary Servisini Başlat
        CloudinaryService.init(requireContext())

        // Kullanıcı Bilgilerini Yükle
        fetchUploaderName()

        // Başlangıçta buton "Add Voice" olarak ayarla
        binding.playVoiceButton.text = "Add Voice"

        binding.playVoiceButton.setOnClickListener {
            when (binding.playVoiceButton.text) {
                "Add Voice" -> openAudioPicker()
                "Play Voice" -> playVoice()
                "Pause Voice" -> pauseVoice()
            }
        }

        binding.uploadButton.setOnClickListener {
            val comment = binding.commentBox.text.toString().trim()

            if (selectedVoiceUri == null) {
                Toast.makeText(requireContext(), "Please select a voice file.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (comment.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a comment.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadVoiceToCloudinary(selectedVoiceUri!!, comment)
        }

        return binding.root
    }

    /**
     * Ses Dosyası Seçimi
     */
    private val selectVoiceLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                selectedVoiceUri = result.data?.data
                selectedVoiceUri?.let {
                    setupMediaPlayer(it)
                    binding.playVoiceButton.text = "Play Voice"
                    Toast.makeText(requireContext(), "Audio file selected.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    /**
     * Telefon Galerisinden Ses Dosyası Açma
     */
    private fun openAudioPicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
        selectVoiceLauncher.launch(intent)
    }

    /**
     * MediaPlayer Kurulumu
     */
    private fun setupMediaPlayer(uri: Uri) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(requireContext(), uri)

            mediaPlayer?.setOnCompletionListener {
                binding.playVoiceButton.text = "Play Voice"
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error initializing media player: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Ses Oynatma Fonksiyonu
     */
    private fun playVoice() {
        mediaPlayer?.let { player ->
            player.start()
            binding.playVoiceButton.text = "Pause Voice"
        }
    }

    /**
     * Ses Duraklatma Fonksiyonu
     */
    private fun pauseVoice() {
        mediaPlayer?.let { player ->
            player.pause()
            binding.playVoiceButton.text = "Play Voice"
        }
    }

    /**
     * Ses Dosyasını Cloudinary'e Yükle
     */
    private fun uploadVoiceToCloudinary(audioUri: Uri, comment: String) {
        try {
            CloudinaryService.uploadFile(audioUri, "memories", "audio", onSuccess = { audioUrl ->
                saveMemoryToFirestore(audioUrl, comment)
            }, onError = { error ->
                Toast.makeText(requireContext(), "Cloudinary Error: $error", Toast.LENGTH_LONG).show()
            })
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Upload Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Firestore'a Anıyı Kaydet
     */
    private fun saveMemoryToFirestore(audioUrl: String, comment: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        val memoryId = UUID.randomUUID().toString()

        val memoryData = mapOf(
            "type" to "voice",
            "url" to audioUrl,
            "uploadedBy" to uploadedBy,
            "description" to comment,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("users").document(userId)
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
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching user info: ${e.message}", Toast.LENGTH_LONG).show()
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
        mediaPlayer?.release()
        _binding=null
        }
}