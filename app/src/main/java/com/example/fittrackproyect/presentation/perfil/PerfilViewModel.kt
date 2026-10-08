package com.example.fittrackproyect.presentation.perfil

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.model.Perfil
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import com.example.fittrackproyect.domain.CategoriaImc
import com.example.fittrackproyect.domain.Macros
import com.example.fittrackproyect.domain.NivelActividad
import com.example.fittrackproyect.domain.Nutricion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val cargando: Boolean = true,
    val nombre: String = "",
    val email: String = "",
    val foto: String? = null,
    val subiendoFoto: Boolean = false,
    val peso: String = "",
    val estatura: String = "",
    val edad: String = "",
    val esHombre: Boolean = true,
    val nivel: NivelActividad = NivelActividad.MODERADO,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
) {
    private val pesoNum get() = peso.replace(',', '.').toDoubleOrNull()
    private val estaturaNum get() = estatura.replace(',', '.').toDoubleOrNull()
    private val edadNum get() = edad.toIntOrNull()

    /** Resultados calculados al momento, mientras el usuario escribe. */
    val imc: Double? get() {
        val p = pesoNum ?: return null
        val e = estaturaNum?.takeIf { it >= 100 } ?: return null
        return Nutricion.imc(p, e)
    }
    val categoriaImc: CategoriaImc? get() = imc?.let(Nutricion::categoriaImc)
    val calorias: Int? get() {
        val p = pesoNum ?: return null
        val e = estaturaNum ?: return null
        val a = edadNum?.takeIf { it > 0 } ?: return null
        return Nutricion.caloriasDiarias(p, e, a, esHombre, nivel)
    }
    val macros: Macros? get() = calorias?.let { Nutricion.macrosObjetivo(pesoNum!!, it) }
    val vasos: Int get() = Nutricion.vasosObjetivo(pesoNum)

    fun validar(): String? {
        val p = pesoNum
        val e = estaturaNum
        val a = edadNum
        return when {
            p == null || p !in 30.0..300.0 -> "Introduce un peso entre 30 y 300 kg"
            e == null || e !in 100.0..250.0 -> "Introduce una estatura entre 100 y 250 cm"
            a == null || a !in 12..110 -> "Introduce una edad entre 12 y 110 años"
            else -> null
        }
    }

    fun aPerfil() = Perfil(pesoNum, estaturaNum, edadNum, esHombre, nivel, foto = foto)
}

class PerfilViewModel(
    private val auth: AuthRepository,
    private val perfiles: PerfilRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(PerfilUiState(nombre = auth.nombre, email = auth.email.orEmpty()))
    val estado: StateFlow<PerfilUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val perfil = auth.uid?.let { runCatching { perfiles.obtener(it) }.getOrNull() }
            _estado.update {
                if (perfil == null) it.copy(cargando = false)
                else it.copy(
                    cargando = false,
                    foto = perfil.foto,
                    peso = perfil.peso?.let(::sinDecimalesSobrantes).orEmpty(),
                    estatura = perfil.estatura?.let(::sinDecimalesSobrantes).orEmpty(),
                    edad = perfil.edad?.toString().orEmpty(),
                    esHombre = perfil.esHombre,
                    nivel = perfil.nivelActividad
                )
            }
        }
    }

    fun cambiarPeso(valor: String) = _estado.update { it.copy(peso = soloNumero(valor), error = null) }
    fun cambiarEstatura(valor: String) = _estado.update { it.copy(estatura = soloNumero(valor), error = null) }
    fun cambiarEdad(valor: String) = _estado.update { it.copy(edad = valor.filter(Char::isDigit).take(3), error = null) }
    fun cambiarSexo(esHombre: Boolean) = _estado.update { it.copy(esHombre = esHombre) }
    fun cambiarNivel(nivel: NivelActividad) = _estado.update { it.copy(nivel = nivel) }
    fun mensajeMostrado() = _estado.update { it.copy(mensaje = null) }

    fun guardar(alGuardar: () -> Unit) {
        val uid = auth.uid ?: return
        _estado.value.validar()?.let { error -> return _estado.update { it.copy(error = error) } }
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                perfiles.guardar(uid, _estado.value.aPerfil())
                _estado.update { it.copy(guardando = false, mensaje = "Perfil guardado") }
                alGuardar()
            } catch (_: Exception) {
                _estado.update { it.copy(guardando = false, error = "No se ha podido guardar. Revisa tu conexión") }
            }
        }
    }

    fun cambiarFoto(imagen: Uri) {
        val uid = auth.uid ?: return
        _estado.update { it.copy(subiendoFoto = true) }
        viewModelScope.launch {
            try {
                val foto = perfiles.guardarFoto(uid, imagen)
                _estado.update { it.copy(subiendoFoto = false, foto = foto, mensaje = "Foto actualizada") }
            } catch (_: Exception) {
                _estado.update { it.copy(subiendoFoto = false, mensaje = "No se ha podido guardar la foto") }
            }
        }
    }

    fun cerrarSesion() = auth.cerrarSesion()

    private fun soloNumero(valor: String) = valor.filter { it.isDigit() || it == '.' || it == ',' }.take(6)

    private fun sinDecimalesSobrantes(numero: Double) =
        if (numero % 1.0 == 0.0) numero.toInt().toString() else numero.toString()

    companion object {
        val Factory = viewModelFactory {
            initializer { PerfilViewModel(contenedor.auth, contenedor.perfil) }
        }
    }
}
