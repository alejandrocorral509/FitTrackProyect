package com.example.fittrackproyect.presentation.login

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Green
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    navigateToHome: () -> Unit,
    navigateBack: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

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
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = navigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Iniciar sesión", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Bienvenido de nuevo", color = Color.Gray, fontSize = 14.sp)

        Spacer(modifier = Modifier.height(36.dp))

        Text("Email", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = "" },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text("tu@email.com", color = Color(0xFF555555)) },
            colors = textFieldColors
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Contraseña", color = Color.Gray, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = "" },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        null,
                        tint = Color.Gray
                    )
                }
            },
            colors = textFieldColors
        )

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(error, color = Color(0xFFFF5252), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                when {
                    email.isBlank() || password.isBlank() -> error = "Rellena todos los campos"
                    !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> error = "Email no válido"
                    else -> {
                        cargando = true
                        auth.signInWithEmailAndPassword(email.trim(), password)
                            .addOnCompleteListener { task ->
                                cargando = false
                                if (task.isSuccessful) {
                                    navigateToHome()
                                } else {
                                    error = "Email o contraseña incorrectos"
                                }
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(color = Black, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Iniciar sesión", color = Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
