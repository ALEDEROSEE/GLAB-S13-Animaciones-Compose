package com.example.lab13_animaciones

import com.example.lab13_animaciones.juego.CARRILES
import com.example.lab13_animaciones.juego.EstadoJuego
import com.example.lab13_animaciones.juego.Fase
import com.example.lab13_animaciones.juego.MotorJuego
import com.example.lab13_animaciones.juego.Objeto
import com.example.lab13_animaciones.juego.TipoObjeto
import com.example.lab13_animaciones.juego.Y_NAVE
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MotorJuegoTest {

    private val motor = MotorJuego(Random(13))
    private val jugando = EstadoJuego(fase = Fase.JUGANDO)

    /** Avanza en pasos de 16 ms (un fotograma a 60 Hz) sin crear objetos nuevos. */
    private fun correr(e: EstadoJuego, ms: Long): EstadoJuego {
        var s = e
        var t = 0L
        while (t < ms && s.fase == Fase.JUGANDO) {
            s = motor.avanzar(s, 16, generar = false)
            t += 16
        }
        return s
    }

    @Test
    fun laNaveNoSaleDeLosCarriles() {
        var e = jugando
        repeat(5) { e = motor.mover(e, -1) }
        assertEquals(0, e.carrilNave)
        repeat(5) { e = motor.mover(e, +1) }
        assertEquals(CARRILES - 1, e.carrilNave)
    }

    @Test
    fun unMeteoritoEnElCarrilDeLaNaveQuitaUnaVida() {
        val e = jugando.copy(objetos = listOf(Objeto(0, carril = 1, y = Y_NAVE - 0.2f, tipo = TipoObjeto.METEORITO)))
        val despues = correr(e, 1500)
        assertEquals(2, despues.vidas)
        assertEquals(1, despues.golpes)
        assertTrue("el meteorito que choca desaparece", despues.objetos.isEmpty())
    }

    @Test
    fun trasUnChoqueHayInvulnerabilidad() {
        val dos = listOf(
            Objeto(0, carril = 1, y = Y_NAVE - 0.10f, tipo = TipoObjeto.METEORITO),
            Objeto(1, carril = 1, y = Y_NAVE - 0.20f, tipo = TipoObjeto.METEORITO)
        )
        // el segundo llega ~300 ms despues del primero: cae dentro de los 1200 ms de invulnerabilidad
        val despues = correr(jugando.copy(objetos = dos), 2000)
        assertEquals(2, despues.vidas)
        assertEquals(1, despues.golpes)
    }

    @Test
    fun esquivarUnMeteoritoSumaUnPunto() {
        val e = jugando.copy(objetos = listOf(Objeto(0, carril = 0, y = 0.5f, tipo = TipoObjeto.METEORITO)))
        val despues = correr(e, 3000)
        assertEquals(3, despues.vidas)
        assertEquals(1, despues.puntos)
        assertEquals(1, despues.esquivados)
    }

    @Test
    fun laEstrellaDaCincoPuntos() {
        val e = jugando.copy(objetos = listOf(Objeto(0, carril = 1, y = Y_NAVE - 0.2f, tipo = TipoObjeto.ESTRELLA)))
        val despues = correr(e, 1500)
        assertEquals(5, despues.puntos)
        assertEquals(1, despues.estrellas)
    }

    @Test
    fun sinVidasTerminaYGuardaElRecord() {
        val e = jugando.copy(
            vidas = 1, puntos = 42, record = 30,
            objetos = listOf(Objeto(0, carril = 1, y = Y_NAVE - 0.1f, tipo = TipoObjeto.METEORITO))
        )
        val despues = correr(e, 1000)
        assertEquals(Fase.FIN, despues.fase)
        assertEquals(42, despues.record)
    }

    @Test
    fun elNivelSubeCada20PuntosYLaVelocidadAumenta() {
        assertEquals(1, jugando.copy(puntos = 19).nivel)
        assertEquals(2, jugando.copy(puntos = 20).nivel)
        assertTrue(motor.velocidad(2) > motor.velocidad(1))
        assertTrue(motor.intervaloAparicion(3) < motor.intervaloAparicion(1))
    }

    @Test
    fun siempreQuedaUnCarrilLibreArriba() {
        // simulo 2 minutos con la generacion activada y reviso la franja de arriba en cada paso
        var e = jugando.copy(vidas = 999)
        repeat(7500) {
            e = motor.avanzar(e, 16)
            val tapados = e.objetos
                .filter { it.tipo == TipoObjeto.METEORITO && it.y < 0.22f }
                .map { it.carril }
                .toSet()
            assertTrue("los $CARRILES carriles tapados en t=${e.tiempoMs}", tapados.size < CARRILES)
        }
    }
}
