package com.example.fittrackproyect.presentation.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.data.model.categoria
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.Tarjeta
import kotlinx.coroutines.launch

@Composable
fun PlantillasScreen(
    alVolver: () -> Unit,
    viewModel: PlantillasViewModel = viewModel(factory = PlantillasViewModel.Factory)
) {
    val anadidas by viewModel.anadidas.collectAsStateWithLifecycle()
    val avisos = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { BarraSuperior("Plantillas", alVolver = alVolver, subtitulo = "${viewModel.lista.size} rutinas listas para usar") },
        snackbarHost = { SnackbarHost(avisos) }
    ) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(viewModel.lista, key = { it.nombre }) { plantilla ->
                val cat = categoria(plantilla.categoria)
                val color = Color(cat.color)
                val anadida = plantilla.nombre in anadidas
                Tarjeta(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).clip(MaterialTheme.shapes.small).background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) { Text(cat.emoji, style = MaterialTheme.typography.titleLarge) }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(plantilla.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("${plantilla.categoria} · ${plantilla.ejercicios.size} ejercicios", style = MaterialTheme.typography.labelMedium, color = color)
                            }
                        }
                        Text(plantilla.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        plantilla.ejercicios.forEach { ejercicio ->
                            Row {
                                Text("•  ${ejercicio.nombre}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text("${ejercicio.series}×${ejercicio.repeticiones}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        FilledTonalButton(
                            onClick = {
                                viewModel.anadir(plantilla) {
                                    scope.launch { avisos.showSnackbar("\"${plantilla.nombre}\" añadida a tus rutinas") }
                                }
                            },
                            enabled = !anadida,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(if (anadida) Icons.Filled.Check else Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (anadida) "Añadida" else "Añadir a mis rutinas")
                        }
                    }
                }
            }
        }
    }
}
