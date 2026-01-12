package com.example.bdp_app.ui.vendedor.pedido

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdp_app.core.network.RetrofitClient
import com.example.bdp_app.domain.model.CartItem
import com.example.bdp_app.domain.model.Cliente
import com.example.bdp_app.ui.vendedor.agregar.UiState
import com.example.bdp_app.domain.model.ProductResponse
import com.example.bdp_app.domain.model.ProductModel

import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

class RealizarPedidoViewModel(application: Application) : AndroidViewModel(application) {

    // 1. ESTADOS DE UI
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Listas y estados del formulario
    val productos = mutableStateListOf<CartItem>()
    val listaClientes = mutableStateListOf<Cliente>()
    var clienteSeleccionado by mutableStateOf<Cliente?>(null)

    // 2. CÁLCULOS DINÁMICOS
    val subtotal: Double get() = productos.sumOf { item ->
        // Comparamos ignorando mayúsculas/minúsculas para evitar errores
        val precio = if (clienteSeleccionado?.tipoCliente?.equals("Mayorista", ignoreCase = true) == true)
            item.producto.precioMayorista
        else item.producto.precioUnitario
        precio * item.cantidad
    }

    val impuestos: Double get() = subtotal * 0.18 // IGV 18%
    val total: Double get() = subtotal + impuestos

    // 3. INICIALIZACIÓN
    init {
        cargarProductos()
        cargarClientes()
    }

    // 4. MÉTODOS DE CARGA (API)
    // DENTRO DE RealizarPedidoViewModel

    private fun cargarProductos() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getProducts()
                if (response.isSuccessful) {
                    // response.body() es ProductResponse -> entramos a .products
                    response.body()?.products?.let { list ->
                        productos.clear()
                        productos.addAll(list.map { CartItem(it) })
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
                    // response.body() es CustomerResponse -> entramos a .data.data
                    response.body()?.data?.data?.let { list ->
                        listaClientes.clear()
                        listaClientes.addAll(list)
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // 5. ACCIONES
    fun actualizarCantidad(productId: Int, nuevaCantidad: Int) {
        val index = productos.indexOfFirst { it.producto.idProducto == productId }
        if (index != -1) {
            // Reemplazamos el item para que Compose detecte el cambio en la lista
            productos[index] = productos[index].copy(cantidad = nuevaCantidad)
        }
    }

    fun enviarPedidoFinal(onSuccess: () -> Unit) {
        // Validación básica
        val cliente = clienteSeleccionado
        if (cliente == null) {
            _uiState.value = UiState.Error("Seleccione un cliente primero")
            return
        }

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // 1. Filtrar productos con cantidad > 0
                val itemsSeleccionados = productos.filter { it.cantidad > 0 }

                if (itemsSeleccionados.isEmpty()) {
                    _uiState.value = UiState.Error("Debe agregar al menos un producto")
                    return@launch
                }

                // --- GENERACIÓN AUTOMÁTICA DE DATOS FALTANTES ---

                // A. ID Vendedor: Como no tenemos login, ponemos 1 por defecto (o el ID de tu usuario admin)
                val idVendedorDefault = 1

                // B. Número de Pedido: Generamos uno único usando la hora actual
                val timeStamp = System.currentTimeMillis()
                val numeroPedidoAuto = "PED-${timeStamp}"

                // C. Fecha de Entrega: Calculamos "Mañana" automáticamente
                val calendar = java.util.Calendar.getInstance()
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 1) // Sumamos 1 día
                val formatoFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                val fechaEntregaAuto = formatoFecha.format(calendar.time)

                // ------------------------------------------------

                // 2. Preparar la lista de "detalles" (PHP exige 'precio_unitario')
                val detallesMapeados = itemsSeleccionados.map { item ->
                    // Determinamos el precio correcto
                    val precioFinal = if (cliente.tipoCliente.equals("Mayorista", ignoreCase = true))
                        item.producto.precioMayorista
                    else
                        item.producto.precioUnitario

                    mapOf(
                        "idProducto" to item.producto.idProducto,
                        "cantidad" to item.cantidad,
                        "precio_unitario" to precioFinal // <--- ¡OBLIGATORIO PARA PHP!
                    )
                }

                // 3. Armar el JSON Final (Coincidiendo con lo que pide Laravel)
                val pedidoData = mapOf(
                    "idCliente" to cliente.idCliente,
                    "idVendedor" to idVendedorDefault,      // Dato automático
                    "numero_pedido" to numeroPedidoAuto,    // Dato automático
                    "fecha_entrega" to fechaEntregaAuto,    // Dato automático
                    "observaciones" to "Pedido generado desde App Android",
                    "detalles" to detallesMapeados          // <--- Se llama "detalles", no "productos"
                )

                // Log para depurar (mira esto en el Logcat si falla)
                val jsonString = Gson().toJson(pedidoData)
                android.util.Log.d("PEDIDO_JSON", "Enviando: $jsonString")

                // 4. Enviar
                val body = jsonString.toRequestBody("application/json".toMediaTypeOrNull())
                val response = RetrofitClient.apiService.enviarPedido(body)

                if (response.isSuccessful) {
                    _uiState.value = UiState.Success("¡Pedido $numeroPedidoAuto creado!")
                    onSuccess()
                    // Opcional: Limpiar carrito
                    productos.forEach { it.cantidad = 0 }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    android.util.Log.e("PEDIDO_ERROR", errorMsg)
                    _uiState.value = UiState.Error("Error del servidor: $errorMsg")
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error de conexión: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}