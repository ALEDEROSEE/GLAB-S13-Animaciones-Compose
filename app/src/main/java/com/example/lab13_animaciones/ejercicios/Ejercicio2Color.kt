package com.example.lab13_animaciones.ejercicios

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

private val Azul = Color(0xFF1565C0)
private val Verde = Color(0xFF2E7D32)

/** Ejercicio 2 - paso 1: un boton cambia el fondo del cuadro entre azul y verde (de golpe). */
@Composable
fun Ejercicio2Color(modifier: Modifier = Modifier) {
    var esAzul by rememberSaveable { mutableStateOf(true) }

    PantallaEjercicio(
        enunciado = "El botón cambia el color de fondo del cuadro entre azul y verde.",
        modifier = modifier
    ) {
        Button(onClick = { esAzul = !esAzul }) { Text("Cambiar color") }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(if (esAzul) Azul else Verde, RoundedCornerShape(16.dp))
        )
    }
}
