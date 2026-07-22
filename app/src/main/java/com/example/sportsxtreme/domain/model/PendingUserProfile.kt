package com.example.sportsxtreme.domain.model

data class PendingUserProfile(
    val id: String,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val profilePhotoUrl: String? = null,
    val authProvider: AuthProvider,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean
) {
    fun toUser(): User {
        return User(
            id = id,
            name = name,
            mobileNumber = phoneNumber,
            email = email,
            profilePhotoUrl = profilePhotoUrl,
            authProvider = authProvider,
            isEmailVerified = isEmailVerified,
            isPhoneVerified = isPhoneVerified
        )
    }
}
