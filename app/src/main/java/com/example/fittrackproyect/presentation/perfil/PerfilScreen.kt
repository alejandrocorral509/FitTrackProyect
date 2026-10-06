package com.example.fittrackproyect.presentation.perfil

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.domain.CategoriaImc
import com.example.fittrackproyect.domain.NivelActividad
import com.example.fittrackproyect.ui.components.Avatar
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.BotonPrincipal
import com.example.fittrackproyect.ui.components.CampoTexto
import com.example.fittrackproyect.ui.components.IconoMetrica
import com.example.fittrackproyect.ui.components.Pastilla
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.components.TituloSeccion
import com.example.fittrackproyect.ui.theme.ColoresMetrica
import java.util.Locale

/**
 * Pantalla de perfil. En modo [bienvenida] se usa justo después de registrarse
 * para pedir los datos con los que se calculan los objetivos.
 */
@Composable
fun PerfilScreen(
    bienvenida: Boolean,
    alGuardar: () -> Unit,
    alCerrarSesion: () -> Unit,
    alSaltar: () -> Unit = {},
    viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()
    val avisos = remember { SnackbarHostState() }
    val selectorFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(viewModel::subirFoto)
    }
    LaunchedEffect(s.mensaje) {
        s.mensaje?.let { avisos.showSnackbar(it); viewModel.mensajeMostrado() }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                if (bienvenida) "Cuéntanos sobre ti" else "Perfil",
                subtitulo = if (bienvenida) "Con estos datos calculamos tus objetivos" else null
            )
        },
        snackbarHost = { SnackbarHost(avisos) }
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Foto y datos de la cuenta
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.clickable {
                        selectorFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Avatar(s.fotoUrl, s.nombre, tamano = 76.dp)
                    Box(
                        Modifier.size(26.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (s.subiendoFoto) {
                            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Cambiar foto", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
                        }
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(s.nombre, style = MaterialTheme.typography.titleLarge)
                    Text(s.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            TituloSeccion("Tus datos")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CampoTexto(s.peso, viewModel::cambiarPeso, "Peso", Modifier.weight(1f), KeyboardType.Decimal, sufijo = "kg")
                CampoTexto(s.estatura, viewModel::cambiarEstatura, "Estatura", Modifier.weight(1f), KeyboardType.Number, sufijo = "cm")
                CampoTexto(s.edad, viewModel::cambiarEdad, "Edad", Modifier.weight(0.8f), KeyboardType.Number)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Pastilla("Hombre", s.esHombre, { viewModel.cambiarSexo(true) }, Modifier.weight(1f))
                Pastilla("Mujer", !s.esHombre, { viewModel.cambiarSexo(false) }, Modifier.weight(1f))
            }

            TituloSeccion("Nivel de actividad")
            Tarjeta(Modifier.fillMaxWidth(), relleno = 4.dp) {
                Column {
                    NivelActividad.entries.forEachIndexed { i, nivel ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable { viewModel.cambiarNivel(nivel) }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                nivel.etiqueta,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (s.nivel == nivel) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            if (s.nivel == nivel) Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        if (i < NivelActividad.entries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    }
                }
            }

            ResultadosCalculados(s)

            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            BotonPrincipal(
                if (bienvenida) "Empezar" else "Guardar cambios",
                { viewModel.guardar(alGuardar) },
                cargando = s.guardando
            )
            if (bienvenida) {
                androidx.compose.material3.TextButton(onClick = alSaltar, modifier = Modifier.fillMaxWidth()) {
                    Text("Ahora no, lo haré más tarde")
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.cerrarSesion(); alCerrarSesion() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ResultadosCalculados(s: PerfilUiState) {
    val calorias = s.calorias ?: return
    val macros = s.macros ?: return
    Tarjeta(Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Tus objetivos", style = MaterialTheme.typography.titleMedium)
            s.imc?.let { imc ->
                val color = colorImc(s.categoriaImc)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Índice de masa corporal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(s.categoriaImc?.etiqueta.orEmpty(), style = MaterialTheme.typography.titleSmall, color = color)
                    }
                    Text(String.format(Locale("es", "ES"), "%.1f", imc), style = MaterialTheme.typography.headlineSmall, color = color)
                }
                EscalaImc(imc)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Calorías al día", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fórmula de Mifflin-St Jeor", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("$calorias kcal", style = MaterialTheme.typography.headlineSmall, color = ColoresMetrica.calorias)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ObjetivoMacro("Proteínas", macros.proteinas, ColoresMetrica.proteinas, Modifier.weight(1f))
                ObjetivoMacro("Carbos", macros.carbos, ColoresMetrica.carbos, Modifier.weight(1f))
                ObjetivoMacro("Grasas", macros.grasas, ColoresMetrica.grasas, Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconoMetrica(Icons.Filled.WaterDrop, ColoresMetrica.agua, tamano = 36.dp)
                Spacer(Modifier.width(12.dp))
                Text("${s.vasos} vasos de agua al día (${s.vasos * 250} ml)", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

/** Barra con los cuatro tramos del IMC y un marcador en el valor actual. */
@Composable
private fun EscalaImc(imc: Double) {
    val tramos = listOf(
        18.5 to colorImc(CategoriaImc.BAJO_PESO),
        6.5 to colorImc(CategoriaImc.NORMAL),
        5.0 to colorImc(CategoriaImc.SOBREPESO),
        10.0 to colorImc(CategoriaImc.OBESIDAD)
    )
    val posicion = ((imc - 10) / 30).coerceIn(0.0, 1.0).toFloat()
    Column {
        Row(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape)) {
            tramos.forEachIndexed { i, (ancho, color) ->
                val peso = if (i == 0) (ancho - 10).toFloat() else ancho.toFloat()
                Box(Modifier.weight(peso).fillMaxSize().background(color))
            }
        }
        Box(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth()) {
                if (posicion > 0f) Spacer(Modifier.weight(posicion))
                Box(Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface))
                if (posicion < 1f) Spacer(Modifier.weight(1f - posicion))
            }
        }
    }
}

@Composable
private fun ObjetivoMacro(nombre: String, gramos: Int, color: Color, modifier: Modifier) {
    Column(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = 0.12f))
            .padding(12.dp)
    ) {
        Text("$gramos g", style = MaterialTheme.typography.titleMedium, color = color)
        Text(nombre, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun colorImc(categoria: CategoriaImc?): Color = when (categoria) {
    CategoriaImc.BAJO_PESO -> ColoresMetrica.agua
    CategoriaImc.NORMAL -> ColoresMetrica.entreno
    CategoriaImc.SOBREPESO -> ColoresMetrica.grasas
    CategoriaImc.OBESIDAD, null -> ColoresMetrica.peligro
}
