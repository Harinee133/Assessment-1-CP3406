package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
            val isDark by viewModel.isDarkMode.collectAsState()
            
            CP3406_CP5603UtilityAppStarterTemplateTheme(darkTheme = isDark, dynamicColor = false) {
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
    var currentScreen by rememberSaveable { mutableStateOf("Home") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("WealthWatch Pro", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    if (currentScreen == "CategoryDetail") {
                        IconButton(onClick = { currentScreen = "Home" }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (currentScreen != "CategoryDetail") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentScreen == "Home",
                        onClick = { currentScreen = "Home" }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentScreen == "Settings",
                        onClick = { currentScreen = "Settings" }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                "Home" -> DashboardScreen(viewModel) { cat ->
                    selectedCategory = cat
                    currentScreen = "CategoryDetail"
                }
                "Settings" -> SettingsScreen(viewModel)
                "CategoryDetail" -> selectedCategory?.let { 
                    CategoryFileScreen(it, viewModel)
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: ExpenseViewModel, onCategoryClick: (ExpenseCategory) -> Unit) {
    val income by viewModel.income.collectAsState()
    val balance = viewModel.getBalance()
    var tooltipInfo by remember { mutableStateOf<String?>(null) }
    
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem("Income", viewModel.formatAmount(income), Color(0xFF4CAF50))
                SummaryItem("Balance", viewModel.formatAmount(balance), if(balance >= 0) Color.Blue else Color.Red)
            }
        }

        if (viewModel.isBudgetOverIncome()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                border = BorderStroke(1.dp, Color.Red)
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Red)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Warning: Total Budget (${viewModel.formatAmount(viewModel.getTotalBudget())}) exceeds Income!",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Text("Spending Breakdown", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))
        
        PieChart(viewModel, onCategoryClick, onHover = { tooltipInfo = it })

        tooltipInfo?.let { 
            Text(it, modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        } ?: Text("Select slices for details", style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))

        Spacer(Modifier.height(32.dp))
        Text("Category Files", modifier = Modifier.align(Alignment.Start), fontSize = 20.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(12.dp))
        
        ExpenseCategory.entries.forEach { category ->
            CategorySummaryCard(category, viewModel) { onCategoryClick(category) }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(24.dp))
        QuickAddExpense(viewModel)
        Spacer(Modifier.height(40.dp))
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
    val progress = viewModel.getCategoryProgress(category)
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
            Column(Modifier.weight(1f)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(category.displayName, fontWeight = FontWeight.Bold)
                    Text(viewModel.formatAmount(total), fontWeight = FontWeight.Black)
                }
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = categoryColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
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
                            onHover("${category.displayName}: ${viewModel.formatAmount(spending)}")
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
            Text(viewModel.formatAmount(total), fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
        }
    }
}

@Composable
fun CategoryFileScreen(category: ExpenseCategory, viewModel: ExpenseViewModel) {
    val expenses by viewModel.expenses.collectAsState()
    val history = remember(expenses) { expenses.filter { it.category == category } }
    val total = remember(history) { history.sumOf { it.amount } }
    val budgets by viewModel.categoryBudgets.collectAsState()
    val limit = budgets[category] ?: 500.0
    val catColors by viewModel.categoryColors.collectAsState()
    val categoryColor = Color(catColors[category] ?: Color.Gray.toArgb())
    val isExceeded = total > limit
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Box(modifier = Modifier.fillMaxWidth().background(categoryColor, RoundedCornerShape(16.dp)).padding(20.dp)) {
            Column {
                Text("${category.displayName} File", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text("Total Spent: ${viewModel.formatAmount(total)}", color = Color.White)
                Text("Allocated Budget: ${viewModel.formatAmount(limit)}", color = Color.White, fontSize = 12.sp)
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isExceeded) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.secondaryContainer
            ),
            border = if (isExceeded) BorderStroke(2.dp, Color.Red) else null
        ) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isExceeded) Icons.Default.Warning else Icons.Default.Star,
                    null,
                    tint = if (isExceeded) Color.Red else Color(0xFF673AB7)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = viewModel.getCategoryInsights(category),
                    fontWeight = FontWeight.Bold,
                    color = if (isExceeded) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Transaction History", fontSize = 22.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(history.reversed(), key = { it.id }) { expense ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, categoryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(expense.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("-${viewModel.formatAmount(expense.amount)}", color = Color.Red, fontWeight = FontWeight.Black)
                    IconButton(onClick = { 
                        viewModel.removeExpense(expense) 
                        Toast.makeText(context, "Transaction Deleted", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val notifications by viewModel.notificationsEnabled.collectAsState()
    val currentCurrency by viewModel.currency.collectAsState()
    val catColors by viewModel.categoryColors.collectAsState()
    val budgets by viewModel.categoryBudgets.collectAsState()
    val income by viewModel.income.collectAsState()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("Personalization", fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(16.dp))
        
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Dark Mode", Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Switch(checked = isDark, onCheckedChange = { viewModel.toggleDarkMode() })
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Enable Notifications", Modifier.weight(1f), fontWeight = FontWeight.Medium)
            Switch(checked = notifications, onCheckedChange = { viewModel.toggleNotifications() })
        }

        Spacer(Modifier.height(32.dp))
        Text("Financial Targets", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        var incomeInput by remember { mutableStateOf(income.toString()) }
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Monthly Income", Modifier.weight(1f))
            OutlinedTextField(
                value = incomeInput, onValueChange = { incomeInput = it },
                label = { Text(currentCurrency.symbol) }, modifier = Modifier.width(120.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            IconButton(onClick = { 
                incomeInput.toDoubleOrNull()?.let { 
                    viewModel.setIncome(it)
                    Toast.makeText(context, "Income Saved", Toast.LENGTH_SHORT).show()
                }
            }) { Icon(Icons.Default.Check, null, tint = Color.Green) }
        }

        Spacer(Modifier.height(32.dp))
        Text("Category Themes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        val availableColors = listOf(Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFFB8C00), Color(0xFFE91E63), Color(0xFF4CAF50), Color.Blue, Color.Red, Color.Black)
        ExpenseCategory.entries.forEach { category ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(category.displayName, fontWeight = FontWeight.Bold)
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(top = 8.dp)) {
                        availableColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp).padding(4.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(if(catColors[category] == color.toArgb()) 3.dp else 0.dp, Color.Gray, CircleShape)
                                    .clickable { viewModel.setCategoryColor(category, color) }
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Currency Preference", fontWeight = FontWeight.Bold)
        Row(Modifier.padding(vertical = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseViewModel.Currency.entries.forEach { currency ->
                FilterChip(
                    selected = currentCurrency == currency,
                    onClick = { viewModel.setCurrency(currency) },
                    label = { Text("${currency.name} (${currency.symbol})") }
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Text("Budget Configuration", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        ExpenseCategory.entries.forEach { category ->
            var tempAmount by remember { mutableStateOf(budgets[category]?.toString() ?: "500") }
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(category.displayName, Modifier.weight(1f))
                OutlinedTextField(
                    value = tempAmount, onValueChange = { tempAmount = it },
                    label = { Text(currentCurrency.symbol) }, modifier = Modifier.width(100.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                IconButton(onClick = { 
                    tempAmount.toDoubleOrNull()?.let { 
                        viewModel.setCategoryBudget(category, it)
                        Toast.makeText(context, "Budget Saved", Toast.LENGTH_SHORT).show()
                    }
                }) { Icon(Icons.Default.Check, null, tint = Color.Green) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddExpense(viewModel: ExpenseViewModel) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCat by remember { mutableStateOf(ExpenseCategory.Food) }
    val currentCurrency by viewModel.currency.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(24.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Quick Entry", fontWeight = FontWeight.Black)
        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Entry Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = amount, onValueChange = { amount = it }, 
            label = { Text("Amount (${currentCurrency.symbol})") }, 
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        Text("Category:", fontWeight = FontWeight.Bold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExpenseCategory.entries.forEach { cat ->
                FilterChip(selected = selectedCat == cat, onClick = { selectedCat = cat }, label = { Text(cat.displayName) })
            }
        }
        
        Button(
            onClick = {
                val amt = amount.toDoubleOrNull()
                if (title.isNotBlank() && amt != null) {
                    viewModel.addExpense(title, amt / currentCurrency.rate, selectedCat)
                    title = ""; amount = ""
                    Toast.makeText(context, "Transaction Logged!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Save Transaction") }
    }
}
