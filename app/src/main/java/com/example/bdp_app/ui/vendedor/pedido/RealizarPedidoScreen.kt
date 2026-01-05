package com.example.bdp_app.ui.vendedor.pedido

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // IMPORTANTE: Sin esto falla la lista
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.bdp_app.ui.vendedor.agregar.UiState // Importa tu sellada de estados

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealizarPedidoScreen(
    navController: NavHostController,
    viewModel: RealizarPedidoViewModel = viewModel()
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Observamos el estado del envío para mostrar mensajes
    val uiState by viewModel.uiState.collectAsState()

    // Manejo de respuestas de la API
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1B5E20)
                )
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
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "S/ ${"%.2f".format(viewModel.total)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342)),
                        shape = RoundedCornerShape(12.dp),
                        // Solo habilitar si hay cliente y hay monto
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

                SeccionSeleccionCliente(viewModel)

                Spacer(Modifier.height(16.dp))
                Text("SELECCIONE PRODUCTO", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
                    onClick = {
                        viewModel.enviarPedidoFinal {
                            showConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342)),
                    enabled = uiState !is UiState.Loading
                ) {
                    Text("CONFIRMAR")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false },
                    enabled = uiState !is UiState.Loading
                ) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun SeccionSeleccionCliente(viewModel: RealizarPedidoViewModel) {
    var expanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = viewModel.clienteSeleccionado?.let { "${it.nombre} ${it.apellidos}" }
                        ?: "Seleccionar cliente...",
                    color = if (viewModel.clienteSeleccionado == null) Color.Gray else Color.Black
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            viewModel.listaClientes.forEach { cliente ->
                DropdownMenuItem(
                    text = { Text("${cliente.nombre} ${cliente.apellidos} - ${cliente.tipoCliente}") },
                    onClick = {
                        viewModel.clienteSeleccionado = cliente
                        expanded = false
                    }
                )
            }
        }
    }
}