package com.example.fittrackproyect.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Gráfico de barras dibujado con Canvas, sin librerías externas.
 * La última barra (hoy) se resalta y, si hay objetivo, se dibuja una línea discontinua.
 */
@Composable
fun GraficoBarras(
    valores: List<Int>,
    etiquetas: List<String>,
    color: Color,
    modifier: Modifier = Modifier,
    objetivo: Int? = null,
    alto: Dp = 140.dp
) {
    val animacion = remember { Animatable(0f) }
    LaunchedEffect(valores) {
        animacion.snapTo(0f)
        animacion.animateTo(1f, tween(800))
    }
    val maximo = maxOf(valores.maxOrNull() ?: 0, objetivo ?: 0, 1).toFloat()
    val colorLinea = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(alto)) {
            val hueco = size.width / valores.size
            val ancho = hueco * 0.56f
            valores.forEachIndexed { i, valor ->
                val esHoy = i == valores.lastIndex
                val altura = (valor / maximo) * size.height * animacion.value
                val x = i * hueco + (hueco - ancho) / 2
                drawRoundRect(
                    color = color.copy(alpha = 0.12f),
                    topLeft = Offset(x, 0f),
                    size = Size(ancho, size.height),
                    cornerRadius = CornerRadius(ancho / 2.5f)
                )
                if (valor > 0) {
                    drawRoundRect(
                        color = if (esHoy) color else color.copy(alpha = 0.65f),
                        topLeft = Offset(x, size.height - altura),
                        size = Size(ancho, altura),
                        cornerRadius = CornerRadius(ancho / 2.5f)
                    )
                }
            }
            if (objetivo != null && objetivo > 0) {
                val y = size.height - (objetivo / maximo) * size.height
                drawLine(
                    color = colorLinea.copy(alpha = 0.7f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            etiquetas.forEachIndexed { i, etiqueta ->
                Text(
                    etiqueta,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (i == etiquetas.lastIndex) color else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (i == etiquetas.lastIndex) FontWeight.Bold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
