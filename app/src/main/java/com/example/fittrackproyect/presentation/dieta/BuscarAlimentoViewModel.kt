package com.example.fittrackproyect.presentation.dieta

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.model.Alimento
import com.example.fittrackproyect.data.model.TipoComida
import com.example.fittrackproyect.data.repository.AlimentoRepository
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.DietaRepository
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BuscarUiState(
    val tipo: TipoComida = TipoComida.DESAYUNO,
    val texto: String = "",
    val locales: List<Alimento> = emptyList(),
    val online: List<Alimento> = emptyList(),
    val buscando: Boolean = false,
    val seleccionado: Alimento? = null,
    val gramos: String = "100",
    val guardando: Boolean = false
) {
    val gramosNum get() = gramos.toIntOrNull()?.coerceIn(1, 2000) ?: 100
    val sinResultados get() = texto.length >= 2 && !buscando && locales.isEmpty() && online.isEmpty()
}

@OptIn(FlowPreview::class)
class BuscarAlimentoViewModel(
    estadoGuardado: SavedStateHandle,
    private val auth: AuthRepository,
    private val dieta: DietaRepository,
    private val alimentos: AlimentoRepository
) : ViewModel() {

    private val fecha: String = estadoGuardado["fecha"] ?: Fechas.clave(Fechas.hoy())
    private val _estado = MutableStateFlow(
        BuscarUiState(
            tipo = TipoComida.desdeEtiqueta(estadoGuardado["tipo"]),
            locales = alimentos.buscarLocal("")
        )
    )
    val estado: StateFlow<BuscarUiState> = _estado.asStateFlow()

    init {
        // Espera a que el usuario deje de escribir antes de llamar a la API
        viewModelScope.launch {
            _estado.map { it.texto.trim() }
                .distinctUntilChanged()
                .debounce(500)
                .collect { texto ->
                    if (texto.length < 2) {
                        _estado.update { it.copy(online = emptyList(), buscando = false) }
                    } else {
                        _estado.update { it.copy(buscando = true) }
                        val resultados = runCatching { alimentos.buscarOnline(texto) }.getOrDefault(emptyList())
                        _estado.update { it.copy(online = resultados, buscando = false) }
                    }
                }
        }
    }

    fun cambiarTexto(texto: String) =
        _estado.update { it.copy(texto = texto, locales = alimentos.buscarLocal(texto)) }

    fun seleccionar(alimento: Alimento?) = _estado.update { it.copy(seleccionado = alimento, gramos = "100") }

    fun cambiarGramos(valor: String) = _estado.update { it.copy(gramos = valor.filter(Char::isDigit).take(4)) }

    fun anadir(alTerminar: () -> Unit) {
        val s = _estado.value
        val alimento = s.seleccionado ?: return
        val uid = auth.uid ?: return
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            runCatching { dieta.anadir(uid, fecha, alimento.paraGramos(s.gramosNum, s.tipo)) }
            _estado.update { it.copy(guardando = false, seleccionado = null) }
            alTerminar()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                BuscarAlimentoViewModel(createSavedStateHandle(), contenedor.auth, contenedor.dieta, contenedor.alimentos)
            }
        }
    }
}
