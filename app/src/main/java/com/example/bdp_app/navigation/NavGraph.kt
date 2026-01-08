package com.example.bdp_app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.bdp_app.ui.auth.LoginScreen
import com.example.bdp_app.ui.auth.SplashScreen
import com.example.bdp_app.ui.components.MapLocationScreen
import com.example.bdp_app.ui.home.HomeScreen
import com.example.bdp_app.ui.rutas.RealizarRutasViewModel
import com.example.bdp_app.ui.rutas.RutaDetalleScreen
import com.example.bdp_app.ui.rutas.RutasScreen
import com.example.bdp_app.ui.vendedor.agregar.AgregarClienteScreen
import com.example.bdp_app.ui.vendedor.pedido.RealizarPedidoScreen
import com.example.bdp_app.ui.vendedor.pedido.RealizarPedidoViewModel

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Pantalla Splash
        composable(route = Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        // Pantalla Login
        composable(route = Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        // Pantalla Home
        composable(route = "home/{role}") { backStrackEntry ->
            val role = backStrackEntry.arguments?.getString("role") ?: "vendedor"
            HomeScreen(navController = navController, role = role)
        }

        // --- PANTALLAS DE VENDEDOR ---

        // 1. Agregar Cliente (Aquí estaba el error, ahora llamamos a la función)
        composable(route = Screen.AgregarCliente.route) {
            AgregarClienteScreen(navController = navController)
        }

        // 2. Mapa (Asegúrate de que el nombre coincida con el que usas en el botón "Mapa")
        composable(route = "map_location_screen") {
            MapLocationScreen(navController = navController)
        }

        // 3. Menu Vendedor (Opcional si usas el Home general)
        composable(route = Screen.VendedorMenu.route) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bienvenido Vendedor - Bebidas del Perú", color = Color(0xFF1B5E20))
            }
        }

        composable(route = Screen.RegistrarPedido.route) {
            // Aquí se instancia el ViewModel automáticamente para esta pantalla
            val pedidoViewModel: RealizarPedidoViewModel = viewModel()

            RealizarPedidoScreen(
                navController = navController,
                viewModel = pedidoViewModel
            )
        }

        composable(route = Screen.Rutas.route) {
            val RutasViewModel: RealizarRutasViewModel = viewModel()

            RutasScreen(
                navController = navController,
                viewModel = RutasViewModel
            )
        }

// ... dentro del NavHost

        composable(
            route = "ruta_detalle/{clienteId}",
            arguments = listOf(navArgument("clienteId") { type = NavType.IntType })
        ) { backStackEntry ->
            val clienteId = backStackEntry.arguments?.getInt("clienteId") ?: 0

            // Necesitamos pasar la MISMA instancia del ViewModel.
            // Si usas Hilt es automático, si no, pásalo desde el grafo padre o crea uno nuevo si la data persiste.
            // Para simplificar, aquí instanciamos uno nuevo, pero idealmente se comparte.
            val viewModel: RealizarRutasViewModel = viewModel()

            RutaDetalleScreen(navController, clienteId, viewModel)
        }

        // --- PANTALLAS DE REPARTIDOR ---
        composable(route = Screen.RepartidorMenu.route) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Bienvenido Repartidor - Bebidas del Perú", color = Color(0xFF1B5E20))
            }
        }
    }
}