package com.example.bdp_app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext


// Esquemas de colores oficiales
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

// Definición de tus colores personalizados
data class BdpColors(
    val DarkGreenBDP: Color,
    val verdeUno: Color,
    val verdeDos: Color,
    val LightGreenBDP: Color
)

// CompositionLocal para acceder a ellos
val LocalBdpColors = staticCompositionLocalOf {
    BdpColors(
        DarkGreenBDP = DarkGreenBDP,
        LightGreenBDP = LightGreenBDP,
        verdeUno = VerdeUno,
        verdeDos = VerdeDos

    )
}

object BdpTheme {
    val colors: BdpColors
        @Composable
        get() = LocalBdpColors.current
}

@Composable
fun BDPAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Proveemos tanto el esquema oficial como tus colores BDP
    CompositionLocalProvider(
        LocalBdpColors provides BdpColors(
            DarkGreenBDP = DarkGreenBDP,
            LightGreenBDP = LightGreenBDP,
           verdeUno = VerdeUno,
           verdeDos = VerdeDos

        )
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
