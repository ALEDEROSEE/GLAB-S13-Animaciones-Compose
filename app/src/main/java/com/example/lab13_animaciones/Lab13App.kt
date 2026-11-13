package com.example.lab13_animaciones

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lab13_animaciones.ejercicios.Ejercicio1Visibilidad
import com.example.lab13_animaciones.ejercicios.Ejercicio2Color

/** Cada pantalla del laboratorio. El orden es el de la guia. */
enum class Destino(
    val titulo: String,
    val descripcion: String,
    val icono: ImageVector
) {
    VISIBILIDAD("Ejercicio 1 · AnimatedVisibility", "Mostrar y ocultar un cuadro con fadeIn / fadeOut", Icons.Filled.Visibility),
    COLOR("Ejercicio 2 · animateColorAsState", "Azul ↔ verde con tween, spring, keyframes y snap", Icons.Filled.Palette),
    TAMANO_POSICION("Ejercicio 3 · Tamaño y posición", "animateDpAsState + Modifier.offset y el orden de los modificadores", Icons.Filled.OpenWith),
    CONTENIDO("Ejercicio 4 · AnimatedContent", "Estados Cargando, Contenido y Error", Icons.Filled.SwapHoriz),
    JUEGO("Ejercicio final · Lluvia de meteoritos", "Prototipo de videojuego con animaciones combinadas", Icons.Filled.SportsEsports)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Lab13App() {
    // null = menu principal. rememberSaveable para no volver al menu al rotar.
    var actual by rememberSaveable { mutableStateOf<Destino?>(null) }

    BackHandler(enabled = actual != null) { actual = null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(actual?.titulo ?: "Lab 13 · Animaciones") },
                navigationIcon = {
                    if (actual != null) {
                        IconButton(onClick = { actual = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver al menú")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)
        when (actual) {
            null -> MenuPrincipal(onElegir = { actual = it }, modifier = modifier)
            Destino.VISIBILIDAD -> Ejercicio1Visibilidad(modifier)
            Destino.COLOR -> Ejercicio2Color(modifier)
            else -> PantallaPendiente(actual!!, modifier)
        }
    }
}

@Composable
private fun MenuPrincipal(onElegir: (Destino) -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Programación en Móviles · Jetpack Compose",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        items(Destino.entries) { destino ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onElegir(destino) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(destino.icono, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(destino.titulo, fontWeight = FontWeight.SemiBold)
                        Text(
                            destino.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
private fun PantallaPendiente(destino: Destino, modifier: Modifier = Modifier) {
    Column(modifier.padding(16.dp)) {
        Text("${destino.titulo}: pendiente")
    }
}
