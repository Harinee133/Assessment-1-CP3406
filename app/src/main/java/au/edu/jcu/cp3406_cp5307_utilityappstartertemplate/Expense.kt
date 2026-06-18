package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import java.util.UUID

enum class ExpenseCategory(val displayName: String) {
    Transportation("Transportation"),
    Food("Food & Dining"),
    Entertainment("Entertainment"),
    Utilities("Utilities"),
    Others("Others")
}

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false
)
