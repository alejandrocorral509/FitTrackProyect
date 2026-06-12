package com.example.fittrackproyect.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

private val nivelesActividad = listOf(
    "Sedentario" to 1.2,
    "Ligero (1-3 días/semana)" to 1.375,
    "Moderado (3-5 días/semana)" to 1.55,
    "Activo (6-7 días/semana)" to 1.725,
    "Muy activo (ejercicio intenso 2x/día)" to 1.9
)

private fun calcularTDEE(peso: Double, estatura: Double, edad: Int, sexo: String, nivelActividad: String): Int {
    val bmr = if (sexo == "Hombre") {
        10 * peso + 6.25 * estatura - 5 * edad + 5
    } else {
        10 * peso + 6.25 * estatura - 5 * edad - 161
    }
    val mult = nivelesActividad.find { it.first == nivelActividad }?.second ?: 1.55
    return (bmr * mult).toInt()
}

private fun imcCategoria(imc: Double): Pair<String, Color> = when {
    imc < 18.5 -> "Bajo peso" to Color(0xFF60A5FA)
    imc < 25.0 -> "Peso normal" to Color(0xFF4ADE80)
    imc < 30.0 -> "Sobrepeso" to Color(0xFFFBBF24)
    else -> "Obesidad" to Color(0xFFFF4444)
}

