package com.example.fittrackproyect.presentation.dieta

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.data.model.Comida
import com.example.fittrackproyect.data.model.TipoComida
import com.example.fittrackproyect.ui.components.AnilloProgreso
import com.example.fittrackproyect.ui.components.BarraProgreso
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.theme.ColoresMetrica
import kotlinx.coroutines.launch

private val emojiComida = mapOf(
    TipoComida.DESAYUNO to "🥣",
    TipoComida.ALMUERZO to "🍽️",
    TipoComida.CENA to "🌙",
    TipoComida.SNACKS to "🍎",
)

@Composable
fun DietaScreen(
    alAnadir: (tipo: String, fecha: String) -> Unit,
    viewModel: DietaViewModel = viewModel(factory = DietaViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()
    val avisos = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val alBorrar: (Comida) -> Unit = { comida ->
        val dia = s.claveFecha
        viewModel.eliminar(comida)
        scope.launch {
            val resultado = avisos.showSnackbar(
                "Eliminado: ${comida.nombre.substringBefore(" (")}",
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Short
            )
            if (resultado == SnackbarResult.ActionPerformed) viewModel.restaurar(comida, dia)
        }
    }

    Scaffold(
        topBar = { BarraSuperior("Nutrición") },
        snackbarHost = { SnackbarHost(avisos) }
    ) { relleno ->
        LazyColumn(
            Modifier.fillMaxSize().padding(relleno),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = viewModel::diaAnterior) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Día anterior") }
                    Text(
                        s.fechaTexto,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (s.esHoy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    IconButton(onClick = viewModel::diaSiguiente, enabled = !s.esHoy) { Icon(Icons.Filled.ChevronRight, contentDescription = "Día siguiente") }
                }
            }
            item { ResumenDia(s) }
            TipoComida.entries.forEach { tipo ->
                item(key = tipo.name) {
                    SeccionComida(
                        tipo = tipo,
                        comidas = s.deTipo(tipo),
                        alAnadir = { alAnadir(tipo.etiqueta, s.claveFecha) },
                        alBorrar = alBorrar
                    )
                }
            }
        }
    }
}

@Composable
private fun ResumenDia(s: DietaUiState) {
    val restantes = s.caloriasObjetivo - s.calorias
    Tarjeta(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AnilloProgreso(s.calorias.toFloat() / s.caloriasObjetivo, ColoresMetrica.calorias, tamano = 128.dp, grosor = 12.dp) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${kotlin.math.abs(restantes)}", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (restantes >= 0) "kcal restantes" else "kcal de más",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val objetivo = s.macrosObjetivo
                BarraMacro("Proteínas", s.proteinas, objetivo?.proteinas, ColoresMetrica.proteinas)
                BarraMacro("Carbos", s.carbos, objetivo?.carbos, ColoresMetrica.carbos)
                BarraMacro("Grasas", s.grasas, objetivo?.grasas, ColoresMetrica.grasas)
            }
        }
    }
}

@Composable
private fun BarraMacro(nombre: String, gramos: Int, objetivo: Int?, color: Color) {
    Column {
        Row {
            Text(nombre, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(if (objetivo != null) "$gramos / $objetivo g" else "$gramos g", style = MaterialTheme.typography.labelMedium)
        }
        Spacer(Modifier.height(4.dp))
        BarraProgreso(if (objetivo != null && objetivo > 0) gramos.toFloat() / objetivo else 0f, color, alto = 6.dp)
    }
}

@Composable
private fun SeccionComida(tipo: TipoComida, comidas: List<Comida>, alAnadir: () -> Unit, alBorrar: (Comida) -> Unit) {
    Tarjeta(Modifier.fillMaxWidth(), relleno = 0.dp) {
        Column {
            Row(Modifier.padding(start = 18.dp, end = 8.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(emojiComida[tipo].orEmpty(), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(tipo.etiqueta, style = MaterialTheme.typography.titleMedium)
                    Text("${comidas.sumOf { it.calorias }} kcal", style = MaterialTheme.typography.bodySmall, color = ColoresMetrica.calorias)
                }
                FilledTonalIconButton(onClick = alAnadir) { Icon(Icons.Filled.Add, contentDescription = "Añadir a ${tipo.etiqueta}") }
            }
            if (comidas.isEmpty()) {
                Text(
                    "Toca + para añadir alimentos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 18.dp, bottom = 14.dp)
                )
            } else {
                comidas.forEach { comida ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    FilaComida(comida, alBorrar)
                }
            }
        }
    }
}

/** Fila de alimento que se borra deslizándola hacia la izquierda. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilaComida(comida: Comida, alBorrar: (Comida) -> Unit) {
    val estadoDeslizar = rememberSwipeToDismissBoxState(
        confirmValueChange = { valor ->
            if (valor == SwipeToDismissBoxValue.EndToStart) alBorrar(comida)
            valor == SwipeToDismissBoxValue.EndToStart
        }
    )
    SwipeToDismissBox(
        state = estadoDeslizar,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier.fillMaxSize().background(ColoresMetrica.peligro).padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) { Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.White) }
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(comida.nombre, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "P ${comida.proteinas} g · C ${comida.carbos} g · G ${comida.grasas} g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Text("${comida.calorias} kcal", style = MaterialTheme.typography.labelLarge)
        }
    }
}
