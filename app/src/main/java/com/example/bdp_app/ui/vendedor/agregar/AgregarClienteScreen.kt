package com.example.bdp_app.ui.vendedor.agregar

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.LatLng
import java.io.File
import androidx.lifecycle.viewmodel.compose.viewModel

// Clase de apoyo para teléfonos
data class TelefonoData(val numero: String, val tipo: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarClienteScreen(
    navController: NavHostController,
    viewModel: AgregarClienteViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados temporales solo para lógica de cámara (no necesitan persistir)
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var tipoCaptura by remember { mutableStateOf("") } // "perfil" o "fachada"

    // Lista estática para el dropdown
    val tiposTelefono = listOf("Casa", "Personal", "Corporativo", "Oficina", "Local")

    // --- LÓGICA DE UBICACIÓN ---
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val locationResult by savedStateHandle?.getLiveData<LatLng>("location_data")?.observeAsState() ?: remember { mutableStateOf(null) }

    // Observamos estado de carga
    val uiState by viewModel.uiState.collectAsState()

    // GUARDAR COORDENADAS EN VIEWMODEL AL VOLVER DEL MAPA
    LaunchedEffect(locationResult) {
        locationResult?.let {
            viewModel.latitud = it.latitude
            viewModel.longitud = it.longitude
            savedStateHandle?.remove<LatLng>("location_data")
        }
    }

    // MANEJO DE RESPUESTAS (ÉXITO/ERROR)
    LaunchedEffect(uiState) {
        when (uiState) {
            is UiState.Success -> {
                Toast.makeText(context, (uiState as UiState.Success).mensaje, Toast.LENGTH_LONG).show()
                viewModel.resetState()
                navController.popBackStack()
            }
            is UiState.Error -> {
                Toast.makeText(context, (uiState as UiState.Error).mensaje, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // --- LANZADORES (Cámara) ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            when (tipoCaptura) {
                // Guardamos directamente en el ViewModel
                "perfil" -> viewModel.fotoPerfilUri = tempUri
                "fachada" -> if (viewModel.fotosFachada.size < 3) viewModel.fotosFachada.add(tempUri)
            }
        }
    }

    fun actualLaunchCamera(tipo: String) {
        try {
            val directory = File(context.externalCacheDir, "camera_photos").apply { mkdirs() }
            val file = File.createTempFile("BDP_${System.currentTimeMillis()}", ".jpg", directory)
            val uri = FileProvider.getUriForFile(context, "com.example.bdp_app.fileprovider", file) // Asegúrate que coincida con manifest
            tempUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) actualLaunchCamera(tipoCaptura)
        else Toast.makeText(context, "Permiso denegado", Toast.LENGTH_SHORT).show()
    }

    fun checkAndLaunchCamera(tipo: String) {
        tipoCaptura = tipo
        val permissionCheckResult = androidx.core.content.ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.CAMERA
        )
        if (permissionCheckResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            actualLaunchCamera(tipo)
        } else {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Cliente", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SECCIÓN FOTO PERFIL (Usando viewModel)
            Text("Foto del Cliente", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Box(Modifier.size(110.dp)) {
                Surface(Modifier.fillMaxSize(), shape = CircleShape, color = Color.LightGray) {
                    if (viewModel.fotoPerfilUri != null) {
                        AsyncImage(model = viewModel.fotoPerfilUri, contentDescription = null, contentScale = ContentScale.Crop)
                    } else {
                        Icon(Icons.Default.Person, null, modifier = Modifier.padding(25.dp))
                    }
                }
                FloatingActionButton(
                    onClick = { checkAndLaunchCamera("perfil") },
                    modifier = Modifier.size(35.dp).align(Alignment.BottomEnd),
                    containerColor = Color(0xFF1B5E20)
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // CAMPOS DE TEXTO (Vinculados al ViewModel)
            OutlinedTextField(
                value = viewModel.nombre,
                onValueChange = { viewModel.nombre = it },
                label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.apellidos,
                onValueChange = { viewModel.apellidos = it },
                label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // RUC
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewModel.tieneRuc,
                    onCheckedChange = { viewModel.tieneRuc = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1B5E20))
                )
                Text("¿Cuenta con RUC / Razón Social?")
            }

            if (viewModel.tieneRuc) {
                OutlinedTextField(
                    value = viewModel.ruc,
                    onValueChange = { viewModel.ruc = it },
                    label = { Text("Número de RUC") }, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = viewModel.razonSocial,
                    onValueChange = { viewModel.razonSocial = it },
                    label = { Text("Razón Social") }, modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.dni,
                onValueChange = { viewModel.dni = it },
                label = { Text("DNI") }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = viewModel.direccion,
                onValueChange = { viewModel.direccion = it },
                label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

// SECCIÓN TIPO DE CLIENTE
            Text("Tipo de Cliente", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))

            Row (Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = viewModel.esMayorista,
                    onCheckedChange = { viewModel.esMayorista = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1B5E20))
                )
                Text("Mayorista")

                Spacer(Modifier.width(24.dp))

                Checkbox(
                    checked = viewModel.esMinorista,
                    onCheckedChange = { viewModel.esMinorista = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF1B5E20))
                )
                Text("Minorista")

                Spacer(Modifier.width(24.dp))
            }

            Spacer(Modifier.height(8.dp))



            // SECCIÓN TELÉFONOS (Vinculada al ViewModel)
            Text("Teléfonos de Contacto", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.fillMaxWidth())
            viewModel.listaTelefonos.forEachIndexed { index, tel ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    OutlinedTextField(
                        value = tel.numero,
                        onValueChange = { viewModel.listaTelefonos[index] = tel.copy(numero = it) },
                        label = { Text("Número") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(Modifier.width(8.dp))

                    var expTel by remember { mutableStateOf(false) }
                    Box(Modifier.weight(0.8f)) {
                        OutlinedButton(onClick = { expTel = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            Text(tel.tipo, fontSize = 11.sp)
                        }
                        DropdownMenu(expanded = expTel, onDismissRequest = { expTel = false }) {
                            tiposTelefono.forEach { tipo ->
                                DropdownMenuItem(text = { Text(tipo) }, onClick = {
                                    viewModel.listaTelefonos[index] = tel.copy(tipo = tipo)
                                    expTel = false
                                })
                            }
                        }
                    }
                    IconButton(onClick = { if (viewModel.listaTelefonos.size > 1) viewModel.listaTelefonos.removeAt(index) }) {
                        Icon(Icons.Default.Delete, null, tint = Color.Red)
                    }
                }
            }
            TextButton(onClick = { viewModel.listaTelefonos.add(TelefonoData("", "Personal")) }, modifier = Modifier.align(Alignment.Start)) {
                Icon(Icons.Default.Add, null)
                Text("Agregar otro teléfono")
            }

            Spacer(Modifier.height(24.dp))

            // SECCIÓN FOTOS FACHADA (Vinculada al ViewModel)
            Text("Fotos de la Fachada (${viewModel.fotosFachada.size}/3)", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    Box(
                        Modifier.size(100.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                            .clickable { if (i == viewModel.fotosFachada.size) checkAndLaunchCamera("fachada") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (i < viewModel.fotosFachada.size) {
                            AsyncImage(model = viewModel.fotosFachada[i], contentDescription = null, contentScale = ContentScale.Crop)
                            IconButton(onClick = { viewModel.fotosFachada.removeAt(i) }, modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(0.4f), CircleShape)) {
                                Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        } else if (i == viewModel.fotosFachada.size) {
                            Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // SECCIÓN UBICACIÓN (Vinculada al ViewModel)
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color(0xFF1B5E20))
                    Column(Modifier.weight(1f).padding(horizontal = 8.dp)) {
                        Text("Coordenadas GPS", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (viewModel.latitud != null)
                                "Lat: ${"%.4f".format(viewModel.latitud)}\nLon: ${"%.4f".format(viewModel.longitud)}"
                            else "No capturado",
                            fontSize = 12.sp
                        )
                    }
                    Button(onClick = { navController.navigate("map_location_screen") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) {
                        Text("Mapa")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Box(contentAlignment = Alignment.Center) {
                Button(
                    onClick = {
                        if (viewModel.nombre.isBlank() || viewModel.dni.isBlank()) {
                            Toast.makeText(context, "Nombre y DNI son obligatorios", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        // Llamada limpia: ya no enviamos argumentos, el ViewModel los tiene
                        viewModel.guardarCliente()
                    },
                    enabled = uiState !is UiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    if (uiState is UiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("ENVIANDO...", fontSize = 16.sp)
                    } else {
                        Text("GUARDAR CLIENTE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}