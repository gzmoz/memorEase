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

    fun uploadFile(
        fileUri: Uri,
        folderName: String,
        fileType: String, // "image", "video" (audio dosyaları için de video kullanıyoruz)
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val resourceType = if (fileType == "image") "image" else "video"

        val requestId = MediaManager.get().upload(fileUri)
            .option("folder", "memorease/$folderName")
            .option("resource_type", resourceType) // "video" olarak ayarladık
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
