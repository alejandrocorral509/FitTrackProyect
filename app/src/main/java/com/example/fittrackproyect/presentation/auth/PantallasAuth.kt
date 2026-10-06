package com.example.fittrackproyect.presentation.auth

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fittrackproyect.R
import com.example.fittrackproyect.ui.components.BarraSuperior
import com.example.fittrackproyect.ui.components.BotonPrincipal
import com.example.fittrackproyect.ui.components.CampoTexto
import com.example.fittrackproyect.ui.components.IconoMetrica
import com.example.fittrackproyect.ui.theme.ColoresMetrica
import com.example.fittrackproyect.ui.theme.FitTrackProyectTheme
import com.example.fittrackproyect.ui.theme.FondoOscuro
import com.example.fittrackproyect.ui.theme.TextoOscuro
import com.example.fittrackproyect.ui.theme.TextoSecundarioOscuro
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/** Pide a Android la cuenta de Google y devuelve su token, o null si el usuario cancela. */
private suspend fun tokenDeGoogle(activity: Activity, webClientId: String): String? = try {
    val opcion = GetGoogleIdOption.Builder()
        .setServerClientId(webClientId)
        .setFilterByAuthorizedAccounts(false)
        .build()
    val respuesta = CredentialManager.create(activity)
        .getCredential(activity, GetCredentialRequest.Builder().addCredentialOption(opcion).build())
    GoogleIdTokenCredential.createFrom(respuesta.credential.data).idToken
} catch (_: GetCredentialCancellationException) {
    null
}

@Composable
private fun BotonGoogle(viewModel: AuthViewModel, alEntrar: (DestinoTrasEntrar) -> Unit, modifier: Modifier = Modifier) {
    val activity = LocalActivity.current
    val webClientId = stringResource(R.string.web_client_id)
    val scope = rememberCoroutineScope()
    OutlinedButton(
        onClick = {
            if (activity == null) return@OutlinedButton
            scope.launch {
                try {
                    tokenDeGoogle(activity, webClientId)?.let { viewModel.entrarConGoogle(it, alEntrar) }
                } catch (_: Exception) {
                    viewModel.mostrarError("No se ha podido entrar con Google")
                }
            }
        },
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Image(painterResource(R.drawable.google), contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text("Continuar con Google", style = MaterialTheme.typography.titleSmall)
    }
}

/** Primera pantalla: presenta la app y deja elegir cómo entrar. Siempre en oscuro. */
@Composable
fun BienvenidaScreen(
    alIrARegistro: () -> Unit,
    alIrALogin: () -> Unit,
    alEntrar: (DestinoTrasEntrar) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    FitTrackProyectTheme(darkTheme = true) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFF1C3A12), FondoOscuro),
                    center = androidx.compose.ui.geometry.Offset(300f, 300f),
                    radius = 1400f
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Spacer(Modifier.height(32.dp))
            Image(
                painterResource(R.drawable.ffit),
                contentDescription = "Logo de FitTrack",
                modifier = Modifier.size(96.dp).clip(MaterialTheme.shapes.large)
            )
            Spacer(Modifier.height(28.dp))
            Text("Entrena.\nCome bien.\nMide tu progreso.", style = MaterialTheme.typography.displaySmall, color = TextoOscuro)
            Spacer(Modifier.height(12.dp))
            Text(
                "Rutinas, nutrición, agua y estadísticas en una sola app.",
                style = MaterialTheme.typography.bodyLarge,
                color = TextoSecundarioOscuro
            )
            Spacer(Modifier.height(28.dp))
            Ventaja(Icons.Filled.FitnessCenter, ColoresMetrica.entreno, "Rutinas y plantillas con modo entreno")
            Ventaja(Icons.Filled.Restaurant, ColoresMetrica.calorias, "Calorías y macros con buscador de alimentos")
            Ventaja(Icons.Filled.WaterDrop, ColoresMetrica.agua, "Hidratación según tu peso")
            Ventaja(Icons.Filled.ShowChart, ColoresMetrica.carbos, "Estadísticas semanales y racha")
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            BotonPrincipal("Crear cuenta gratis", alIrARegistro)
            Spacer(Modifier.height(12.dp))
            BotonGoogle(viewModel, alEntrar)
            estado.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = alIrALogin, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text("Ya tengo cuenta · Iniciar sesión", color = TextoOscuro)
            }
        }
    }
    }
}

