package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("WealthWatchPrefsV2", Context.MODE_PRIVATE)

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _income = MutableStateFlow(prefs.getFloat("monthly_income", 0f).toDouble())
    val income: StateFlow<Double> = _income.asStateFlow()
}
