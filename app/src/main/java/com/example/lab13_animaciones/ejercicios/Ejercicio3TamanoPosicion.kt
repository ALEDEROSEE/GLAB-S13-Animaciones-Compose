package com.example.lab13_animaciones.ejercicios

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.ui.Nota
import com.example.lab13_animaciones.ui.PantallaEjercicio
import com.example.lab13_animaciones.ui.Subtitulo

private val Naranja = Color(0xFFEF6C00)
private val Morado = Color(0xFF6A1B9A)

/**
 * Ejercicio 3 - paso 3: el mismo par de valores animados (tamano y desplazamiento) aplicado a
 * dos cuadros cuyo unico cambio es el ORDEN de los modificadores. Se puede elegir tween o spring.
 */
@Composable
fun Ejercicio3TamanoPosicion(modifier: Modifier = Modifier) {
    var movido by rememberSaveable { mutableStateOf(false) }
    var conRebote by rememberSaveable { mutableStateOf(false) }
    val spec: AnimationSpec<Dp> = remember(conRebote) {
        if (conRebote) spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        else tween(durationMillis = 900, easing = FastOutSlowInEasing)
    }
    val tamano by animateDpAsState(if (movido) 100.dp else 60.dp, spec, label = "tamano")
    val dx by animateDpAsState(if (movido) 180.dp else 0.dp, spec, label = "offsetX")
    val dy by animateDpAsState(if (movido) 20.dp else 0.dp, spec, label = "offsetY")

    PantallaEjercicio(
        enunciado = "Mismos valores animados (animateDpAsState) en los dos cuadros; solo cambia el orden de los modificadores.",
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { movido = !movido }) {
                Text(if (movido) "Volver" else "Mover y agrandar")
            }
            FilterChip(selected = !conRebote, onClick = { conRebote = false }, label = { Text("tween") })
            FilterChip(selected = conRebote, onClick = { conRebote = true }, label = { Text("spring") })
        }
        Text(
            "tamaño = ${tamano.value.toInt()} dp · offset = (${dx.value.toInt()}, ${dy.value.toInt()}) dp",
            style = MaterialTheme.typography.labelLarge
        )

        Subtitulo("A) offset → size → background")
        Area {
            Box(
                modifier = Modifier
                    .offset(x = dx, y = dy)
                    .size(tamano)
                    .background(Naranja, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Letra("A") }
        }
        Barra("Contenido debajo de A")

        Subtitulo("B) size → background → offset")
        Area {
            Box(
                modifier = Modifier
                    .size(tamano)
                    .background(Morado, RoundedCornerShape(12.dp))
                    .offset(x = dx, y = dy),
                contentAlignment = Alignment.Center
            ) { Letra("B") }
        }
        Barra("Contenido debajo de B")

        Nota(
            "En A el offset va primero y desplaza todo lo que viene después, así que el cuadro entero se mueve. " +
                "En B el fondo se dibuja antes del offset: el cuadro morado se queda en su sitio y solo la letra se va. " +
                "El tamaño sí cambia el layout (las barras de abajo bajan); el offset no reserva espacio, por eso A se sale del borde."
        )
    }
}

/** Insignia oscura: se lee igual sobre el cuadro de color que sobre el fondo blanco. */
@Composable
private fun Letra(texto: String) {
    Text(
        texto,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(Color(0xCC000000), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun Area(contenido: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) { contenido() }
}

@Composable
private fun Barra(texto: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(texto, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
    }
}
