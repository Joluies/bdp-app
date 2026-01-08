package com.example.bdp_app.ui.rutas

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdp_app.domain.model.Cliente
import com.example.bdp_app.core.network.RetrofitClient
import kotlinx.coroutines.launch

class RealizarRutasViewModel : ViewModel() {

    // Lista observable de clientes para la ruta
    var listaClientesRuta = mutableStateListOf<Cliente>()
        private set

    var isLoading = mutableStateOf(false)

    init {
        obtenerClientesRuta()
    }

    fun obtenerClientesRuta() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getCustomers()
                if (response.isSuccessful) {
                    // Accedemos a response -> body -> data -> data (por el wrapper)
                    response.body()?.data?.data?.let { clientes ->
                        listaClientesRuta.clear()
                        listaClientesRuta.addAll(clientes)
                    }
                }
            } catch (e: Exception) {
                // Manejar error
            } finally {
                isLoading.value = false
            }
        }
    }

    fun obtenerClientePorId(id: Int): Cliente? {
        return listaClientesRuta.find { it.idCliente == id }
    }
}


