package com.example.fittrackproyect.presentation.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.data.model.Rutina
import com.example.fittrackproyect.data.model.categoria
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.EstadoVacio
import com.example.fittrackproyect.ui.components.Pastilla
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.theme.ColoresMetrica
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.launch

@Composable
fun RutinasScreen(
    alCrear: () -> Unit,
    alEditar: (String) -> Unit,
    alEmpezar: (String) -> Unit,
    alVerPlantillas: () -> Unit,
    viewModel: RutinasViewModel = viewModel(factory = RutinasViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()
    val avisos = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            BarraSuperior("Entreno") {
                IconButton(onClick = alVerPlantillas) { Icon(Icons.Filled.LibraryBooks, contentDescription = "Plantillas") }
            }
        },
        snackbarHost = { SnackbarHost(avisos) },
        floatingActionButton = {
            if (s.rutinas.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = alCrear,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nueva rutina") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SemanaEntreno(s) }

            if (!s.cargando && s.rutinas.isEmpty()) {
                item {
                    EstadoVacio(
                        Icons.Filled.FitnessCenter,
                        "Todavía no tienes rutinas",
                        "Empieza con una plantilla o crea la tuya ejercicio a ejercicio."
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = alVerPlantillas) { Text("Ver plantillas") }
                            OutlinedButton(onClick = alCrear) { Text("Crear rutina") }
                        }
                    }
                }
            }

            if (s.categoriasUsadas.size > 1) {
                item {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pastilla("Todas", s.filtro == null, { viewModel.filtrar(null) })
                        s.categoriasUsadas.forEach { nombre ->
                            val cat = categoria(nombre)
                            Pastilla(nombre, s.filtro == nombre, { viewModel.filtrar(nombre) }, color = Color(cat.color), emoji = cat.emoji)
                        }
                    }
                }
            }

            items(s.visibles, key = { it.id }) { rutina ->
                TarjetaRutina(
                    rutina = rutina,
                    hechaHoy = rutina.completadaEl(s.hoy),
                    alEmpezar = { alEmpezar(rutina.id) },
                    alEditar = { alEditar(rutina.id) },
                    alAlternarHecha = { viewModel.alternarHecha(rutina) },
                    alEliminar = {
                        viewModel.eliminar(rutina)
                        scope.launch {
                            val r = avisos.showSnackbar("Rutina \"${rutina.nombre}\" eliminada", actionLabel = "Deshacer")
                            if (r == SnackbarResult.ActionPerformed) viewModel.restaurar(rutina)
                        }
                    }
                )
            }
        }
    }
}

/** Los últimos 7 días con un punto en los que se entrenó, y la racha actual. */
@Composable
private fun SemanaEntreno(s: RutinasUiState) {
    Tarjeta(Modifier.fillMaxWidth()) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Tu semana", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${s.semana.count { it.entrenado }} de 7 días entrenados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    Modifier.clip(CircleShape).background(ColoresMetrica.calorias.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = ColoresMetrica.calorias, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${s.racha}", style = MaterialTheme.typography.titleSmall, color = ColoresMetrica.calorias)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                s.semana.forEachIndexed { i, dia ->
                    val esHoy = i == s.semana.lastIndex
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            Fechas.inicial(dia.fecha),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (esHoy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (dia.entrenado) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceContainerHighest
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dia.entrenado) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                            else Text("${dia.fecha.dayOfMonth}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRutina(
    rutina: Rutina,
    hechaHoy: Boolean,
    alEmpezar: () -> Unit,
    alEditar: () -> Unit,
    alAlternarHecha: () -> Unit,
    alEliminar: () -> Unit
) {
    val cat = categoria(rutina.categoria)
    val color = Color(cat.color)
    var menu by remember { mutableStateOf(false) }

    Tarjeta(Modifier.fillMaxWidth(), alPulsar = alEmpezar, relleno = 16.dp) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(MaterialTheme.shapes.small).background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Text(cat.emoji, style = MaterialTheme.typography.titleLarge) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(rutina.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(rutina.categoria, style = MaterialTheme.typography.labelMedium, color = color)
                        Text(" · ${rutina.ejercicios.size} ejercicios", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                if (hechaHoy) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Hecha hoy", tint = MaterialTheme.colorScheme.primary)
                } else {
                    FilledIconButton(onClick = alEmpezar, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Empezar")
                    }
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (hechaHoy) "Desmarcar de hoy" else "Marcar como hecha hoy") },
                            leadingIcon = { Icon(if (hechaHoy) Icons.Outlined.CheckCircle else Icons.Filled.CheckCircle, contentDescription = null) },
                            onClick = { menu = false; alAlternarHecha() }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = { menu = false; alEditar() }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { menu = false; alEliminar() }
                        )
                    }
                }
            }
            if (rutina.ejercicios.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    rutina.ejercicios.joinToString(" · ") { it.nombre },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
