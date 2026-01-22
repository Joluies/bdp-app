package com.example.bdp_app.domain.model

import com.google.gson.annotations.SerializedName

// Respuesta para LISTAS
data class CustomerResponse(
    val success: Boolean,
    val data: CustomerPaginationData
)

data class CustomerPaginationData(
    val data: List<Cliente>
)

// Respuesta para CREAR/ACTUALIZAR
data class SingleCustomerResponse(
    val success: Boolean,
    val message: String?,
    val data: Cliente?
)

data class Cliente(
    val idCliente: Int,

    // AGREGA ESTO: Aunque se genera en DB, la App lo necesita para mostrarlo
    val codigoCliente: String?,

    val nombre: String,
    val apellidos: String,
    val dni: String,
    val direccion: String,
    val distritos: String?,
    val tipoCliente: String,
    val ruc: String?,

    @SerializedName("razon_social") val razonSocial: String?,
    val fotoCliente: String?,

    val coordenadas: CoordenadasMap?,

    val telefonos: List<Telefono>? = emptyList(),

)

data class CoordenadasMap(
    val latitud: Double,
    val longitud: Double
)

data class Telefono(
    val idTelefono: Int?,

    // TRUCO: Mapeamos el JSON "number" a la variable "numero" de Kotlin
    // Así no tienes que cambiar todo tu código de la UI
    @SerializedName("number") val numero: String,

    val description: String
)

