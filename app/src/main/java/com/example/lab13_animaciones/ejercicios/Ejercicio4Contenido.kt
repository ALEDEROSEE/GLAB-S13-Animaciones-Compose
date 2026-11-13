package com.example.lab13_animaciones.ejercicios

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.ui.Nota
import com.example.lab13_animaciones.ui.PantallaEjercicio
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Tiempos personalizados de la transicion. */
private const val ENTRADA_MS = 600
private const val RETRASO_ENTRADA_MS = 250
private const val SALIDA_MS = 300

/** Los tres estados de la pantalla. */
enum class EstadoCarga(val etiqueta: String) {
    CARGANDO("Cargando"),
    CONTENIDO("Contenido"),
    ERROR("Error")
}

/**
 * Ejercicio 4 - paso 3: transicion propia con fadeIn/fadeOut y tiempos personalizados.
 * El contenido viejo se va en 300 ms y el nuevo entra despues de 250 ms, durante 600 ms.
 * "Simular petición" pasa por Cargando y termina en Contenido o en Error (se alternan).
 */
@Composable
fun Ejercicio4Contenido(modifier: Modifier = Modifier) {
    var estado by rememberSaveable { mutableStateOf(EstadoCarga.CARGANDO) }
    var animar by rememberSaveable { mutableStateOf(true) }
    var proximaFalla by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var peticion by remember { mutableStateOf<Job?>(null) }

    fun simularPeticion() {
        peticion?.cancel()
        peticion = scope.launch {
            estado = EstadoCarga.CARGANDO
            delay(1800)
            estado = if (proximaFalla) EstadoCarga.ERROR else EstadoCarga.CONTENIDO
            proximaFalla = !proximaFalla
        }
    }

    PantallaEjercicio(
        enunciado = "Tres estados con su propio mensaje. AnimatedContent hace la transición entre ellos " +
            "(salida ${SALIDA_MS} ms, entrada ${ENTRADA_MS} ms con ${RETRASO_ENTRADA_MS} ms de retraso).",
        modifier = modifier
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstadoCarga.entries.forEach { opcion ->
                FilterChip(
                    selected = estado == opcion,
                    onClick = { peticion?.cancel(); estado = opcion },
                    label = { Text(opcion.etiqueta) }
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(onClick = { simularPeticion() }) { Text("Simular petición") }
            Spacer(Modifier.weight(1f))
            Text("Animar", style = MaterialTheme.typography.labelLarge)
            Switch(checked = animar, onCheckedChange = { animar = it }, modifier = Modifier.padding(start = 8.dp))
        }

        if (animar) {
            AnimatedContent(
                targetState = estado,
                transitionSpec = {
                    val entrada = fadeIn(tween(ENTRADA_MS, delayMillis = RETRASO_ENTRADA_MS)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(ENTRADA_MS, delayMillis = RETRASO_ENTRADA_MS))
                    val salida = fadeOut(tween(SALIDA_MS))
                    (entrada togetherWith salida).using(
                        // el alto de la tarjeta tambien se anima, sin recortar el contenido
                        SizeTransform(clip = false) { _, _ -> tween(ENTRADA_MS) }
                    )
                },
                label = "estado"
            ) { objetivo ->
                // Importante: usar "objetivo" (el estado de ESTE contenido), no "estado"
                VistaEstado(objetivo, onReintentar = { simularPeticion() })
            }
        } else {
            // Mismo contenido sin AnimatedContent, para comparar
            VistaEstado(estado, onReintentar = { simularPeticion() })
        }
        Nota("Con «Animar» apagado el cambio es instantáneo: la tarjeta cambia de alto de golpe y todo lo que está debajo salta.")
        Box(Modifier.height(8.dp))
    }
}

/** Lo que se ve en cada estado. */
@Composable
fun VistaEstado(estado: EstadoCarga, onReintentar: () -> Unit = {}) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (estado) {
                EstadoCarga.CARGANDO -> {
                    CircularProgressIndicator()
                    Text("Cargando tareas…")
                }
                EstadoCarga.CONTENIDO -> {
                    Icon(
                        Icons.Filled.CheckCircle, contentDescription = null,
                        tint = Color(0xFF2E7D32), modifier = Modifier.size(40.dp)
                    )
                    Text("3 tareas pendientes", fontWeight = FontWeight.Bold)
                    Text("Lab 13 · Animaciones", style = MaterialTheme.typography.bodyMedium)
                    Text("Informe del laboratorio", style = MaterialTheme.typography.bodyMedium)
                    Text("Video de los ejercicios", style = MaterialTheme.typography.bodyMedium)
                }
                EstadoCarga.ERROR -> {
                    Icon(
                        Icons.Filled.CloudOff, contentDescription = null,
                        tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp)
                    )
                    Text("No se pudo conectar", fontWeight = FontWeight.Bold)
                    Text("Revisa tu conexión e inténtalo otra vez.", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = onReintentar) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Text("Reintentar", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }
    }
}
