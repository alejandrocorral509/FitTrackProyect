package com.example.fittrackproyect.presentation.estadisticas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.GraficoBarras
import com.example.fittrackproyect.ui.components.IconoMetrica
import com.example.fittrackproyect.ui.components.Pastilla
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.theme.ColoresMetrica
import java.util.Locale

@Composable
fun EstadisticasScreen(viewModel: ProgresoViewModel = viewModel(factory = ProgresoViewModel.Factory)) {
    val s by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(topBar = { BarraSuperior("Progreso") }) { relleno ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(relleno)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Periodo.entries.forEach { periodo ->
                    Pastilla(periodo.etiqueta, s.periodo == periodo, { viewModel.elegir(periodo) })
                }
            }

            if (s.cargando) {
                Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                return@Column
            }

            // Rachas y totales
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Resumen(Icons.Filled.LocalFireDepartment, ColoresMetrica.calorias, "${s.rachaActual}", "racha actual", Modifier.weight(1f))
                Resumen(Icons.Filled.EmojiEvents, ColoresMetrica.grasas, "${s.mejorRacha}", "mejor racha", Modifier.weight(1f))
                Resumen(Icons.Filled.FitnessCenter, ColoresMetrica.entreno, "${s.totalEntrenos}", "entrenos", Modifier.weight(1f))
            }

            TarjetaGrafico(
                titulo = "Entrenos",
                detalle = "${s.diasEntrenados} de ${s.periodo.dias} días entrenados",
                icono = Icons.Filled.FitnessCenter,
                color = ColoresMetrica.entreno,
                valores = s.entrenos,
                etiquetas = s.etiquetas
            )
            TarjetaGrafico(
                titulo = "Calorías",
                detalle = "Media de ${s.mediaCalorias} kcal en los días registrados · objetivo ${s.caloriasObjetivo}",
                icono = Icons.Filled.Restaurant,
                color = ColoresMetrica.calorias,
                valores = s.calorias,
                etiquetas = s.etiquetas,
                objetivo = s.caloriasObjetivo
            )
            TarjetaGrafico(
                titulo = "Agua",
                detalle = String.format(Locale("es", "ES"), "Media de %.1f vasos · objetivo cumplido %d días", s.mediaVasos, s.diasObjetivoAgua),
                icono = Icons.Filled.WaterDrop,
                color = ColoresMetrica.agua,
                valores = s.vasos,
                etiquetas = s.etiquetas,
                objetivo = s.vasosObjetivo
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Resumen(icono: ImageVector, color: Color, valor: String, texto: String, modifier: Modifier) {
    Tarjeta(modifier, relleno = 14.dp) {
        Column {
            IconoMetrica(icono, color, tamano = 34.dp)
            Spacer(Modifier.height(8.dp))
            Text(valor, style = MaterialTheme.typography.headlineSmall)
            Text(texto, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TarjetaGrafico(
    titulo: String,
    detalle: String,
    icono: ImageVector,
    color: Color,
    valores: List<Int>,
    etiquetas: List<String>,
    objetivo: Int? = null
) {
    Tarjeta(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoMetrica(icono, color, tamano = 36.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(titulo, style = MaterialTheme.typography.titleMedium)
                    Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            GraficoBarras(valores, etiquetas, color, objetivo = objetivo, alto = 120.dp)
        }
    }
}
