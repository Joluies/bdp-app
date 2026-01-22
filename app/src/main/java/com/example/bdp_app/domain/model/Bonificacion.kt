package com.example.bdp_app.domain.model

data class BonificacionResponse(
    val success: Boolean,
    val data: BonificacionData
)

data class BonificacionData(
    val data: List<Bonificacion>
)

data class Bonificacion(
    val idBonificacion: Int,
    val nombre: String,
    val tipo_bonificacion: String,
    val productosRequeridos: List<Int>?,
    val idProducto_requerido: Int?,
    val cantidad_minima: Int?,
    val precio_minimo: String?,
    val idProducto_bonificacion: Int,
    val cantidad_bonificacion: Int,
    val producto_bonificacion: ProductSimple?,
    val escalas_cantidad: List<Escala>? // <--- ¡ESTO FALTABA!
)

data class Escala(
    val cantidad_minima: Int,
    val bonificacion: Int,
    val paquetes: Int?,
    val tipo_unidad: String?
)

data class ProductSimple(
    val idProducto: Int,
    val nombre: String,
    val precioUnitario: Double
)