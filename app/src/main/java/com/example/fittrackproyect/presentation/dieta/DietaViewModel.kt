package com.example.fittrackproyect.presentation.dieta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.model.Comida
import com.example.fittrackproyect.data.model.TipoComida
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.DietaRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import com.example.fittrackproyect.domain.Macros
import com.example.fittrackproyect.domain.Nutricion
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DietaUiState(
    val cargando: Boolean = true,
    val fecha: LocalDate = Fechas.hoy(),
    val comidas: List<Comida> = emptyList(),
    val caloriasObjetivo: Int = 2000,
    val macrosObjetivo: Macros? = null
) {
    val esHoy get() = fecha == Fechas.hoy()
    val fechaTexto get() = Fechas.relativo(fecha)
    val claveFecha get() = Fechas.clave(fecha)
    val calorias get() = comidas.sumOf { it.calorias }
    val proteinas get() = comidas.sumOf { it.proteinas }
    val carbos get() = comidas.sumOf { it.carbos }
    val grasas get() = comidas.sumOf { it.grasas }
    fun deTipo(tipo: TipoComida) = comidas.filter { it.tipo == tipo }
}

@OptIn(ExperimentalCoroutinesApi::class)
class DietaViewModel(
    auth: AuthRepository,
    perfiles: PerfilRepository,
    private val dieta: DietaRepository
) : ViewModel() {

    private val uid = auth.uid
    private val fecha = MutableStateFlow(Fechas.hoy())

    // Cada lista de comidas va unida a su día, para no mezclar datos al cambiar de fecha
    private val comidasDelDia = fecha.flatMapLatest { dia ->
        (uid?.let { dieta.observar(it, Fechas.clave(dia)) } ?: emptyFlow())
            .map { dia to it }
            .onStart { emit(dia to emptyList()) }
    }

    val estado: StateFlow<DietaUiState> = combine(
        comidasDelDia,
        (uid?.let { perfiles.observar(it) } ?: emptyFlow()).onStart { emit(null) }
    ) { (dia, comidas), perfil ->
        val calorias = perfil?.caloriasObjetivo ?: 2000
        DietaUiState(
            cargando = false,
            fecha = dia,
            comidas = comidas,
            caloriasObjetivo = calorias,
            macrosObjetivo = perfil?.peso?.let { Nutricion.macrosObjetivo(it, calorias) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DietaUiState())

    fun diaAnterior() { fecha.value = fecha.value.minusDays(1) }

    fun diaSiguiente() {
        if (fecha.value < Fechas.hoy()) fecha.value = fecha.value.plusDays(1)
    }

    fun eliminar(comida: Comida) {
        val usuario = uid ?: return
        val dia = estado.value.claveFecha
        viewModelScope.launch { runCatching { dieta.eliminar(usuario, dia, comida.id) } }
    }

    /** Vuelve a añadir un alimento borrado (botón "Deshacer"). */
    fun restaurar(comida: Comida, claveFecha: String) {
        val usuario = uid ?: return
        viewModelScope.launch { runCatching { dieta.anadir(usuario, claveFecha, comida) } }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { DietaViewModel(contenedor.auth, contenedor.perfil, contenedor.dieta) }
        }
    }
}
