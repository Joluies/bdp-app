package com.example.bdp_app.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bdp_app.domain.model.CartItem
import com.example.bdp_app.ui.theme.BdpTheme

@Composable
fun ProductoItemRow(
    item: CartItem,
    tipoCliente: String,
    onQtyChange: (Int) -> Unit
) {
    // Estado para saber si la imagen está expandida
    var isExpanded by remember { mutableStateOf(false) }

    // Calcular precio según cliente
    val precioFinal = if (tipoCliente.equals("Mayorista", ignoreCase = true)) {
        item.producto.precioMayorista
    } else {
        item.producto.precioUnitario
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize( // Animación suave al cambiar tamaño
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded } // Al tocar la tarjeta, se expande/contrae
        ) {
            // --- PARTE SUPERIOR (IMAGEN) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 480.dp else 160.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
// 1. CORRECCIÓN INTELIGENTE DE URL
                // Si la URL viene de la BD sin "/productos", se lo agregamos a la fuerza.
                val urlBase = item.producto.imagen
                val urlFinal = if (urlBase.contains("/productos/")) {
                    urlBase // Ya está bien
                } else {
                    // Reemplazamos ".../img/" por ".../img/productos/"
                    urlBase.replace("/storage/img/", "/storage/img/productos/")
                }

                // LOG PARA VERIFICAR (Debe salir igual que en tu navegador)
                android.util.Log.d("URL_CORREGIDA", "Cargando: $urlFinal")

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(urlFinal) // <--- Usamos la URL corregida
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_report_image)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .build(),
                    contentDescription = item.producto.nombre,
                    contentScale = if (isExpanded) ContentScale.Fit else ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                )
            }

            // --- PARTE INFERIOR (DATOS Y CONTROLES) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Textos
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Marca",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = item.producto.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BdpTheme.colors.DarkGreenBDP
                    )
                    Text(
                        text = item.producto.descripcion,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = if (isExpanded) Int.MAX_VALUE else 2 // Muestra todo si está expandido
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Cantidad: ${item.cantidad}",
                        fontSize = 14.sp
                    )
                }

                // Precio y Botones
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "S/ ${"%.2f".format(precioFinal)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(Modifier.height(8.dp))

                    // Controles de Cantidad
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (item.cantidad > 0) onQtyChange(item.cantidad - 1) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Remove, null)
                        }

                        Text(
                            text = "${item.cantidad}",
                            modifier = Modifier.padding(horizontal = 8.dp),
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = { onQtyChange(item.cantidad + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, null)
                        }
                    }
                }
            }
        }
    }
}