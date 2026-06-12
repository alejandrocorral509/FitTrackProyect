package com.example.fittrackproyect.presentation.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    auth: FirebaseAuth,
    navigateToProfile: () -> Unit = {},
    navigateToRutinas: () -> Unit = {},
    navigateToAgua: () -> Unit = {},
    navigateToDieta: () -> Unit = {},
    navigateToInitial: () -> Unit = {},
    navigateToEstadisticas: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var totalRutinas by rememberSaveable { mutableIntStateOf(0) }
    var rutinasCompletadas by rememberSaveable { mutableIntStateOf(0) }
    var caloriasHoy by rememberSaveable { mutableIntStateOf(0) }
    var objetivoCalorias by rememberSaveable { mutableIntStateOf(2000) }
    var vasosHoy by rememberSaveable { mutableIntStateOf(0) }
    var imcValor by rememberSaveable { mutableStateOf("") }
    var pesoValor by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (userId == null) return@LaunchedEffect

        // Vasos de agua de hoy
        db.collection("users").document(userId)
            .collection("agua").document(hoy)
            .get()
            .addOnSuccessListener { doc ->
                val v = doc?.get("vasos")
                vasosHoy = when (v) {
                    is Long -> v.toInt()
                    is Int -> v
                    else -> 0
                }
            }

        // Calorías de hoy
        db.collection("users").document(userId)
            .collection("dieta").document(hoy)
            .collection("comidas")
            .get()
            .addOnSuccessListener { snapshot ->
                caloriasHoy = snapshot?.documents?.sumOf { doc ->
                    val c = doc.get("calorias")
                    when (c) {
                        is Long -> c.toInt()
                        is Int -> c
                        else -> 0
                    }
                } ?: 0
            }

        // Rutinas
        db.collection("users").document(userId)
            .collection("rutinas")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null) {
                    totalRutinas = snapshot.documents.size
                    rutinasCompletadas = snapshot.documents.count {
                        it.getString("completadaFecha") == hoy
                    }
                }
            }

        // Perfil (peso, IMC, objetivo calórico)
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val kcal = doc.getLong("objetivoCalorias")?.toInt()
                    if (kcal != null && kcal > 0) objetivoCalorias = kcal
                    val imc = doc.getDouble("imc")
                    if (imc != null) imcValor = String.format("%.1f", imc)
                    val peso = doc.getDouble("peso")
                    if (peso != null) pesoValor = String.format("%.1f", peso)
                }
            }
    }

    val hora = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toInt()
    val saludo = when {
        hora < 12 -> "Buenos días"
        hora < 20 -> "Buenas tardes"
        else -> "Buenas noches"
    }
    val nombreUsuario = auth.currentUser?.email
        ?.substringBefore("@")
        ?.replaceFirstChar { it.uppercase() }
        ?: "Campeón"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // — Header —
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("FitTrack", color = Green, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { auth.signOut(); navigateToInitial() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    tint = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("$saludo, $nombreUsuario 👋", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(
            SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "ES")).format(Date())
                .replaceFirstChar { it.uppercase() },
            color = Color.Gray,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // — Resumen del día —
        Text("Resumen de hoy", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ResumenCard(
                modifier = Modifier.weight(1f),
                emoji = "🔥",
                valor = "$caloriasHoy",
                total = "$objetivoCalorias",
                label = "kcal",
                progreso = (caloriasHoy.toFloat() / objetivoCalorias).coerceIn(0f, 1f),
                barColor = Color(0xFFFF6B35)
            )
            ResumenCard(
                modifier = Modifier.weight(1f),
                emoji = "💧",
                valor = "$vasosHoy",
                total = "8",
                label = "vasos",
                progreso = (vasosHoy.toFloat() / 8).coerceIn(0f, 1f),
                barColor = Color(0xFF60A5FA)
            )
            ResumenCard(
                modifier = Modifier.weight(1f),
                emoji = "💪",
                valor = "$rutinasCompletadas",
                total = "$totalRutinas",
                label = "rutinas",
                progreso = if (totalRutinas > 0) (rutinasCompletadas.toFloat() / totalRutinas).coerceIn(0f, 1f) else 0f,
                barColor = Green
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // — Acceso rápido —
        Text("Acceso rápido", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(10.dp))

        // Perfil con gradiente
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF4ADE80))))
                .clickable { navigateToProfile() }
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x26FFFFFF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AccountCircle, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mi Perfil", color = Color(0xB3FFFFFF), fontSize = 11.sp)
                    if (pesoValor.isNotEmpty() || imcValor.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (pesoValor.isNotEmpty()) {
                                Text(
                                    "${pesoValor.toFloatOrNull()?.toInt() ?: pesoValor} kg",
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (pesoValor.isNotEmpty() && imcValor.isNotEmpty()) {
                                Text("  ·  ", color = Color(0x99FFFFFF), fontSize = 13.sp)
                            }
                            if (imcValor.isNotEmpty()) {
                                Text(
                                    "IMC $imcValor",
                                    color = Color(0xCCFFFFFF),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Text("Completa tu perfil", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color(0x99FFFFFF), modifier = Modifier.size(22.dp))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        NavCard(
            emoji = "🥗",
            titulo = "Mi Dieta",
            subtitulo = "$caloriasHoy / $objetivoCalorias kcal",
            progreso = (caloriasHoy.toFloat() / objetivoCalorias).coerceIn(0f, 1f),
            barColor = Color(0xFFFF6B35),
            onClick = navigateToDieta
        )

        Spacer(modifier = Modifier.height(10.dp))

        NavCard(
            icon = Icons.Filled.FitnessCenter,
            titulo = "Mis Rutinas",
            subtitulo = if (totalRutinas > 0) "$rutinasCompletadas / $totalRutinas completadas"
                        else "Sin rutinas creadas",
            progreso = if (totalRutinas > 0) (rutinasCompletadas.toFloat() / totalRutinas).coerceIn(0f, 1f) else 0f,
            barColor = Green,
            onClick = navigateToRutinas
        )

        Spacer(modifier = Modifier.height(10.dp))

        NavCard(
            emoji = "💧",
            titulo = "Agua diaria",
            subtitulo = "$vasosHoy / 8 vasos hoy",
            progreso = (vasosHoy.toFloat() / 8).coerceIn(0f, 1f),
            barColor = Color(0xFF60A5FA),
            onClick = navigateToAgua
        )

        Spacer(modifier = Modifier.height(10.dp))

        NavCard(
            emoji = "📊",
            titulo = "Estadísticas",
            subtitulo = "Progreso de la semana",
            progreso = 0f,
            barColor = Color(0xFFA78BFA),
            onClick = navigateToEstadisticas
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ResumenCard(
    modifier: Modifier,
    emoji: String,
    valor: String,
    total: String,
    label: String,
    progreso: Float,
    barColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(valor, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("/ $total", color = Color.Gray, fontSize = 11.sp)
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2A2A2A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso)
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
private fun NavCard(
    titulo: String,
    subtitulo: String,
    progreso: Float,
    barColor: Color,
    onClick: () -> Unit,
    emoji: String? = null,
    icon: ImageVector? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                if (emoji != null) {
                    Text(emoji, fontSize = 22.sp)
                } else if (icon != null) {
                    Icon(icon, null, tint = Green, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, color = Color.Gray, fontSize = 11.sp)
                Text(
                    subtitulo,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF2A2A2A))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progreso)
                            .height(3.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}
