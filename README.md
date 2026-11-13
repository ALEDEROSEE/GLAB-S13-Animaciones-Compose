# Lab 13 · Animaciones en Jetpack Compose

Laboratorio 13 de **Programación en Móviles** (Tecsup). Una sola app con un menú y una pantalla por ejercicio.

| Pantalla | Qué hace | APIs |
|---|---|---|
| Ejercicio 1 · Visibilidad | Un botón muestra/oculta dos cuadros: uno con `AnimatedVisibility` y otro con un `if` para comparar. Tres efectos: fade, fade + expand, slide + fade. | `AnimatedVisibility`, `fadeIn/fadeOut`, `expandVertically`, `slideInHorizontally` |
| Ejercicio 2 · Color | Azul ↔ verde con tween, spring, keyframes y snap; modo continuo. Cuenta recomposiciones de un cuadro que lee el color en composición (`background`) y otro que lo lee al dibujar (`drawBehind`). | `animateColorAsState`, `tween`, `spring`, `keyframes`, `snap` |
| Ejercicio 3 · Tamaño y posición | Mueve y agranda dos cuadros con los mismos valores; solo cambia el orden de los modificadores. | `animateDpAsState`, `Modifier.offset` |
| Ejercicio 4 · Estados | Cargando / Contenido / Error con transición propia (salida 300 ms, entrada 600 ms con 250 ms de retraso) y simulación de petición. | `AnimatedContent`, `SizeTransform` |
| Ejercicio final · Lluvia de meteoritos | Prototipo de juego de carriles: esquivar meteoritos, juntar estrellas, niveles, pausa, récord. | `updateTransition`, `rememberInfiniteTransition`, `Animatable` + `keyframes`, `AnimatedContent`, `AnimatedVisibility`, `animateColorAsState`, `animateDpAsState`, `derivedStateOf`, `Canvas` |

## Videos

En [`demo/`](demo/):

- `GLAB-S13-video-ej1-visibilidad.mp4`
- `GLAB-S13-video-ej2-color.mp4`
- `GLAB-S13-video-ej3-tamano-posicion.mp4`
- `GLAB-S13-video-ej4-estados.mp4`
- `GLAB-S13-video-final-juego.mp4`

## Cómo correrlo

Android Studio (AGP 9.3.1, compileSdk 37, minSdk 24). Desde la terminal:

```
gradlew assembleDebug
gradlew testDebugUnitTest
```

Para abrir una pantalla directamente (útil al grabar):

```
adb shell am start -n com.example.lab13_animaciones/.MainActivity --es destino JUEGO
```

Valores de `destino`: `VISIBILIDAD`, `COLOR`, `TAMANO_POSICION`, `CONTENIDO`, `JUEGO`.

## El juego

- La lógica (`juego/MotorJuego.kt`) no depende de Compose y tiene 8 pruebas JUnit (`MotorJuegoTest`).
- La versión 2 leía todo el estado en composición y se recomponía en cada fotograma (~100-150 recomposiciones cada 5 s).
  La versión 3 usa `derivedStateOf` para el marcador, un solo `Canvas` para los objetos y lee las posiciones en
  `offset { }` / `graphicsLayer { }`: bajó a 1-30 recomposiciones cada 5 s y, en el emulador, de 824-876 a 895-926
  fotogramas en 30 s de juego (medido con `dumpsys gfxinfo`).
