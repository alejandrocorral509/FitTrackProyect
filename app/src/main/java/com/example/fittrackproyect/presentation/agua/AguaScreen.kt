package com.example.fittrackproyect.presentation.agua

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AguaScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var vasos by remember { mutableIntStateOf(0) }
    var objetivo by remember { mutableIntStateOf(8) }

    DisposableEffect(userId) {
        if (userId == null) return@DisposableEffect onDispose {}
        val aguaReg = db.collection("users").document(userId)
            .collection("agua").document(hoy)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    vasos = (doc.getLong("vasos") ?: 0).toInt()
                }
            }
        onDispose { aguaReg.remove() }
    }

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    val peso = doc?.getDouble("peso")
                    if (peso != null && peso > 0) {
                        objetivo = ((peso * 0.033 / 0.25).roundToInt()).coerceIn(6, 15)
                    }
                }
        }
    }

    fun guardarVasos(nuevosVasos: Int) {
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("agua").document(hoy)
                .set(mapOf("vasos" to nuevosVasos.toLong(), "fecha" to hoy))
        }
    }

    val progreso = (vasos.toFloat() / objetivo).coerceIn(0f, 1f)
    val completado = vasos >= objetivo

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agua diaria", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Big count displayed ABOVE the tank
        Text(
            text = "$vasos",
            color = if (completado) Green else Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "de $objetivo vasos",
            color = Color.Gray,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Water tank — purely visual, no text inside
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF1A2A3A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(progreso)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF93C5FD), Color(0xFF1D4ED8))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Tick marks beside the tank
            Column(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) { i ->
                    val nivel = objetivo - (i * objetivo / 4)
                    Text(
                        "$nivel",
                        color = if (vasos >= nivel) Color(0xFF60A5FA) else Color(0xFF333333),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (completado) "¡Objetivo cumplido! 🎉" else "Faltan ${objetivo - vasos} vasos",
            color = if (completado) Green else Color.Gray,
            fontSize = 14.sp,
            fontWeight = if (completado) FontWeight.Bold else FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Stats card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "${vasos * 250} ml",
                        color = Color(0xFF60A5FA),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("ingeridos", color = Color.Gray, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFF2C2C2E))
                        .align(Alignment.CenterVertically)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "${objetivo * 250} ml",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("objetivo", color = Color.Gray, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFF2C2C2E))
                        .align(Alignment.CenterVertically)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "${(progreso * 100).toInt()}%",
                        color = if (completado) Green else Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text("progreso", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    if (vasos > 0) { vasos--; guardarVasos(vasos) }
                },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Text("−", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = {
                    if (vasos < objetivo) { vasos++; guardarVasos(vasos) }
                },
                modifier = Modifier.weight(2f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (!completado) Color(0xFF1D4ED8) else Color(0xFF1C1C1E)
                )
            ) {
                Text(
                    "+ Añadir vaso (250 ml)",
                    color = if (!completado) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Meta calculada según tu peso · se reinicia cada día",
            color = Color(0xFF444444),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
