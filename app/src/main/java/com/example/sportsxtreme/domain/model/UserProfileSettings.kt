package com.example.sportsxtreme.domain.model

data class UserProfileSettings(
    val userId: String,
    val language: String = "English (UK)",
    val theme: String = "system",
    val notificationsEnabled: Boolean = true,
    val profileVisibility: String = "public"
)
