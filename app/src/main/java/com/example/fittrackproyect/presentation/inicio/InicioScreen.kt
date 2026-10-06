package com.example.fittrackproyect.presentation.inicio

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.data.model.categoria
import com.example.fittrackproyect.ui.components.AnillosActividad
import com.example.fittrackproyect.ui.components.Avatar
import com.example.fittrackproyect.ui.components.BarraProgreso
import com.example.fittrackproyect.ui.components.DatoAnillo
import com.example.fittrackproyect.ui.components.IconoMetrica
import com.example.fittrackproyect.ui.components.Tarjeta
import com.example.fittrackproyect.ui.components.TituloSeccion
import com.example.fittrackproyect.ui.theme.ColoresMetrica
import com.example.fittrackproyect.ui.theme.DegradadoMarca

@Composable
fun InicioScreen(
    alIrAPerfil: () -> Unit,
    alIrAEntreno: () -> Unit,
    alEmpezarRutina: (String) -> Unit,
    alIrANutricion: () -> Unit,
    alIrAAgua: () -> Unit,
    viewModel: InicioViewModel = viewModel(factory = InicioViewModel.Factory)
) {
    val s by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera con saludo y foto
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(s.fecha, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${s.saludo},", style = MaterialTheme.typography.headlineSmall)
                Text(s.nombre, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }
            Avatar(s.fotoUrl, s.nombre, Modifier.clickable(onClick = alIrAPerfil), tamano = 52.dp)
        }

        if (!s.cargando && !s.perfilCompleto) {
            Tarjeta(Modifier.fillMaxWidth(), alPulsar = alIrAPerfil, color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                    "Completa tu perfil para calcular tus calorías, macros y agua recomendados →",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Anillos del día
        Tarjeta(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnillosActividad(
                    listOf(
                        DatoAnillo(s.progresoCalorias, ColoresMetrica.calorias),
                        DatoAnillo(s.progresoAgua, ColoresMetrica.agua),
                        DatoAnillo(s.progresoEntreno, ColoresMetrica.entreno)
                    ),
                    tamano = 140.dp
                )
                Spacer(Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LeyendaAnillo("Calorías", "${s.calorias}", "/ ${s.caloriasObjetivo} kcal", ColoresMetrica.calorias)
                    LeyendaAnillo("Agua", "${s.vasos}", "/ ${s.vasosObjetivo} vasos", ColoresMetrica.agua)
                    LeyendaAnillo("Entreno", if (s.rutinasHoy > 0) "Hecho" else "Pendiente", "", ColoresMetrica.entreno)
                }
            }
        }

        // Racha y agua rápida
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tarjeta(Modifier.weight(1f)) {
                Column {
                    IconoMetrica(Icons.Filled.LocalFireDepartment, ColoresMetrica.calorias, tamano = 40.dp)
                    Spacer(Modifier.height(10.dp))
                    Text("${s.racha} ${if (s.racha == 1) "día" else "días"}", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (s.racha > 0) "entrenando seguidos" else "Empieza tu racha hoy",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Tarjeta(Modifier.weight(1f), alPulsar = alIrAAgua) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconoMetrica(Icons.Filled.WaterDrop, ColoresMetrica.agua, tamano = 40.dp)
                        Spacer(Modifier.weight(1f))
                        FilledIconButton(
                            onClick = viewModel::sumarVaso,
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = ColoresMetrica.agua),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Añadir un vaso", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("${s.vasos} / ${s.vasosObjetivo}", style = MaterialTheme.typography.titleLarge)
                    Text("vasos de agua", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Entreno de hoy
        TituloSeccion("Entreno de hoy", accion = "Ver rutinas", alPulsarAccion = alIrAEntreno)
        val siguiente = s.siguienteRutina
        when {
            s.rutinasHoy > 0 -> TarjetaEntreno(
                titulo = "¡Entreno completado!",
                texto = "Hoy ya has hecho ${s.rutinasHoy} ${if (s.rutinasHoy == 1) "rutina" else "rutinas"}. Buen trabajo.",
                icono = Icons.Filled.FitnessCenter,
                alPulsar = alIrAEntreno
            )
            siguiente != null -> TarjetaEntreno(
                titulo = siguiente.nombre,
                texto = "${categoria(siguiente.categoria).emoji} ${siguiente.categoria} · ${siguiente.ejercicios.size} ejercicios",
                icono = Icons.Filled.PlayArrow,
                textoBoton = "Empezar",
                alPulsar = { alEmpezarRutina(siguiente.id) }
            )
            else -> TarjetaEntreno(
                titulo = "Crea tu primera rutina",
                texto = "Elige una plantilla o hazla a tu medida.",
                icono = Icons.Filled.Add,
                alPulsar = alIrAEntreno
            )
        }

        // Nutrición
        TituloSeccion("Nutrición", accion = "Ver diario", alPulsarAccion = alIrANutricion)
        Tarjeta(Modifier.fillMaxWidth(), alPulsar = alIrANutricion) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconoMetrica(Icons.Filled.Restaurant, ColoresMetrica.calorias, tamano = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        val restantes = s.caloriasObjetivo - s.calorias
                        Text(
                            if (restantes >= 0) "Te quedan $restantes kcal" else "${-restantes} kcal por encima",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("${s.calorias} de ${s.caloriasObjetivo} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                BarraProgreso(s.progresoCalorias, ColoresMetrica.calorias)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Macro("Proteínas", s.proteinas, ColoresMetrica.proteinas, Modifier.weight(1f))
                    Macro("Carbos", s.carbos, ColoresMetrica.carbos, Modifier.weight(1f))
                    Macro("Grasas", s.grasas, ColoresMetrica.grasas, Modifier.weight(1f))
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun LeyendaAnillo(titulo: String, valor: String, detalle: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(valor, style = MaterialTheme.typography.titleMedium, color = color)
                if (detalle.isNotEmpty()) {
                    Spacer(Modifier.width(4.dp))
                    Text(detalle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun TarjetaEntreno(titulo: String, texto: String, icono: ImageVector, alPulsar: () -> Unit, textoBoton: String? = null) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(DegradadoMarca)
            .clickable(onClick = alPulsar)
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleLarge, color = Color(0xFF052E16), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(texto, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF14532D))
            }
            Spacer(Modifier.width(12.dp))
            Row(
                Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF052E16))
                    .padding(horizontal = if (textoBoton != null) 16.dp else 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                if (textoBoton != null) {
                    Spacer(Modifier.width(6.dp))
                    Text(textoBoton, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun Macro(nombre: String, gramos: Int, color: Color, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text("$gramos g", style = MaterialTheme.typography.titleSmall, color = color)
        Text(nombre, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
