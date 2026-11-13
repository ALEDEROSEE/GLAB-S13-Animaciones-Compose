package com.example.lab13_animaciones.juego

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lab13_animaciones.ejercicios.ContadorRecomposiciones
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

private const val TAG = "Juego"
private val TAM_OBJETO = 44.dp

/** Color de arriba del cielo segun el nivel: cada nivel el cielo se pone mas "caliente". */
private fun colorCielo(nivel: Int, fase: Fase): Color = when {
    fase == Fase.FIN -> Color(0xFF3B0A0A)
    nivel <= 1 -> Color(0xFF0B1033)
    nivel == 2 -> Color(0xFF2A0F4F)
    nivel == 3 -> Color(0xFF4A1042)
    else -> Color(0xFF5E1A1A)
}

private data class EstrellaFondo(val x: Float, val y: Float, val radio: Float, val fase: Float)

/**
 * Ejercicio final - version 3: mismas animaciones que la version 2, pero ordenadas para que
 * el estado que cambia en cada fotograma (posiciones, tiempo) se lea solo en las fases de
 * layout y dibujo. La composicion solo lee valores derivados (derivedStateOf) que cambian
 * pocas veces: fase, puntos, vidas, nivel, carril.
 *
 * Animaciones: updateTransition (fase), animateColorAsState (cielo), animateDpAsState con
 * spring (nave), rememberInfiniteTransition (llama, estrellas, titulo), Animatable con
 * keyframes (choque), AnimatedContent (puntaje y pausa) y AnimatedVisibility (vidas, carteles).
 */
