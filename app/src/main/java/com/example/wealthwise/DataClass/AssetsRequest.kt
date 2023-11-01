package com.example.wealthwise.DataClass

data class AssetsRequest(val token: String,
                         val currency: String,
                         val name : String,
                         val value: Double) {
}