package com.example.sportsxtreme.domain.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String = "",
    val phoneNumber: String = "",
    val profilePhotoUrl: String? = null,
    val location: String = "",
    val joinedLabel: String = "",
    val gender: String = "",
    val role: String = "",
    val battingStyle: String = "",
    val bowlingStyle: String = "",
    val followers: Int = 0,
    val following: Int = 0,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val bestScore: String = "",
    val trophies: Int = 0,
    val hostedTournaments: Int = 0,
    val hostedSeries: Int = 0,
    val hostedMatches: Int = 0,
    val topPerformerStreak: Boolean = false,
    val isPro: Boolean = false,
    val authProvider: AuthProvider = AuthProvider.EMAIL_PASSWORD,
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false
)
