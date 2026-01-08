package com.example.bdp_app.domain.model


import com.google.gson.annotations.SerializedName

// Respuesta completa de la API
data class CustomerResponse(
    val success: Boolean,
    val data: CustomerPaginationData
)

data class CustomerPaginationData(
    val data: List<Cliente> // Aquí es donde están los clientes reales
)

data class CustomerData(
    val cliente: Cliente,
    val telefonos: List<Telefono>,
    val fotosFachada: List<FotoFachada>
)

data class Cliente(
    val idCliente: Int,
    val codigoCliente: String,
    val nombre: String,
    val apellidos: String,
    val direccion: String,
    val tipoCliente: String,
    val dni: String,
    val fotoCliente: String?, // Viene como "/img/fotosCliente/..."
    val telefonos: List<Telefono>? = emptyList(),
    val fotosFachada: List<FotoFachada>? = emptyList()
)

data class Coordenadas(
    val latitud: Double,
    val longitud: Double
)

data class Telefono(
    val idTelefono: Int,
    val numero: String,
    val description: String
)

data class FotoFachada(
    val idFotoFachada: Int,
    val foto: String
)
