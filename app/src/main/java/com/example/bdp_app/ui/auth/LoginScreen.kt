package com.example.bdp_app.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.bdp_app.R
import com.example.bdp_app.navigation.Screen

@Composable
fun LoginScreen(navController: NavHostController) {
    var usuario by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("vendedor") } // "vendedor" o "repartidor"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo pequeño en la parte superior del login
        Image(
            painter = painterResource(id = R.drawable.logo_bdp),
            contentDescription = "Logo BDP",
            modifier = Modifier.size(330.dp)
        )

        Text(
            text = "Bienvenido",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )

        Text(
            text = "Inicia sesión para continuar",
            fontSize = 18.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo de Usuario
        OutlinedTextField(
            value = usuario,
            onValueChange = { usuario = it },
            label = { Text("Usuario") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1B5E20),
                focusedLabelColor = Color(0xFF1B5E20)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(image, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1B5E20),
                focusedLabelColor = Color(0xFF1B5E20)
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Selector de Rol (Vendedor / Repartidor)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == "vendedor",
                    onClick = { selectedRole = "vendedor" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1B5E20))
                )
                Text("Vendedor")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = selectedRole == "repartidor",
                    onClick = { selectedRole = "repartidor" },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF1B5E20))
                )
                Text("Repartidor")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))


        // Botón de Ingreso
        Button(
            onClick = {
                // Validación básica local
                val userClean = usuario.trim()
                val passClean = password.trim()
                // Dentro del onClick del botón de Login
                if (selectedRole == "vendedor" && userClean == "vendedor1" && password == "123") {
                    navController.navigate("home/vendedor") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else if (selectedRole == "repartidor" && passClean == "repa1" && password == "123") {
                    navController.navigate("home/repartidor") {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }

                } else {
                    // Aquí podrías mostrar un Toast o un mensaje de error
                    println("Fallo de login: Usuario='$userClean', Rol='$selectedRole'")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
        ) {
            Text("INGRESAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}