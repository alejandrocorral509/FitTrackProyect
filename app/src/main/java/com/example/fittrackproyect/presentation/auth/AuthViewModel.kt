package com.example.fittrackproyect.presentation.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmacion: String = "",
    val cargando: Boolean = false,
    val error: String? = null
)

/** Adónde ir después de entrar: al inicio o, si el perfil está vacío, a completarlo. */
enum class DestinoTrasEntrar { INICIO, COMPLETAR_PERFIL }

class AuthViewModel(
    private val auth: AuthRepository,
    private val perfiles: PerfilRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    fun cambiarEmail(valor: String) = _estado.update { it.copy(email = valor, error = null) }
    fun cambiarPassword(valor: String) = _estado.update { it.copy(password = valor, error = null) }
    fun cambiarConfirmacion(valor: String) = _estado.update { it.copy(confirmacion = valor, error = null) }
    fun mostrarError(mensaje: String) = _estado.update { it.copy(error = mensaje, cargando = false) }

    fun iniciarSesion(alTerminar: (DestinoTrasEntrar) -> Unit) {
        val s = _estado.value
        val error = when {
            s.email.isBlank() || s.password.isBlank() -> "Rellena el email y la contraseña"
            !Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches() -> "El email no es válido"
            else -> null
        }
        if (error != null) return mostrarError(error)
        ejecutar(alTerminar) { auth.iniciarSesion(s.email, s.password) }
    }

    fun registrarse(alTerminar: (DestinoTrasEntrar) -> Unit) {
        val s = _estado.value
        val error = when {
            s.email.isBlank() || s.password.isBlank() || s.confirmacion.isBlank() -> "Rellena todos los campos"
            !Patterns.EMAIL_ADDRESS.matcher(s.email.trim()).matches() -> "El email no es válido"
            s.password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            s.password != s.confirmacion -> "Las contraseñas no coinciden"
            else -> null
        }
        if (error != null) return mostrarError(error)
        ejecutar(alTerminar) { auth.registrarse(s.email, s.password) }
    }

    fun entrarConGoogle(idToken: String, alTerminar: (DestinoTrasEntrar) -> Unit) =
        ejecutar(alTerminar) { auth.iniciarSesionConGoogle(idToken) }

    private fun ejecutar(alTerminar: (DestinoTrasEntrar) -> Unit, accion: suspend () -> Unit) {
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                accion()
                val uid = auth.uid
                val completo = uid != null && runCatching { perfiles.obtener(uid)?.completo == true }.getOrDefault(true)
                _estado.update { it.copy(cargando = false) }
                alTerminar(if (completo) DestinoTrasEntrar.INICIO else DestinoTrasEntrar.COMPLETAR_PERFIL)
            } catch (e: Exception) {
                mostrarError(mensajeDeError(e))
            }
        }
    }

    private fun mensajeDeError(e: Exception) = when (e) {
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese email"
        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil"
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Email o contraseña incorrectos"
        else -> "No se ha podido conectar. Revisa tu conexión e inténtalo de nuevo"
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AuthViewModel(contenedor.auth, contenedor.perfil) }
        }
    }
}
