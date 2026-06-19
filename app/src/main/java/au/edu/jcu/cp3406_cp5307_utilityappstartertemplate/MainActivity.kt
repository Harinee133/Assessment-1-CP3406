package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
            Text("Dashboard Content Placeholder")
        }
    }
}
