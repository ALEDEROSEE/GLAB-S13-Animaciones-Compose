package com.example.lab13_animaciones.ejercicios

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.ui.PantallaEjercicio

/** Los tres estados de la pantalla. */
enum class EstadoCarga(val etiqueta: String) {
    CARGANDO("Cargando"),
    CONTENIDO("Contenido"),
    ERROR("Error")
}

/** Ejercicio 4 - paso 1: tres estados con mensajes distintos; el cambio todavia es instantaneo. */
@Composable
fun Ejercicio4Contenido(modifier: Modifier = Modifier) {
    var estado by rememberSaveable { mutableStateOf(EstadoCarga.CARGANDO) }

    PantallaEjercicio(
        enunciado = "Tres estados (Cargando, Contenido y Error), cada uno con su propio mensaje.",
        modifier = modifier
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EstadoCarga.entries.forEach { opcion ->
                FilterChip(
                    selected = estado == opcion,
                    onClick = { estado = opcion },
                    label = { Text(opcion.etiqueta) }
                )
            }
        }
        VistaEstado(estado)
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
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(40.dp))
                    Text("3 tareas pendientes", fontWeight = FontWeight.Bold)
                    Text("Lab 13 · Animaciones", style = MaterialTheme.typography.bodyMedium)
                    Text("Informe del laboratorio", style = MaterialTheme.typography.bodyMedium)
                    Text("Video de los ejercicios", style = MaterialTheme.typography.bodyMedium)
                }
                EstadoCarga.ERROR -> {
                    Icon(Icons.Filled.CloudOff, contentDescription = null, modifier = Modifier.size(40.dp))
                    Text("No se pudo conectar", fontWeight = FontWeight.Bold)
                    Text("Revisa tu conexión e inténtalo otra vez.", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}
