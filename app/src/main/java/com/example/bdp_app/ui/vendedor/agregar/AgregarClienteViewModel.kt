package com.example.bdp_app.ui.vendedor.agregar

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdp_app.core.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.FileOutputStream

class AgregarClienteViewModel(application: Application) : AndroidViewModel(application) {

    // --- ESTADOS DE DATOS ---
    var nombre by mutableStateOf("")
    var apellidos by mutableStateOf("")
    var dni by mutableStateOf("")
    var direccion by mutableStateOf("")

    var tieneRuc by mutableStateOf(false)
    var ruc by mutableStateOf("")
    var razonSocial by mutableStateOf("")

    var esMayorista by mutableStateOf(false)
    var esMinorista by mutableStateOf(false)

    var latitud by mutableStateOf<Double?>(null)
    var longitud by mutableStateOf<Double?>(null)

    // Listas
    val listaTelefonos = mutableStateListOf(TelefonoData("", "Personal"))

    // Fotos
    var fotoPerfilUri by mutableStateOf<Uri?>(null)
    val fotosFachada = mutableStateListOf<Uri?>()

    // Estados de UI
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun resetState() { _uiState.value = UiState.Idle }

    // --- LÓGICA PRINCIPAL ---
    fun guardarCliente() {
        viewModelScope.launch(Dispatchers.IO) { // Hilo secundario para no congelar la UI
            // 1. Validaciones Locales
            if (nombre.isBlank() || dni.isBlank()) {
                _uiState.value = UiState.Error("Nombre y DNI son obligatorios")
                return@launch
            }
            if (!esMayorista && !esMinorista) {
                _uiState.value = UiState.Error("Seleccione el tipo de cliente")
                return@launch
            }

            _uiState.value = UiState.Loading

            try {
                // 2. Preparar Textos Simples
                val nombreBody = createPartFromString(nombre)
                val apellidosBody = createPartFromString(apellidos)
                val dniBody = createPartFromString(dni)
                val direccionBody = createPartFromString(direccion)
                val tipoClienteBody = createPartFromString(if (esMayorista) "Mayorista" else "Minorista")

                val rucBody = if (tieneRuc) createPartFromString(ruc) else null
                val razonBody = if (tieneRuc) createPartFromString(razonSocial) else null

                // 3. Construir Mapa de Datos Complejos (AQUÍ SE ARREGLAN LAS COORDENADAS)
                val dataMap = buildDataMap()

                // 4. Preparar Fotos (Sin compresión manual pesada, solo copia)
                val perfilPart = fotoPerfilUri?.let { prepareFilePart(getApplication(), "fotoCliente", it) }

                val fachadaParts = fotosFachada.filterNotNull().map {
                    prepareFilePart(getApplication(), "fotosFachada[]", it)!!
                }

                // 5. Llamada a la API
                val response = RetrofitClient.apiService.crearCliente(
                    nombre = nombreBody,
                    apellidos = apellidosBody,
                    tipoCliente = tipoClienteBody,
                    dni = dniBody,
                    direccion = direccionBody,
                    ruc = rucBody,
                    razonSocial = razonBody,
                    dataMap = dataMap, // <--- Enviamos coordenadas y teléfonos aquí
                    fotoCliente = perfilPart,
                    fotosFachada = fachadaParts
                )

                if (response.isSuccessful) {
                    _uiState.value = UiState.Success("¡Cliente creado exitosamente!")
                    limpiarFormulario()
                } else {
                    // --- CORRECCIÓN DE SEGURIDAD ---
                    // No leemos todo el string porque si es un HTML gigante de Laravel, la app explota.
                    // Leemos solo un fragmento seguro.
                    val errorBody = try {
                        response.errorBody()?.string()?.take(200) ?: "Error desconocido"
                    } catch (e: Exception) {
                        "Error al leer respuesta del servidor"
                    }

                    _uiState.value = UiState.Error("Error ${response.code()}: $errorBody...")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = UiState.Error("Fallo de conexión: ${e.message}")
            }
        }
    }

    // --- HELPERS IMPORTANTES ---

    // Esta función estructura los datos como Laravel los espera: coordenadas[latitud]
    private fun buildDataMap(): Map<String, RequestBody> {
        val map = mutableMapOf<String, RequestBody>()

        // 1. Coordenadas
        if (latitud != null && longitud != null) {
            map["coordenadas[latitud]"] = createPartFromString(latitud.toString())
            map["coordenadas[longitud]"] = createPartFromString(longitud.toString())
        }

        // 2. Teléfonos
        listaTelefonos.forEachIndexed { index, tel ->
            if (tel.numero.isNotBlank()) {
                map["telefonos[$index][number]"] = createPartFromString(tel.numero)
                map["telefonos[$index][description]"] = createPartFromString(tel.tipo)
            }
        }
        return map
    }

    private fun createPartFromString(text: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), text)
    }

    private fun prepareFilePart(context: Context, partName: String, fileUri: Uri): MultipartBody.Part? {
        return try {
            android.util.Log.d("COMPRESSION", "🔴 prepareFilePart INICIO: partName=$partName, uri=$fileUri")

            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            android.util.Log.d("COMPRESSION", "✅ Bitmap decodificado: ${bitmap?.width}x${bitmap?.height}")

            // COMPRIMIR según tipo de foto
            val compressedBitmap = if (partName == "fotoCliente") {
                android.util.Log.d("COMPRESSION", "📸 Comprimiendo PERFIL a 300x300")
                Bitmap.createScaledBitmap(bitmap!!, 300, 300, true)
            } else {
                android.util.Log.d("COMPRESSION", "🏪 Comprimiendo FACHADA a 600x600")
                Bitmap.createScaledBitmap(bitmap!!, 600, 600, true)
            }

            val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.webp")
            val outputStream = FileOutputStream(tempFile)

            val quality = if (partName == "fotoCliente") 50 else 60
            android.util.Log.d("COMPRESSION", "🗜️ Comprimiendo a WebP con quality=$quality")
            compressedBitmap.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
            outputStream.close()

            android.util.Log.d("COMPRESSION", "✅ Archivo guardado: ${tempFile.length()} bytes (${tempFile.length() / 1024} KB)")

            val requestFile = RequestBody.create("image/webp".toMediaTypeOrNull(), tempFile)
            MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
        } catch (e: Exception) {
            android.util.Log.e("COMPRESSION", "❌ ERROR: ${e.message}", e)
            e.printStackTrace()
            null
        }
    }

    private fun limpiarFormulario() {
        nombre = ""; apellidos = ""; dni = ""; direccion = ""
        tieneRuc = false; ruc = ""; razonSocial = ""
        esMayorista = false; esMinorista = false
        latitud = null; longitud = null
        fotoPerfilUri = null
        fotosFachada.clear()
        listaTelefonos.clear()
        listaTelefonos.add(TelefonoData("", "Personal"))
    }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val mensaje: String) : UiState()
    data class Error(val mensaje: String) : UiState()
}