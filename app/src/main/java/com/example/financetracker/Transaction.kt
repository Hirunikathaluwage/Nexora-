package com.example.financetracker.model

data class Transaction(
    val id: Int,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val currency: String,
    val timestamp: String
)
