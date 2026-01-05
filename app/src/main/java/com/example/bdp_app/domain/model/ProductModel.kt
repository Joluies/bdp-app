package com.example.bdp_app.domain.model

import com.google.gson.annotations.SerializedName

// Este es el "Wrapper" que envuelve la respuesta de la API
data class ProductResponse(
    val message: String,
    val products: List<ProductModel>
)

data class ProductModel(
    val idProducto: Int,
    val nombre: String,
    val descripcion: String,
    val presentacion: String,
    val precioUnitario: Double,
    val precioMayorista: Double,
    val stock: Int,
    @SerializedName("urlImage") val imagen: String
)

data class CartItem(
    val producto: ProductModel,
    var cantidad: Int = 0
)