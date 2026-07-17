package com.example.sportsxtreme.data.repository

import com.example.sportsxtreme.common.Resource
import com.example.sportsxtreme.domain.model.User
import com.example.sportsxtreme.domain.repository.AuthRepository

class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(mobileNumber: String): Resource<User> {
        // TODO: Implement network/local call
        return Resource.Success(User("1", "Dummy User", mobileNumber))
    }

    override suspend fun signup(user: User): Resource<User> {
        // TODO: Implement network/local call
        return Resource.Success(user)
    }
}
