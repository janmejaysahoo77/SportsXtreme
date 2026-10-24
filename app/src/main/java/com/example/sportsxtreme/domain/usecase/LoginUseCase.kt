package com.example.sportsxtreme.domain.usecase

import android.util.Log
import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(mobileNumber: String): Resource<User> {
        Log.d(TAG, "[AUTH] LoginUseCase invoked for mobileNumber: '$mobileNumber'")
        val result = repository.login(mobileNumber)
        Log.d(TAG, "[AUTH] LoginUseCase result from repository: $result")
        return result
    }

    private companion object {
        private const val TAG = "LoginUseCase"
    }
}
