package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("WealthWatchPrefsV2", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _expenses = MutableStateFlow<List<Expense>>(loadExpenses())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _categoryBudgets = MutableStateFlow<Map<ExpenseCategory, Double>>(loadBudgets())
    val categoryBudgets: StateFlow<Map<ExpenseCategory, Double>> = _categoryBudgets.asStateFlow()

    private val _income = MutableStateFlow(prefs.getFloat("monthly_income", 0f).toDouble())
    val income: StateFlow<Double> = _income.asStateFlow()

    fun addExpense(title: String, amount: Double, category: ExpenseCategory, isRecurring: Boolean = false) {
        if (title.isBlank() || amount <= 0) return
        val newExpense = Expense(title = title, amount = amount, category = category, isRecurring = isRecurring)
        _expenses.update { it + newExpense }
        saveExpenses()
    }

    fun removeExpense(expense: Expense) {
        _expenses.update { currentList -> currentList.filter { it.id != expense.id } }
        saveExpenses()
    }

    fun setCategoryBudget(category: ExpenseCategory, amount: Double) {
        _categoryBudgets.update { current -> current.toMutableMap().apply { put(category, amount) } }
        saveBudgets()
    }

    private fun saveExpenses() {
        val json = gson.toJson(_expenses.value)
        prefs.edit().putString("expenses_v2", json).apply()
    }

    private fun loadExpenses(): List<Expense> {
        val json = prefs.getString("expenses_v2", null) ?: return emptyList()
        return try { gson.fromJson(json, object : TypeToken<List<Expense>>() {}.type) } catch (e: Exception) { emptyList() }
    }

    private fun saveBudgets() {
        val json = gson.toJson(_categoryBudgets.value)
        prefs.edit().putString("budgets_v2", json).apply()
    }

    private fun loadBudgets(): Map<ExpenseCategory, Double> {
        val json = prefs.getString("budgets_v2", null) ?: return ExpenseCategory.entries.associateWith { 500.0 }
        return try { gson.fromJson(json, object : TypeToken<Map<ExpenseCategory, Double>>() {}.type) } catch (e: Exception) { ExpenseCategory.entries.associateWith { 500.0 } }
    }
}
