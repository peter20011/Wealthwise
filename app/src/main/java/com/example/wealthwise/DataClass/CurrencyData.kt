package com.example.wealthwise.DataClass

data class CurrencyData( val table: String,
                         val no: String,
                         val effectiveDate: String,
                         val rates: List<Rate>)
