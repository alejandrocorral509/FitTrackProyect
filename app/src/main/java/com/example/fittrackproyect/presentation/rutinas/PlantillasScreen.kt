package com.example.fittrackproyect.presentation.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun PlantillasScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var añadidas by remember { mutableStateOf(setOf<Int>()) }

    Scaffold(
        containerColor = Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Plantillas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "${plantillasRutinas.size} rutinas listas para usar",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            itemsIndexed(plantillasRutinas) { index, plantilla ->
                PlantillaCard(
                    plantilla = plantilla,
                    añadida = index in añadidas,
                    onAñadir = {
                        if (userId != null && index !in añadidas) {
                            db.collection("users").document(userId)
                                .collection("rutinas")
                                .add(
                                    hashMapOf(
                                        "nombre" to plantilla.nombre,
                                        "categoria" to plantilla.categoria,
                                        "ejercicios" to plantilla.ejercicios,
                                        "completadaFecha" to ""
                                    )
                                )
                                .addOnSuccessListener {
                                    añadidas = añadidas + index
                                    scope.launch {
                                        snackbarHostState.showSnackbar("\"${plantilla.nombre}\" añadida a tus rutinas")
                                    }
                                }
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PlantillaCard(
    plantilla: RutinaPlantilla,
    añadida: Boolean,
    onAñadir: () -> Unit
) {
    val cat = categoriaInfo(plantilla.categoria)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (añadida) Color(0xFF0F2A1A) else Color(0xFF1C1C1E)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(cat.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(cat.emoji, fontSize = 22.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        plantilla.nombre,
                        color = if (añadida) Green else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(cat.color.copy(alpha = 0.15f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                cat.nombre,
                                color = cat.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "· ${plantilla.ejercicios.size} ejercicios",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (plantilla.descripcion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(plantilla.descripcion, color = Color(0xFF666666), fontSize = 12.sp)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = Color(0xFF2C2C2E),
                thickness = 0.5.dp
            )

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                plantilla.ejercicios.take(4).forEach { ej ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(cat.color.copy(alpha = 0.6f))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            ej["nombre"] ?: "",
                            color = Color(0xFF888888),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${ej["series"]}×${ej["repeticiones"]}",
                            color = Color(0xFF555555),
                            fontSize = 11.sp
                        )
                    }
                }
                if (plantilla.ejercicios.size > 4) {
                    Text(
                        "+${plantilla.ejercicios.size - 4} ejercicio${if (plantilla.ejercicios.size - 4 != 1) "s" else ""} más",
                        color = Color(0xFF444444),
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAñadir,
                enabled = !añadida,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green,
                    disabledContainerColor = Color(0xFF1A3A2A)
                )
            ) {
                if (añadida) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = Green,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Añadida", color = Green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                } else {
                    Icon(
                        Icons.Filled.Add,
                        null,
                        tint = Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "Añadir a mis rutinas",
                        color = Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
