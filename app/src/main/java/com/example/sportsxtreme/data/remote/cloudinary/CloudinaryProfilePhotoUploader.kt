package com.example.sportsxtreme.data.remote.cloudinary

import android.content.Context
import android.net.Uri
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import com.google.firebase.functions.FirebaseFunctions
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

/** Uploads a selected profile image using a short-lived Firebase Functions signature. */
class CloudinaryProfilePhotoUploader(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun upload(photoUri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val signatureData = FirebaseFunctions.getInstance()
                .getHttpsCallable("createProfilePhotoUploadSignature")
                .call(emptyMap<String, Any>())
                .await()
                .data as? Map<*, *> ?: throw IOException("Could not get an upload signature.")
            val cloudName = signatureData["cloudName"] as? String ?: throw IOException("Cloudinary cloud name is unavailable.")
            val apiKey = signatureData["apiKey"] as? String ?: throw IOException("Cloudinary API key is unavailable.")
            val folder = signatureData["folder"] as? String ?: throw IOException("Cloudinary folder is unavailable.")
            val publicId = signatureData["publicId"] as? String ?: throw IOException("Cloudinary public ID is unavailable.")
            val timestamp = (signatureData["timestamp"] as? Number)?.toLong()
                ?: throw IOException("Cloudinary timestamp is unavailable.")
            val signature = signatureData["signature"] as? String ?: throw IOException("Cloudinary signature is unavailable.")
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(photoUri)?.takeIf { it.startsWith("image/") }
                ?: throw IllegalArgumentException("Please select an image file.")
            val temporaryFile = java.io.File.createTempFile("profile-photo-", ".upload", context.cacheDir)
            try {
                contentResolver.openInputStream(photoUri)?.use { input ->
                    temporaryFile.outputStream().use(input::copyTo)
                } ?: throw IOException("Could not read the selected image.")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", apiKey)
                    .addFormDataPart("timestamp", timestamp.toString())
                    .addFormDataPart("signature", signature)
                    .addFormDataPart("folder", folder)
                    .addFormDataPart("public_id", publicId)
                    .addFormDataPart("overwrite", "true")
                    .addFormDataPart("file", "profile-photo", temporaryFile.asRequestBody(mimeType.toMediaTypeOrNull()))
                    .build()
                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val message = runCatching {
                            JSONObject(responseBody)
                                .optJSONObject("error")
                                ?.optString("message")
                                .orEmpty()
                        }.getOrDefault("")
                        if (message.contains("Unknown API key", ignoreCase = true)) {
                            throw IOException(
                                "Cloudinary rejected the API key for cloud '$cloudName'. " +
                                    "Check CLOUDINARY_CLOUD_NAME in Firebase Secret Manager."
                            )
                        }
                        throw IOException(message.ifBlank { "Cloudinary upload failed (${response.code})." })
                    }
                    JSONObject(responseBody).optString("secure_url")
                        .takeIf { it.isNotBlank() }
                        ?: throw IOException("Cloudinary did not return an image URL.")
                }
            } finally {
                temporaryFile.delete()
            }
        }
    }
}
