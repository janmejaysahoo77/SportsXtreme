package com.example.sportsxtreme.domain.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.User

interface AuthRepository {
    suspend fun login(mobileNumber: String): Resource<User>
    suspend fun signup(name: String, email: String, password: String): Resource<User>
}
