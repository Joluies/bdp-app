package com.example.bdp_app.ui.vendedor.pedido

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealizarPedidoScreen(
    navController: NavHostController,
    viewModel: RealizarPedidoViewModel = viewModel()
) {
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()

    // Calendario
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth -> viewModel.actualizarFechaEntrega(year, month, dayOfMonth) },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    datePickerDialog.datePicker.minDate = calendar.timeInMillis

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
            Surface(modifier = Modifier.fillMaxWidth(), shadowElevation = 8.dp, color = Color.White) {
                Column(Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold, color = BdpTheme.colors.DarkGreenBDP, fontSize = 20.sp)
                        Text("S/ ${"%.2f".format(viewModel.total)}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = BdpTheme.colors.DarkGreenBDP)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = viewModel.total > 0 && viewModel.clienteSeleccionado != null && uiState !is UiState.Loading
                    ) {
                        if (uiState is UiState.Loading) CircularProgressIndicator(color = Color.White) else Text("Haz un pedido", fontSize = 18.sp)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text("DATOS DEL CLIENTE", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                SeccionSeleccionClienteMejorada(viewModel)
                Spacer(Modifier.height(16.dp))

                Text("FECHA DE ENTREGA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                OutlinedCard(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = Color(0xFF1B5E20))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Fecha programada", fontSize = 12.sp, color = Color.Gray)
                            Text(viewModel.fechaEntregaSeleccionada, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Cambiar", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("SELECCIONE PRODUCTO", fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Text("(Toque la imagen para ver detalles)", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
            }

            // LISTA DE PRODUCTOS
            items(viewModel.productos) { item ->
                if (item.esBonificacion) {
                    // --- DISEÑO CARTA VERDE (REGALOS) ---
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("🎁", fontSize = 24.sp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.producto.nombre, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                Text(item.producto.descripcion, fontSize = 12.sp, color = Color(0xFF388E3C))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("${item.cantidad} UND", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("GRATIS", fontWeight = FontWeight.Bold, color = Color(0xFF388E3C), fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // --- DISEÑO NORMAL (CON IMAGEN) ---
                    ProductoItemRow(
                        item = item,
                        tipoCliente = viewModel.clienteSeleccionado?.tipoCliente ?: "Minorista",
                        onQtyChange = { nuevaCantidad ->
                            viewModel.actualizarCantidad(item.producto.idProducto, nuevaCantidad)
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(100.dp)) }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN MEJORADO
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { if (uiState !is UiState.Loading) showConfirmDialog = false },
            containerColor = Color.White,
            title = {
                Column {
                    Text("Resumen del Pedido", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(viewModel.clienteSeleccionado?.nombre ?: "", fontSize = 14.sp, color = Color.Gray)
                }
            },
            text = {
                Column(Modifier.fillMaxWidth()) {
                    val itemsEnCarrito = viewModel.productos.filter { it.cantidad > 0 }
                    Divider()
                    LazyColumn(Modifier.weight(1f, fill = false).heightIn(max = 300.dp)) {
                        items(itemsEnCarrito) { item ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // COLUMNA IZQUIERDA
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.producto.nombre,
                                        fontWeight = if (item.esBonificacion) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (item.esBonificacion) Color(0xFF2E7D32) else Color.Black
                                    )

                                    if (item.esBonificacion) {
                                        Text(item.producto.descripcion, fontSize = 10.sp, color = Color(0xFF2E7D32), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    } else {
                                        // --- NUEVA LÓGICA DE TEXTO (PAQTES vs PAQTE) ---
                                        val esPaquete = item.producto.presentacion.contains("x", true) || item.producto.presentacion.contains("paq", true)

                                        val unidadTexto = if (esPaquete) {
                                            if (item.cantidad > 1) "paqtes" else "paqte"
                                        } else {
                                            if (item.cantidad > 1) "unds" else "und"
                                        }

                                        Text("${item.cantidad} x $unidadTexto ${item.producto.presentacion}", fontSize = 12.sp, color = Color.Gray)
                                    }
                                }

                                // COLUMNA DERECHA (PRECIO CON REGLA 100)
                                val precioAplicado = if (!item.esBonificacion &&
                                    viewModel.clienteSeleccionado?.tipoCliente == "Mayorista" &&
                                    item.cantidad >= 100) // <--- REGLA 100 APLICADA
                                    item.producto.precioMayorista
                                else
                                    item.producto.precioUnitario

                                Text(
                                    text = if (item.esBonificacion) "GRATIS" else "S/ ${"%.2f".format(precioAplicado * item.cantidad)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (item.esBonificacion) Color(0xFF2E7D32) else Color.Black
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.3f))
                        }
                    }
                    Divider(Modifier.padding(vertical = 8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("Subtotal:", fontSize = 14.sp)
                        Text("S/ ${"%.2f".format(viewModel.subtotal)}", fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = Color(0xFF1B5E20))
                        Spacer(Modifier.width(4.dp))
                        Text("Entrega: ${viewModel.fechaEntregaSeleccionada}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1B5E20))
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("TOTAL:", fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text("S/ ${"%.2f".format(viewModel.total)}", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF1B5E20))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.enviarPedidoFinal { showConfirmDialog = false } },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7CB342)),
                    enabled = uiState !is UiState.Loading
                ) { Text("ENVIAR PEDIDO") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }, enabled = uiState !is UiState.Loading) { Text("Volver") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeccionSeleccionClienteMejorada(viewModel: RealizarPedidoViewModel) {
    var showBottomSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
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

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = Color.White
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Seleccionar Cliente", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar por nombre...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                val clientesFiltrados = viewModel.listaClientes.filter {
                    it.nombre.contains(searchText, ignoreCase = true) || it.apellidos.contains(searchText, ignoreCase = true)
                }
                LazyColumn(modifier = Modifier.fillMaxHeight(0.6f)) {
                    items(clientesFiltrados) { cliente ->
                        ListItem(
                            headlineContent = { Text("${cliente.nombre} ${cliente.apellidos}", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text(cliente.tipoCliente, color = Color.Gray) },
                            leadingContent = { Icon(Icons.Default.Person, null, tint = if (cliente.idCliente == viewModel.clienteSeleccionado?.idCliente) Color(0xFF1B5E20) else Color.Gray) },
                            modifier = Modifier.clickable {
                                viewModel.clienteSeleccionado = cliente
                                showBottomSheet = false
                                searchText = ""
                            }.padding(vertical = 4.dp)
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    }
                    if (clientesFiltrados.isEmpty()) {
                        item { Text("No se encontraron clientes", modifier = Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center, color = Color.Gray) }
                    }
                }
            }
        }
    }
}