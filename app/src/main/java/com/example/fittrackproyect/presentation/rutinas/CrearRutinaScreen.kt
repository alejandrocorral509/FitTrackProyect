package com.example.fittrackproyect.presentation.rutinas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CrearRutinaScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    var nombreRutina by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("Full Body") }
    var nombreEjercicio by remember { mutableStateOf("") }
    var series by remember { mutableStateOf("") }
    var repeticiones by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var ejercicios by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Green,
        unfocusedBorderColor = Color(0xFF333333),
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        cursorColor = Green,
        focusedLabelColor = Green,
        unfocusedLabelColor = Color.Gray
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear Rutina", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Nombre rutina
        Text("Nombre de la rutina", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = nombreRutina,
            onValueChange = { nombreRutina = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: Día de pecho, Piernas A...", color = Color(0xFF555555)) },
            singleLine = true,
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Selector de categoría
        Text("Categoría", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categorias.forEach { cat ->
                val seleccionada = categoriaSeleccionada == cat.nombre
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (seleccionada) cat.color.copy(alpha = 0.2f) else Color(0xFF1C1C1E)
                        )
                        .border(
                            width = if (seleccionada) 1.5.dp else 0.dp,
                            color = if (seleccionada) cat.color else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { categoriaSeleccionada = cat.nombre }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            cat.nombre,
                            color = if (seleccionada) cat.color else Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFF2C2C2E))
        Spacer(modifier = Modifier.height(20.dp))

        // Sección ejercicios
        Text("Añadir ejercicio", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Text("Nombre del ejercicio", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = nombreEjercicio,
            onValueChange = { nombreEjercicio = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Ej: Press banca, Sentadilla...", color = Color(0xFF555555)) },
            singleLine = true,
            colors = textFieldColors
        )

        val sugerencias = if (nombreEjercicio.isNotEmpty()) {
            ejerciciosSugeridos.filter {
                it.contains(nombreEjercicio, ignoreCase = true) &&
                !it.equals(nombreEjercicio, ignoreCase = true)
            }.take(6)
        } else emptyList()

        if (sugerencias.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                sugerencias.forEach { sug ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1C1C1E))
                            .border(0.5.dp, Color(0xFF2C2C2E), RoundedCornerShape(20.dp))
                            .clickable { nombreEjercicio = sug }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(sug, color = Color(0xFFCCCCCC), fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Series", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = series,
                    onValueChange = { series = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Reps", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = repeticiones,
                    onValueChange = { repeticiones = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = textFieldColors
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Peso (kg)", color = Color.Gray, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = peso,
                    onValueChange = { peso = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = textFieldColors
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                if (nombreEjercicio.isNotBlank()) {
                    ejercicios = ejercicios + mapOf(
                        "nombre" to nombreEjercicio,
                        "series" to (series.ifBlank { "0" }),
                        "repeticiones" to (repeticiones.ifBlank { "0" }),
                        "peso" to (peso.ifBlank { "0" })
                    )
                    nombreEjercicio = ""
                    series = ""
                    repeticiones = ""
                    peso = ""
                }
            },
            modifier = Modifier.fillMaxWidth().height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Text("+ Añadir ejercicio", color = Green, fontWeight = FontWeight.Bold)
        }

        // Lista de ejercicios añadidos
        if (ejercicios.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                "${ejercicios.size} ejercicio${if (ejercicios.size != 1) "s" else ""} añadido${if (ejercicios.size != 1) "s" else ""}",
                color = Color.Gray,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ejercicios.forEachIndexed { index, ejercicio ->
                    val cat = categoriaInfo(categoriaSeleccionada)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cat.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${index + 1}", color = cat.color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                ejercicio["nombre"] ?: "",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            val pesoEj = ejercicio["peso"]
                            val pesoTexto = if (!pesoEj.isNullOrBlank() && pesoEj != "0") " · ${pesoEj}kg" else ""
                            Text(
                                "${ejercicio["series"]} series × ${ejercicio["repeticiones"]} reps$pesoTexto",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        IconButton(
                            onClick = { ejercicios = ejercicios.toMutableList().also { it.removeAt(index) } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Filled.Close, null, tint = Color(0xFF6B2020), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nombreRutina.isNotBlank() && userId != null) {
                    db.collection("users").document(userId)
                        .collection("rutinas")
                        .add(
                            hashMapOf(
                                "nombre" to nombreRutina,
                                "categoria" to categoriaSeleccionada,
                                "ejercicios" to ejercicios,
                                "completada" to false
                            )
                        )
                        .addOnSuccessListener { navigateBack() }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (nombreRutina.isNotBlank() && ejercicios.isNotEmpty()) Green else Color(0xFF2A2A2A)
            )
        ) {
            Icon(Icons.Filled.FitnessCenter, null, tint = if (nombreRutina.isNotBlank() && ejercicios.isNotEmpty()) Black else Color.Gray, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Guardar rutina",
                color = if (nombreRutina.isNotBlank() && ejercicios.isNotEmpty()) Black else Color.Gray,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
