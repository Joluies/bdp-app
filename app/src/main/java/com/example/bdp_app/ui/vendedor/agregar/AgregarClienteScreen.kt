package com.example.bdp_app.ui.vendedor.agregar

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bdp_app.ui.theme.BdpTheme
import com.google.android.gms.maps.model.LatLng
import java.io.File

// Clase de datos simple para la UI
data class TelefonoData(val numero: String, val tipo: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarClienteScreen(
    navController: NavHostController,
    viewModel: AgregarClienteViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados temporales para la cámara
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var tipoCaptura by remember { mutableStateOf("") }
    val tiposTelefono = listOf("Casa", "Personal", "Corporativo", "Oficina", "Local")

    // Observadores
    val uiState by viewModel.uiState.collectAsState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val locationResult = savedStateHandle?.getLiveData<LatLng>("location_data")?.observeAsState()?.value

    // --- CÁMARA (Lógica Actualizada para Max 2 Fachadas) ---
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) {
            when (tipoCaptura) {
                "perfil" -> viewModel.fotoPerfilUri = tempUri
                "fachada" -> if (viewModel.fotosFachada.size < 2) viewModel.fotosFachada.add(tempUri) // CAMBIO: Max 2
            }
        }
    }

    fun abrirCamara(tipo: String) {
        try {
            val directory = File(context.externalCacheDir, "camera_photos").apply { mkdirs() }
            val file = File.createTempFile("BDP_${System.currentTimeMillis()}", ".jpg", directory)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempUri = uri
            tipoCaptura = tipo
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error al abrir cámara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun launchCamera(tipo: String) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara(tipo)
        } else {
            Toast.makeText(context, "Se requiere permiso de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // --- EFECTOS ---
    LaunchedEffect(locationResult) {
        locationResult?.let {
            viewModel.latitud = it.latitude
            viewModel.longitud = it.longitude
            savedStateHandle?.remove<LatLng>("location_data")
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            Toast.makeText(context, (uiState as UiState.Success).mensaje, Toast.LENGTH_LONG).show()
            viewModel.resetState()
            navController.popBackStack()
        } else if (uiState is UiState.Error) {
            Toast.makeText(context, (uiState as UiState.Error).mensaje, Toast.LENGTH_LONG).show()
            viewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Cliente", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BdpTheme.colors.DarkGreenBDP)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(scrollState)) {

            // --- SECCIÓN FOTO PERFIL (FLOTANTE MEJORADO) ---
            Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    // Imagen Circular
                    Box(
                        Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .border(2.dp, BdpTheme.colors.DarkGreenBDP, CircleShape)
                            .clickable { launchCamera("perfil") }
                    ) {
                        if (viewModel.fotoPerfilUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(viewModel.fotoPerfilUri).build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(Icons.Default.Person, null, Modifier.align(Alignment.Center).size(60.dp), tint = Color.Gray)
                        }
                    }
                    // Botón Flotante de Cámara
                    Box(
                        Modifier
                            .padding(end = 4.dp, bottom = 4.dp)
                            .size(80.dp)
                            .shadow(4.dp, CircleShape)
                            .background(BdpTheme.colors.DarkGreenBDP, CircleShape)
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { launchCamera("perfil") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(50.dp))
                    }
                }
            }

            // DATOS BÁSICOS
            OutlinedTextField(value = viewModel.nombre, onValueChange = { viewModel.nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = viewModel.apellidos, onValueChange = { viewModel.apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = viewModel.tieneRuc, onCheckedChange = { viewModel.tieneRuc = it }, colors = CheckboxDefaults.colors(BdpTheme.colors.DarkGreenBDP))
                Text("¿Tiene RUC / Razón Social?")
            }

            if (viewModel.tieneRuc) {
                OutlinedTextField(value = viewModel.ruc, onValueChange = { viewModel.ruc = it }, label = { Text("RUC") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = viewModel.razonSocial, onValueChange = { viewModel.razonSocial = it }, label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }

            OutlinedTextField(value = viewModel.dni, onValueChange = { viewModel.dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = viewModel.direccion, onValueChange = { viewModel.direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))

            // TIPO CLIENTE
            Text("Tipo de Cliente", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP, modifier = Modifier.padding(top = 16.dp))
            Row {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = viewModel.esMayorista, onCheckedChange = { viewModel.esMayorista = it; if(it) viewModel.esMinorista = false }, colors = CheckboxDefaults.colors(BdpTheme.colors.DarkGreenBDP))
                    Text("Mayorista")
                }
                Spacer(Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = viewModel.esMinorista, onCheckedChange = { viewModel.esMinorista = it; if(it) viewModel.esMayorista = false }, colors = CheckboxDefaults.colors(BdpTheme.colors.DarkGreenBDP))
                    Text("Minorista")
                }
            }

            // TELÉFONOS
            Text("Teléfonos", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP, modifier = Modifier.padding(top = 16.dp))
            viewModel.listaTelefonos.forEachIndexed { index, tel ->
                Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(value = tel.numero, onValueChange = { viewModel.listaTelefonos[index] = tel.copy(numero = it) }, label = { Text("Número") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                    Spacer(Modifier.width(8.dp))

                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(onClick = { expanded = true }) { Text(tel.tipo.ifBlank { "Tipo" }) }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            tiposTelefono.forEach { tipo -> DropdownMenuItem(text = { Text(tipo) }, onClick = { viewModel.listaTelefonos[index] = tel.copy(tipo = tipo); expanded = false }) }
                        }
                    }
                    IconButton(onClick = { if (viewModel.listaTelefonos.size > 1) viewModel.listaTelefonos.removeAt(index) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                }
            }
            TextButton(onClick = { viewModel.listaTelefonos.add(TelefonoData("", "Personal")) }) { Icon(Icons.Default.Add, null); Text("Agregar Teléfono") }

            // --- SECCIÓN FOTOS FACHADA (MAX 2) ---
            Text("Fotos Fachada (${viewModel.fotosFachada.size}/2)", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP, modifier = Modifier.padding(top = 16.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.fotosFachada.forEachIndexed { index, uri ->
                    Box(Modifier.size(160.dp)) {
                        AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
                        IconButton(onClick = { viewModel.fotosFachada.removeAt(index) }, Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.White, CircleShape)) { Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp)) }
                    }
                }
                // CAMBIO: Mostrar botón solo si hay menos de 2 fotos
                if (viewModel.fotosFachada.size < 2) {
                    Box(Modifier.size(160.dp).background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp)).clickable { launchCamera("fachada") }, contentAlignment = Alignment.Center) { Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(80.dp)) }
                }
            }

            // --- SECCIÓN GPS (DISEÑO TIPO TARJETA) ---
            Spacer(Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), // Fondo verde muy claro
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "GPS",
                            tint = BdpTheme.colors.DarkGreenBDP,
                            modifier = Modifier.size(28.dp)
                        )
                        Column(Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Coordenadas GPS",
                                fontWeight = FontWeight.Bold,
                                color = BdpTheme.colors.DarkGreenBDP, // Color similar a la imagen (un poco beige/rosado claro) o gris oscuro
                                fontSize = 16.sp
                            )
                            if (viewModel.latitud != null && viewModel.longitud != null) {
                                Text(
                                    text = "${viewModel.latitud},\n${viewModel.longitud}",
                                    fontSize = 12.sp,
                                    color = BdpTheme.colors.DarkGreenBDP,
                                    lineHeight = 14.sp
                                )
                            } else {
                                Text(
                                    text = "No capturado",
                                    fontSize = 12.sp,
                                    color = BdpTheme.colors.DarkGreenBDP

                                )
                            }
                        }
                    }
                    Button(
                        onClick = { navController.navigate("map_location_screen") },
                        colors = ButtonDefaults.buttonColors(BdpTheme.colors.DarkGreenBDP),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 24.dp)
                    ) {
                        Text("Mapa")
                    }
                }
            }

            // BOTÓN GUARDAR
            Spacer(Modifier.height(32.dp))
            Button(onClick = { viewModel.guardarCliente() }, enabled = uiState !is UiState.Loading, modifier = Modifier.fillMaxWidth().height(55.dp), colors = ButtonDefaults.buttonColors(BdpTheme.colors.DarkGreenBDP), shape = RoundedCornerShape(12.dp)) {
                if (uiState is UiState.Loading) CircularProgressIndicator(color = Color.White)
                else Text("GUARDAR CLIENTE", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(50.dp))
        }
    }
}