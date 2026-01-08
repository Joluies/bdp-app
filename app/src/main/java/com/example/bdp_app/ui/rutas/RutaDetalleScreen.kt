package com.example.bdp_app.ui.rutas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage // Requiere Coil
import com.example.bdp_app.R
import com.example.bdp_app.domain.model.Cliente

// Constante para la URL base de las imágenes
const val BASE_URL_IMG = "https://api.bebidasdelperuapp.com/storage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutaDetalleScreen(
    navController: NavHostController,
    clienteId: Int,
    viewModel: RealizarRutasViewModel
) {
    // Buscamos el cliente en la memoria del ViewModel
    val cliente = viewModel.obtenerClientePorId(clienteId)
    val context = LocalContext.current

    if (cliente == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Cliente no encontrado") }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ruta Detalle", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // 1. FOTO DE PERFIL
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF1B5E20)), // Fondo verde superior
                    contentAlignment = Alignment.Center
                ) {
                    // Círculo blanco detrás de la foto
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )

                    // Imagen del cliente
                    AsyncImage(
                        model = cliente.fotoCliente, // Ya viene completo del API
                        contentDescription = "Foto Cliente",
                        modifier = Modifier
                            .size(145.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // 2. DATOS PERSONALES
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("Nombre y Apellido : ${cliente.nombre} ${cliente.apellidos}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("DNI : ${cliente.dni}")
                    Spacer(Modifier.height(4.dp))
                    Text("Codigo Cliente: ${cliente.codigoCliente}")
                }
                Divider(color = Color.LightGray, thickness = 0.5.dp)
            }

            // 3. TELÉFONOS (Lista Dinámica)
            item {
                Column(Modifier.padding(16.dp)) {
                    // Título de la sección
                    Text("Telefonos:", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Spacer(Modifier.height(8.dp))

                    // Lista de teléfonos
                    cliente.telefonos?.forEach { telefono ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Columna para número y descripción
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = telefono.numero,
                                    color = Color(0xFF1B5E20),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = telefono.description,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            // BOTÓN DE LLAMADA
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL)
                                intent.data = Uri.parse("tel:${telefono.numero}")
                                context.startActivity(intent)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Llamar",
                                    tint = Color(0xFF1B5E20)
                                )
                            }
                        }

                        if (telefono != cliente.telefonos?.last()) {
                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }
                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }

            // 4. VENDEDOR (Dato estático por ahora ya que no viene en JSON de cliente)
            item {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("VENDEDOR", fontWeight = FontWeight.Bold)
                    Text("Jose Diaz Rojas", color = Color.Gray) // Placeholder o dato global
                }
                Divider(color = Color.LightGray, thickness = 0.5.dp)
            }

            // 5. FOTOS FACHADA (Carrusel Horizontal)
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("FOTO FACHADA", fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(8.dp))

                    if (!cliente.fotosFachada.isNullOrEmpty()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        ) {
                            LazyRow {
                                items(cliente.fotosFachada) { foto ->
                                    AsyncImage(
                                        model = foto.foto, // ✅ URL completa del API
                                        contentDescription = "Fachada",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(300.dp)
                                            .fillMaxHeight()
                                            .padding(end = 8.dp),
                                        error = painterResource(R.drawable.logo_bdp), // Opcional: imagen de error
                                        placeholder = painterResource(R.drawable.logo_bdp) // Opcional: placeholder
                                    )
                                }
                            }
                        }
                    } else {
                        Text("No hay fotos de fachada disponibles", color = Color.Gray)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(cliente.direccion, color = Color.Gray)
                    }
                }
            }
        }
    }
}

