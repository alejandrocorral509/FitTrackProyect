package com.example.fittrackproyect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fittrackproyect.ui.theme.DegradadoMarca
import com.example.fittrackproyect.util.Imagenes

/** Foto de perfil o, si no hay, la inicial del nombre sobre el degradado de marca. */
@Composable
fun Avatar(foto: String?, nombre: String, modifier: Modifier = Modifier, tamano: Dp = 44.dp) {
    // Se decodifica una sola vez por foto, no en cada recomposición
    val bytes = remember(foto) { foto?.let(Imagenes::aBytes) }
    Box(
        modifier
            .size(tamano)
            .clip(CircleShape)
            .background(DegradadoMarca),
        contentAlignment = Alignment.Center
    ) {
        if (bytes != null) {
            AsyncImage(
                model = bytes,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(tamano)
            )
        } else {
            Text(
                nombre.take(1).uppercase(),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = (tamano.value * 0.42f).sp
            )
        }
    }
}
