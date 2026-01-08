package com.example.bdp_app.ui.vendedor.agregar

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdp_app.core.network.RetrofitClient
import com.example.bdp_app.core.utils.UriUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AgregarClienteViewModel(application: Application) : AndroidViewModel(application) {

    // --- ESTADOS PERSISTENTES DEL FORMULARIO ---
    // Al estar aquí, sobreviven a la navegación (ir al mapa y volver)
    var nombre by mutableStateOf("")
    var apellidos by mutableStateOf("")
    var dni by mutableStateOf("")
    var direccion by mutableStateOf("")
    var tieneRuc by mutableStateOf(false)
    var ruc by mutableStateOf("")
    var razonSocial by mutableStateOf("")

    // Coordenadas
    var latitud by mutableStateOf<Double?>(null)
    var longitud by mutableStateOf<Double?>(null)

    // Listas (Usamos StateList para que Compose detecte cambios)
    val listaTelefonos = mutableStateListOf(TelefonoData("", "Personal"))

    // Fotos (Las guardamos aquí para que no se pierdan al rotar o navegar)
    var fotoPerfilUri by mutableStateOf<Uri?>(null)
    val fotosFachada = mutableStateListOf<Uri?>()

    // En AgregarClienteViewModel.kt, agrega estas propiedades:
    var esMayorista by mutableStateOf(false)
    var esMinorista by mutableStateOf(false)

    // Estado de la UI (Carga, Éxito, Error)
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    // Función para resetear estado después de éxito
    fun resetState() {
        _uiState.value = UiState.Idle
    }

    fun comprimirImagen(context: Context, uri: Uri, maxSizeKB: Int = 1024): File? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            var quality = 85
            var compressedFile: File?

            do {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val byteArray = outputStream.toByteArray()
                val sizeInKB = byteArray.size / 1024

                if (sizeInKB <= maxSizeKB || quality <= 10) {
                    // Guardar en archivo temporal
                    compressedFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(compressedFile).use { it.write(byteArray) }
                    break
                }

                quality -= 10
            } while (true)

            bitmap.recycle()
            return compressedFile

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    // Función principal de guardado
    // NOTA: Ya no necesitamos pasarle parámetros porque los lee del mismo ViewModel
    fun guardarCliente() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val context = getApplication<Application>().applicationContext

            try {
                val textType = "text/plain".toMediaTypeOrNull()

                // 1. Textos básicos (Usamos las variables de la clase)
                val nombreRB = nombre.toRequestBody(textType)
                val apellidosRB = apellidos.toRequestBody(textType)
                val dniRB = dni.toRequestBody(textType)
                val direccionRB = direccion.toRequestBody(textType)
                val tipoClienteRB = (if (tieneRuc) "Mayorista" else "Minorista").toRequestBody(textType)

                val rucRB = if (tieneRuc && ruc.isNotEmpty()) ruc.toRequestBody(textType) else null
                val razonSocialRB = if (tieneRuc && razonSocial.isNotEmpty()) razonSocial.toRequestBody(textType) else null
                val latRB = latitud?.toString()?.toRequestBody(textType)
                val lonRB = longitud?.toString()?.toRequestBody(textType)

                // Comprimir foto de perfil si existe
                val fotoPerfilComprimida = fotoPerfilUri?.let {
                    comprimirImagen(context, it, maxSizeKB = 1500) // 1.5 MB máximo
                }

                // Comprimir fotos de fachada
                val fotosComprimidas = fotosFachada.mapNotNull { uri ->
                    if (uri != null) {
                        comprimirImagen(context, uri, maxSizeKB = 800)
                    } // 800 KB cada una
                }

                // 2. TELÉFONOS (MAPA DE PARTES)
                val telefonosMap = mutableMapOf<String, RequestBody>()
                listaTelefonos.forEachIndexed { index, tel ->
                    telefonosMap["telefonos[$index][number]"] = tel.numero.toRequestBody(textType)
                    telefonosMap["telefonos[$index][description]"] = tel.tipo.toRequestBody(textType)
                }

                // 3. Foto Perfil
                var fotoPerfilPart: MultipartBody.Part? = null
                fotoPerfilUri?.let { uri ->
                    val file = UriUtils.fileFromContentUri(context, uri)
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    fotoPerfilPart = MultipartBody.Part.createFormData("fotoCliente", file.name, requestFile)
                }

                // 4. Fotos Fachada
                val listaFachadaParts = ArrayList<MultipartBody.Part>()
                fotosFachada.filterNotNull().forEachIndexed { index, uri ->
                    val file = UriUtils.fileFromContentUri(context, uri)
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("fotosFachada[]", "fachada_$index.jpg", requestFile)
                    listaFachadaParts.add(part)
                }

                // 5. Llamada a la API
                val response = RetrofitClient.apiService.crearCliente(
                    nombre = nombreRB,
                    apellidos = apellidosRB,
                    tipoCliente = tipoClienteRB,
                    dni = dniRB,
                    direccion = direccionRB,
                    ruc = rucRB,
                    razonSocial = razonSocialRB,
                    latitud = latRB,
                    longitud = lonRB,
                    fotoCliente = fotoPerfilPart,
                    fotosFachada = listaFachadaParts,
                    telefonos = telefonosMap
                )

                if (response.isSuccessful) {
                    _uiState.value = UiState.Success("¡Cliente creado exitosamente!")
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Error desconocido"
                    _uiState.value = UiState.Error("Error ${response.code()}: $errorMsg")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("Fallo de conexión: ${e.message}")
            }
        }
    }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val mensaje: String) : UiState()
    data class Error(val mensaje: String) : UiState()
}