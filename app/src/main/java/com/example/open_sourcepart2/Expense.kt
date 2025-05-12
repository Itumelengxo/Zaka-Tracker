package com.example.open_sourcepart2

// Expense.kt

data class Expense(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val date: String,
    val categoryId: Long,
    val userId: Long,
    val imagePath: String? = null
)

data class ExpenseWithCategory(
    val id: Long = 0,
    val amount: Double,
    val description: String,
    val date: String,
    val categoryId: Long,
    val userId: Long,
    val imagePath: String? = null,
    val categoryName: String,
    val categoryColor: String
)