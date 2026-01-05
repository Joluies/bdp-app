package com.example.bdp_app.ui.auth

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bdp_app.R
import com.example.bdp_app.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    // 1. Definimos el valor de la escala inicial (0f = invisible/minúsculo)
    val scale = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // 2. Ejecutamos la animación de escala hasta 1f (tamaño original)
        // Usamos un tween de 1000ms para que sea suave
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000)
        )

        // Esperamos el tiempo restante para completar los 3 segundos totales
        delay(2000)

        navController.navigate(Screen.Login.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_bdp),
                contentDescription = "Bebidas del Perú Logo",
                modifier = Modifier
                    .size(450.dp)
                    .scale(scale.value) // 3. Aplicamos el valor animado aquí
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "App móvil\nVentas y Despacho",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 35.sp
                )
            )
        }
    }
}