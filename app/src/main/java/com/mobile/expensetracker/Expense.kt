package com.mobile.expensetracker

import java.util.*

data class Expense( val id : String = "",
    val expenseName : String = "",
                    val expenseCategory : String = "",
                    val expenseAmount : String = "",
                    val expenseDate : Date = Date()
)
