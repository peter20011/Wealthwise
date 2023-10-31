package com.example.wealthwise.DataClass

import com.example.wealthwise.Rate

data class CurrencyData( val table: String,
                         val no: String,
                         val effectiveDate: String,
                         val rates: List<Rate>)
