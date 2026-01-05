package com.example.bdp_app.ui.components



import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapLocationScreen(navController: NavHostController) {
    // Coordenadas iniciales (Centro de Lima por defecto)
    val defaultLocation = LatLng(-12.0463, -77.0427)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ubicar Cliente", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B5E20))
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Obtenemos la ubicación donde quedó el centro del mapa
                    val resultLocation = cameraPositionState.position.target

                    // Enviamos el resultado de vuelta a la pantalla anterior
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("location_data", resultLocation)

                    navController.popBackStack()
                },
                containerColor = Color(0xFF1B5E20),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Check, null) },
                text = { Text("CONFIRMAR PUNTO") }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // COMPONENTE DE GOOGLE MAPS
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = true
                )
            )

            // PIN FIJO EN EL CENTRO DE LA PANTALLA
            // El usuario mueve el mapa, pero el pin nos indica el centro exacto
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .size(50.dp)
                    .align(Alignment.Center)
                    .padding(bottom = 25.dp) // Alinea la punta del pin al centro
            )

            // Pequeño indicador de sombra para precisión visual
            Box(
                modifier = Modifier
                    .size(5.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
                    .align(Alignment.Center)
            )
        }
    }
}