@Composable
fun PantallaJuego(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val prefs = remember { contexto.getSharedPreferences("lluvia_meteoritos", Context.MODE_PRIVATE) }
    val motor = remember { MotorJuego() }
    var estado by remember { mutableStateOf(EstadoJuego(record = prefs.getInt("record", 0))) }
    var recordAlIniciar by remember { mutableIntStateOf(estado.record) }

    // Valores que SI se muestran en la composicion: solo avisan cuando cambian de verdad
    val fase by remember { derivedStateOf { estado.fase } }
    val puntos by remember { derivedStateOf { estado.puntos } }
    val vidas by remember { derivedStateOf { estado.vidas } }
    val nivel by remember { derivedStateOf { estado.nivel } }
    val carril by remember { derivedStateOf { estado.carrilNave } }
    val golpes by remember { derivedStateOf { estado.golpes } }
    val estrellas by remember { derivedStateOf { estado.estrellas } }
    val record by remember { derivedStateOf { estado.record } }

    fun iniciar() {
        recordAlIniciar = estado.record
        estado = motor.iniciar(estado)
        Log.d(TAG, "inicio record=${estado.record}")
    }

    fun mover(dir: Int) {
        val antes = estado.carrilNave
        estado = motor.mover(estado, dir)
        if (estado.carrilNave != antes) Log.d(TAG, "nave carril=${estado.carrilNave}")
    }

    // ---------------------------------------------------------------- bucle del juego
    LaunchedEffect(fase) {
        if (fase != Fase.JUGANDO) return@LaunchedEffect
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
    LaunchedEffect(fase) {
        if (fase == Fase.FIN) prefs.edit().putInt("record", estado.record).apply()
    }

    // ---------------------------------------------------------------- animaciones
    // 1) updateTransition: varios valores que dependen de la fase y se animan juntos
    val transicion = updateTransition(targetState = fase, label = "fase")
    val velo = transicion.animateFloat(transitionSpec = { tween(500) }, label = "velo") { f ->
        when (f) {
            Fase.INICIO -> 0.45f
            Fase.JUGANDO -> 0f
            Fase.PAUSA -> 0.5f
            Fase.FIN -> 0.65f
        }
    }
    val escalaNave = transicion.animateFloat(
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) },
        label = "escalaNave"
    ) { f ->
        when (f) {
            Fase.INICIO -> 1.6f
            Fase.FIN -> 0.5f
            else -> 1f
        }
    }
    val alphaHud = transicion.animateFloat(transitionSpec = { tween(400) }, label = "hud") { f ->
        if (f == Fase.INICIO) 0f else 1f
    }

    // 2) color del cielo
    val cielo by animateColorAsState(colorCielo(nivel, fase), tween(1200), label = "cielo")

    // 3) animaciones infinitas (se leen dentro de graphicsLayer / Canvas, no en composicion)
    val infinita = rememberInfiniteTransition(label = "siempre")
    val llama = infinita.animateFloat(0.7f, 1.25f, infiniteRepeatable(tween(140), RepeatMode.Reverse), label = "llama")
    val brillo = infinita.animateFloat(0f, 1f, infiniteRepeatable(tween(1800, easing = LinearEasing)), label = "brillo")
    val flotar = infinita.animateFloat(
        -8f, 8f, infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "flotar"
    )
    val parpadeo = infinita.animateFloat(0.2f, 1f, infiniteRepeatable(tween(110), RepeatMode.Reverse), label = "parpadeo")

    // 4) Animatable: sacudida del campo y destello rojo en cada choque
    val sacudida = remember { Animatable(0f) }
    val destello = remember { Animatable(0f) }
    LaunchedEffect(golpes) {
        if (golpes == 0) return@LaunchedEffect
        launch {
            destello.snapTo(0.5f)
            destello.animateTo(0f, tween(450))
        }
        sacudida.snapTo(0f)
        sacudida.animateTo(0f, keyframes {
            durationMillis = 420
            22f at 40
            -18f at 110
            14f at 180
            -9f at 250
            5f at 320
        })
    }

    // 5) cartel de nivel y "+5" de las estrellas
    var mostrarNivel by remember { mutableStateOf(false) }
    var nivelCartel by remember { mutableIntStateOf(1) }
    LaunchedEffect(nivel) {
        if (nivel > 1 && fase == Fase.JUGANDO) {
            nivelCartel = nivel
            mostrarNivel = true
            delay(1500)
            mostrarNivel = false
        }
    }
    var mostrarMas5 by remember { mutableStateOf(false) }
    LaunchedEffect(estrellas) {
        if (estrellas > 0) {
            mostrarMas5 = true
            delay(650)
            mostrarMas5 = false
        }
    }

    val estrellasFondo = remember {
        val r = Random(7)
        List(45) { EstrellaFondo(r.nextFloat(), r.nextFloat(), 1f + r.nextFloat() * 1.8f, r.nextFloat()) }
    }
    val medidor = rememberTextMeasurer()
    val capaEstrella = remember(medidor) { medidor.measure("⭐", TextStyle(fontSize = 26.sp)) }
    val capaCorazon = remember(medidor) { medidor.measure("❤️", TextStyle(fontSize = 24.sp)) }

    val recomposiciones = remember { ContadorRecomposiciones() }
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            Log.d(TAG, "recomposiciones en 5 s: ${recomposiciones.valor} (fase=${estado.fase})")
            recomposiciones.valor = 0
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cielo, Color(0xFF02030A))))
            .pointerInput(Unit) {
                detectTapGestures { toque ->
                    when (estado.fase) {
                        Fase.JUGANDO -> mover(if (toque.x < size.width / 2) -1 else 1)
                        Fase.INICIO -> iniciar()
                        else -> Unit
                    }
                }
            }
            .pointerInput(Unit) {
                var acumulado = 0f
                detectHorizontalDragGestures(onDragEnd = { acumulado = 0f }) { cambio, dx ->
                    cambio.consume()
                    acumulado += dx
                    if (abs(acumulado) > 48.dp.toPx()) {
                        mover(if (acumulado > 0) 1 else -1)
                        acumulado = 0f
                    }
                }
            }
    ) {
        SideEffect { recomposiciones.valor++ }
        val anchoCarril = maxWidth / CARRILES
        val alto = maxHeight

        // 6) posicion de la nave con un spring que rebota un poco al llegar al carril
        val xObjetivo = anchoCarril * (carril + 0.5f)
        val xNave = animateDpAsState(
            targetValue = xObjetivo,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
            label = "xNave"
        )

        // ------------------------------------------------ campo de juego (se sacude entero)
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = sacudida.value * density }
        ) {
            // Un solo Canvas para el fondo y todos los objetos: estado.objetos se lee al dibujar
            Canvas(Modifier.fillMaxSize()) {
                val e = estado
                val b = brillo.value
                estrellasFondo.forEach { s ->
                    val y = (s.y + e.tiempoMs * 0.00003f * s.radio) % 1f
                    val a = 0.25f + 0.75f * abs(sin((b + s.fase) * PI.toFloat()))
                    drawCircle(Color.White.copy(alpha = a), radius = s.radio * density, center = Offset(s.x * size.width, y * size.height))
                }
                for (i in 1 until CARRILES) {
                    val x = size.width * i / CARRILES
                    drawLine(Color.White.copy(alpha = 0.07f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 2f)
                }
                val anchoPx = size.width / CARRILES
                e.objetos.forEach { o ->
                    val c = Offset(anchoPx * (o.carril + 0.5f), size.height * o.y)
                    when (o.tipo) {
                        TipoObjeto.METEORITO -> meteorito(c, TAM_OBJETO.toPx() * 0.38f, (e.tiempoMs * 0.18f + o.id * 47f) % 360f)
                        TipoObjeto.ESTRELLA -> {
                            val k = 0.9f + 0.2f * abs(sin(b * 2 * PI.toFloat()))
                            scale(k, c) { emoji(capaEstrella, c) }
                        }
                        TipoObjeto.CORAZON -> emoji(capaCorazon, c)
                    }
                }
            }

            // nave + llama: posicion en layout (offset con lambda) y giro/escala/parpadeo en graphicsLayer
            Box(
                modifier = Modifier
                    .offset {
                        val flota = if (fase == Fase.INICIO) flotar.value.dp else 0.dp
                        IntOffset(
                            (xNave.value - TAM_OBJETO / 2).roundToPx(),
                            (alto * Y_NAVE - TAM_OBJETO / 2 + flota).roundToPx()
                        )
                    }
                    .size(TAM_OBJETO)
                    .graphicsLayer {
                        // la inclinacion sale del "retraso" del spring: se ladea al viajar y se endereza al llegar
                        rotationZ = ((xObjetivo - xNave.value).value * 0.35f).coerceIn(-25f, 25f)
                        scaleX = escalaNave.value
                        scaleY = escalaNave.value
                        alpha = if (estado.invulnerable) parpadeo.value else 1f
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🔥", fontSize = 18.sp,
                    modifier = Modifier
                        .offset(y = 26.dp)
                        .graphicsLayer { rotationZ = 180f; scaleY = llama.value; scaleX = 0.8f }
                )
                Text("🚀", fontSize = 32.sp, modifier = Modifier.graphicsLayer { rotationZ = -45f })
            }

            // "+5" que sube desde la nave al recoger una estrella
            AnimatedVisibility(
                visible = mostrarMas5,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.offset { IntOffset((xNave.value - 14.dp).roundToPx(), (alto * Y_NAVE - 70.dp).roundToPx()) }
            ) {
                Text("+5", color = Color(0xFFFFD54F), fontWeight = FontWeight.Black, fontSize = 22.sp)
            }
        }

        // destello rojo del choque
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = destello.value }
                .background(Color(0xFFD50000))
        )
        // velo oscuro que depende de la fase (updateTransition)
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = velo.value }
                .background(Color.Black)
        )

        // ------------------------------------------------ marcador
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer { alpha = alphaHud.value }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⭐", fontSize = 18.sp)
            AnimatedContent(
                targetState = puntos,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                    } else {
                        fadeIn() togetherWith fadeOut()
                    } using SizeTransform(clip = false)
                },
                label = "puntos"
            ) { p ->
                Text(
                    "$p", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
            Text(
                "  Nivel $nivel", color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.weight(1f)
            )
            // vidas: cada corazon entra y sale con su propia animacion
            repeat(VIDAS_MAX) { i ->
                Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = i < vidas,
                        enter = scaleIn(spring(dampingRatio = 0.35f)) + fadeIn(),
                        exit = scaleOut(tween(350), targetScale = 2f) + fadeOut(tween(350))
                    ) {
                        Text("❤️", fontSize = 18.sp)
                    }
                }
            }
            IconButton(
                onClick = { estado = motor.pausar(estado) },
                enabled = fase == Fase.JUGANDO || fase == Fase.PAUSA
            ) {
                AnimatedContent(targetState = fase == Fase.PAUSA, label = "iconoPausa") { enPausa ->
                    Icon(
                        if (enPausa) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (enPausa) "Seguir" else "Pausa",
                        tint = Color.White
                    )
                }
            }
        }

        // ------------------------------------------------ carteles
        AnimatedVisibility(
            visible = mostrarNivel,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy), initialScale = 0.3f) + fadeIn(),
            exit = slideOutVertically { -it * 2 } + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("¡Nivel $nivelCartel!", color = Color(0xFFFFD54F), fontSize = 40.sp, fontWeight = FontWeight.Black)
        }

        AnimatedVisibility(
            visible = fase == Fase.INICIO,
            enter = fadeIn(),
            exit = fadeOut() + scaleOut(targetScale = 1.4f),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("LLUVIA DE", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("METEORITOS", color = Color(0xFFFF8A65), fontSize = 38.sp, fontWeight = FontWeight.Black)
                Text(
                    "Toca para empezar",
                    color = Color.White,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .graphicsLayer { val k = 1f + 0.06f * flotar.value / 8f; scaleX = k; scaleY = k }
                )
                Text(
                    "Toca a la izquierda o a la derecha (o desliza) para cambiar de carril",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
                )
                Text("Récord: $record", color = Color(0xFFFFD54F), modifier = Modifier.padding(top = 16.dp))
            }
        }

        AnimatedVisibility(
            visible = fase == Fase.PAUSA,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text("PAUSA", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
        }

        AnimatedVisibility(
            visible = fase == Fase.FIN,
            enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow), initialScale = 0.4f) + fadeIn(),
            exit = fadeOut(tween(200)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            PanelFin(estado, nuevoRecord = estado.puntos > recordAlIniciar, onOtraVez = { iniciar() })
        }
    }
}

