package com.example.memorease

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryService {

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to "dqkgrjl0b",
            "api_key" to "713947242619374",
            "api_secret" to "3QkSfPPV2bC9cHOpO6iP_x4vUnc"
        )
        MediaManager.init(context, config)
    }

    fun getCloudinary(): MediaManager? {
        return MediaManager.get()
    }

    // "folderName" parametresi eklendi
    fun uploadImage(
        imageUri: Uri,
        folderName: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val requestId = MediaManager.get().upload(imageUri)
            .option("folder", "memorease/$folderName") // Klasör dinamik olarak belirleniyor
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = (bytes / totalBytes.toDouble()) * 100
                    Log.d("Cloudinary", "Upload progress: $progress%")
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String ?: ""
                    onSuccess(url)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Unknown error")
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    onError(error?.description ?: "Upload rescheduled")
                }
            }).dispatch()
    }
}
