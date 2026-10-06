package com.example.fittrackproyect.presentation.agua

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.repository.AguaRepository
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AguaUiState(
    val vasos: Int = 0,
    val objetivo: Int = 8,
    val semana: List<Int> = List(7) { 0 },
    val etiquetasSemana: List<String> = emptyList()
) {
    val mililitros get() = vasos * 250
    val progreso get() = vasos.toFloat() / objetivo
    val completado get() = vasos >= objetivo
}

class AguaViewModel(
    auth: AuthRepository,
    perfiles: PerfilRepository,
    private val agua: AguaRepository
) : ViewModel() {

    private val uid = auth.uid
    private val dias = Fechas.ultimosDias(7)
    private val hoy = Fechas.clave(dias.last())
    private val semanaAnterior = MutableStateFlow(List(6) { 0 })

    val estado: StateFlow<AguaUiState> = combine(
        uid?.let { agua.observar(it, hoy) } ?: emptyFlow(),
        uid?.let { perfiles.observar(it) } ?: emptyFlow(),
        semanaAnterior
    ) { vasos, perfil, anteriores ->
        AguaUiState(
            vasos = vasos,
            objetivo = perfil?.vasosObjetivo ?: 8,
            semana = anteriores + vasos,
            etiquetasSemana = dias.map(Fechas::inicial)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AguaUiState(etiquetasSemana = dias.map(Fechas::inicial)))

    init {
        // Los seis días anteriores no cambian, basta con leerlos una vez
        val usuario = uid
        if (usuario != null) viewModelScope.launch {
            semanaAnterior.value = dias.dropLast(1)
                .map { dia -> async { runCatching { agua.vasos(usuario, Fechas.clave(dia)) }.getOrDefault(0) } }
                .awaitAll()
        }
    }

    fun cambiar(diferencia: Int) {
        val usuario = uid ?: return
        val nuevo = (estado.value.vasos + diferencia).coerceIn(0, 30)
        viewModelScope.launch { runCatching { agua.guardar(usuario, hoy, nuevo) } }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AguaViewModel(contenedor.auth, contenedor.perfil, contenedor.agua) }
        }
    }
}
