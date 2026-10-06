package com.example.fittrackproyect.presentation.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.data.model.Ejercicio
import com.example.fittrackproyect.data.model.categoria
import com.example.fittrackproyect.data.model.categorias
import com.example.fittrackproyect.data.model.ejerciciosSugeridos
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.BotonPrincipal
import com.example.fittrackproyect.ui.components.CampoTexto
import com.example.fittrackproyect.ui.components.Pastilla
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.components.TituloSeccion

/** Pantalla única para crear una rutina nueva o editar una existente. */
@Composable
fun EditorRutinaScreen(
    alVolver: () -> Unit,
    viewModel: EditorRutinaViewModel = viewModel(factory = EditorRutinaViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { BarraSuperior(if (s.editando) "Editar rutina" else "Nueva rutina", alVolver = alVolver) },
        bottomBar = {
            Box(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                BotonPrincipal(
                    if (s.editando) "Guardar cambios" else "Guardar rutina",
                    { viewModel.guardar(alVolver) },
                    cargando = s.guardando,
                    activo = s.puedeGuardar,
                    icono = Icons.Filled.Save
                )
            }
        }
    ) { relleno ->
        if (s.cargando) {
            Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CampoTexto(s.nombre, viewModel::cambiarNombre, "Nombre de la rutina", marcador = "Ej.: Pierna A, Día de empuje...")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categorias.forEach { cat ->
                    Pastilla(cat.nombre, s.categoria == cat.nombre, { viewModel.cambiarCategoria(cat.nombre) }, color = Color(cat.color), emoji = cat.emoji)
                }
            }

            TituloSeccion("Ejercicios (${s.ejercicios.size})")
            if (s.ejercicios.isEmpty()) {
                Text("Añade el primer ejercicio con el formulario de abajo.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            s.ejercicios.forEachIndexed { i, ejercicio ->
                FilaEjercicio(
                    numero = i + 1,
                    ejercicio = ejercicio,
                    color = Color(categoria(s.categoria).color),
                    puedeSubir = i > 0,
                    puedeBajar = i < s.ejercicios.lastIndex,
                    alSubir = { viewModel.mover(i, true) },
                    alBajar = { viewModel.mover(i, false) },
                    alQuitar = { viewModel.quitarEjercicio(i) }
                )
            }

            Tarjeta(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Añadir ejercicio", style = MaterialTheme.typography.titleMedium)
                    CampoTexto(s.nuevoNombre, viewModel::cambiarNuevoNombre, "Ejercicio", marcador = "Press banca, sentadilla...")
                    val sugerencias = if (s.nuevoNombre.length >= 2)
                        ejerciciosSugeridos.filter { it.contains(s.nuevoNombre, true) && !it.equals(s.nuevoNombre, true) }.take(8)
                    else emptyList()
                    if (sugerencias.isNotEmpty()) {
                        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            sugerencias.forEach { Pastilla(it, false, { viewModel.cambiarNuevoNombre(it) }) }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CampoTexto(s.nuevasSeries, viewModel::cambiarSeries, "Series", Modifier.weight(1f), KeyboardType.Number, marcador = "3")
                        CampoTexto(s.nuevasReps, viewModel::cambiarReps, "Reps", Modifier.weight(1f), KeyboardType.Number, marcador = "10")
                        CampoTexto(s.nuevoPeso, viewModel::cambiarPeso, "Peso", Modifier.weight(1.1f), KeyboardType.Decimal, sufijo = "kg")
                    }
                    FilledTonalButton(onClick = viewModel::anadirEjercicio, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Añadir a la rutina")
                    }
                }
            }
            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FilaEjercicio(
    numero: Int,
    ejercicio: Ejercicio,
    color: Color,
    puedeSubir: Boolean,
    puedeBajar: Boolean,
    alSubir: () -> Unit,
    alBajar: () -> Unit,
    alQuitar: () -> Unit
) {
    Tarjeta(Modifier.fillMaxWidth(), relleno = 10.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(MaterialTheme.shapes.small).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text("$numero", style = MaterialTheme.typography.titleSmall, color = color) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(ejercicio.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(descripcion(ejercicio), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column {
                IconButton(onClick = alSubir, enabled = puedeSubir, modifier = Modifier.size(28.dp)) { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Subir") }
                IconButton(onClick = alBajar, enabled = puedeBajar, modifier = Modifier.size(28.dp)) { Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Bajar") }
            }
            IconButton(onClick = alQuitar) { Icon(Icons.Filled.Close, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error) }
        }
    }
}

fun descripcion(ejercicio: Ejercicio): String {
    val peso = if (ejercicio.pesoKg > 0) " · ${ejercicio.pesoKg.toString().removeSuffix(".0")} kg" else ""
    return "${ejercicio.series} series × ${ejercicio.repeticiones} reps$peso"
}
