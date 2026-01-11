package com.example.bdp_app.core.network

import com.example.bdp_app.domain.model.CustomerResponse
import com.example.bdp_app.domain.model.FotosFachadaApiResponse
import com.example.bdp_app.domain.model.SingleCustomerResponse
import com.example.bdp_app.domain.model.ProductResponse // Asegúrate de tener este import
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- CLIENTES ---


    // POR ESTO:
    @GET("customers/lite")
    suspend fun getCustomers(): Response<CustomerResponse>

    // Buscar clientes (si usas un endpoint de búsqueda específico)
    // POR ESTO:
    @GET("customers/lite")
    suspend fun buscarClientes(@Query("nombre") query: String): Response<CustomerResponse>



    @Multipart
    @POST("customers/create")
    suspend fun crearCliente(
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("tipoCliente") tipoCliente: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("direccion") direccion: RequestBody,
        @Part("ruc") ruc: RequestBody?,
        @Part("razonSocial") razonSocial: RequestBody?,
        @PartMap dataMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part fotoCliente: MultipartBody.Part?,
        @Part fotosFachada: List<MultipartBody.Part>
    ): Response<ResponseBody>

    @GET("customers/{id}/photos")
    suspend fun getFotosFachada(@Path("id") id: Int): Response<FotosFachadaApiResponse>

    @Multipart
    @POST("customers/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: Int,
        @Part("_method") method: RequestBody,
        @Part("nombre") nombre: RequestBody,
        @Part("apellidos") apellidos: RequestBody,
        @Part("dni") dni: RequestBody,
        @Part("direccion") direccion: RequestBody,
        @Part("tipoCliente") tipoCliente: RequestBody,
        @Part("ruc") ruc: RequestBody?,
        @Part("razonSocial") razonSocial: RequestBody?,
        @Part("coordenadas") coordenadas: RequestBody?, // <--- Faltaba esto
        @PartMap dataMap: Map<String, @JvmSuppressWildcards RequestBody>, // <--- Faltaba esto (reemplaza a telefnos)
        @Part fotoPerfil: MultipartBody.Part?,
    ): Response<SingleCustomerResponse>

    // 3. SUBIR FOTO FACHADA (Arregla 'Unresolved reference: subirFotoFachada')
    @Multipart
    @POST("customers/{id}/photo")
    suspend fun subirFotoFachada(
        @Path("id") id: Int,
        @Part foto: MultipartBody.Part
    ): Response<Void>

    // 4. ELIMINAR FOTO FACHADA (Arregla 'Unresolved reference: eliminarFotoFachada')
    @DELETE("customers/{id}/photo/{filename}")
    suspend fun eliminarFotoFachada(
        @Path("id") id: Int,
        @Path("filename") filename: String
    ): Response<Void>


    // --- PRODUCTOS Y PEDIDOS (ESTO ES LO QUE FALTABA) ---

    @GET("products")
    suspend fun getProducts(): Response<ProductResponse>

    @POST("pedidos") // O la ruta correcta de tu backend para pedidos
    suspend fun enviarPedido(@Body pedido: RequestBody): Response<ResponseBody>
}