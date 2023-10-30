package com.example.wealthwise.DataClass

data class SavingsGoal(
    val title: String,
    var currentAmount: Double,
    var targetAmount: Double,
    var active: Boolean
) {
}