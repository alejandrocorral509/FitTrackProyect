package com.example.fittrackproyect.presentation.agua

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.GraficoBarras
import com.example.fittrackproyect.ui.components.Pastilla
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.theme.ColoresMetrica
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AguaScreen(
    alVolver: () -> Unit,
    viewModel: AguaViewModel = viewModel(factory = AguaViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()
    val azul = ColoresMetrica.agua

    Scaffold(topBar = { BarraSuperior("Hidratación", alVolver = alVolver) }) { relleno ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                if (s.completado) "¡Objetivo cumplido! 💧" else "Te quedan ${s.objetivo - s.vasos} vasos",
                style = MaterialTheme.typography.titleMedium,
                color = if (s.completado) azul else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box(contentAlignment = Alignment.Center) {
                VasoConOlas(s.progreso, azul, Modifier.size(width = 190.dp, height = 250.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${s.vasos}", style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.onSurface)
                    Text("de ${s.objetivo} vasos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${s.mililitros} ml", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                FilledTonalIconButton(onClick = { viewModel.cambiar(-1) }, enabled = s.vasos > 0, modifier = Modifier.size(60.dp)) {
                    Icon(Icons.Filled.Remove, contentDescription = "Quitar un vaso")
                }
                FilledIconButton(
                    onClick = { viewModel.cambiar(+1) },
                    modifier = Modifier.size(84.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = azul, contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Añadir un vaso", modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.width(60.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Pastilla("Botella 500 ml", false, { viewModel.cambiar(+2) }, color = azul, emoji = "🧴")
                Pastilla("Litro", false, { viewModel.cambiar(+4) }, color = azul, emoji = "🫗")
            }

            Tarjeta(Modifier.fillMaxWidth()) {
                Column {
                    Text("Últimos 7 días", style = MaterialTheme.typography.titleMedium)
                    Text("La línea marca tu objetivo diario", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    GraficoBarras(s.semana, s.etiquetasSemana, azul, objetivo = s.objetivo, alto = 120.dp)
                }
            }
            Text(
                "Tu objetivo se calcula con unos 33 ml por kilo de peso. Si entrenas o hace calor, bebe un poco más.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Vaso dibujado con Canvas: el agua sube con el progreso y su superficie se mueve en ondas. */
@Composable
private fun VasoConOlas(progreso: Float, color: Color, modifier: Modifier = Modifier) {
    val nivel by animateFloatAsState(progreso.coerceIn(0f, 1f), spring(dampingRatio = 0.6f, stiffness = 60f), label = "nivel")
    val fase by rememberInfiniteTransition(label = "olas").animateFloat(
        0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "fase"
    )
    val borde = MaterialTheme.colorScheme.outline
    val fondo = color.copy(alpha = 0.08f)

    Canvas(modifier) {
        val grosor = 4.dp.toPx()
        // Vaso con forma de trapecio: más ancho arriba que abajo
        val estrechamiento = size.width * 0.12f
        val vaso = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - estrechamiento, size.height)
            lineTo(estrechamiento, size.height)
            close()
        }
        clipPath(vaso) {
            drawRect(fondo)
            val superficie = size.height * (1f - nivel)
            val amplitud = if (nivel in 0.01f..0.99f) 8.dp.toPx() else 0f
            val agua = Path().apply {
                moveTo(0f, size.height)
                var x = 0f
                while (x <= size.width) {
                    lineTo(x, superficie + amplitud * sin(x / size.width * 2 * PI.toFloat() * 1.5f + fase))
                    x += 6f
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(agua, Brush.verticalGradient(listOf(color.copy(alpha = 0.55f), color), startY = superficie, endY = size.height))
        }
        drawPath(vaso, borde, style = Stroke(width = grosor))
        drawRoundRect(borde, topLeft = Offset(-grosor, -grosor / 2), size = androidx.compose.ui.geometry.Size(size.width + grosor * 2, grosor), cornerRadius = CornerRadius(grosor))
    }
}
