package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.data.remote.auth.AuthDataSource
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.repository.AuthRepository
import com.google.firebase.auth.GoogleAuthProvider

class AuthRepositoryImpl(
    private val authDataSource: AuthDataSource = AuthDataSource()
) : AuthRepository {
    override suspend fun login(mobileNumber: String): Resource<User> {
        // TODO: Implement network/local call
        return Resource.Success(User("1", "Dummy User", mobileNumber))
    }

    override suspend fun signup(name: String, email: String, password: String): Resource<User> {
        val result = authDataSource.signup(name, email, password)
        return result.fold(
            onSuccess = { user -> Resource.Success(user) },
            onFailure = { error -> Resource.Error(error.message ?: "Signup failed") }
        )
    }

    override suspend fun signupWithGoogle(idToken: String): Resource<User> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = authDataSource.signupWithCredential(credential)
        return result.fold(
            onSuccess = { user -> Resource.Success(user) },
            onFailure = { error -> Resource.Error(error.message ?: "Google signup failed") }
        )
    }
}
