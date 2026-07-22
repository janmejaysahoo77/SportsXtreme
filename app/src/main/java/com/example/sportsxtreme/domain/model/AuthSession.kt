package com.example.sportsxtreme.domain.model

data class AuthSession(
    val user: User,
    val isNewUser: Boolean
)
