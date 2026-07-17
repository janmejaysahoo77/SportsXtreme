package com.example.sportsxtreme.data.remote.auth

import com.example.sportsxtreme.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class AuthDataSource(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signup(name: String, email: String, password: String): Result<User> {
        return suspendCancellableCoroutine { continuation ->
            val task = firebaseAuth.createUserWithEmailAndPassword(email, password)

            task.addOnSuccessListener { authResult ->
                val firebaseUser = authResult.user
                val user = User(
                    id = firebaseUser?.uid.orEmpty(),
                    name = name,
                    mobileNumber = email
                )
                continuation.resume(Result.success(user))
            }.addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
        }
    }
}
