package com.example.bdp_app.ui.rutas

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bdp_app.R

// Constante para la URL base de las imágenes (Asegúrate que coincida con tu backend)
// Si usas el emulador de Android: http://10.0.2.2:8000/storage
// Si es producción: https://api.bebidasdelperuapp.com/storage
const val BASE_URL_IMG = "https://api.bebidasdelperuapp.com/storage"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutaDetalleScreen(
    navController: NavHostController,
    clienteId: Int,
    viewModel: RealizarRutasViewModel
) {
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
                    modifier = Modifier.fillMaxWidth().height(180.dp).background(Color(0xFF1B5E20)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(150.dp).clip(CircleShape).background(Color.White))

                    // Manejo seguro de la URL de la foto
                    val fotoUrl = if (!cliente.fotoCliente.isNullOrBlank()) {
                        if (cliente.fotoCliente.startsWith("http")) cliente.fotoCliente
                        else "$BASE_URL_IMG/img/fotosCliente/${cliente.fotoCliente}"
                    } else null

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fotoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Foto Cliente",
                        modifier = Modifier.size(145.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.logo_bdp), // Imagen por defecto si falla
                        placeholder = painterResource(R.drawable.logo_bdp)
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
                    // AQUI ESTABA EL ERROR: Ahora ya existe en el modelo
                    Text("Codigo Cliente: ${cliente.codigoCliente ?: "Sin código"}")
                }
                Divider(color = Color.LightGray, thickness = 0.5.dp)
            }

            // 3. TELÉFONOS
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("Telefonos:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))

                    cliente.telefonos?.forEach { telefono ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                // .numero funciona gracias a @SerializedName("number")
                                Text(text = telefono.numero, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = telefono.description, color = Color.Gray, fontSize = 12.sp)
                            }
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL)
                                intent.data = Uri.parse("tel:${telefono.numero}")
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Phone, contentDescription = "Llamar", tint = Color(0xFF1B5E20))
                            }
                        }
                        if (telefono != cliente.telefonos.last()) {
                            Divider(color = Color.LightGray, thickness = 0.5.dp)
                        }
                    }
                }
                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }

            // 4. FOTOS FACHADA
            item {
                Column(Modifier.padding(16.dp)) {
                    Text("FOTO FACHADA", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                  //  if (!cliente.fotoFachadaResponse.isNullOrEmpty()) {
                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(200.dp)) {
                            LazyRow {
                               // items(cliente.fotosFachada) { foto ->
                                    // Construcción de URL segura para fachada
                                  //  val fachadaUrl = if (foto.foto.startsWith("http")) foto.foto
                                  //  else "$BASE_URL_IMG/img/fotosFachada/${foto.foto}"

                                   // AsyncImage(
                                     //   model = ImageRequest.Builder(context).data(fachadaUrl).crossfade(true).build(),
                                     //   contentDescription = "Fachada",
                                        //contentScale = ContentScale.Crop,
                                    //    modifier = Modifier.width(300.dp).fillMaxHeight().padding(end = 8.dp),
                                     //   error = painterResource(R.drawable.logo_bdp)
                                   // )
                                }
                            }
                        }
                 //   } else {
                        Text("No hay fotos de fachada disponibles", color = Color.Gray)
                    }

                 //   Spacer(Modifier.height(8.dp))
                 //   Row(verticalAlignment = Alignment.CenterVertically) {
                   //     Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                     //   Spacer(Modifier.width(4.dp))
                       // Text(cliente.direccion, color = Color.Gray)
                    }
                }
            }
       // }
  //  }
//}