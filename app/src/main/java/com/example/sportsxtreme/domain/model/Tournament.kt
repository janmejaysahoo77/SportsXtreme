package com.example.sportsxtreme.domain.model

data class Tournament(
    val id: String = "",
    val type: String = "Tournament", // Tournament or Series
    val name: String = "",
    val city: String = "",
    val ground: String = "",
    val organizerName: String = "",
    val phone: String = "",
    val email: String = "",
    val startDate: String = "",
    val ballType: String = "Tennis",
    val matchForm: String = "Limited Overs",
    val lookingForTeams: Boolean = true,
    val hostUid: String = ""
)
