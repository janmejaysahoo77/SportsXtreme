package com.sportsxtreme.app.domain.usecase

import android.util.Log
import com.sportsxtreme.app.common.Resource
import com.sportsxtreme.app.domain.model.User
import com.sportsxtreme.app.domain.repository.AuthRepository

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
