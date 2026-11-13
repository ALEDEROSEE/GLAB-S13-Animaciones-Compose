package com.example.lab13_animaciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lab13_animaciones.ui.theme.Lab13AnimacionesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Acceso directo para pruebas y grabaciones:
        // adb shell am start -n com.example.lab13_animaciones/.MainActivity --es destino JUEGO
        val inicial = intent.getStringExtra("destino")
            ?.let { nombre -> Destino.entries.firstOrNull { it.name == nombre } }
        setContent {
            Lab13AnimacionesTheme {
                // Menu con un acceso a cada ejercicio de la guia.
                Lab13App(inicial)
            }
        }
    }
}
