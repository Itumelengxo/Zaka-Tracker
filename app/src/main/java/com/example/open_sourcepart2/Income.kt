package com.example.open_sourcepart2


import java.util.*

data class Income(
    val id: Long = 0,
    val amount: Double,
    val source: String,
    val note: String = "",
    val date: String,
    val userId: Long
)