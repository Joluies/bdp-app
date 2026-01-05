package com.example.bdp_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.bdp_app.navigation.SetupNavGraph
import com.example.bdp_app.ui.theme.BDPAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Importante para que Hilt funcione en la actividad
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BDPAppTheme { // Tu tema de Compose
                val navController = rememberNavController()
                SetupNavGraph(navController = navController)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BDPAppTheme {
        Greeting("Android")
    }
}