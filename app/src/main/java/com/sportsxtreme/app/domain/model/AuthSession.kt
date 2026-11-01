package com.sportsxtreme.app.domain.model

data class AuthSession(
    val user: User,
    val isNewUser: Boolean
)
