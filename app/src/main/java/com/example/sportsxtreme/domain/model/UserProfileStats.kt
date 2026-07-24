package com.example.sportsxtreme.domain.model

data class UserProfileStats(
    val userId: String,
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val runs: Int = 0,
    val wickets: Int = 0,
    val bestScore: String = "",
    val trophies: Int = 0,
    val topPerformerStreak: Boolean = false,
    val battingAverage: Double = 0.0,
    val bowlingAverage: Double = 0.0,
    val strikeRate: Double = 0.0,
    val mvpCount: Int = 0
)
