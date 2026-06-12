package com.example.fittrackproyect.presentation.estadisticas

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EstadisticasScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // 7 days of data (index 0 = 6 days ago, index 6 = today)
    val caloriasData = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0) }
    val aguaData = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0) }
    val rutinasData = remember { mutableStateListOf(0, 0, 0, 0, 0, 0, 0) }

    val labels = remember {
        (0..6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i - 6)
            SimpleDateFormat("EEE", Locale("es", "ES")).format(cal.time)
                .take(1).uppercase()
        }
    }

    val fechas = remember {
        (0..6).map { i ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i - 6)
            sdf.format(cal.time)
        }
    }

    LaunchedEffect(userId) {
        if (userId == null) return@LaunchedEffect

        fechas.forEachIndexed { index, fecha ->
            // Calories
            db.collection("users").document(userId)
                .collection("dieta").document(fecha)
                .collection("comidas").get()
                .addOnSuccessListener { snap ->
                    caloriasData[index] = snap.documents.sumOf {
                        (it.getLong("calorias") ?: 0).toInt()
                    }
                }

            // Water
            db.collection("users").document(userId)
                .collection("agua").document(fecha).get()
                .addOnSuccessListener { doc ->
                    aguaData[index] = (doc.getLong("vasos") ?: 0).toInt()
                }

            // Routines completed that day
            db.collection("users").document(userId)
                .collection("rutinas").get()
                .addOnSuccessListener { snap ->
                    rutinasData[index] = snap.documents.count {
                        it.getString("completadaFecha") == fecha
                    }
                }
        }
    }

    // Summary stats
    val avgCalorias = caloriasData.filter { it > 0 }.let { if (it.isEmpty()) 0 else it.average().toInt() }
    val totalVasos = aguaData.sum()
    val totalRutinas = rutinasData.sum()
    val diasConDatos = caloriasData.count { it > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Estadísticas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Últimos 7 días", color = Color.Gray, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Summary banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF4ADE80))))
                .padding(20.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem("$avgCalorias", "kcal/día", "🔥")
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0x33FFFFFF)))
                SummaryItem("$totalVasos", "vasos total", "💧")
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0x33FFFFFF)))
                SummaryItem("$totalRutinas", "entrenos", "💪")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Calories chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Calorías",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (avgCalorias > 0) "~$avgCalorias kcal/día" else "sin datos",
                        color = Color(0xFFFF6B35),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                BarChart(
                    data = caloriasData.toList(),
                    labels = labels,
                    color = Color(0xFFFF6B35),
                    today = 6
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Water chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Hidratación",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        if (totalVasos > 0) "$totalVasos vasos" else "sin datos",
                        color = Color(0xFF60A5FA),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                BarChart(
                    data = aguaData.toList(),
                    labels = labels,
                    color = Color(0xFF60A5FA),
                    today = 6,
                    maxOverride = 15
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Routines chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Entrenos",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "$totalRutinas esta semana",
                        color = Green,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                BarChart(
                    data = rutinasData.toList(),
                    labels = labels,
                    color = Green,
                    today = 6,
                    maxOverride = maxOf(rutinasData.maxOrNull() ?: 0, 3)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Consistency card
        if (diasConDatos > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Constancia", color = Color.Gray, fontSize = 13.sp)
                        Text(
                            "$diasConDatos / 7 días con registro",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "${diasConDatos * 100 / 7}%",
                        color = Green,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BarChart(
    data: List<Int>,
    labels: List<String>,
    color: Color,
    today: Int = -1,
    maxOverride: Int = 0
) {
    val max = maxOf(data.maxOrNull() ?: 0, maxOverride, 1)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().height(110.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEachIndexed { index, value ->
                val fraction = (value.toFloat() / max).coerceIn(0f, 1f)
                val isToday = index == today
                val barColor = if (isToday) color else color.copy(alpha = 0.45f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).padding(horizontal = 3.dp)
                ) {
                    // Value label above bar (only if > 0)
                    if (value > 0) {
                        Text(
                            text = if (value >= 1000) "${value / 1000}k" else "$value",
                            color = barColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    // Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((fraction * 90).dp.coerceAtLeast(if (value > 0) 4.dp else 0.dp))
                            .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                            .background(if (value > 0) barColor else Color.Transparent)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEachIndexed { index, label ->
                Text(
                    label,
                    color = if (index == today) Color.White else Color(0xFF555555),
                    fontSize = 11.sp,
                    fontWeight = if (index == today) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(valor: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(valor, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xB3FFFFFF), fontSize = 10.sp)
    }
}