@Composable
private fun Ventaja(icono: ImageVector, color: Color, texto: String) {
    Row(Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        IconoMetrica(icono, color, tamano = 36.dp)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = TextoOscuro)
    }
}

@Composable
fun LoginScreen(
    alVolver: () -> Unit,
    alIrARegistro: () -> Unit,
    alEntrar: (DestinoTrasEntrar) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    FormularioAuth(
        titulo = "Bienvenido de nuevo",
        subtitulo = "Inicia sesión para seguir con tu progreso",
        estado = estado,
        viewModel = viewModel,
        conConfirmacion = false,
        textoBoton = "Iniciar sesión",
        alEnviar = { viewModel.iniciarSesion(alEntrar) },
        alVolver = alVolver,
        textoCambio = "¿No tienes cuenta? Regístrate",
        alCambiar = alIrARegistro,
        alEntrar = alEntrar
    )
}

@Composable
fun RegistroScreen(
    alVolver: () -> Unit,
    alIrALogin: () -> Unit,
    alEntrar: (DestinoTrasEntrar) -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory)
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    FormularioAuth(
        titulo = "Crea tu cuenta",
        subtitulo = "Solo necesitas un email y una contraseña",
        estado = estado,
        viewModel = viewModel,
        conConfirmacion = true,
        textoBoton = "Crear cuenta",
        alEnviar = { viewModel.registrarse(alEntrar) },
        alVolver = alVolver,
        textoCambio = "¿Ya tienes cuenta? Inicia sesión",
        alCambiar = alIrALogin,
        alEntrar = alEntrar
    )
}

@Composable
private fun FormularioAuth(
    titulo: String,
    subtitulo: String,
    estado: AuthUiState,
    viewModel: AuthViewModel,
    conConfirmacion: Boolean,
    textoBoton: String,
    alEnviar: () -> Unit,
    alVolver: () -> Unit,
    textoCambio: String,
    alCambiar: () -> Unit,
    alEntrar: (DestinoTrasEntrar) -> Unit
) {
    var verPassword by remember { mutableStateOf(false) }
    val transformacion = if (verPassword) VisualTransformation.None else PasswordVisualTransformation()
    val ojo: @Composable () -> Unit = {
        IconButton(onClick = { verPassword = !verPassword }) {
            Icon(if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Mostrar contraseña")
        }
    }

    Scaffold(topBar = { BarraSuperior("", alVolver = alVolver) }) { relleno ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(relleno)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.headlineMedium)
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            CampoTexto(estado.email, viewModel::cambiarEmail, "Email", teclado = KeyboardType.Email, icono = Icons.Filled.Email)
            CampoTexto(
                estado.password, viewModel::cambiarPassword, "Contraseña",
                teclado = KeyboardType.Password, icono = Icons.Filled.Lock, transformacion = transformacion, finalIcono = ojo
            )
            if (conConfirmacion) {
                CampoTexto(
                    estado.confirmacion, viewModel::cambiarConfirmacion, "Repite la contraseña",
                    teclado = KeyboardType.Password, icono = Icons.Filled.Lock, transformacion = transformacion
                )
            }
            estado.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            BotonPrincipal(textoBoton, alEnviar, cargando = estado.cargando)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
                Text("  o  ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                Box(Modifier.weight(1f).height(1.dp).background(MaterialTheme.colorScheme.outline))
            }
            BotonGoogle(viewModel, alEntrar)
            TextButton(onClick = alCambiar, modifier = Modifier.fillMaxWidth()) { Text(textoCambio) }
        }
    }
}
