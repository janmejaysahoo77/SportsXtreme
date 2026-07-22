package com.example.sportsxtreme.data.remote.firestore

import com.example.sportsxtreme.domain.model.PendingUserProfile
import com.example.sportsxtreme.domain.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirebaseFirestoreUserDataSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : FirestoreUserDataSource {
    override suspend fun createOrUpdateUser(profile: PendingUserProfile): Result<User> {
        return suspendCancellableCoroutine { continuation ->
            val payload = mapOf(
                "id" to profile.id,
                "name" to profile.name,
                "email" to profile.email,
                "phoneNumber" to profile.phoneNumber,
                "profilePhotoUrl" to profile.profilePhotoUrl,
                "authProvider" to profile.authProvider.name,
                "isEmailVerified" to profile.isEmailVerified,
                "isPhoneVerified" to profile.isPhoneVerified,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            firestore.collection(USERS_COLLECTION)
                .document(profile.id)
                .set(payload, SetOptions.merge())
                .addOnSuccessListener {
                    continuation.resume(Result.success(profile.toUser()))
                }
                .addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                }
        }
    }

    private companion object {
        const val USERS_COLLECTION = "users"
    }
}
