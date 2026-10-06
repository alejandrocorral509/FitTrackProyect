package com.example.fittrackproyect.presentation.sesion

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.data.model.Ejercicio
import com.example.fittrackproyect.presentation.rutinas.descripcion
import com.example.fittrackproyect.ui.components.AnilloProgreso
import com.example.fittrackproyect.ui.components.BarraProgreso
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.BotonPrincipal
import com.example.fittrackproyect.ui.components.Pastilla
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.theme.ColoresMetrica

/** Modo entreno: se siguen las series de una rutina con cronómetro y descansos. */
@Composable
fun SesionScreen(
    alSalir: () -> Unit,
    viewModel: SesionViewModel = viewModel(factory = SesionViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()
    var confirmarSalida by remember { mutableStateOf(false) }
    val haptico = LocalHapticFeedback.current

    // La pantalla no se apaga mientras se entrena
    val vista = LocalView.current
    DisposableEffect(Unit) {
        vista.keepScreenOn = true
        onDispose { vista.keepScreenOn = false }
    }
    // Vibración corta al acabar el descanso
    var habiaDescanso by remember { mutableStateOf(false) }
    LaunchedEffect(s.descansoRestante) {
        if (habiaDescanso && s.descansoRestante == null && !s.terminado) haptico.performHapticFeedback(HapticFeedbackType.LongPress)
        habiaDescanso = s.descansoRestante != null
    }

    val salir: () -> Unit = {
        if (s.seriesCompletadas > 0 && !s.terminado) confirmarSalida = true else alSalir()
    }
    BackHandler(onBack = salir)

    Scaffold(
        topBar = {
            BarraSuperior(
                s.rutina?.nombre ?: "Entreno",
                subtitulo = "⏱ ${formatoTiempo(s.segundos)}"
            ) {
                IconButton(onClick = salir) { Icon(Icons.Filled.Close, contentDescription = "Salir") }
            }
        },
        bottomBar = {
            if (s.rutina != null) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    BotonPrincipal(
                        if (s.todoHecho) "Terminar entreno" else "Terminar ahora",
                        viewModel::terminar,
                        cargando = s.guardando,
                        icono = Icons.Filled.Flag
                    )
                }
            }
        }
    ) { relleno ->
        when {
            s.cargando -> Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            s.rutina == null -> Box(Modifier.fillMaxSize().padding(relleno), contentAlignment = Alignment.Center) {
                Text("No se ha encontrado la rutina", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(relleno),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column {
                        Row {
                            Text("Progreso", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                            Text("${s.seriesCompletadas} de ${s.totalSeries} series", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(8.dp))
                        BarraProgreso(s.progreso, MaterialTheme.colorScheme.primary, alto = 10.dp)
                    }
                }
                item {
                    AnimatedVisibility(s.descansoRestante != null, enter = expandVertically(), exit = shrinkVertically()) {
                        TarjetaDescanso(s, viewModel)
                    }
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Descanso:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf(60, 90, 120).forEach { segundos ->
                            Pastilla("$segundos s", s.descansoElegido == segundos, { viewModel.elegirDescanso(segundos) })
                        }
                    }
                }
                itemsIndexed(s.rutina!!.ejercicios) { i, ejercicio ->
                    TarjetaEjercicio(i + 1, ejercicio, s.seriesHechas.getOrElse(i) { 0 }) { serie -> viewModel.tocarSerie(i, serie) }
                }
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Salir del entreno?") },
            text = { Text("Perderás las series marcadas. Si has acabado, pulsa «Terminar» para guardarlo.") },
            confirmButton = { TextButton(onClick = { confirmarSalida = false; alSalir() }) { Text("Salir", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Seguir entrenando") } }
        )
    }

    if (s.terminado) {
        AlertDialog(
            onDismissRequest = alSalir,
            icon = { Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = ColoresMetrica.grasas, modifier = Modifier.size(40.dp)) },
            title = { Text("¡Entreno completado!") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("⏱ Duración: ${formatoTiempo(s.segundos)}")
                    Text("💪 Series: ${s.seriesCompletadas} de ${s.totalSeries}")
                    Text("📋 Ejercicios: ${s.rutina?.ejercicios?.size ?: 0}")
                    Text("Queda guardado en tu historial y suma a tu racha.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = { TextButton(onClick = alSalir) { Text("Genial") } }
        )
    }
}

@Composable
private fun TarjetaDescanso(s: SesionUiState, viewModel: SesionViewModel) {
    val restante = s.descansoRestante ?: 0
    Tarjeta(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnilloProgreso(restante.toFloat() / s.descansoElegido, MaterialTheme.colorScheme.primary, tamano = 84.dp, grosor = 8.dp) {
                Text(formatoTiempo(restante.toLong()), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Descansa", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = { viewModel.sumarDescanso(15) }) { Text("+15 s") }
                    FilledTonalButton(onClick = viewModel::saltarDescanso) { Text("Saltar") }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TarjetaEjercicio(numero: Int, ejercicio: Ejercicio, hechas: Int, alTocarSerie: (Int) -> Unit) {
    val series = ejercicio.series.coerceAtLeast(1)
    val completo = hechas >= series
    Tarjeta(Modifier.fillMaxWidth(), color = if (completo) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceContainer) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$numero", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(ejercicio.nombre, style = MaterialTheme.typography.titleMedium)
                    Text(descripcion(ejercicio), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (completo) Icon(Icons.Filled.Check, contentDescription = "Completado", tint = MaterialTheme.colorScheme.primary)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(series) { serie ->
                    val hecha = serie < hechas
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (hecha) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(1.dp, if (hecha) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { alTocarSerie(serie) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (hecha) Icon(Icons.Filled.Check, contentDescription = "Serie ${serie + 1} hecha", tint = MaterialTheme.colorScheme.onPrimary)
                        else Text("${serie + 1}", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

private fun formatoTiempo(segundos: Long): String {
    val h = segundos / 3600
    val m = (segundos % 3600) / 60
    val s = segundos % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
