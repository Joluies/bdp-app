package com.example.bdp_app.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")

    // Flujo Vendedor
    object VendedorMenu : Screen("vendedor_menu")
    object AgregarCliente : Screen("agregar_cliente")
    object RegistrarPedido : Screen("registrar_pedido")
    object Rutas : Screen( route = "rutas")


    // Flujo Repartidor
    object RepartidorMenu : Screen("repartidor_menu")

}