package com.example.bdp_app.ui.vendedor.pedido

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdp_app.core.network.RetrofitClient
import com.example.bdp_app.domain.model.Bonificacion
import com.example.bdp_app.domain.model.CartItem
import com.example.bdp_app.domain.model.Cliente
import com.example.bdp_app.domain.model.ProductModel
import com.example.bdp_app.ui.vendedor.agregar.UiState
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.Calendar
import java.util.Locale

class RealizarPedidoViewModel(application: Application) : AndroidViewModel(application) {

    // 1. ESTADOS DE UI
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Listas y estados del formulario
    val productos = mutableStateListOf<CartItem>()
    val listaClientes = mutableStateListOf<Cliente>()
    var clienteSeleccionado by mutableStateOf<Cliente?>(null)

    // Estado para la fecha de entrega
    var fechaEntregaSeleccionada by mutableStateOf("")

    // Almacenamiento local de las reglas de bonificación
    private var listaBonificaciones = listOf<Bonificacion>()

    // 2. CÁLCULOS DINÁMICOS
    // 2. CÁLCULOS DINÁMICOS (Corregido)
    val subtotal: Double get() = productos.sumOf { item ->
        if (item.esBonificacion) {
            0.0
        } else {
            // LÓGICA DE PRECIO CORREGIDA (REGLA >= 100)
            val esClienteMayorista = clienteSeleccionado?.tipoCliente?.equals("Mayorista", ignoreCase = true) == true
            val cumpleCantidadMinima = item.cantidad >= 100 // Ahora es 100

            val precio = if (esClienteMayorista && cumpleCantidadMinima)
                item.producto.precioMayorista
            else
                item.producto.precioUnitario

            precio * item.cantidad
        }
    }

    // LÓGICA DE IMPUESTOS (Región Selva/Exonerada)
    val impuestos: Double get() = 0.0 // IGV 0%

    val total: Double get() = subtotal // Total es igual al subtotal

    // 3. INICIALIZACIÓN
    init {
        cargarProductos()
        cargarClientes()
        cargarBonificaciones() // Cargar reglas
        establecerFechaManana() // Fecha por defecto
    }

    // 4. MÉTODOS DE CARGA (API)

