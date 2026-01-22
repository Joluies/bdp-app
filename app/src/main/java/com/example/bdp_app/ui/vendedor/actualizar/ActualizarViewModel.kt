package com.example.bdp_app.ui.vendedor.actualizar

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bdp_app.core.network.RetrofitClient
import com.example.bdp_app.ui.vendedor.agregar.TelefonoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ActualizarClienteViewModel(application: Application) : AndroidViewModel(application) {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val mensaje: String) : UiState()
        data class Error(val mensaje: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    // Datos del Cliente
    var clienteId by mutableStateOf<String?>(null)
    var nombre by mutableStateOf("")
    var apellidos by mutableStateOf("")
    var dni by mutableStateOf("")
    var direccion by mutableStateOf("")
    var distritos by mutableStateOf("")
    var tieneRuc by mutableStateOf(false)
    var ruc by mutableStateOf("")
    var razonSocial by mutableStateOf("")
    var esMayorista by mutableStateOf(false)
    var esMinorista by mutableStateOf(false)
    var latitud by mutableStateOf<Double?>(null)
    var longitud by mutableStateOf<Double?>(null)

    // Listas e Imágenes
    val listaTelefonos = mutableStateListOf<TelefonoData>()

    // Foto Perfil (URI local para nueva, URL para existente)
    var fotoPerfilUri by mutableStateOf<Uri?>(null)
    var fotoPerfilUrl by mutableStateOf<String?>(null)

    // Fotos Fachada
    val fotosFachada = mutableStateListOf<Uri?>() // Nuevas fotos (URI)
    val fotosFachadaUrls = mutableStateListOf<String>() // Fotos existentes (URL)
    val fotosParaBorrarDelServidor = mutableStateListOf<String>() // Nombres de archivo a borrar
    var indiceFotoEdicion by mutableStateOf(-1) // Para saber si estamos editando una existente

    init {
        if (listaTelefonos.isEmpty()) listaTelefonos.add(TelefonoData("", "Personal"))
    }

    // Control de Edición de Fotos
    fun prepararEdicionFoto(index: Int) { indiceFotoEdicion = index }
    fun prepararNuevaFoto() { indiceFotoEdicion = -1 }

    // --- BUSCAR CLIENTE ---
    // --- BUSCAR CLIENTE (MEJORADO) ---
// --- BUSCAR CLIENTE (CORREGIDO) ---
    // --- BUSCAR CLIENTE (VERSIÓN ESTRICTA) ---
    // --- BUSCAR CLIENTE (VERSIÓN DINÁMICA) ---
    fun buscarCliente(tipoBusqueda: String, criterio: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                android.util.Log.d("BUSQUEDA", "Buscando: $criterio")

                // CAMBIO CLAVE: Ignoramos el tipoBusqueda por ahora.
                // Forzamos el uso de "search" porque es lo más estándar en Laravel/PHP.
                // Esto hará que la URL sea: .../api/customers?search=71415542
                val filtros = mapOf("search" to criterio)

                // 2. LLAMADA A LA API
                val response = RetrofitClient.apiService.buscarClientes(filtros)

                if (response.isSuccessful) {
                    val listaResultados = response.body()?.data?.data ?: emptyList()

                    // ... (El resto del código de selección sigue igual) ...
                    val encontrado = listaResultados.find {
                        (it.dni == criterio) ||
                                (it.ruc == criterio) ||
                                it.nombre.contains(criterio, true) ||
                                it.apellidos.contains(criterio, true)
                    } ?: listaResultados.firstOrNull()

                    if (encontrado != null) {
                        limpiarFormulario()

                        // Llenado de datos (Igual que antes)
                        clienteId = encontrado.idCliente.toString()
                        nombre = encontrado.nombre
                        apellidos = encontrado.apellidos
                        dni = encontrado.dni ?: ""
                        direccion = encontrado.direccion
                        distritos = encontrado.distritos ?: ""
                        tieneRuc = !encontrado.ruc.isNullOrBlank()
                        ruc = encontrado.ruc ?: ""
                        razonSocial = encontrado.razonSocial ?: ""
                        esMayorista = encontrado.tipoCliente.equals("Mayorista", true)
                        esMinorista = !esMayorista

                        encontrado.coordenadas?.let {
                            latitud = it.latitud
                            longitud = it.longitud
                        }

                        listaTelefonos.clear()
                        encontrado.telefonos?.forEach {
                            listaTelefonos.add(TelefonoData(it.numero, it.description))
                        }
                        if (listaTelefonos.isEmpty()) listaTelefonos.add(TelefonoData("", "Personal"))

                        cargarFotosFachada(encontrado.idCliente)
                        fotoPerfilUrl = encontrado.fotoCliente

                        _uiState.value = UiState.Idle
                    } else {
                        _uiState.value = UiState.Error("No se encontró: $criterio")
                    }
                } else {
                    _uiState.value = UiState.Error("Error servidor: ${response.code()}")
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Conexión: ${e.message}")
            }
        }
    }

    // --- CARGAR FOTOS FACHADA (Endpoint Separado) ---
    fun cargarFotosFachada(clienteId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getFotosFachada(clienteId)
                if (response.isSuccessful && response.body() != null) {
                    val respuestaApi = response.body()!!

                    // Solo procesamos si success es true
                    if (respuestaApi.success) {
                        fotosFachadaUrls.clear()

                        // Recorremos la lista que está dentro de 'data'
                        respuestaApi.data.forEach { item ->
                            // Agregamos la URL que viene en el campo 'foto'
                            fotosFachadaUrls.add(item.foto)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- ACTUALIZAR CLIENTE (LÓGICA DE 3 PASOS) ---
    fun actualizarCliente() {
        val idStr = clienteId ?: return
        val id = idStr.toIntOrNull() ?: return

        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                android.util.Log.d("UPDATE_DEBUG", "Enviando ID: $id")
                android.util.Log.d("UPDATE_DEBUG", "Valor Distrito: '$distritos'")
                // --------------------------------
                // --- PREPARAR DATOS (Lógica de Nulos) ---
                // CORRECCIÓN: Si el texto está vacío, enviamos NULL para que Laravel ignore la validación
                val methodPart = createPartFromString("PUT")

                val nombrePart = if (nombre.isNotBlank()) createPartFromString(nombre) else null
                val apellidosPart = if (apellidos.isNotBlank()) createPartFromString(apellidos) else null
                val dniPart = if (dni.isNotBlank()) createPartFromString(dni) else null
                val direccionPart = if (direccion.isNotBlank()) createPartFromString(direccion) else null
                val distritoPart = if (distritos.isNotBlank()) createPartFromString(distritos) else null
                val tipoStr = if (esMayorista) "Mayorista" else "Minorista"
                val tipoPart = createPartFromString(tipoStr)

                // RUC y Razón Social (Validamos que tenga check Y texto)
                val rucPart = if (tieneRuc && ruc.isNotBlank()) createPartFromString(ruc) else null
                val razonSocialPart = if (tieneRuc && razonSocial.isNotBlank()) createPartFromString(razonSocial) else null

                // --- 1. MAPA DE DATOS (Coordenadas + Teléfonos) ---
                val dataMap = mutableMapOf<String, RequestBody>()

                // A. COORDENADAS
                if (latitud != null && longitud != null) {
                    dataMap["coordenadas[latitud]"] = createPartFromString(latitud.toString())
                    dataMap["coordenadas[longitud]"] = createPartFromString(longitud.toString())
                }

                // B. TELÉFONOS
                listaTelefonos.forEachIndexed { index, tel ->
                    if (tel.numero.isNotEmpty()) {
                        dataMap["telefonos[$index][number]"] = createPartFromString(tel.numero)
                        // Si no eligió tipo, ponemos "Personal" por defecto
                        val tipoFinal = if (tel.tipo.isBlank()) "Personal" else tel.tipo
                        dataMap["telefonos[$index][description]"] = createPartFromString(tipoFinal)
                    }
                }

                // --- 2. FOTO PERFIL ---
                val fotoPerfilPart = fotoPerfilUri?.let { uri ->
                    prepareFilePart(getApplication(), "fotoCliente", uri)
                }

                // --- 3. LLAMADA AL SERVIDOR ---
                val responseMain = RetrofitClient.apiService.actualizarCliente(
                    id = id,
                    method = methodPart,
                    nombre = nombrePart,
                    apellidos = apellidosPart,
                    dni = dniPart,         // <--- Ahora viaja como null si está vacío
                    direccion = direccionPart,
                    distritos = distritoPart,
                    tipoCliente = tipoPart,
                    ruc = rucPart,         // <--- Ahora viaja como null si está vacío
                    razonSocial = razonSocialPart,
                    coordenadas = null,    // Null aquí porque ya van en dataMap
                    dataMap = dataMap,
                    fotoPerfil = fotoPerfilPart
                )

                if (!responseMain.isSuccessful) {
                    val errorMsg = responseMain.errorBody()?.string() ?: "Error desconocido"
                    // Log para ver el error real en la consola
                    android.util.Log.e("ERROR_API", "El servidor respondió: $errorMsg")
                    _uiState.value = UiState.Error("Error al guardar: $errorMsg")
                    return@launch
                }

                // --- 4. GESTIÓN DE FOTOS DE FACHADA (POST / DELETE) ---

                // Subir nuevas
                fotosFachada.filterNotNull().forEach { uri ->
                    val fotoPart = prepareFilePart(getApplication(), "foto", uri)
                    if (fotoPart != null) {
                        try {
                            RetrofitClient.apiService.subirFotoFachada(id, fotoPart)
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }

                // Borrar viejas
                fotosParaBorrarDelServidor.forEach { filename ->
                    try {
                        RetrofitClient.apiService.eliminarFotoFachada(id, filename)
                    } catch (e: Exception) { e.printStackTrace() }
                }

                _uiState.value = UiState.Success("¡Cliente actualizado correctamente!")

                // Limpiar y recargar
                cargarFotosFachada(id)
                limpiarListasLocales()

            } catch (e: Exception) {
                _uiState.value = UiState.Error("Error de conexión: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // --- UTILIDADES ---

    private fun createPartFromString(text: String): RequestBody {
        return RequestBody.create("text/plain".toMediaTypeOrNull(), text)
    }

    // Función de compresión (CRUCIAL para evitar crash de memoria)
    private fun prepareFilePart(context: Context, partName: String, fileUri: Uri): MultipartBody.Part? {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream) ?: return null
            inputStream.close()

            // 1. Redimensionar si es muy grande
            val scaledBitmap = if (originalBitmap.width > 1024 || originalBitmap.height > 1024) {
                Bitmap.createScaledBitmap(originalBitmap, 1024, 1024, true)
            } else originalBitmap

            // 2. Comprimir a JPEG
            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // 3. Crear archivo temporal
            val tempFile = File(context.cacheDir, "temp_up_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { it.write(byteArray) }

            // 4. Crear RequestBody
            val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun limpiarListasLocales() {
        fotosFachada.clear()
        fotosParaBorrarDelServidor.clear()
        fotoPerfilUri = null
    }

    fun resetState() { _uiState.value = UiState.Idle }

    fun limpiarFormulario() {
        clienteId = null; nombre = ""; apellidos = ""; dni = ""; direccion = ""
        distritos = ""
        tieneRuc = false; ruc = ""; razonSocial = ""
        esMayorista = false; esMinorista = false
        latitud = null; longitud = null
        listaTelefonos.clear(); listaTelefonos.add(TelefonoData("", "Personal"))
        fotoPerfilUri = null; fotoPerfilUrl = null
        fotosFachada.clear(); fotosFachadaUrls.clear(); fotosParaBorrarDelServidor.clear()
    }
}