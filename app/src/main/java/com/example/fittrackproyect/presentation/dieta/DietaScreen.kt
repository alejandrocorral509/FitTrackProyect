package com.example.fittrackproyect.presentation.dieta

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Alimento(
    val nombre: String,
    val calorias: Int,
    val proteinas: Int,
    val carbos: Int,
    val grasas: Int
)

data class ComidaRegistrada(
    val id: String = "",
    val nombre: String = "",
    val calorias: Int = 0,
    val proteinas: Int = 0,
    val carbos: Int = 0,
    val grasas: Int = 0,
    val tipo: String = ""
)

val alimentosBase = listOf(
    Alimento("Pollo a la plancha", 165, 31, 0, 4),
    Alimento("Arroz cocido", 130, 3, 28, 0),
    Alimento("Huevo entero", 155, 13, 1, 11),
    Alimento("Avena", 389, 17, 66, 7),
    Alimento("Plátano", 89, 1, 23, 0),
    Alimento("Manzana", 52, 0, 14, 0),
    Alimento("Leche entera", 61, 3, 5, 3),
    Alimento("Pan integral", 247, 13, 41, 3),
    Alimento("Atún en lata", 116, 26, 0, 1),
    Alimento("Pasta cocida", 131, 5, 25, 1),
    Alimento("Salmón", 208, 20, 0, 13),
    Alimento("Lechuga", 15, 1, 2, 0),
    Alimento("Tomate", 18, 1, 4, 0),
    Alimento("Yogur natural", 59, 10, 3, 1),
    Alimento("Almendras", 579, 21, 22, 50),
    Alimento("Naranja", 47, 1, 12, 0),
    Alimento("Queso fresco", 98, 11, 3, 4),
    Alimento("Lentejas cocidas", 116, 9, 20, 0),
    Alimento("Pechuga de pavo", 135, 30, 0, 1),
    Alimento("Aceite de oliva", 884, 0, 0, 100),
    Alimento("Pechuga de pollo", 165, 31, 0, 4),
    Alimento("Brócoli", 34, 3, 7, 0),
    Alimento("Espinacas", 23, 3, 4, 0),
    Alimento("Batata / boniato", 86, 2, 20, 0),
    Alimento("Quinoa cocida", 120, 4, 21, 2),
    Alimento("Aguacate", 160, 2, 9, 15),
    Alimento("Claras de huevo", 52, 11, 1, 0),
    Alimento("Requesón", 98, 11, 3, 4),
    Alimento("Carne de ternera", 250, 26, 0, 17),
    Alimento("Garbanzo cocido", 164, 9, 27, 3)
)

