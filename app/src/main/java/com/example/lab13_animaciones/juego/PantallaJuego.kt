package com.example.lab13_animaciones.juego

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive

private const val TAG = "Juego"
private val TAM_OBJETO = 44.dp

/**
 * Ejercicio final - version 1: el juego ya se puede jugar (bucle con withFrameMillis,
 * carriles, choques y puntaje), pero todavia sin animaciones de interfaz.
 */
@Composable
fun PantallaJuego(modifier: Modifier = Modifier) {
    val motor = remember { MotorJuego() }
    var estado by remember { mutableStateOf(EstadoJuego()) }

    // Bucle del juego: un paso de simulacion por fotograma mientras la fase sea JUGANDO
    LaunchedEffect(estado.fase) {
        if (estado.fase != Fase.JUGANDO) return@LaunchedEffect
        var anterior = withFrameMillis { it }
        while (isActive) {
            withFrameMillis { ahora ->
                val dt = (ahora - anterior).coerceIn(0L, 50L)
                anterior = ahora
                val previo = estado
                estado = motor.avanzar(previo, dt)
                registrar(previo, estado, motor)
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B1033))
            .pointerInput(Unit) {
                detectTapGestures { toque ->
                    when (estado.fase) {
                        Fase.JUGANDO -> {
                            val dir = if (toque.x < size.width / 2) -1 else 1
                            estado = motor.mover(estado, dir)
                            Log.d(TAG, "nave carril=${estado.carrilNave}")
                        }
                        Fase.INICIO -> estado = motor.iniciar(estado)
                        else -> Unit
                    }
                }
            }
    ) {
        val anchoCarril = maxWidth / CARRILES
        val alto = maxHeight

        // Objetos que caen
        estado.objetos.forEach { o ->
            val x = anchoCarril * (o.carril + 0.5f) - TAM_OBJETO / 2
            val y = alto * o.y - TAM_OBJETO / 2
            Box(Modifier.offset(x, y).size(TAM_OBJETO), contentAlignment = Alignment.Center) {
                when (o.tipo) {
                    TipoObjeto.METEORITO -> Box(Modifier.size(36.dp).background(Color(0xFF8D6E63), CircleShape))
                    TipoObjeto.ESTRELLA -> Text("⭐", fontSize = 26.sp)
                    TipoObjeto.CORAZON -> Text("❤️", fontSize = 24.sp)
                }
            }
        }

        // Nave
        val xNave = anchoCarril * (estado.carrilNave + 0.5f) - TAM_OBJETO / 2
        Text(
            "🚀",
            fontSize = 34.sp,
            modifier = Modifier.offset(xNave, alto * Y_NAVE - TAM_OBJETO / 2)
        )

        // Marcador
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Puntos ${estado.puntos}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Nivel ${estado.nivel}", color = Color.White)
            Text("❤️".repeat(estado.vidas), color = Color.White)
        }

        when (estado.fase) {
            Fase.INICIO -> Text(
                "Toca para empezar",
                color = Color.White,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            Fase.FIN -> Column(
                Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Fin del juego", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Puntos: ${estado.puntos} · Récord: ${estado.record}", color = Color.White)
                Button(onClick = { estado = motor.iniciar(estado) }) { Text("Jugar otra vez") }
            }
            else -> Unit
        }
    }
}

/** Deja en el logcat lo que pasa en la partida (sirve para revisar y para el video). */
private fun registrar(previo: EstadoJuego, nuevo: EstadoJuego, motor: MotorJuego) {
    nuevo.objetos.filter { it.id >= previo.proximoId }.forEach {
        Log.d(TAG, "aparece id=${it.id} carril=${it.carril} tipo=${it.tipo} v=${motor.velocidad(nuevo.nivel)}")
    }
    if (nuevo.golpes > previo.golpes) Log.d(TAG, "choque vidas=${nuevo.vidas}")
    if (nuevo.nivel > previo.nivel) Log.d(TAG, "nivel=${nuevo.nivel}")
    if (nuevo.fase == Fase.FIN && previo.fase != Fase.FIN) Log.d(TAG, "fin puntos=${nuevo.puntos}")
}
