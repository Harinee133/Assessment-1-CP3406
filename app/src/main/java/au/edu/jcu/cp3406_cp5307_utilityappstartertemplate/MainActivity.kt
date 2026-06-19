package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.ui.theme.CP3406_CP5603UtilityAppStarterTemplateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ExpenseViewModel = viewModel()
            
            CP3406_CP5603UtilityAppStarterTemplateTheme(dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    WealthWatchApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WealthWatchApp(viewModel: ExpenseViewModel) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("WealthWatch Pro") })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            DashboardScreen(viewModel)
        }
    }
}

@Composable
fun DashboardScreen(viewModel: ExpenseViewModel) {
    val income by viewModel.income.collectAsState()
    // Simple calculation for now, will be refined in later commits
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent = expenses.sumOf { it.amount }
    val balance = income - totalSpent
    
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem("Income", "$${"%.2f".format(income)}", Color(0xFF4CAF50))
                SummaryItem("Balance", "$${"%.2f".format(balance)}", if(balance >= 0) Color.Blue else Color.Red)
            }
        }
        Text("Spending Breakdown Placeholder", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun SummaryItem(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(amount, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
    }
}
