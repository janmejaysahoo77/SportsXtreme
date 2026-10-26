package com.example.sportsxtreme.domain.model

import java.util.UUID

data class InAppMessage(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val landscapeImageUrl: String? = null,
    val primaryButtonText: String = "VIEW NOW",
    val targetUrl: String? = null,
    val category: String = "OFFICIAL ANNOUNCEMENT"
)
