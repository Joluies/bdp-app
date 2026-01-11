package com.example.bdp_app.ui.rutas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
// IMPORTANTE: Esta importación es la que soluciona el error de "Type mismatch"
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bdp_app.R
import com.example.bdp_app.domain.model.Cliente

const val BASE_URL_STORAGE = "https://api.bebidasdelperuapp.com/storage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutasScreen(
    navController: NavHostController,
    viewModel: RealizarRutasViewModel
) {
    // Ahora 'clientes' sí existe porque actualizamos el ViewModel
    val clientes by viewModel.clientes.observeAsState(emptyList())
    var searchText by remember { mutableStateOf("") }

    val clientesFiltrados = remember(clientes, searchText) {
        if (searchText.isBlank()) {
            clientes
        } else {
            clientes.filter { cliente -> // Usamos nombre explícito 'cliente' en lugar de 'it'
                cliente.nombre.contains(searchText, ignoreCase = true) ||
                        cliente.dni.contains(searchText) ||
                        (cliente.codigoCliente?.contains(searchText, ignoreCase = true) == true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rutas del Día", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // BUSCADOR
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    placeholder = { Text("Buscar por nombre, DNI o código") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }

            // LISTA DE CLIENTES
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // CORRECCIÓN: Usamos 'items(items = ...)' explícitamente
                items(items = clientesFiltrados, key = { it.idCliente }) { clienteObj ->
                    ClienteItem(cliente = clienteObj) {
                        navController.navigate("ruta_detalle_screen/${clienteObj.idCliente}")
                    }
                }
            }
        }
    }
}

@Composable
fun ClienteItem(
    cliente: Cliente,
    onClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // FOTO
            val fotoUrl = if (!cliente.fotoCliente.isNullOrBlank()) {
                if (cliente.fotoCliente.startsWith("http")) cliente.fotoCliente
                else "$BASE_URL_STORAGE/img/fotosCliente/${cliente.fotoCliente}"
            } else null

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(fotoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.logo_bdp),
                placeholder = painterResource(R.drawable.logo_bdp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // DATOS
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${cliente.nombre} ${cliente.apellidos}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Aquí usábamos 'it', pero ahora está dentro de 'cliente'
                Text(
                    text = "Cód: ${cliente.codigoCliente ?: "Pendiente"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = cliente.direccion,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = Color.DarkGray
                )
            }
        }
    }
}