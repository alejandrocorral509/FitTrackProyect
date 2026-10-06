package com.example.fittrackproyect.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeMarca,
    onPrimary = Color(0xFF052E16),
    primaryContainer = Color(0xFF14532D),
    onPrimaryContainer = Color(0xFFBBF7D0),
    secondary = Lima,
    onSecondary = Color(0xFF1A2E05),
    secondaryContainer = Color(0xFF1C3324),
    onSecondaryContainer = Color(0xFFBBF7D0),
    tertiary = ColoresMetrica.agua,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = FondoOscuro,
    onSurface = TextoOscuro,
    surfaceVariant = SuperficieOscuraAlta,
    onSurfaceVariant = TextoSecundarioOscuro,
    surfaceContainerLowest = FondoOscuro,
    surfaceContainerLow = SuperficieOscura,
    surfaceContainer = SuperficieOscura,
    surfaceContainerHigh = SuperficieOscuraAlta,
    surfaceContainerHighest = SuperficieOscuraMax,
    outline = BordeOscuro,
    outlineVariant = BordeOscuro,
    error = ColoresMetrica.peligro,
)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeOscuro,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCFCE7),
    onPrimaryContainer = Color(0xFF14532D),
    secondary = Color(0xFF65A30D),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCFCE7),
    onSecondaryContainer = Color(0xFF14532D),
    tertiary = Color(0xFF0284C7),
    background = FondoClaro,
    onBackground = TextoClaro,
    surface = FondoClaro,
    onSurface = TextoClaro,
    surfaceVariant = SuperficieClaraAlta,
    onSurfaceVariant = TextoSecundarioClaro,
    surfaceContainerLowest = SuperficieClara,
    surfaceContainerLow = SuperficieClara,
    surfaceContainer = SuperficieClara,
    surfaceContainerHigh = SuperficieClaraAlta,
    surfaceContainerHighest = BordeClaro,
    outline = BordeClaro,
    outlineVariant = BordeClaro,
    error = Color(0xFFDC2626),
)

private val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun FitTrackProyectTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) EsquemaOscuro else EsquemaClaro,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}
