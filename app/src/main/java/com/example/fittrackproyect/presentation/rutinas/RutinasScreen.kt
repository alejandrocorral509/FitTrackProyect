package com.example.fittrackproyect.presentation.rutinas

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

data class Categoria(val nombre: String, val emoji: String, val color: Color)

val categorias = listOf(
    Categoria("Full Body",  "🔥", Color(0xFF4ADE80)),
    Categoria("Pecho",      "💪", Color(0xFFFF6B35)),
    Categoria("Espalda",    "🏋️", Color(0xFF60A5FA)),
    Categoria("Piernas",    "🦵", Color(0xFFFBBF24)),
    Categoria("Hombros",    "🎯", Color(0xFFEC4899)),
    Categoria("Bíceps",     "💪", Color(0xFFA78BFA)),
    Categoria("Tríceps",    "⚡", Color(0xFFFF4444)),
    Categoria("Abdomen",    "🧱", Color(0xFFFF8C42)),
    Categoria("Cardio",     "🏃", Color(0xFF00D4FF)),
)

fun categoriaInfo(nombre: String): Categoria =
    categorias.find { it.nombre == nombre } ?: Categoria("General", "💪", Color(0xFF4ADE80))

data class Rutina(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "Full Body",
    val ejercicios: List<Map<String, String>> = emptyList(),
    val completadaFecha: String = ""
)

@Composable
fun RutinasScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {},
    navigateToCrearRutina: () -> Unit = {},
    navigateToEditarRutina: (String) -> Unit = {},
    navigateToPlantillas: () -> Unit = {}
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var rutinas by remember { mutableStateOf<List<Rutina>>(emptyList()) }
    var rutinaAEliminar by remember { mutableStateOf<Rutina?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(userId) {
        if (userId != null) {
            db.collection("users").document(userId)
                .collection("rutinas")
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        rutinas = snapshot.documents.map { doc ->
                            Rutina(
                                id = doc.id,
                                nombre = doc.getString("nombre") ?: "",
                                categoria = doc.getString("categoria") ?: "Full Body",
                                ejercicios = (doc.get("ejercicios") as? List<*>)
                                    ?.filterIsInstance<Map<String, String>>() ?: emptyList(),
                                completadaFecha = doc.getString("completadaFecha") ?: ""
                            )
                        }
                    }
                }
        }
    }

    val totalEjercicios = rutinas.sumOf { it.ejercicios.size }
    val completadas = rutinas.count { it.completadaFecha == hoy }

    // Delete confirmation dialog
    rutinaAEliminar?.let { rutina ->
        AlertDialog(
            onDismissRequest = { rutinaAEliminar = null },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text("Eliminar rutina", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Eliminar \"${rutina.nombre}\"? Esta acción no se puede deshacer.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .collection("rutinas").document(rutina.id)
                                .delete()
                                .addOnSuccessListener {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Rutina \"${rutina.nombre}\" eliminada")
                                    }
                                }
                        }
                        rutinaAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1A1A))
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { rutinaAEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        containerColor = Black,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigateToCrearRutina() },
                containerColor = Green,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir", tint = Black)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Black)
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = navigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mis Rutinas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Banner de stats
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF4ADE80))))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        BannerStat("${rutinas.size}", "rutinas", "🏋️")
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0x33FFFFFF)))
                        BannerStat("$completadas", "completadas", "✅")
                        Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0x33FFFFFF)))
                        BannerStat("$totalEjercicios", "ejercicios", "💪")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para explorar plantillas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1C1C1E))
                            .clickable { navigateToPlantillas() }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🗂️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Explorar plantillas",
                                color = Green,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            if (rutinas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.FitnessCenter, null, tint = Color(0xFF333333), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No tienes rutinas aún", color = Color.Gray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Pulsa + para crear una", color = Color(0xFF555555), fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(rutinas) { rutina ->
                    RutinaCard(
                        rutina = rutina,
                        hoy = hoy,
                        onToggle = {
                            if (userId != null) {
                                val nuevaFecha = if (rutina.completadaFecha == hoy) "" else hoy
                                db.collection("users").document(userId)
                                    .collection("rutinas").document(rutina.id)
                                    .update("completadaFecha", nuevaFecha)
                            }
                        },
                        onEdit = { navigateToEditarRutina(rutina.id) },
                        onDelete = { rutinaAEliminar = rutina }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun RutinaCard(rutina: Rutina, hoy: String, onToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val completadaHoy = rutina.completadaFecha == hoy
    val cat = categoriaInfo(rutina.categoria)
    val ejercicioPreview = rutina.ejercicios.take(3)
        .joinToString(" · ") { it["nombre"] ?: "" }
        .let { if (rutina.ejercicios.size > 3) "$it..." else it }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (completadaHoy) Color(0xFF0F2A1A) else Color(0xFF1C1C1E)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        rutina.nombre,
                        color = if (completadaHoy) Green else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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
                            "· ${rutina.ejercicios.size} ej.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Icon(
                    imageVector = if (completadaHoy) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = if (completadaHoy) Green else Color(0xFF444444),
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onToggle() }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    Icons.Filled.Edit,
                    contentDescription = null,
                    tint = Color(0xFF555555),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onEdit() }
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFF6B2020),
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onDelete() }
                )
            }

            if (ejercicioPreview.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color(0xFF2C2C2E),
                    thickness = 0.5.dp
                )
                Text(
                    ejercicioPreview,
                    color = Color(0xFF666666),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BannerStat(valor: String, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(valor, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color(0xB3FFFFFF), fontSize = 11.sp)
    }
}
