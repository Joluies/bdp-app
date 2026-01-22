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
    // Estado para expandir/contraer imagen
    var isExpanded by remember { mutableStateOf(false) }

    // 1. LÓGICA DE PRECIO VISUAL (Regla >= 100)
    // Solo mostramos precio mayorista si el cliente es Mayorista Y lleva >= 100
    val precioMostrar = if (tipoCliente.equals("Mayorista", true) && item.cantidad >= 100) {
        item.producto.precioMayorista
    } else {
        item.producto.precioUnitario
    }

    // 2. LÓGICA DE TEXTO (Pluralización inteligente)
    // Si la presentación tiene una "x" (ej: "12x500ml") asumimos que es paquete
    val esUnidad = item.producto.presentacion.contains("und", ignoreCase = true) ||
            item.producto.presentacion.contains("unidad", ignoreCase = true) ||
            item.producto.presentacion.contains("pz", ignoreCase = true)

    val unidadTexto = if (esUnidad) {
        // Caso: Es unidad explícita
        if (item.cantidad > 1) "unds" else "und"
    } else {
        // Caso: Es paquete (o cualquier otra cosa como "625ml")
        // Aquí entrará "0" y devolverá "paqte" correctamente
        if (item.cantidad > 1) "paqtes" else "paqte"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .animateContentSize(
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
                .clickable { isExpanded = !isExpanded }
        ) {
            // --- PARTE SUPERIOR (IMAGEN) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isExpanded) 450.dp else 140.dp) // Altura ajustada
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Corrección de URL si es necesario
                val urlImg = item.producto.imagen
                val urlFinal = if (urlImg.contains("http")) urlImg else "https://api.bebidasdelperuapp.com$urlImg"

                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(urlFinal)
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_report_image)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .build(),
                    contentDescription = item.producto.nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // --- PARTE INFERIOR (DATOS) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.producto.nombre,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = BdpTheme.colors.DarkGreenBDP
                    )
                    // TEXTO FORMATEADO: Ej "10 x paqtes PRESENTACION"
                    Text(
                        text = "${item.cantidad} x $unidadTexto ${item.producto.presentacion}",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Pqte: S/ ${"%.2f".format(precioMostrar)}",
                        fontSize = 12.sp,
                        color = Color(0xFF388E3C)
                    )
                }

                // Controles
                Column(horizontalAlignment = Alignment.End) {
                    // Subtotal de la línea
                    Text(
                        text = "S/ ${"%.2f".format(precioMostrar * item.cantidad)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (item.cantidad > 0) onQtyChange(item.cantidad - 1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            // Usamos el icono estándar Remove (-)
                            Icon(Icons.Default.Remove, contentDescription = "-")
                        }

                        Text(
                            text = "${item.cantidad}",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        IconButton(
                            onClick = { onQtyChange(item.cantidad + 1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "+")
                        }
                    }
                }
            }
        }
    }
}