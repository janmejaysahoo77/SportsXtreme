package com.example.sportsxtreme.data.remote.cloudinary

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException

/** Uploads a tournament logo or banner with a short-lived Firebase Functions signature. */
class CloudinaryTournamentImageUploader(
    private val context: Context,
    private val client: OkHttpClient = OkHttpClient()
) {
    suspend fun upload(imageUri: Uri, assetId: String, assetType: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
                ?: throw IOException("Sign in to your SportsXtreme account before uploading a tournament logo.")
            // Refresh before the callable so Firebase Functions has a current ID token to attach.
            firebaseUser.getIdToken(true).await()

            val signatureData = FirebaseFunctions.getInstance("us-central1")
                .getHttpsCallable("createTournamentLogoUploadSignature")
                .call(mapOf("assetId" to assetId, "assetType" to assetType))
                .await()
                .data as? Map<*, *> ?: throw IOException("Could not get an upload signature.")
            val cloudName = signatureData["cloudName"] as? String ?: throw IOException("Cloudinary cloud name is unavailable.")
            val apiKey = signatureData["apiKey"] as? String ?: throw IOException("Cloudinary API key is unavailable.")
            val folder = signatureData["folder"] as? String ?: throw IOException("Cloudinary folder is unavailable.")
            val publicId = signatureData["publicId"] as? String ?: throw IOException("Cloudinary public ID is unavailable.")
            val timestamp = (signatureData["timestamp"] as? Number)?.toLong()
                ?: throw IOException("Cloudinary timestamp is unavailable.")
            val signature = signatureData["signature"] as? String ?: throw IOException("Cloudinary signature is unavailable.")
            val mimeType = context.contentResolver.getType(imageUri)?.takeIf { it.startsWith("image/") }
                ?: throw IllegalArgumentException("Please select an image file.")
            val temporaryFile = File.createTempFile("tournament-$assetType-", ".upload", context.cacheDir)
            try {
                context.contentResolver.openInputStream(imageUri)?.use { input ->
                    temporaryFile.outputStream().use(input::copyTo)
                } ?: throw IOException("Could not read the selected image.")

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", apiKey)
                    .addFormDataPart("timestamp", timestamp.toString())
                    .addFormDataPart("signature", signature)
                    .addFormDataPart("folder", folder)
                    .addFormDataPart("public_id", publicId)
                    .addFormDataPart("overwrite", "false")
                    .addFormDataPart("file", "tournament-$assetType", temporaryFile.asRequestBody(mimeType.toMediaTypeOrNull()))
                    .build()
                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val message = runCatching {
                            JSONObject(responseBody).optJSONObject("error")?.optString("message").orEmpty()
                        }.getOrDefault("")
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
