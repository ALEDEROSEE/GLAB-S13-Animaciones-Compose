package com.example.lab13_animaciones.ejercicios

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.ui.CuadroColor
import com.example.lab13_animaciones.ui.Nota
import com.example.lab13_animaciones.ui.PantallaEjercicio
import com.example.lab13_animaciones.ui.Subtitulo

/** Duraciones elegidas a proposito largas para que el efecto se note en el video. */
private const val DURACION_ENTRADA = 900
private const val DURACION_SALIDA = 700

/** Combinaciones de entrada/salida que se pueden probar desde la pantalla. */
private enum class Efecto(val etiqueta: String) {
    FADE("fade"),
    FADE_EXPAND("fade + expand"),
    SLIDE("slide + fade")
}

private fun entrada(efecto: Efecto): EnterTransition = when (efecto) {
    Efecto.FADE -> fadeIn(animationSpec = tween(DURACION_ENTRADA))
    Efecto.FADE_EXPAND -> fadeIn(tween(DURACION_ENTRADA)) + expandVertically(tween(DURACION_ENTRADA))
    Efecto.SLIDE -> slideInHorizontally(tween(DURACION_ENTRADA)) { ancho -> -ancho } + fadeIn(tween(DURACION_ENTRADA))
}

private fun salida(efecto: Efecto): ExitTransition = when (efecto) {
    Efecto.FADE -> fadeOut(animationSpec = tween(DURACION_SALIDA))
    Efecto.FADE_EXPAND -> fadeOut(tween(DURACION_SALIDA)) + shrinkVertically(tween(DURACION_SALIDA))
    Efecto.SLIDE -> slideOutHorizontally(tween(DURACION_SALIDA)) { ancho -> ancho } + fadeOut(tween(DURACION_SALIDA))
}

/**
 * Ejercicio 1 - paso 3: entrada y salida configuradas con fadeIn / fadeOut (y dos combinaciones
 * extra). Debajo dejo el mismo cuadro con un if normal para comparar los dos comportamientos.
 */
@Composable
fun Ejercicio1Visibilidad(modifier: Modifier = Modifier) {
    var visible by rememberSaveable { mutableStateOf(true) }
    var efecto by rememberSaveable { mutableStateOf(Efecto.FADE) }

    PantallaEjercicio(
        enunciado = "Un botón alterna la visibilidad de dos cuadros: el azul usa AnimatedVisibility " +
            "y el rojo un if normal. La barra gris de abajo de cada uno muestra cómo se mueve el resto de la pantalla.",
        modifier = modifier
    ) {
        Button(onClick = { visible = !visible }) {
            Icon(
                if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = null
            )
            Text(
                if (visible) "Ocultar cuadros" else "Mostrar cuadros",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Efecto.entries.forEach { opcion ->
                FilterChip(
                    selected = efecto == opcion,
                    onClick = { efecto = opcion },
                    label = { Text(opcion.etiqueta) }
                )
            }
        }

        Subtitulo("Con AnimatedVisibility (entrada ${DURACION_ENTRADA} ms, salida ${DURACION_SALIDA} ms)")
        AnimatedVisibility(
            visible = visible,
            enter = entrada(efecto),
            exit = salida(efecto)
        ) {
            CuadroColor(color = Color(0xFF1E88E5), texto = "Notificación\nanimada")
        }
        ContenidoDeAbajo("Contenido debajo del cuadro azul")

        HorizontalDivider()

        Subtitulo("Sin animación (if)")
        if (visible) {
            CuadroColor(color = Color(0xFFE53935), texto = "Aparece\nde golpe")
        }
        ContenidoDeAbajo("Contenido debajo del cuadro rojo")
        Nota("Con «fade» el espacio del cuadro se mantiene mientras se desvanece y recién al final se libera; con «fade + expand» el contenido de abajo se acomoda suavemente.")
    }
}

/** Barra que representa al resto de la interfaz; sirve para ver si "salta" o se desliza. */
@Composable
private fun ContenidoDeAbajo(texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(texto, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
    }
}
