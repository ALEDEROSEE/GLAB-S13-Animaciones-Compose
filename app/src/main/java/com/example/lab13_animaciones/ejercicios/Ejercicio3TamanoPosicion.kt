package com.example.lab13_animaciones.ejercicios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.ui.PantallaEjercicio

/** Ejercicio 3 - paso 1: el boton mueve y agranda el cuadro, todavia de golpe. */
@Composable
fun Ejercicio3TamanoPosicion(modifier: Modifier = Modifier) {
    var movido by rememberSaveable { mutableStateOf(false) }
    val tamano = if (movido) 120.dp else 64.dp
    val desplazamiento = if (movido) 170.dp else 0.dp

    PantallaEjercicio(
        enunciado = "El botón mueve el cuadro a la derecha y lo agranda.",
        modifier = modifier
    ) {
        Button(onClick = { movido = !movido }) {
            Text(if (movido) "Volver" else "Mover y agrandar")
        }
        Box(
            modifier = Modifier
                .offset(x = desplazamiento)
                .size(tamano)
                .background(Color(0xFFEF6C00), RoundedCornerShape(12.dp))
        )
    }
}
