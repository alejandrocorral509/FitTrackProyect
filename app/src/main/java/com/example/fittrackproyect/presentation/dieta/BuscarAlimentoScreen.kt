package com.example.fittrackproyect.presentation.dieta

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AlimentoConMacros(
    val nombre: String,
    val calorias: Double,
    val proteinas: Double,
    val carbos: Double,
    val grasas: Double
)

@Composable
fun BuscarAlimentoScreen(
    auth: FirebaseAuth,
    tipoComida: String,
    navigateBack: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    var query by remember { mutableStateOf("") }
    var resultadosApi by remember { mutableStateOf<List<AlimentoConMacros>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var alimentoSeleccionado by remember { mutableStateOf<AlimentoConMacros?>(null) }
    var cantidadTexto by remember { mutableStateOf("100") }

    val alimentosLocalesFiltrados = if (query.isEmpty()) {
        alimentosBase.map {
            AlimentoConMacros(it.nombre, it.calorias.toDouble(), it.proteinas.toDouble(), it.carbos.toDouble(), it.grasas.toDouble())
        }
    } else {
        alimentosBase
            .filter { it.nombre.contains(query, ignoreCase = true) }
            .map {
                AlimentoConMacros(it.nombre, it.calorias.toDouble(), it.proteinas.toDouble(), it.carbos.toDouble(), it.grasas.toDouble())
            }
    }

    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(600)
            cargando = true
            try {
                resultadosApi = buscarEnOpenFoodFacts(query)
            } catch (_: Exception) { }
            cargando = false
        } else {
            resultadosApi = emptyList()
        }
    }

    alimentoSeleccionado?.let { alimento ->
        val gramos = cantidadTexto.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 100.0
        val factor = gramos / 100.0

        AlertDialog(
            onDismissRequest = { alimentoSeleccionado = null; cantidadTexto = "100" },
            containerColor = Color(0xFF1C1C1E),
            title = {
                Text(
                    alimento.nombre.replace(Regex("\\s*\\(\\d+[gml]+\\)$"), ""),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                Column {
                    Text("Cantidad (gramos)", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cantidadTexto,
                        onValueChange = { cantidadTexto = it.filter { c -> c.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = Color(0xFF444444),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Green
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroPreview("${(alimento.calorias * factor).toInt()}", "kcal", Green)
                        MacroPreview("${(alimento.proteinas * factor).toInt()}g", "Prot.", Color(0xFF4ADE80))
                        MacroPreview("${(alimento.carbos * factor).toInt()}g", "Carbos", Color(0xFF60A5FA))
                        MacroPreview("${(alimento.grasas * factor).toInt()}g", "Grasas", Color(0xFFFBBF24))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userId != null) {
                            val g = cantidadTexto.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 100.0
                            val f = g / 100.0
                            val nombreBase = alimento.nombre.replace(Regex("\\s*\\(\\d+[gml]+\\)$"), "")
                            db.collection("users").document(userId)
                                .collection("dieta").document(hoy)
                                .collection("comidas")
                                .add(
                                    mapOf(
                                        "nombre" to "$nombreBase (${g.toInt()}g)",
                                        "calorias" to (alimento.calorias * f).toInt(),
                                        "proteinas" to (alimento.proteinas * f).toInt(),
                                        "carbos" to (alimento.carbos * f).toInt(),
                                        "grasas" to (alimento.grasas * f).toInt(),
                                        "tipo" to tipoComida
                                    )
                                )
                        }
                        alimentoSeleccionado = null
                        cantidadTexto = "100"
                        navigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text("Añadir", color = Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { alimentoSeleccionado = null; cantidadTexto = "100" }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = navigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Añadir a $tipoComida",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar alimento...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Green
                )
            )
        }

        if (cargando) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Green, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp)
        ) {
            if (alimentosLocalesFiltrados.isNotEmpty()) {
                item {
                    Text(
                        if (query.isEmpty()) "Alimentos frecuentes" else "Coincidencias locales",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(alimentosLocalesFiltrados) { alimento ->
                    AlimentoResultadoItem(alimento) { alimentoSeleccionado = it; cantidadTexto = "100" }
                }
            }

            if (resultadosApi.isNotEmpty()) {
                item {
                    Text(
                        "Resultados de Open Food Facts",
                        color = Color.Gray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                items(resultadosApi) { alimento ->
                    AlimentoResultadoItem(alimento) { alimentoSeleccionado = it; cantidadTexto = "100" }
                }
            }

            if (query.length >= 2 && !cargando && resultadosApi.isEmpty() && alimentosLocalesFiltrados.isEmpty()) {
                item {
                    Text(
                        "Sin resultados para \"$query\"",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun AlimentoResultadoItem(
    alimento: AlimentoConMacros,
    onSeleccionar: (AlimentoConMacros) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSeleccionar(alimento) }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(alimento.nombre, color = Color.White, fontSize = 14.sp)
            Text(
                "P: ${alimento.proteinas.toInt()}g · C: ${alimento.carbos.toInt()}g · G: ${alimento.grasas.toInt()}g",
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "${alimento.calorias.toInt()} kcal",
            color = Green,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
    HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
}

@Composable
private fun MacroPreview(valor: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = Color.Gray, fontSize = 11.sp)
    }
}

private suspend fun buscarEnOpenFoodFacts(query: String): List<AlimentoConMacros> = withContext(Dispatchers.IO) {
    try {
        val encoded = URLEncoder.encode(query, "UTF-8")
        val url = URL("https://world.openfoodfacts.org/api/v2/search?search_terms=$encoded&fields=product_name,nutriments&page_size=20")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("User-Agent", "FitTrack/1.0 (Android Student Project)")
        conn.connectTimeout = 6000
        conn.readTimeout = 10000

        if (conn.responseCode == 200) {
            val body = conn.inputStream.bufferedReader().readText()
            val products = JSONObject(body).getJSONArray("products")
            val result = mutableListOf<AlimentoConMacros>()
            for (i in 0 until minOf(products.length(), 20)) {
                val p = products.getJSONObject(i)
                val nombre = p.optString("product_name").trim().takeIf { it.isNotBlank() } ?: continue
                val n = p.optJSONObject("nutriments") ?: continue
                val kcal = n.optDouble("energy-kcal_100g", -1.0)
                if (kcal < 0) continue
                result.add(
                    AlimentoConMacros(
                        nombre = nombre.take(50),
                        calorias = kcal,
                        proteinas = n.optDouble("proteins_100g", 0.0),
                        carbos = n.optDouble("carbohydrates_100g", 0.0),
                        grasas = n.optDouble("fat_100g", 0.0)
                    )
                )
            }
            result
        } else emptyList()
    } catch (_: Exception) { emptyList() }
}
