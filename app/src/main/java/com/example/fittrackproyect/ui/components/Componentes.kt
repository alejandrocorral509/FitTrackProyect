package com.example.fittrackproyect.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.KeyboardOptions

/** Barra superior estándar de las pantallas secundarias. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    alVolver: (() -> Unit)? = null,
    subtitulo: String? = null,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(titulo, style = MaterialTheme.typography.titleLarge)
                if (subtitulo != null) {
                    Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        navigationIcon = {
            if (alVolver != null) {
                IconButton(onClick = alVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        },
        actions = acciones,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

/** Tarjeta base de la app: superficie con bordes redondeados. */
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    alPulsar: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    relleno: Dp = 18.dp,
    contenido: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .then(if (alPulsar != null) Modifier.clickable(onClick = alPulsar) else Modifier),
        color = color,
        shape = MaterialTheme.shapes.large,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Box(Modifier.padding(relleno)) { contenido() }
    }
}

/** Anillo de progreso animado, con contenido en el centro. */
@Composable
fun AnilloProgreso(
    progreso: Float,
    color: Color,
    modifier: Modifier = Modifier,
    tamano: Dp = 120.dp,
    grosor: Dp = 12.dp,
    contenido: @Composable () -> Unit = {}
) {
    val animado by animateFloatAsState(progreso.coerceIn(0f, 1f), tween(900), label = "anillo")
    val fondo = color.copy(alpha = 0.15f)
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(tamano)) {
            val trazo = Stroke(width = grosor.toPx(), cap = StrokeCap.Round)
            val inset = grosor.toPx() / 2
            val area = androidx.compose.ui.geometry.Size(size.width - inset * 2, size.height - inset * 2)
            val origen = androidx.compose.ui.geometry.Offset(inset, inset)
            drawArc(fondo, -90f, 360f, false, origen, area, style = trazo)
            drawArc(color, -90f, 360f * animado, false, origen, area, style = trazo)
        }
        contenido()
    }
}

data class DatoAnillo(val progreso: Float, val color: Color)

/** Tres anillos concéntricos: calorías, agua y entreno. */
@Composable
fun AnillosActividad(datos: List<DatoAnillo>, modifier: Modifier = Modifier, tamano: Dp = 150.dp) {
    val grosor = 14.dp
    val separacion = 4.dp
    Box(modifier.size(tamano), contentAlignment = Alignment.Center) {
        datos.forEachIndexed { i, dato ->
            AnilloProgreso(
                progreso = dato.progreso,
                color = dato.color,
                tamano = tamano - (grosor + separacion) * 2 * i,
                grosor = grosor
            )
        }
    }
}

/** Barra de progreso horizontal animada. */
@Composable
fun BarraProgreso(progreso: Float, color: Color, modifier: Modifier = Modifier, alto: Dp = 8.dp) {
    val animado by animateFloatAsState(progreso.coerceIn(0f, 1f), tween(700), label = "barra")
    Box(
        modifier
            .fillMaxWidth()
            .height(alto)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(animado)
                .height(alto)
                .clip(CircleShape)
                .background(color)
        )
    }
}

/** Icono dentro de un cuadrado de color suave. */
@Composable
fun IconoMetrica(icono: ImageVector, color: Color, modifier: Modifier = Modifier, tamano: Dp = 44.dp) {
    Box(
        modifier
            .size(tamano)
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(tamano * 0.55f))
    }
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier, accion: String? = null, alPulsarAccion: () -> Unit = {}) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(texto, style = MaterialTheme.typography.titleMedium)
        if (accion != null) {
            TextButton(onClick = alPulsarAccion) { Text(accion) }
        }
    }
}

@Composable
fun EstadoVacio(icono: ImageVector, titulo: String, texto: String, modifier: Modifier = Modifier, accion: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconoMetrica(icono, MaterialTheme.colorScheme.primary, tamano = 64.dp)
        Spacer(Modifier.height(16.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (accion != null) {
            Spacer(Modifier.height(16.dp))
            accion()
        }
    }
}

@Composable
fun BotonPrincipal(
    texto: String,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    activo: Boolean = true,
    icono: ImageVector? = null
) {
    Button(
        onClick = alPulsar,
        enabled = activo && !cargando,
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(22.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(texto, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text,
    marcador: String? = null,
    icono: ImageVector? = null,
    sufijo: String? = null,
    transformacion: VisualTransformation = VisualTransformation.None,
    finalIcono: (@Composable () -> Unit)? = null,
    error: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        placeholder = marcador?.let { { Text(it) } },
        leadingIcon = icono?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = finalIcono,
        suffix = sufijo?.let { { Text(it) } },
        singleLine = true,
        isError = error,
        visualTransformation = transformacion,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/** Pastilla seleccionable (categorías, filtros, sexo...). */
@Composable
fun Pastilla(
    texto: String,
    seleccionada: Boolean,
    alPulsar: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    emoji: String? = null
) {
    val forma = RoundedCornerShape(50)
    Row(
        modifier
            .clip(forma)
            .background(if (seleccionada) color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, if (seleccionada) color else MaterialTheme.colorScheme.outline, forma)
            .clickable(onClick = alPulsar)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (emoji != null) {
            Text(emoji, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            texto,
            style = MaterialTheme.typography.labelLarge,
            color = if (seleccionada) color else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (seleccionada) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
