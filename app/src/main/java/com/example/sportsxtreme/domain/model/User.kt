package com.example.sportsxtreme.domain.model

data class User(
    val id: String,
    val name: String,
    val mobileNumber: String,
    val email: String = "",
    val profilePhotoUrl: String? = null,
    val authProvider: AuthProvider = AuthProvider.EMAIL_PASSWORD,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
)
