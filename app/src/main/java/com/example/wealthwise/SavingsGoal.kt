package com.example.wealthwise

data class SavingsGoal(
    val title: String,
    var currentAmount: Double,
    var targetAmount: Double,
    var active: Boolean
) {
    val progress: Int
        get() {
            if (targetAmount <= 0.0) {
                return 0
            }
            val percent = (currentAmount / targetAmount * 100).toInt()
            return if (percent > 100) 100 else percent
        }
}