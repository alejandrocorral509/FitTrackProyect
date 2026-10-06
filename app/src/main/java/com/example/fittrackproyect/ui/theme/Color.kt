package com.example.fittrackproyect.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Colores de marca, sacados del logo: degradado de lima a verde
val Lima = Color(0xFFA3E635)
val VerdeMarca = Color(0xFF4ADE80)
val VerdeOscuro = Color(0xFF16A34A)
val DegradadoMarca = Brush.linearGradient(listOf(Lima, VerdeMarca, Color(0xFF22C55E)))

// Un color por cada métrica, igual en modo claro y oscuro
object ColoresMetrica {
    val calorias = Color(0xFFFB923C)
    val agua = Color(0xFF38BDF8)
    val entreno = VerdeMarca
    val proteinas = Color(0xFFF472B6)
    val carbos = Color(0xFF60A5FA)
    val grasas = Color(0xFFFBBF24)
    val peligro = Color(0xFFF87171)
}

// Modo oscuro (el principal de la app)
val FondoOscuro = Color(0xFF0B0D0C)
val SuperficieOscura = Color(0xFF131715)
val SuperficieOscuraAlta = Color(0xFF1A1F1C)
val SuperficieOscuraMax = Color(0xFF222824)
val BordeOscuro = Color(0xFF2A302C)
val TextoOscuro = Color(0xFFECEFED)
val TextoSecundarioOscuro = Color(0xFF9AA39E)

// Modo claro
val FondoClaro = Color(0xFFF5F7F6)
val SuperficieClara = Color(0xFFFFFFFF)
val SuperficieClaraAlta = Color(0xFFEEF2EF)
val BordeClaro = Color(0xFFDCE2DE)
val TextoClaro = Color(0xFF111513)
val TextoSecundarioClaro = Color(0xFF5B655F)

// Colores de la versión anterior. Se eliminarán cuando todas las pantallas usen el tema.
val Gray = Color(0xFF393939)
val Black = Color(0xFF121212)
val Green = Color(0xFF49dd63)
val BackgroundButton = Color(0xFF111111)
val ShapeButton = Color(0xFF3e3e3e)