@Composable
fun ProfileScreen(
    auth: FirebaseAuth,
    navigateBack: () -> Unit = {},
    navigateToHome: () -> Unit = {}
) {
    val userId = auth.currentUser?.uid
    var peso by remember { mutableStateOf("") }
    var estatura by remember { mutableStateOf("") }
    var edad by remember { mutableStateOf("") }
    var sexo by remember { mutableStateOf("Hombre") }
    var nivelActividad by remember { mutableStateOf("Moderado (3-5 días/semana)") }
    var imc by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf<String?>(null) }
    var errorValidacion by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var subiendo by remember { mutableStateOf(false) }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null && userId != null) {
            subiendo = true
            val ref = FirebaseStorage.getInstance().reference.child("profiles/$userId/avatar.jpg")
            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { url ->
                        fotoUrl = url.toString()
                        FirebaseFirestore.getInstance().collection("users").document(userId)
                            .update("fotoUrl", url.toString())
                        subiendo = false
                    }
                }
                .addOnFailureListener { subiendo = false }
        }
    }

    LaunchedEffect(Unit) {
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        peso = doc.getDouble("peso")?.toString() ?: ""
                        estatura = doc.getDouble("estatura")?.toString() ?: ""
                        edad = doc.getLong("edad")?.toString() ?: ""
                        sexo = doc.getString("sexo") ?: "Hombre"
                        nivelActividad = doc.getString("nivelActividad") ?: "Moderado (3-5 días/semana)"
                        val imcVal = doc.getDouble("imc")
                        if (imcVal != null) imc = String.format(java.util.Locale.US, "%.1f", imcVal)
                        fotoUrl = doc.getString("fotoUrl")
                    }
                }
        }
    }

    val pesoVal = peso.toDoubleOrNull()
    val estaturaVal = estatura.toDoubleOrNull()
    val edadVal = edad.toIntOrNull()
    val objetivoKcal: Int? = if (pesoVal != null && estaturaVal != null && edadVal != null && edadVal > 0 && estaturaVal > 0) {
        calcularTDEE(pesoVal, estaturaVal, edadVal, sexo, nivelActividad)
    } else null

    val proteinasObj = if (pesoVal != null) (pesoVal * 2).toInt() else null
    val grasasObj = if (objetivoKcal != null) (objetivoKcal * 0.25 / 9).toInt() else null
    val carbosObj = if (objetivoKcal != null && proteinasObj != null && grasasObj != null)
        ((objetivoKcal - proteinasObj * 4 - grasasObj * 9) / 4).coerceAtLeast(0) else null

    Scaffold(
        containerColor = Black,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
            .padding(innerPadding)
            .padding(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Mi Perfil", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF166534), Color(0xFF4ADE80))))
                .clickable {
                    photoLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (subiendo) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(40.dp))
            } else if (fotoUrl != null) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = fotoUrl,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.CameraAlt, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.AccountCircle, null, tint = Color.White, modifier = Modifier.size(60.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Toca para añadir foto", color = Color(0xB3FFFFFF), fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Peso (kg)", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            colors = inputColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Estatura (cm)", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = estatura,
            onValueChange = { estatura = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = inputColors()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Edad", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = edad,
            onValueChange = { edad = it },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = inputColors()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Sexo", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("Hombre", "Mujer").forEach { opcion ->
                Button(
                    onClick = { sexo = opcion },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (sexo == opcion) Green else Color(0xFF1C1C1E),
                        contentColor = if (sexo == opcion) Black else Color.Gray
                    )
                ) {
                    Text(opcion, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Nivel de actividad", color = Color.Gray, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                nivelesActividad.forEachIndexed { idx, (label, _) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { nivelActividad = label }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            label,
                            color = if (nivelActividad == label) Color.White else Color(0xFF888888),
                            fontSize = 13.sp
                        )
                        if (nivelActividad == label) {
                            Icon(Icons.Filled.Check, null, tint = Green, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (idx < nivelesActividad.lastIndex) {
                        HorizontalDivider(color = Color(0xFF2C2C2E), thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // IMC + objective card
        if (imc.isNotEmpty() || objetivoKcal != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (imc.isNotEmpty()) {
                        val imcDouble = imc.toDoubleOrNull() ?: 0.0
                        val (categoria, catColor) = imcCategoria(imcDouble)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("IMC", color = Color.Gray, fontSize = 14.sp)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(catColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(categoria, color = catColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(imc, color = Green, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (imc.isNotEmpty() && objetivoKcal != null) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2C2C2E))
                    }

                    if (objetivoKcal != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Objetivo calórico", color = Color.Gray, fontSize = 14.sp)
                                Text("Fórmula Mifflin-St Jeor", color = Color(0xFF555555), fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("$objetivoKcal kcal", color = Green, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        }

                        if (proteinasObj != null && carbosObj != null && grasasObj != null) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF2C2C2E))
                            Text("Distribución de macros", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                MacroGoalItem("Proteínas", "${proteinasObj}g", Color(0xFF4ADE80))
                                MacroGoalItem("Carbos", "${carbosObj}g", Color(0xFF60A5FA))
                                MacroGoalItem("Grasas", "${grasasObj}g", Color(0xFFFBBF24))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (errorValidacion.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorValidacion, color = Color(0xFFFF4444), fontSize = 13.sp)
        }

        Button(
            onClick = {
                val p = peso.toDoubleOrNull()
                val e = estatura.toDoubleOrNull()
                val a = edad.toIntOrNull()
                when {
                    p == null || p < 30 || p > 300 -> {
                        errorValidacion = "Introduce un peso válido (30–300 kg)"
                        return@Button
                    }
                    e == null || e < 100 || e > 250 -> {
                        errorValidacion = "Introduce una estatura válida (100–250 cm)"
                        return@Button
                    }
                    a == null || a < 5 || a > 120 -> {
                        errorValidacion = "Introduce una edad válida (5–120 años)"
                        return@Button
                    }
                }
                errorValidacion = ""
                val estaturaMetros = e!! / 100
                val imcVal = p!! / (estaturaMetros * estaturaMetros)
                imc = String.format(java.util.Locale.US, "%.1f", imcVal)
                val tdee = calcularTDEE(p, e, a!!, sexo, nivelActividad)
                val pObj = (p * 2).toInt()
                val gObj = (tdee * 0.25 / 9).toInt()
                val cObj = ((tdee - pObj * 4 - gObj * 9) / 4).coerceAtLeast(0)
                if (userId != null) {
                    val data = hashMapOf<String, Any>(
                        "peso" to p,
                        "estatura" to e,
                        "edad" to a,
                        "sexo" to sexo,
                        "nivelActividad" to nivelActividad,
                        "imc" to imcVal,
                        "objetivoCalorias" to tdee,
                        "proteinasObj" to pObj,
                        "carbosObj" to cObj,
                        "grasasObj" to gObj
                    )
                    fotoUrl?.let { data["fotoUrl"] = it }
                    FirebaseFirestore.getInstance()
                        .collection("users").document(userId)
                        .set(data)
                        .addOnSuccessListener {
                            navigateToHome()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text("Guardar perfil", color = Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
    }
}

@Composable
private fun MacroGoalItem(label: String, valor: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
private fun inputColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Green,
    unfocusedBorderColor = Color(0xFF333333),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Green
)
