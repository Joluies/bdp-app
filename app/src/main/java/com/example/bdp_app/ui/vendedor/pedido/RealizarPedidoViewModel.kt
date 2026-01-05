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
        if (clienteSeleccionado == null) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Filtramos solo los productos que tengan cantidad > 0
                val itemsSeleccionados = productos.filter { it.cantidad > 0 }

                if (itemsSeleccionados.isEmpty()) {
                    _uiState.value = UiState.Error("Debe agregar al menos un producto")
                    return@launch
                }

                val pedidoData = mapOf(
                    "idCliente" to clienteSeleccionado?.idCliente,
                    "productos" to itemsSeleccionados.map {
                        mapOf("idProducto" to it.producto.idProducto, "cantidad" to it.cantidad)
                    },
                    "total" to total
                )

                val body = Gson().toJson(pedidoData).toRequestBody("application/json".toMediaTypeOrNull())
                val response = RetrofitClient.apiService.enviarPedido(body)

                if (response.isSuccessful) {
                    _uiState.value = UiState.Success("Pedido realizado con éxito")
                    onSuccess()
                } else {
                    _uiState.value = UiState.Error("Error al enviar pedido: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error de red")
            }
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}