    private fun cargarProductos() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getProducts()
                if (response.isSuccessful) {
                    response.body()?.products?.let { list ->
                        productos.clear()
                        // Instanciamos explícitamente para evitar errores de argumentos
                        productos.addAll(list.map {
                            CartItem(
                                producto = it,
                                cantidad = 0,
                                esBonificacion = false
                            )
                        })
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun cargarClientes() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getCustomers()
                if (response.isSuccessful) {
                    response.body()?.data?.data?.let { list ->
                        listaClientes.clear()
                        listaClientes.addAll(list)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun cargarBonificaciones() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getBonificaciones()
                if (response.isSuccessful) {
                    // Accedemos a data.data según la estructura JSON que enviaste
                    listaBonificaciones = response.body()?.data?.data ?: emptyList()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 5. GESTIÓN DE FECHAS (DATE PICKER)

    private fun establecerFechaManana() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1) // Mañana
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US)
        fechaEntregaSeleccionada = sdf.format(calendar.time)
    }

    fun actualizarFechaEntrega(anio: Int, mes: Int, dia: Int) {
        // Ajuste porque mes empieza en 0 en Calendar
        val mesReal = mes + 1
        val mesStr = if (mesReal < 10) "0$mesReal" else "$mesReal"
        val diaStr = if (dia < 10) "0$dia" else "$dia"
        fechaEntregaSeleccionada = "$anio-$mesStr-$diaStr"
    }

    // 6. ACCIONES Y LÓGICA DE NEGOCIO

    fun actualizarCantidad(productId: Int, nuevaCantidad: Int) {
        val index = productos.indexOfFirst { it.producto.idProducto == productId }
        if (index != -1) {
            // Solo permitimos editar manualmente productos que NO sean bonificaciones
            if (!productos[index].esBonificacion) {
                productos[index] = productos[index].copy(cantidad = nuevaCantidad)
                aplicarBonificaciones() // Recalcular regalos cada vez que cambia una cantidad
            }
        }
    }

    private fun aplicarBonificaciones() {
        // 1. Limpiar bonificaciones previas para evitar duplicados
        productos.removeIf { it.esBonificacion }

        // 2. Recorrer reglas activas
        listaBonificaciones.forEach { regla ->
            var aplicaBonificacion = false

            when (regla.tipo_bonificacion) {
                "producto" -> {
                    // Regla: Por comprar X cantidad de un producto específico
                    val prodRequeridoId = regla.idProducto_requerido ?: 0
                    val itemEnCarrito = productos.find { it.producto.idProducto == prodRequeridoId && !it.esBonificacion }

                    if (itemEnCarrito != null) {
                        val cantidadMinima = regla.cantidad_minima ?: 0
                        if (itemEnCarrito.cantidad >= cantidadMinima) {
                            aplicaBonificacion = true
                        }
                    }
                }
                "cantidad" -> {
                    // Regla: Por volumen de compra (mix de productos)
                    val idsRequeridos = regla.productosRequeridos ?: emptyList()
                    // Sumamos la cantidad de todos los productos que están en la lista requerida
                    val cantidadTotalMix = productos
                        .filter { it.producto.idProducto in idsRequeridos && !it.esBonificacion }
                        .sumOf { it.cantidad }

                    // Verificamos la primera escala
                    val escala = regla.escalas_cantidad?.firstOrNull()
                    if (escala != null && cantidadTotalMix >= escala.cantidad_minima) {
                        aplicaBonificacion = true
                    }
                }
                "precio" -> {
                    // Regla: Por monto de compra de un producto específico
                    val prodId = regla.idProducto_requerido ?: 0
                    val item = productos.find { it.producto.idProducto == prodId && !it.esBonificacion }
                    if (item != null) {
                        val precioItem = if (clienteSeleccionado?.tipoCliente.equals("Mayorista", true))
                            item.producto.precioMayorista
                        else item.producto.precioUnitario

                        val subtotalItem = item.cantidad * precioItem
                        val precioMinimo = regla.precio_minimo?.toDoubleOrNull() ?: 0.0

                        if (subtotalItem >= precioMinimo) {
                            aplicaBonificacion = true
                        }
                    }
                }
            }

            // 3. Si cumple la regla, agregar el regalo
            if (aplicaBonificacion) {
                val qtyRegalo = regla.cantidad_bonificacion

                // Usamos la info del producto bonificado que viene en el JSON de la regla
                regla.producto_bonificacion?.let { prodInfo ->
                    // Creamos el ProductModel manual para el regalo
                    val productoRegalo = ProductModel(
                        idProducto = prodInfo.idProducto,
                        nombre = "${prodInfo.nombre} (REGALO)",
                        descripcion = "Bonificación: ${regla.nombre}",
                        presentacion = "Unidad", // <--- Obligatorio según tu modelo
                        precioUnitario = 0.0,
                        precioMayorista = 0.0,
                        stock = 9999,
                        imagen = "" // <--- Tu propiedad se llama 'imagen', no 'urlImage'
                    )

                    // Agregamos al carrito con el flag activado
                    productos.add(
                        CartItem(
                            producto = productoRegalo,
                            cantidad = qtyRegalo,
                            esBonificacion = true
                        )
                    )
                }
            }
        }
    }

    fun enviarPedidoFinal(onSuccess: () -> Unit) {
        val cliente = clienteSeleccionado
        if (cliente == null) {
            _uiState.value = UiState.Error("Seleccione un cliente primero")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // 1. CLASIFICAR ÍTEMS DEL CARRITO
                // Ventas: Productos normales con cantidad > 0
                val itemsVenta = productos.filter { !it.esBonificacion && it.cantidad > 0 }

                // Bonificaciones: Productos marcados como bonificación
                val itemsBonificacion = productos.filter { it.esBonificacion && it.cantidad > 0 }

                if (itemsVenta.isEmpty()) {
                    _uiState.value = UiState.Error("El pedido debe tener al menos un producto de venta.")
                    return@launch
                }

                // --- DATOS DE CABECERA ---
                // RECUERDA: Usar el ID real del usuario logueado. Si es fijo pon el que sirva (ej: 1 o 4)
                val idVendedorActual = 4
                val timeStamp = System.currentTimeMillis()
                val numeroPedidoAuto = "PED-${timeStamp}"

                // 2. CONSTRUIR ESTRUCTURA ANIDADA (Detalles -> Bonificaciones)
                val detallesEstructurados = itemsVenta.map { venta ->

                    // A. Calculamos precio
                    val precioAplicado = if (cliente.tipoCliente.equals("Mayorista", ignoreCase = true) && venta.cantidad >= 100)
                        venta.producto.precioMayorista
                    else
                        venta.producto.precioUnitario

                    // B. Buscamos si este producto tiene bonificaciones asociadas (Mismo ID)
                    // Ej: Si vendo "Inca Kola" (ID 1), busco si hay "Inca Kola (Regalo)" (ID 1) en el carrito.
                    val bonosDelProducto = itemsBonificacion.filter { bono ->
                        bono.producto.idProducto == venta.producto.idProducto
                    }

                    // C. Mapeamos las bonificaciones internas
                    val bonificacionesList = bonosDelProducto.map { bono ->
                        mapOf(
                            "idProducto" to bono.producto.idProducto,
                            "cantidad_bonificada" to bono.cantidad,

                            // --- NUEVA LÍNEA OBLIGATORIA ---
                            // Enviamos la cantidad del "padre" (venta) como cantidad_comprada
                            "cantidad_comprada" to venta.cantidad,
                            // -------------------------------

                            "tipo_bonificacion" to "cantidad",
                            "observaciones" to (bono.producto.descripcion ?: "Promo")
                        )
                    }

                    // D. Armamos el objeto del detalle con sus hijos
                    val detalleMap = mutableMapOf<String, Any>(
                        "idProducto" to venta.producto.idProducto,
                        "cantidad" to venta.cantidad,
                        "precio_unitario" to precioAplicado
                    )

                    // Solo agregamos la lista "bonificaciones" si realmente tiene algo
                    if (bonificacionesList.isNotEmpty()) {
                        detalleMap["bonificaciones"] = bonificacionesList
                    }

                    detalleMap // Return del map
                }

                // 3. JSON FINAL
                val pedidoData = mapOf(
                    "idCliente" to cliente.idCliente,
                    "idVendedor" to idVendedorActual,
                    "numero_pedido" to numeroPedidoAuto,
                    "fecha_entrega" to fechaEntregaSeleccionada,
                    "detalles" to detallesEstructurados
                )

                // Log para verificar antes de enviar
                val jsonString = Gson().toJson(pedidoData)
                android.util.Log.d("PEDIDO_JSON", "Enviando estructura anidada: $jsonString")

                // 4. ENVÍO
                val body = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
                val response = RetrofitClient.apiService.enviarPedido(body)

                if (response.isSuccessful) {
                    _uiState.value = UiState.Success("¡Pedido creado exitosamente!")
                    onSuccess()

                    // Limpiar carrito
                    productos.forEach { if (!it.esBonificacion) it.cantidad = 0 }
                    productos.removeIf { it.esBonificacion }

                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    android.util.Log.e("PEDIDO_ERROR", errorMsg)
                    _uiState.value = UiState.Error("Error del servidor: $errorMsg")
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error de conexión: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}