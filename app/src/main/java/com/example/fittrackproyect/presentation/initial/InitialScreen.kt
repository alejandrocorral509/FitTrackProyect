package com.example.fittrackproyect.presentation.initial

import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.example.fittrackproyect.R
import com.example.fittrackproyect.ui.theme.BackgroundButton
import com.example.fittrackproyect.ui.theme.Black
import com.example.fittrackproyect.ui.theme.Gray
import com.example.fittrackproyect.ui.theme.Green
import com.example.fittrackproyect.ui.theme.ShapeButton
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.sp

@Composable
fun InitialScreen(
    auth: FirebaseAuth,
    navigateToLogin: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
    navigateToHome: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val webClientId = stringResource(id = R.string.web_client_id)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Gray, Black), startY = 0f, endY = 600f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Image(
            painter = painterResource(id = R.drawable.ffit),
            contentDescription = "FitTrack",
            modifier = Modifier.size(200.dp)
        )
        Text(
            "Controla tu progreso.",
            color = Color.White,
            fontSize = 28.sp, // ← reducido de 38 a 28
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center, // ← añade esto
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp) // ← añade esto
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { navigateToSignUp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Green)
        ) {
            Text(text = "Sign up free", color = Black, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(8.dp))
        CustomButton(
            modifier = Modifier.clickable {
                scope.launch {
                    try {
                        val credentialManager = CredentialManager.create(activity!!)
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setServerClientId(webClientId)
                            .setFilterByAuthorizedAccounts(false)
                            .setAutoSelectEnabled(true)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        val result = credentialManager.getCredential(activity, request)
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
                        val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        auth.signInWithCredential(firebaseCredential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.i("aris", "GOOGLE LOGIN OK")
                                    navigateToHome()
                                } else {
                                    Log.i("aris", "GOOGLE LOGIN KO: ${task.exception?.message}")
                                }
                            }
                    } catch (e: Exception) {
                        Log.i("aris", "GOOGLE ERROR: ${e.message}")
                    }
                }
            },
            painter = painterResource(id = R.drawable.google),
            title = "Continue with Google"
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Log In",
            color = Color.White,
            modifier = Modifier
                .padding(24.dp)
                .clickable { navigateToLogin() },
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun CustomButton(modifier: Modifier, painter: Painter, title: String) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .padding(horizontal = 32.dp)
            .background(BackgroundButton)
            .border(2.dp, ShapeButton, CircleShape),
        contentAlignment = Alignment.CenterStart
    ) {
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
        )
        Text(
            text = title,
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}