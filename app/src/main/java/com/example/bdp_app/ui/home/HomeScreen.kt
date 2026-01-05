package com.example.bdp_app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bdp_app.R
import com.example.bdp_app.navigation.Screen

// Definimos la estructura de cada opción del menú
data class MenuOption(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val roles: List<String>
)

@Composable
fun HomeScreen(navController: NavHostController, role: String) {
    val allOptions = listOf(
        MenuOption("Agregar Nuevo Cliente", Icons.Default.PersonAdd, Screen.AgregarCliente.route, listOf("vendedor")),
        MenuOption("Agregar Pedidos", Icons.Default.Assignment, Screen.RegistrarPedido.route, listOf("vendedor")),
        MenuOption("Ver rutas", Icons.Default.LocationOn, "rutas", listOf("vendedor", "repartidor")),
        MenuOption("Hacer Despacho", Icons.Default.LocalShipping, "despacho", listOf("repartidor")),
        MenuOption("Lista de Entrega", Icons.Default.ListAlt, "entrega", listOf("repartidor")),
        MenuOption("Cerrar Sesion", Icons.Default.PowerSettingsNew, Screen.Login.route, listOf("vendedor", "repartidor"))
    )

    // Filtramos las opciones según el rol que recibimos ("vendedor" o "repartidor")
    val filteredOptions = allOptions.filter { it.roles.contains(role) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo superior
        Image(
            painter = painterResource(id = R.drawable.logo_bdp),
            contentDescription = "Logo BDP",
            modifier = Modifier.size(380.dp).padding(top = 20.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Rejilla de opciones (2 columnas)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filteredOptions) { option ->
                MenuCard(option) {
                    if (option.title == "Cerrar Sesion") {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) // Limpia el historial
                        }
                    } else {
                        navController.navigate(option.route)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuCard(option: MenuOption, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)) // Color verde oscuro de la imagen
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(45.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = option.title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}