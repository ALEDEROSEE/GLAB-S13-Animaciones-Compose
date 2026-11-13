package com.example.lab13_animaciones.ejercicios

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.lab13_animaciones.ui.CuadroColor
import com.example.lab13_animaciones.ui.PantallaEjercicio

/**
 * Ejercicio 1 - paso 2: el mismo cuadro, ahora dentro de AnimatedVisibility.
 * Sin parametros usa las animaciones por defecto (fadeIn + expandIn / fadeOut + shrinkOut).
 */
@Composable
fun Ejercicio1Visibilidad(modifier: Modifier = Modifier) {
    var visible by rememberSaveable { mutableStateOf(true) }

    PantallaEjercicio(
        enunciado = "Un botón alterna la visibilidad de un cuadro de color.",
        modifier = modifier
    ) {
        Button(onClick = { visible = !visible }) {
            Text(if (visible) "Ocultar cuadro" else "Mostrar cuadro")
        }
        AnimatedVisibility(visible = visible) {
            CuadroColor(color = Color(0xFF1E88E5), texto = "Cuadro\nde color")
        }
    }
}
