package com.example.bdp_app.ui.rutas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bdp_app.core.network.RetrofitClient
import com.example.bdp_app.domain.model.Cliente
import kotlinx.coroutines.launch

class RealizarRutasViewModel(application: Application) : AndroidViewModel(application) {

    // --- ESTADO DE CLIENTES ---
    private val _clientes = MutableLiveData<List<Cliente>>(emptyList())
    val clientes: LiveData<List<Cliente>> = _clientes // Esta es la variable que faltaba

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        cargarClientes()
    }

    fun cargarClientes() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.apiService.getCustomers()
                if (response.isSuccessful) {
                    // Mapeo seguro de la respuesta
                    val lista = response.body()?.data?.data ?: emptyList()
                    _clientes.value = lista
                } else {
                    _error.value = "Error al cargar: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Fallo de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper para la pantalla de detalle
    fun obtenerClientePorId(id: Int): Cliente? {
        return _clientes.value?.find { it.idCliente == id }
    }
}