/** Meteorito dibujado a mano: estela de fuego hacia arriba, roca con degradado y crateres que giran. */
private fun DrawScope.meteorito(c: Offset, r: Float, giro: Float) {
    val estela = Path().apply {
        moveTo(c.x - r * 0.8f, c.y)
        lineTo(c.x, c.y - r * 3.2f)
        lineTo(c.x + r * 0.8f, c.y)
        close()
    }
    drawPath(estela, Brush.verticalGradient(listOf(Color.Transparent, Color(0xCCFF7043)), startY = c.y - r * 3.2f, endY = c.y))
    drawCircle(Brush.radialGradient(listOf(Color(0xFFBCAAA4), Color(0xFF4E342E)), center = c, radius = r), r, c)
    rotate(giro, c) {
        drawCircle(Color(0xFF3E2723), r * 0.22f, Offset(c.x - r * 0.35f, c.y - r * 0.25f))
        drawCircle(Color(0xFF3E2723), r * 0.15f, Offset(c.x + r * 0.40f, c.y + r * 0.10f))
        drawCircle(Color(0xFF3E2723), r * 0.10f, Offset(c.x - r * 0.05f, c.y + r * 0.45f))
    }
}

/** Dibuja un emoji ya medido (TextLayoutResult) centrado en c. */
private fun DrawScope.emoji(capa: TextLayoutResult, c: Offset) {
    drawText(capa, topLeft = Offset(c.x - capa.size.width / 2f, c.y - capa.size.height / 2f))
}

