package com.example.bdp_app.ui.vendedor.pedido

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.bdp_app.ui.components.ProductoItemRow
import com.example.bdp_app.ui.theme.BdpTheme
import com.example.bdp_app.ui.vendedor.agregar.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealizarPedidoScreen(
    navController: NavHostController,
    viewModel: RealizarPedidoViewModel = viewModel()
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Estado del envío
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de respuestas
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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Realizar un Pedido", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total",
                            fontWeight = FontWeight.Bold,
                            color = BdpTheme.colors.DarkGreenBDP,
                            fontSize = 20.sp)
                        Text(
                            "S/ ${"%.2f".format(viewModel.total)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = BdpTheme.colors.DarkGreenBDP
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = viewModel.total > 0 && viewModel.clienteSeleccionado != null && uiState !is UiState.Loading
                    ) {
                        if (uiState is UiState.Loading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Haz un pedido", color = Color.White, fontSize = 18.sp)
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("DATOS DEL CLIENTE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)

                // COMPONENTE MEJORADO DE SELECCIÓN
                SeccionSeleccionClienteMejorada(viewModel)

                Spacer(Modifier.height(16.dp))
                Text("SELECCIONE PRODUCTO", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text("(Toque un producto para ver detalles)", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
            }

            // LISTA DE PRODUCTOS
            items(viewModel.productos) { item ->
                ProductoItemRow(
                    item = item,
                    tipoCliente = viewModel.clienteSeleccionado?.tipoCliente ?: "Minorista",
                    onQtyChange = { viewModel.actualizarCantidad(item.producto.idProducto, it) }
                )
            }

            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN (Sin cambios mayores)
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (uiState !is UiState.Loading) showConfirmDialog = false },
            containerColor = Color.White,
            title = { Text("Confirmar Pedido", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Cliente: ${viewModel.clienteSeleccionado?.nombre} ${viewModel.clienteSeleccionado?.apellidos}")
                    Text("Tipo: ${viewModel.clienteSeleccionado?.tipoCliente}")
                    Divider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Subtotal:")
                        Text("S/ ${"%.2f".format(viewModel.subtotal)}")
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Impuestos (IGV):")
                        Text("S/ ${"%.2f".format(viewModel.impuestos)}")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total:", fontWeight = FontWeight.Bold)
                        Text("S/ ${"%.2f".format(viewModel.total)}", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.enviarPedidoFinal { showConfirmDialog = false } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342)),
                    enabled = uiState !is UiState.Loading
                ) { Text("CONFIRMAR") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }, enabled = uiState !is UiState.Loading) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}

// --- NUEVO COMPONENTE DE SELECCIÓN DE CLIENTE ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionSeleccionClienteMejorada(viewModel: RealizarPedidoViewModel) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        // Tarjeta que abre el BottomSheet
        OutlinedCard(
            onClick = { showBottomSheet = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (viewModel.clienteSeleccionado != null) {
                        Text(
                            text = "${viewModel.clienteSeleccionado!!.nombre} ${viewModel.clienteSeleccionado!!.apellidos}",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = viewModel.clienteSeleccionado!!.tipoCliente,
                            fontSize = 12.sp,
                            color = Color(0xFF1B5E20)
                        )
                    } else {
                        Text("Seleccionar cliente...", color = Color.Gray)
                    }
                }
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
    }

    // ModalBottomSheet: La mejor opción para listas largas
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Seleccionar Cliente",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Buscador dentro del BottomSheet
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar por nombre...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Lista filtrada
                val clientesFiltrados = viewModel.listaClientes.filter {
                    it.nombre.contains(searchText, ignoreCase = true) ||
                            it.apellidos.contains(searchText, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.6f) // Ocupa hasta el 60% de la pantalla
                ) {
                    items(clientesFiltrados) { cliente ->
                        ListItem(
                            headlineContent = { Text("${cliente.nombre} ${cliente.apellidos}", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(cliente.tipoCliente, color = Color.Gray) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Person,
                                    null,
                                    tint = if (cliente.idCliente == viewModel.clienteSeleccionado?.idCliente) Color(0xFF1B5E20) else Color.Gray
                                )
                            },
                            modifier = Modifier
                                .clickable {
                                    viewModel.clienteSeleccionado = cliente
                                    showBottomSheet = false // Cierra al seleccionar
                                    searchText = "" // Limpia búsqueda
                                }
                                .padding(vertical = 4.dp)
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }

                    if (clientesFiltrados.isEmpty()) {
                        item {
                            Text(
                                "No se encontraron clientes",
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                textAlign = TextAlign.Center,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}