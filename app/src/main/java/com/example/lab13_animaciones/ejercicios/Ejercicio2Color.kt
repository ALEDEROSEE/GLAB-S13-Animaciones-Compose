package com.example.lab13_animaciones.ejercicios

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.ui.Nota
import com.example.lab13_animaciones.ui.PantallaEjercicio
import com.example.lab13_animaciones.ui.Subtitulo
import kotlinx.coroutines.delay

private val Azul = Color(0xFF1565C0)
private val Verde = Color(0xFF2E7D32)
private const val TAG = "Lab13-Ej2"

/** Especificaciones de animacion que se pueden probar. */
private enum class TipoSpec(val etiqueta: String) {
    TWEEN("tween 1500 ms"),
    SPRING("spring lento"),
    SPRING_REBOTE("spring con rebote"),
    KEYFRAMES("keyframes"),
    SNAP("snap")
}

private fun specDe(tipo: TipoSpec): AnimationSpec<Color> = when (tipo) {
    TipoSpec.TWEEN -> tween(durationMillis = 1500, easing = FastOutSlowInEasing)
    TipoSpec.SPRING -> spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessVeryLow)
    TipoSpec.SPRING_REBOTE -> spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessLow)
    // Pasa por amarillo a mitad de camino, sin importar si va hacia azul o hacia verde
    TipoSpec.KEYFRAMES -> keyframes {
        durationMillis = 1500
        Color(0xFFF9A825) at 600
    }
    TipoSpec.SNAP -> snap(delayMillis = 300)
}

/** Contador comun (no es estado): sumarlo no provoca otra recomposicion. */
class ContadorRecomposiciones { var valor = 0 }

/**
 * Ejercicio 2 - paso 3: se experimenta con tween, spring, keyframes y snap, y se mide cuantas
 * veces se recompone cada cuadro durante un cambio de color.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Ejercicio2Color(modifier: Modifier = Modifier) {
    var esAzul by rememberSaveable { mutableStateOf(true) }
    var tipo by rememberSaveable { mutableStateOf(TipoSpec.TWEEN) }
    var continuo by rememberSaveable { mutableStateOf(false) }
    val spec = remember(tipo) { specDe(tipo) }
    val objetivo = if (esAzul) Azul else Verde

    val cDirecto = remember { ContadorRecomposiciones() }
    val cBackground = remember { ContadorRecomposiciones() }
    val cDrawBehind = remember { ContadorRecomposiciones() }
    var medicion by remember { mutableStateOf("Toca «Cambiar color» para medir.") }
    var huboCambio by remember { mutableStateOf(false) }

    fun cambiar() {
        cDirecto.valor = 0; cBackground.valor = 0; cDrawBehind.valor = 0
        huboCambio = true
        esAzul = !esAzul
    }

    // Modo continuo: el objetivo se invierte solo y la animacion nunca se detiene
    LaunchedEffect(continuo, tipo) {
        while (continuo) {
            delay(1800)
            cambiar()
        }
    }
    // 2.5 s despues de cada cambio leo los contadores (todas las specs ya terminaron)
    LaunchedEffect(esAzul, tipo) {
        if (!huboCambio) return@LaunchedEffect   // la primera composicion no cuenta
        delay(2500)
        medicion = "Último cambio (${tipo.etiqueta}): directo ${cDirecto.valor} · " +
            "background ${cBackground.valor} · drawBehind ${cDrawBehind.valor} recomposiciones"
        Log.i(TAG, medicion)
    }

    PantallaEjercicio(
        enunciado = "El botón cambia el color objetivo entre azul y verde. Los tres cuadros reciben el mismo " +
            "objetivo: el primero cambia de golpe y los otros dos usan animateColorAsState.",
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { cambiar() }) { Text("Cambiar color") }
            Text("  Continuo", style = MaterialTheme.typography.labelLarge)
            Switch(checked = continuo, onCheckedChange = { continuo = it }, modifier = Modifier.padding(start = 8.dp))
        }
        Subtitulo("Especificación de la animación")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TipoSpec.entries.forEach { opcion ->
                FilterChip(
                    selected = tipo == opcion,
                    onClick = { tipo = opcion },
                    label = { Text(opcion.etiqueta) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            CajaDirecta(objetivo, cDirecto, Modifier.weight(1f))
            CajaBackground(objetivo, spec, cBackground, Modifier.weight(1f))
            CajaDrawBehind(objetivo, spec, cDrawBehind, Modifier.weight(1f))
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(medicion, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
        }
        Nota("background(color) lee el color animado durante la composición, así que el cuadro se recompone en cada fotograma. drawBehind lo lee recién al dibujar y solo se recompone una vez por cambio.")
    }
}

/** Sin animacion: el color salta al objetivo. */
@Composable
private fun CajaDirecta(objetivo: Color, contador: ContadorRecomposiciones, modifier: Modifier = Modifier) {
    SideEffect { contador.valor++ }
    Etiquetada("Directo", Modifier.background(objetivo), modifier)
}

/** animateColorAsState leido en composicion (Modifier.background). */
@Composable
private fun CajaBackground(
    objetivo: Color,
    spec: AnimationSpec<Color>,
    contador: ContadorRecomposiciones,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(targetValue = objetivo, animationSpec = spec, label = "fondo")
    SideEffect { contador.valor++ }
    // "color" se lee aqui mismo, durante la composicion de CajaBackground
    Etiquetada("background", Modifier.background(color), modifier)
}

/** animateColorAsState leido solo en la fase de dibujo (Modifier.drawBehind). */
@Composable
private fun CajaDrawBehind(
    objetivo: Color,
    spec: AnimationSpec<Color>,
    contador: ContadorRecomposiciones,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(targetValue = objetivo, animationSpec = spec, label = "fondoDraw")
    SideEffect { contador.valor++ }
    // "color" se lee dentro del lambda de dibujo, no durante la composicion
    Etiquetada("drawBehind", Modifier.drawBehind { drawRect(color) }, modifier)
}

@Composable
private fun Etiquetada(titulo: String, fondo: Modifier, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(14.dp))
                .then(fondo)
        )
        Text(
            titulo,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
