package com.example.lab13_animaciones.juego

import kotlin.math.max
import kotlin.random.Random

/**
 * Logica del prototipo "Lluvia de meteoritos", sin nada de Compose ni de Android para poder
 * probarla con JUnit. Las posiciones verticales son fracciones del alto del campo:
 * 0 = borde de arriba, 1 = borde de abajo.
 */
const val CARRILES = 3
const val Y_NAVE = 0.86f
const val VIDAS_MAX = 3
const val PUNTOS_POR_NIVEL = 20
private const val Y_APARICION = -0.06f
private const val ZONA_CHOQUE_ARRIBA = 0.06f   // cuanto antes de la nave empieza el choque
private const val ZONA_CHOQUE_ABAJO = 0.03f
private const val Y_SALIDA = 1.06f
private const val INVULNERABLE_MS = 1200L

enum class Fase { INICIO, JUGANDO, PAUSA, FIN }

enum class TipoObjeto { METEORITO, ESTRELLA, CORAZON }

data class Objeto(val id: Long, val carril: Int, val y: Float, val tipo: TipoObjeto)

data class EstadoJuego(
    val fase: Fase = Fase.INICIO,
    val carrilNave: Int = 1,
    val objetos: List<Objeto> = emptyList(),
    val puntos: Int = 0,
    val vidas: Int = VIDAS_MAX,
    val esquivados: Int = 0,
    val golpes: Int = 0,          // cada choque suma 1: la UI lo usa para disparar la sacudida
    val estrellas: Int = 0,       // estrellas recogidas en la partida
    val tiempoMs: Long = 0,
    val invulnerableHastaMs: Long = 0,
    val msDesdeAparicion: Long = 0,
    val proximoId: Long = 0,
    val record: Int = 0
) {
    val nivel: Int get() = 1 + puntos / PUNTOS_POR_NIVEL
    val invulnerable: Boolean get() = tiempoMs < invulnerableHastaMs
}

class MotorJuego(private val random: Random = Random.Default) {

    /** Fraccion del alto que baja un objeto por milisegundo. */
    fun velocidad(nivel: Int): Float = 0.00032f + 0.00006f * (nivel - 1)

    /** Cada cuantos ms aparece un objeto nuevo. */
    fun intervaloAparicion(nivel: Int): Long = max(420L, 1000L - 110L * (nivel - 1))

    fun iniciar(e: EstadoJuego): EstadoJuego = EstadoJuego(fase = Fase.JUGANDO, record = e.record)

    fun pausar(e: EstadoJuego): EstadoJuego = when (e.fase) {
        Fase.JUGANDO -> e.copy(fase = Fase.PAUSA)
        Fase.PAUSA -> e.copy(fase = Fase.JUGANDO)
        else -> e
    }

    /** dir = -1 izquierda, +1 derecha. La nave no sale de los carriles. */
    fun mover(e: EstadoJuego, dir: Int): EstadoJuego {
        if (e.fase != Fase.JUGANDO) return e
        return e.copy(carrilNave = (e.carrilNave + dir).coerceIn(0, CARRILES - 1))
    }

    /**
     * Avanza la simulacion dt milisegundos: baja los objetos, revisa choques y recogidas,
     * cuenta los meteoritos esquivados y, si toca, crea uno nuevo arriba.
     */
    fun avanzar(e: EstadoJuego, dtMs: Long, generar: Boolean = true): EstadoJuego {
        if (e.fase != Fase.JUGANDO) return e
        val tiempo = e.tiempoMs + dtMs
        val paso = velocidad(e.nivel) * dtMs
        var puntos = e.puntos
        var vidas = e.vidas
        var esquivados = e.esquivados
        var golpes = e.golpes
        var estrellas = e.estrellas
        var invulnerableHasta = e.invulnerableHastaMs
        val quedan = ArrayList<Objeto>(e.objetos.size + 1)

        for (o in e.objetos) {
            val y = o.y + paso
            val enZona = o.carril == e.carrilNave &&
                y >= Y_NAVE - ZONA_CHOQUE_ARRIBA && y <= Y_NAVE + ZONA_CHOQUE_ABAJO
            when {
                enZona && o.tipo == TipoObjeto.METEORITO && tiempo >= invulnerableHasta -> {
                    vidas -= 1
                    golpes += 1
                    invulnerableHasta = tiempo + INVULNERABLE_MS
                }
                enZona && o.tipo == TipoObjeto.ESTRELLA -> {
                    puntos += 5
                    estrellas += 1
                }
                enZona && o.tipo == TipoObjeto.CORAZON -> {
                    vidas = (vidas + 1).coerceAtMost(VIDAS_MAX)
                }
                y > Y_SALIDA -> {
                    if (o.tipo == TipoObjeto.METEORITO) {
                        puntos += 1
                        esquivados += 1
                    }
                }
                else -> quedan += o.copy(y = y)
            }
        }

        var proximoId = e.proximoId
        var desdeAparicion = e.msDesdeAparicion + dtMs
        val nivel = 1 + puntos / PUNTOS_POR_NIVEL
        if (generar && desdeAparicion >= intervaloAparicion(nivel)) {
            desdeAparicion = 0
            quedan += nuevoObjeto(proximoId++, quedan, vidas)
        }

        val fin = vidas <= 0
        return e.copy(
            fase = if (fin) Fase.FIN else Fase.JUGANDO,
            objetos = quedan,
            puntos = puntos,
            vidas = vidas.coerceAtLeast(0),
            esquivados = esquivados,
            golpes = golpes,
            estrellas = estrellas,
            tiempoMs = tiempo,
            invulnerableHastaMs = invulnerableHasta,
            msDesdeAparicion = desdeAparicion,
            proximoId = proximoId,
            record = if (fin) max(e.record, puntos) else e.record
        )
    }

    /**
     * Elige tipo y carril. Regla para que siempre haya salida: un meteorito nunca puede dejar
     * los tres carriles tapados en la franja de arriba de la pantalla.
     */
    private fun nuevoObjeto(id: Long, actuales: List<Objeto>, vidas: Int): Objeto {
        val r = random.nextFloat()
        val tipo = when {
            r < 0.05f && vidas < VIDAS_MAX -> TipoObjeto.CORAZON
            r < 0.22f -> TipoObjeto.ESTRELLA
            else -> TipoObjeto.METEORITO
        }
        var carril = random.nextInt(CARRILES)
        if (tipo == TipoObjeto.METEORITO) {
            val ocupados = actuales
                .filter { it.tipo == TipoObjeto.METEORITO && it.y < 0.22f }
                .map { it.carril }
                .toSet()
            if ((ocupados + carril).size == CARRILES) {
                // ese carril cerraria el paso: lo muevo a uno que ya tenga meteorito
                carril = ocupados.first()
            }
        }
        return Objeto(id = id, carril = carril, y = Y_APARICION, tipo = tipo)
    }
}