@Composable
fun DietaScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {},
    navigateToBuscar: (String) -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val hoyStr = remember { sdf.format(Date()) }

    var diasOffset by remember { mutableIntStateOf(0) }
    val fecha = remember(diasOffset) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, diasOffset)
        sdf.format(cal.time)
    }
    val fechaDisplay = remember(diasOffset) {
        when (diasOffset) {
            0 -> "Hoy"
            -1 -> "Ayer"
            else -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, diasOffset)
                SimpleDateFormat("EEE d MMM", Locale("es", "ES")).format(cal.time)
                    .replaceFirstChar { it.uppercase() }
            }
        }
    }

    var comidasFecha by remember { mutableStateOf<List<ComidaRegistrada>>(emptyList()) }
    var objetivoCalorias by remember { mutableIntStateOf(2000) }
    var pesoUsuario by remember { mutableStateOf(0.0) }
    var cargando by remember { mutableStateOf(true) }
    var comidaAEliminar by remember { mutableStateOf<ComidaRegistrada?>(null) }

    // Real-time listener per fecha (cleans up when fecha changes)
    DisposableEffect(userId, fecha) {
        cargando = true
        val registration = if (userId != null) {
            db.collection("users").document(userId)
                .collection("dieta").document(fecha)
                .collection("comidas")
                .addSnapshotListener { snapshot, _ ->
                    comidasFecha = snapshot?.documents?.map { doc ->
                        ComidaRegistrada(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            calorias = (doc.getLong("calorias") ?: 0).toInt(),
                            proteinas = (doc.getLong("proteinas") ?: 0).toInt(),
                            carbos = (doc.getLong("carbos") ?: 0).toInt(),
                            grasas = (doc.getLong("grasas") ?: 0).toInt(),
                            tipo = doc.getString("tipo") ?: ""
                        )
                    } ?: emptyList()
                    cargando = false
                }
        } else {
            cargando = false
            null
        }
        onDispose { registration?.remove() }
    }

    DisposableEffect(userId) {
        if (userId == null) return@DisposableEffect onDispose {}
        val perfilReg = db.collection("users").document(userId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    val kcal = doc.getLong("objetivoCalorias")?.toInt()
                    if (kcal != null && kcal > 0) objetivoCalorias = kcal
                    val peso = doc.getDouble("peso")
                    if (peso != null && peso > 0) pesoUsuario = peso
                }
            }
        onDispose { perfilReg.remove() }
    }

    val totalCalorias = comidasFecha.sumOf { it.calorias }
    val totalProteinas = comidasFecha.sumOf { it.proteinas }
    val totalCarbos = comidasFecha.sumOf { it.carbos }
    val totalGrasas = comidasFecha.sumOf { it.grasas }

    // Macro goals calculated from profile
    val proteinasObj = if (pesoUsuario > 0) (pesoUsuario * 2).toInt() else 0
    val grasasObj = if (objetivoCalorias > 0) (objetivoCalorias * 0.25 / 9).toInt() else 0
    val carbosObj = if (objetivoCalorias > 0 && proteinasObj > 0 && grasasObj > 0)
        ((objetivoCalorias - proteinasObj * 4 - grasasObj * 9) / 4).coerceAtLeast(0) else 0
    val hasMacroGoals = proteinasObj > 0 && carbosObj > 0 && grasasObj > 0

    // Delete confirmation dialog
    comidaAEliminar?.let { comida ->
        AlertDialog(
            onDismissRequest = { comidaAEliminar = null },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text("Eliminar alimento", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Eliminar \"${comida.nombre.substringBefore(" (")}\"?",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .collection("dieta").document(fecha)
                                .collection("comidas").document(comida.id)
                                .delete()
                        }
                        comidaAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A))
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { comidaAEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .padding(24.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mi Dieta", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { diasOffset-- }) {
                    Icon(Icons.Filled.ChevronLeft, null, tint = Color.White)
                }
                Text(
                    fechaDisplay,
                    color = if (diasOffset == 0) Green else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { if (diasOffset < 0) diasOffset++ },
                    enabled = diasOffset < 0
                ) {
                    Icon(
                        Icons.Filled.ChevronRight,
                        null,
                        tint = if (diasOffset < 0) Color.White else Color(0xFF333333)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green, modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                }
            }

            // Calorie banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF4ADE80)))),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "$totalCalorias / $objetivoCalorias kcal",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (totalCalorias >= objetivoCalorias) "¡Objetivo alcanzado!" else "${objetivoCalorias - totalCalorias} kcal restantes",
                        color = Color(0xB3FFFFFF),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((totalCalorias.toFloat() / objetivoCalorias).coerceIn(0f, 1f))
                                .height(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Macro card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroItem(
                            "Proteínas", "${totalProteinas}g",
                            if (hasMacroGoals) "/ ${proteinasObj}g" else null,
                            Color(0xFF4ADE80),
                            if (hasMacroGoals && proteinasObj > 0) totalProteinas.toFloat() / proteinasObj else null
                        )
                        MacroItem(
                            "Carbos", "${totalCarbos}g",
                            if (hasMacroGoals) "/ ${carbosObj}g" else null,
                            Color(0xFF60A5FA),
                            if (hasMacroGoals && carbosObj > 0) totalCarbos.toFloat() / carbosObj else null
                        )
                        MacroItem(
                            "Grasas", "${totalGrasas}g",
                            if (hasMacroGoals) "/ ${grasasObj}g" else null,
                            Color(0xFFFBBF24),
                            if (hasMacroGoals && grasasObj > 0) totalGrasas.toFloat() / grasasObj else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        listOf("Desayuno", "Almuerzo", "Cena", "Snacks").forEach { tipoComida ->
            item {
                val comidasTipo = comidasFecha.filter { it.tipo == tipoComida }
                val caloriasComida = comidasTipo.sumOf { it.calorias }
                val esHoy = diasOffset == 0

                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tipoComida, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("$caloriasComida kcal", color = Green, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                if (esHoy) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { navigateToBuscar(tipoComida) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, null, tint = Green, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        if (comidasTipo.isEmpty()) {
                            Text(
                                if (esHoy) "Sin alimentos registrados" else "Sin registros",
                                color = Color(0xFF555555),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            comidasTipo.forEach { comida ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(comida.nombre, color = Color.White, fontSize = 13.sp)
                                        Text(
                                            "P: ${comida.proteinas}g · C: ${comida.carbos}g · G: ${comida.grasas}g",
                                            color = Color.Gray,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("${comida.calorias} kcal", color = Green, fontSize = 12.sp)
                                        if (esHoy) {
                                            IconButton(
                                                onClick = { comidaAEliminar = comida },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Filled.Delete, null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    valor: String,
    objetivo: String?,
    color: Color,
    progreso: Float?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(90.dp)) {
        Text(valor, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (objetivo != null) {
            Text(objetivo, color = Color(0xFF555555), fontSize = 11.sp)
        }
        Text(label, color = Color.Gray, fontSize = 12.sp)
        if (progreso != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2A2A2A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progreso.coerceIn(0f, 1f))
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
    }
}
