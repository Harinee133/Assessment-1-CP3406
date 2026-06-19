package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.ui.theme.CP3406_CP5603UtilityAppStarterTemplateTheme
import kotlin.math.PI
import kotlin.math.atan2

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
    val expenses by viewModel.expenses.collectAsState()
    val totalSpent = expenses.sumOf { it.amount }
    val balance = income - totalSpent
    var tooltipInfo by remember { mutableStateOf<String?>(null) }
    
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
        Text("Spending Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))
        
        PieChart(viewModel, onCategoryClick = {}, onHover = { tooltipInfo = it })

        tooltipInfo?.let { 
            Text(it, modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        } ?: Text("Tap slices for insights", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

        Spacer(Modifier.height(32.dp))
        ExpenseCategory.entries.forEach { category ->
            CategorySummaryCard(category, viewModel) { /* Will navigate in later commits */ }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text(amount, fontWeight = FontWeight.Bold, color = color, fontSize = 18.sp)
    }
}

@Composable
fun CategorySummaryCard(category: ExpenseCategory, viewModel: ExpenseViewModel, onClick: () -> Unit) {
    val expenses by viewModel.expenses.collectAsState()
    val total = remember(expenses) { expenses.filter { it.category == category }.sumOf { it.amount } }
    val catColors by viewModel.categoryColors.collectAsState()
    val categoryColor = Color(catColors[category] ?: Color.Gray.toArgb())

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(12.dp).clip(CircleShape).background(categoryColor))
            Spacer(Modifier.width(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category.displayName, fontWeight = FontWeight.Bold)
                Text("$${"%.2f".format(total)}", fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
fun PieChart(viewModel: ExpenseViewModel, onCategoryClick: (ExpenseCategory) -> Unit, onHover: (String?) -> Unit) {
    val expenses by viewModel.expenses.collectAsState()
    val catColors by viewModel.categoryColors.collectAsState()
    val total = remember(expenses) { expenses.sumOf { it.amount } }
    val categories = ExpenseCategory.entries

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier
            .size(200.dp)
            .pointerInput(total) {
                detectTapGestures { offset ->
                    if (total == 0.0) return@detectTapGestures
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val angle = (atan2(offset.y - center.y, offset.x - center.x) * 180 / PI).let {
                        if (it < 0) it + 360 else it
                    }
                    var currentAngle = 0f
                    categories.forEach { category ->
                        val spending = expenses.filter { it.category == category }.sumOf { it.amount }
                        val sweepAngle = (spending / total).toFloat() * 360f
                        if (angle >= currentAngle && angle <= currentAngle + sweepAngle) {
                            onCategoryClick(category)
                            onHover("${category.displayName}: $${"%.2f".format(spending)}")
                            return@forEach
                        }
                        currentAngle += sweepAngle
                    }
                }
            }
        ) {
            var startAngle = 0f
            categories.forEach { category ->
                val spending = expenses.filter { it.category == category }.sumOf { it.amount }
                val sweepAngle = if (total > 0) (spending / total).toFloat() * 360f else 0f
                if (sweepAngle > 0) {
                    drawArc(
                        color = Color(catColors[category] ?: Color.Gray.toArgb()),
                        startAngle = startAngle, sweepAngle = sweepAngle, useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweepAngle
                }
            }
            if (total == 0.0) {
                drawCircle(color = Color.LightGray, radius = size.width / 2)
            }
            drawCircle(color = Color.White, radius = size.width / 3.5f)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Spent", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text("$${"%.2f".format(total)}", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}
