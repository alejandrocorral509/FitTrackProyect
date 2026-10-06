package com.example.fittrackproyect.presentation.dieta

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.data.model.Alimento
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.BotonPrincipal
import com.example.fittrackproyect.ui.components.CampoTexto
import com.example.fittrackproyect.ui.components.Pastilla
import com.example.fittrackproyect.ui.theme.ColoresMetrica

@Composable
fun BuscarAlimentoScreen(
    alVolver: () -> Unit,
    viewModel: BuscarAlimentoViewModel = viewModel(factory = BuscarAlimentoViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()
    val foco = remember { FocusRequester() }
    LaunchedEffect(Unit) { foco.requestFocus() }

    Scaffold(topBar = { BarraSuperior("Añadir a ${s.tipo.etiqueta}", alVolver = alVolver) }) { relleno ->
        Column(Modifier.fillMaxSize().padding(relleno)) {
            CampoTexto(
                s.texto, viewModel::cambiarTexto, "Buscar alimento",
                Modifier.padding(horizontal = 20.dp).focusRequester(foco),
                marcador = "Pollo, avena, yogur...",
                icono = Icons.Filled.Search,
                finalIcono = if (s.texto.isNotEmpty()) ({
                    IconButton(onClick = { viewModel.cambiarTexto("") }) { Icon(Icons.Filled.Close, contentDescription = "Borrar") }
                }) else null
            )
            LazyColumn(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 32.dp)) {
                if (s.locales.isNotEmpty()) {
                    item { Cabecera(if (s.texto.isBlank()) "Alimentos frecuentes" else "En tu lista") }
                    items(s.locales, key = { "l" + it.nombre }) { FilaAlimento(it) { viewModel.seleccionar(it) } }
                }
                if (s.buscando) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
                if (s.online.isNotEmpty()) {
                    item { Cabecera("Open Food Facts") }
                    items(s.online, key = { "o" + it.nombre }) { FilaAlimento(it) { viewModel.seleccionar(it) } }
                }
                if (s.sinResultados) {
                    item {
                        Text(
                            "No hay resultados para \"${s.texto}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 32.dp)
                        )
                    }
                }
            }
        }
    }

    s.seleccionado?.let { alimento ->
        HojaCantidad(alimento, s, viewModel, alVolver)
    }
}

@Composable
private fun Cabecera(texto: String) {
    Text(
        texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun FilaAlimento(alimento: Alimento, alPulsar: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = alPulsar).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(alimento.nombre, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "P ${alimento.proteinas.toInt()} · C ${alimento.carbos.toInt()} · G ${alimento.grasas.toInt()} · por 100 g",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Text("${alimento.calorias.toInt()} kcal", style = MaterialTheme.typography.labelLarge, color = ColoresMetrica.calorias)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    }
}

/** Hoja inferior para elegir los gramos y ver los macros antes de añadir. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaCantidad(alimento: Alimento, s: BuscarUiState, viewModel: BuscarAlimentoViewModel, alVolver: () -> Unit) {
    val hoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val comida = alimento.paraGramos(s.gramosNum, s.tipo)
    ModalBottomSheet(onDismissRequest = { viewModel.seleccionar(null) }, sheetState = hoja) {
        Column(
            Modifier.padding(horizontal = 24.dp).navigationBarsPadding().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(alimento.nombre, style = MaterialTheme.typography.titleLarge)
            CampoTexto(s.gramos, viewModel::cambiarGramos, "Cantidad", teclado = KeyboardType.Number, sufijo = "g")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(50, 100, 150, 200).forEach { g ->
                    Pastilla("$g g", s.gramosNum == g, { viewModel.cambiarGramos(g.toString()) }, Modifier.weight(1f))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ValorMacro("${comida.calorias}", "kcal", ColoresMetrica.calorias, Modifier.weight(1f))
                ValorMacro("${comida.proteinas} g", "Proteínas", ColoresMetrica.proteinas, Modifier.weight(1f))
                ValorMacro("${comida.carbos} g", "Carbos", ColoresMetrica.carbos, Modifier.weight(1f))
                ValorMacro("${comida.grasas} g", "Grasas", ColoresMetrica.grasas, Modifier.weight(1f))
            }
            BotonPrincipal("Añadir a ${s.tipo.etiqueta}", { viewModel.anadir(alVolver) }, cargando = s.guardando)
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ValorMacro(valor: String, nombre: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.titleMedium, color = color)
        Text(nombre, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
