package com.example.bdp_app.navigation

sealed class Screen(val route: String) {
    // Pantallas generales
    object Splash : Screen("splash_screen")
    object Login : Screen("login_screen")
    object Home : Screen("home/{role}") {
        fun createRoute(role: String) = "home/$role"
    }

    // --- PANTALLAS DE VENDEDOR ---
    object VendedorMenu : Screen("vendedor_menu")
    object AgregarCliente : Screen("agregar_cliente")
    object Actualizar : Screen("actualizar_cliente")
    object RegistrarPedido : Screen("registrar_pedido")
    object Rutas : Screen("rutas")
    object RutaDetalle : Screen("ruta_detalle/{clienteId}") {
        fun createRoute(clienteId: Int) = "ruta_detalle/$clienteId"
    }

    // Pantalla compartida
    object MapLocation : Screen("map_location_screen")

    // --- PANTALLAS DE REPARTIDOR ---
    object RepartidorMenu : Screen("repartidor_menu")
}