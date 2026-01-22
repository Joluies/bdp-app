package com.example.bdp_app.ui.vendedor.actualizar

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bdp_app.ui.theme.BdpTheme
import com.example.bdp_app.ui.vendedor.agregar.TelefonoData
import com.google.android.gms.maps.model.LatLng
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualizarClienteScreen(
    navController: NavHostController,
    viewModel: ActualizarClienteViewModel = viewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // --- ESTADOS LOCALES ---
    var criterioBusqueda by remember { mutableStateOf("") }
    // Opciones del Dropdown
    val opcionesBusqueda = listOf("DNI", "RUC", "Nombre", "Apellidos", "Razón Social")
    var opcionSeleccionada by remember { mutableStateOf(opcionesBusqueda[0]) }
    var menuExpandido by remember { mutableStateOf(false) }

    // Estados de Cámara
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    var tipoCaptura by remember { mutableStateOf("") }

    // Observadores del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val locationResult = savedStateHandle?.getLiveData<LatLng>("location_data")?.observeAsState()?.value

    // --- LÓGICA DE CÁMARA ---
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) {
            when (tipoCaptura) {
                "perfil" -> viewModel.fotoPerfilUri = tempUri
                "fachada" -> {
                    // Si estamos editando (reemplazando) una foto existente
                    if (viewModel.indiceFotoEdicion != -1) {
                        if (viewModel.indiceFotoEdicion < viewModel.fotosFachadaUrls.size) {
                            // Marcar la vieja para borrar en el server
                            val urlVieja = viewModel.fotosFachadaUrls[viewModel.indiceFotoEdicion]
                            viewModel.fotosParaBorrarDelServidor.add(urlVieja.substringAfterLast("/"))
                            // Quitar la URL vieja
                            viewModel.fotosFachadaUrls.removeAt(viewModel.indiceFotoEdicion)
                        }
                        // Agregar la nueva como Uri local
                        viewModel.fotosFachada.add(tempUri)
                        viewModel.indiceFotoEdicion = -1 // Resetear índice
                    } else {
                        // Agregando nueva foto
                        if ((viewModel.fotosFachadaUrls.size + viewModel.fotosFachada.size) < 2) {
                            viewModel.fotosFachada.add(tempUri)
                        }
                    }
                }
            }
        }
    }

    fun launchCamera(tipo: String) {
        try {
            val directory = File(context.externalCacheDir, "camera_photos").apply { mkdirs() }
            val file = File.createTempFile("BDP_${System.currentTimeMillis()}", ".jpg", directory)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            tempUri = uri
            tipoCaptura = tipo
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error al iniciar cámara: ${e.message}", Toast.LENGTH_SHORT).show()
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
        if (uiState is ActualizarClienteViewModel.UiState.Success) {
            Toast.makeText(context, (uiState as ActualizarClienteViewModel.UiState.Success).mensaje, Toast.LENGTH_LONG).show()
            viewModel.resetState()
            navController.popBackStack()
        } else if (uiState is ActualizarClienteViewModel.UiState.Error) {
            Toast.makeText(context, (uiState as ActualizarClienteViewModel.UiState.Error).mensaje, Toast.LENGTH_LONG).show()
            viewModel.resetState() // Resetear para permitir reintentos
        }
    }

    // --- UI ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualizar Cliente", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BdpTheme.colors.DarkGreenBDP),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {

            // --- 1. BUSCADOR (DROPDOWN + INPUT) ---
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)), // Verde muy claro
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Buscar Cliente", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))

                    Row(Modifier.fillMaxWidth()) {
                        // Dropdown Tipo
                        Box(Modifier.weight(0.4f)) {
                            OutlinedButton(
                                onClick = { menuExpandido = true },
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text(opcionSeleccionada, fontSize = 13.sp, maxLines = 1)
                                Icon(Icons.Default.ArrowDropDown, null)
                            }
                            DropdownMenu(expanded = menuExpandido, onDismissRequest = { menuExpandido = false }) {
                                opcionesBusqueda.forEach { opcion ->
                                    DropdownMenuItem(
                                        text = { Text(opcion) },
                                        onClick = {
                                            opcionSeleccionada = opcion
                                            menuExpandido = false
                                            criterioBusqueda = ""
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Input Texto
                        OutlinedTextField(
                            value = criterioBusqueda,
                            onValueChange = { criterioBusqueda = it },
                            label = { Text("Valor") },
                            modifier = Modifier.weight(0.6f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = if (opcionSeleccionada == "DNI" || opcionSeleccionada == "RUC") KeyboardType.Number else KeyboardType.Text,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(onSearch = {
                                focusManager.clearFocus()
                                viewModel.buscarCliente(opcionSeleccionada, criterioBusqueda)
                            })
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.buscarCliente(opcionSeleccionada, criterioBusqueda)
                        },
                        colors = ButtonDefaults.buttonColors(BdpTheme.colors.DarkGreenBDP),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("BUSCAR")
                    }
                }
            }

            // --- FORMULARIO (Solo visible si hay cliente cargado) ---
            if (viewModel.clienteId != null) {
                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(24.dp))

                // --- 2. FOTO PERFIL (FLOTANTE) ---
                Box(Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        // Círculo de Imagen
                        Box(
                            Modifier
                                .size(130.dp) // Tamaño estándar balanceado
                                .clip(CircleShape)
                                .background(Color.LightGray)
                                .border(2.dp, BdpTheme.colors.DarkGreenBDP, CircleShape)
                                .clickable { launchCamera("perfil") }
                        ) {
                            val request = if (viewModel.fotoPerfilUri != null) {
                                ImageRequest.Builder(context).data(viewModel.fotoPerfilUri).build()
                            } else if (viewModel.fotoPerfilUrl != null) {
                                ImageRequest.Builder(context).data("${viewModel.fotoPerfilUrl}?t=${System.currentTimeMillis()}").crossfade(true).build()
                            } else null

                            if (request != null) {
                                AsyncImage(model = request, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            } else {
                                Icon(Icons.Default.Person, null, Modifier.align(Alignment.Center).size(60.dp), tint = Color.Gray)
                            }
                        }
                        // Botón Flotante Cámara
                        Box(
                            Modifier
                                .padding(end = 4.dp, bottom = 4.dp)
                                .size(40.dp)
                                .shadow(4.dp, CircleShape)
                                .background(BdpTheme.colors.DarkGreenBDP, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable { launchCamera("perfil") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                // DATOS PERSONALES
                Text("Datos Generales", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP)
                OutlinedTextField(value = viewModel.nombre, onValueChange = { viewModel.nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = viewModel.apellidos, onValueChange = { viewModel.apellidos = it }, label = { Text("Apellidos") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(value = viewModel.dni, onValueChange = { viewModel.dni = it }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                OutlinedTextField(value = viewModel.direccion, onValueChange = { viewModel.direccion = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                OutlinedTextField(
                    value = viewModel.distritos,
                    onValueChange = { viewModel.distritos = it },
                    label = { Text("Distritos") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Tipo de Cliente", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = viewModel.esMayorista, onCheckedChange = { viewModel.esMayorista = it; if(it) viewModel.esMinorista = false }, colors = CheckboxDefaults.colors(BdpTheme.colors.DarkGreenBDP))
                    Text("Mayorista")
                    Spacer(Modifier.width(16.dp))
                    Checkbox(checked = viewModel.esMinorista, onCheckedChange = { viewModel.esMinorista = it; if(it) viewModel.esMayorista = false }, colors = CheckboxDefaults.colors(BdpTheme.colors.DarkGreenBDP))
                    Text("Minorista")
                }

                // TELÉFONOS
                // TELÉFONOS
                Spacer(Modifier.height(16.dp))
                Text("Teléfonos", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP)

                // Definimos los tipos disponibles (igual que en Agregar)
                val tiposTelefono = listOf("Casa", "Personal", "Corporativo", "Oficina", "Local")

                viewModel.listaTelefonos.forEachIndexed { index, tel ->
                    Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Input Número
                        OutlinedTextField(
                            value = tel.numero,
                            onValueChange = { viewModel.listaTelefonos[index] = tel.copy(numero = it) },
                            label = { Text("Número") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )

                        Spacer(Modifier.width(8.dp))

                        // --- AGREGADO: Selector de Tipo ---
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            OutlinedButton(onClick = { expanded = true }) {
                                Text(tel.tipo.ifBlank { "Tipo" }) // Muestra "Personal" o lo que venga de la BD
                            }
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                tiposTelefono.forEach { tipo ->
                                    DropdownMenuItem(
                                        text = { Text(tipo) },
                                        onClick = {
                                            viewModel.listaTelefonos[index] = tel.copy(tipo = tipo)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        // ----------------------------------

                        // Botón Borrar
                        IconButton(onClick = { if (viewModel.listaTelefonos.size > 1) viewModel.listaTelefonos.removeAt(index) }) {
                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                        }
                    }
                }
                TextButton(onClick = { viewModel.listaTelefonos.add(TelefonoData("", "Personal")) }) {
                    Icon(Icons.Default.Add, null)
                    Text("Agregar Teléfono")
                }
                TextButton(onClick = { viewModel.listaTelefonos.add(TelefonoData("", "Personal")) }) { Icon(Icons.Default.Add, null); Text("Agregar Teléfono") }


                // --- 3. FOTOS FACHADA (MAX 2 + EDICIÓN) ---
                val totalFotos = viewModel.fotosFachadaUrls.size + viewModel.fotosFachada.size
                Text("Fotos Fachada ($totalFotos/2)", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP, modifier = Modifier.padding(top = 16.dp))

                // DEFINIR ESTILOS COMUNES PARA QUE TODO MIDA IGUAL
                val imageSize = 160.dp
                val imageShape = RoundedCornerShape(16.dp)

                Row(
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // A. FOTOS EXISTENTES (URL) - Muestran Lápiz
                    viewModel.fotosFachadaUrls.forEachIndexed { index, url ->
                        Box(Modifier.size(imageSize)) { // <--- Tamaño unificado
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data("$url?t=${System.currentTimeMillis()}")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop, // Llenar cuadro
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(imageShape)
                                    .border(1.dp, Color.LightGray, imageShape)
                            )

                            // Botón Editar (Lápiz)
                            Box(
                                Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(4.dp)
                                    .size(28.dp)
                                    .background(BdpTheme.colors.DarkGreenBDP, CircleShape)
                                    .clickable {
                                        viewModel.prepararEdicionFoto(index)
                                        launchCamera("fachada")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }

                            // Botón Borrar (X)
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(24.dp)
                                    .background(Color.White, CircleShape)
                                    .clickable {
                                        viewModel.fotosParaBorrarDelServidor.add(url.substringAfterLast("/"))
                                        viewModel.fotosFachadaUrls.removeAt(index)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // B. FOTOS NUEVAS (URI) - Solo Borrar
                    viewModel.fotosFachada.filterNotNull().forEachIndexed { index, uri ->
                        Box(Modifier.size(imageSize)) { // <--- Tamaño unificado
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(imageShape)
                                    .border(1.dp, Color.LightGray, imageShape)
                            )
                            // Botón Borrar (X)
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(24.dp)
                                    .background(Color.White, CircleShape)
                                    .clickable { viewModel.fotosFachada.removeAt(index) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // C. BOTÓN AGREGAR (Solo si < 2)
                    if (totalFotos < 2) {
                        Box(
                            Modifier
                                .size(imageSize) // <--- Tamaño unificado
                                .background(Color(0xFFE0E0E0), imageShape)
                                .clip(imageShape)
                                .clickable {
                                    viewModel.prepararNuevaFoto()
                                    launchCamera("fachada")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AddAPhoto,
                                null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }
                } // Fin Row

                // --- 4. UBICACIÓN (DISEÑO TARJETA) ---
                Spacer(Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F9E9)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.LocationOn, "GPS", tint = BdpTheme.colors.DarkGreenBDP, modifier = Modifier.size(28.dp))
                            Column(Modifier.padding(start = 12.dp)) {
                                Text("Coordenadas GPS", fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 16.sp)
                                if (viewModel.latitud != null && viewModel.longitud != null) {
                                    Text(
                                        text = "${viewModel.latitud},\n${viewModel.longitud}",
                                        fontSize = 12.sp,
                                        color = BdpTheme.colors.DarkGreenBDP,
                                        lineHeight = 14.sp
                                    )
                                } else {
                                    Text("No capturado", fontSize = 12.sp, color = Color(0xFFFFCDD2))
                                }
                            }
                        }
                        Button(
                            onClick = { navController.navigate("map_location_screen") },
                            colors = ButtonDefaults.buttonColors(BdpTheme.colors.DarkGreenBDP),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Text("Cambiar")
                        }
                    }
                }

                // BOTÓN ACTUALIZAR
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { viewModel.actualizarCliente() },
                    enabled = uiState !is ActualizarClienteViewModel.UiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(BdpTheme.colors.DarkGreenBDP),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is ActualizarClienteViewModel.UiState.Loading) CircularProgressIndicator(color = Color.White)
                    else Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.height(50.dp))
            }
        }
    }
}
