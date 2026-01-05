package com.example.bdp_app.domain.model

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
    val idCliente: Int? = null,
    val nombre: String,
    val apellidos: String,
    val tipoCliente: String, // minorista o mayorista
    val dni: String,
    val ruc: String?,  //opcional
    val razon_social: String?,  //opcional
    val direccion: String,
    val coordenadas: Coordenadas?,
    val fotoCliente: String? = null
)

data class Coordenadas(
    val latitud: Double,
    val longitud: Double
)

data class Telefono(
    val idTelefono: Int? = null,
    val description: String,
    val number: String
)

data class FotoFachada(
    val idFotoFachada: Int? = null,
    val foto: String
)
