package com.example.bdp_app.ui.rutas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

// Definimos los colores (si ya los tienes en Theme, úsalos desde ahí)
val DarkGreenBDP = Color(0xFF1B5E20)
val LightGreenCard = Color(0xFFC8E6C9)

@Composable
fun RutasScreen(
    navController: NavHostController,
    // Asegúrate de que el nombre del ViewModel coincida con tu clase (RutasViewModel o RealizarRutasViewModel)
    viewModel: RealizarRutasViewModel = viewModel()
) {
    val clientes = viewModel.listaClientesRuta

    Scaffold(
        bottomBar = {
            Button(
                onClick = { /* Lógica para iniciar recorrido */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkGreenBDP)
            ) {
                Text("Iniciar Recorrido", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // --- ENCABEZADO ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkGreenBDP)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Ruta San Juan 2:",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Prolongacion Navarro Cauper/\nQuiñones",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            // --- MAPA (Placeholder) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
            ) {
                Text("Mapa de la Zona", modifier = Modifier.align(Alignment.Center))
            }

            // --- TÍTULO DE LISTA ---
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = DarkGreenBDP)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Clientes en la ruta San Juan 2:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreenBDP
                    )
                    Text(
                        text = "${clientes.size} clientes",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- LISTA DE CLIENTES ---
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (viewModel.isLoading.value) {
                    item {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DarkGreenBDP)
                        }
                    }
                } else {
                    items(clientes) { cliente ->
                        ClienteRutaCard(
                            nombre = "${cliente.nombre} ${cliente.apellidos}",
                            codigo = cliente.codigoCliente,
                            direccion = cliente.direccion,
                            onClick = {
                                // Navegamos usando el ID del cliente
                                navController.navigate("ruta_detalle/${cliente.idCliente}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClienteRutaCard(
    nombre: String,
    codigo: String,
    direccion: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = LightGreenCard),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = DarkGreenBDP,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkGreenBDP)
                Text(text = codigo, fontSize = 14.sp, color = DarkGreenBDP)
                Text(text = direccion, fontSize = 14.sp, color = DarkGreenBDP, fontWeight = FontWeight.Medium)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Edit, contentDescription = "Ver Detalle", tint = DarkGreenBDP)
                Text(text = "Ver Detalle", fontSize = 10.sp, color = DarkGreenBDP)
            }
        }
    }
}

