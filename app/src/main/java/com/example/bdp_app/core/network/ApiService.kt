package com.example.bdp_app.core.network


import com.example.bdp_app.domain.model.CustomerResponse // Asegúrate de tener este modelo (o usa ResponseBody)
import com.example.bdp_app.domain.model.ProductModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*
import com.example.bdp_app.domain.model.Cliente
import com.example.bdp_app.domain.model.ProductResponse

interface ApiService {
    @Multipart
    @POST("customers/create") // <--- RUTA CORREGIDA
    suspend fun crearCliente(
        // Datos de Texto
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("tipoCliente") tipoCliente: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("direccion") direccion: RequestBody,

        // Datos Opcionales
        @Part("ruc") ruc: RequestBody?,
        @Part("razonSocial") razonSocial: RequestBody?,

        // Coordenadas
        @Part("latitud") latitud: RequestBody?,
        @Part("longitud") longitud: RequestBody?,

        // Archivos (Fotos)
        @Part fotoCliente: MultipartBody.Part?,
        @Part fotosFachada: List<MultipartBody.Part>,

        // Teléfonos (Enviaremos la lista como un String JSON dentro del multipart)
        @PartMap telefonos: Map<String, @JvmSuppressWildcards RequestBody>
    ): Response<CustomerResponse>

    // ... clientes ...

    @GET("products")
    suspend fun getProducts(): Response<ProductResponse>

    @GET("customers")
    suspend fun getCustomers(): Response<CustomerResponse>// <-- Asegúrate que sea CustomerModel o el nombre exacto de tu data class

    @POST("pedidos")
    suspend fun enviarPedido(@Body pedido: RequestBody): Response<ResponseBody>
}