@Composable
private fun PanelFin(estado: EstadoJuego, nuevoRecord: Boolean, onOtraVez: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF21A1B2E)),
        modifier = Modifier.padding(32.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("¡Fin del juego!", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text("${estado.puntos} puntos", color = Color(0xFFFFD54F), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(
                "Nivel ${estado.nivel} · ${estado.esquivados} esquivados · ${estado.estrellas} ⭐",
                color = Color.White.copy(alpha = 0.85f)
            )
            Text(
                if (nuevoRecord) "¡Nuevo récord!" else "Récord: ${estado.record}",
                color = if (nuevoRecord) Color(0xFF69F0AE) else Color.White.copy(alpha = 0.7f),
                fontWeight = if (nuevoRecord) FontWeight.Bold else FontWeight.Normal
            )
            Button(onClick = onOtraVez, modifier = Modifier.padding(top = 10.dp)) { Text("Jugar otra vez") }
        }
    }
}

/** Deja en el logcat lo que pasa en la partida (sirve para revisar y para el video). */
private fun registrar(previo: EstadoJuego, nuevo: EstadoJuego, motor: MotorJuego) {
    nuevo.objetos.filter { it.id >= previo.proximoId }.forEach {
        Log.d(TAG, "aparece id=${it.id} carril=${it.carril} tipo=${it.tipo} v=${motor.velocidad(nuevo.nivel)}")
    }
    if (nuevo.golpes > previo.golpes) Log.d(TAG, "choque vidas=${nuevo.vidas}")
    if (nuevo.estrellas > previo.estrellas) Log.d(TAG, "estrella puntos=${nuevo.puntos}")
    if (nuevo.nivel > previo.nivel) Log.d(TAG, "nivel=${nuevo.nivel}")
    if (nuevo.fase == Fase.FIN && previo.fase != Fase.FIN) Log.d(TAG, "fin puntos=${nuevo.puntos}")
    // foto del estado cada 200 ms de juego (la uso para el script que graba el video)
    if (nuevo.tiempoMs / 200 != previo.tiempoMs / 200) {
        val objs = nuevo.objetos.joinToString(";") { "${it.id}:${it.carril}:${"%.3f".format(it.y)}:${it.tipo.name[0]}" }
        Log.v(TAG, "estado t=${nuevo.tiempoMs} nave=${nuevo.carrilNave} nivel=${nuevo.nivel} objs=$objs")
    }
}
