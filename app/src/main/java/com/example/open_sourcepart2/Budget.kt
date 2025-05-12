package com.example.open_sourcepart2

// Budget.kt

data class Budget(
    val id: Long = 0,
    val amount: Double,
    val period: String, // "daily", "weekly", "monthly", "yearly"
    val startDate: String,
    val endDate: String,
    val userId: Long
)