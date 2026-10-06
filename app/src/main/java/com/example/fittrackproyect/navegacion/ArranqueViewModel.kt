package com.example.fittrackproyect.navegacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Decide la primera pantalla mientras se muestra el splash:
 * sin sesión → bienvenida; con sesión y perfil vacío → completar perfil; si no → inicio.
 */
class ArranqueViewModel(
    auth: AuthRepository,
    perfiles: PerfilRepository
) : ViewModel() {

    private val _destino = MutableStateFlow<String?>(null)
    val destino: StateFlow<String?> = _destino.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = auth.uid
            _destino.value = if (uid == null) {
                Rutas.BIENVENIDA
            } else {
                // Sin conexión no bloqueamos el arranque: se entra al inicio
                val completo = withTimeoutOrNull(4000) {
                    runCatching { perfiles.obtener(uid)?.completo == true }.getOrDefault(true)
                } ?: true
                if (completo) Rutas.INICIO else Rutas.COMPLETAR_PERFIL
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { ArranqueViewModel(contenedor.auth, contenedor.perfil) }
        }
    }
}
