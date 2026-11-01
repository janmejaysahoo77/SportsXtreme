package com.sportsxtreme.app.domain.model

data class EmailSignupRequest(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String
)
