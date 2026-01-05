package com.example.bdp_app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.bdp_app.domain.model.CartItem

@Composable
fun ProductoItemRow(
    item: CartItem,
    tipoCliente: String,
    onQtyChange: (Int) -> Unit
) {
    // Determinamos qué precio mostrar según el tipo de cliente
    val precioAMostrar = if (tipoCliente == "Mayorista")
        item.producto.precioMayorista
    else item.producto.precioUnitario

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto
            AsyncImage(
                model = item.producto.imagen,
                contentDescription = null,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(text = "Marca", fontSize = 12.sp, color = Color.Gray)
                Text(text = item.producto.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = item.producto.descripcion, fontSize = 12.sp, color = Color.Gray)
                Text(text = "Cantidad: ${item.cantidad.toString().padStart(2, '0')}", fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${"%.2f".format(precioAMostrar)} $",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // Selector de cantidad (+ / -)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (item.cantidad > 0) onQtyChange(item.cantidad - 1) }) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Text(text = item.cantidad.toString())
                    IconButton(onClick = { onQtyChange(item.cantidad + 1) }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}