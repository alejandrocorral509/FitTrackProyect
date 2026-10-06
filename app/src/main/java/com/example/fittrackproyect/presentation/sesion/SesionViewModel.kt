package com.example.fittrackproyect.presentation.sesion

import android.os.SystemClock
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.model.Rutina
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.RutinaRepository
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class SesionUiState(
    val cargando: Boolean = true,
    val rutina: Rutina? = null,
    /** Series completadas de cada ejercicio, en el mismo orden que la rutina. */
    val seriesHechas: List<Int> = emptyList(),
    val segundos: Long = 0,
    val descansoElegido: Int = 90,
    val descansoRestante: Int? = null,
    val guardando: Boolean = false,
    val terminado: Boolean = false
) {
    val totalSeries get() = rutina?.ejercicios?.sumOf { it.series.coerceAtLeast(1) } ?: 0
    val seriesCompletadas get() = seriesHechas.sum()
    val progreso get() = if (totalSeries == 0) 0f else seriesCompletadas.toFloat() / totalSeries
    val todoHecho get() = totalSeries > 0 && seriesCompletadas >= totalSeries
}

/** Lleva el cronómetro, las series hechas y el temporizador de descanso de un entreno. */
class SesionViewModel(
    estadoGuardado: SavedStateHandle,
    private val auth: AuthRepository,
    private val repo: RutinaRepository
) : ViewModel() {

    private val id: String = checkNotNull(estadoGuardado["id"])
    private val inicio = SystemClock.elapsedRealtime()
    private var finDescanso: Long? = null

    private val _estado = MutableStateFlow(SesionUiState())
    val estado: StateFlow<SesionUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val rutina = auth.uid?.let { runCatching { repo.obtener(it, id) }.getOrNull() }
            _estado.update {
                it.copy(cargando = false, rutina = rutina, seriesHechas = List(rutina?.ejercicios?.size ?: 0) { 0 })
            }
        }
        // Un solo reloj para el tiempo total y el descanso, calculado con el tiempo real del sistema
        viewModelScope.launch {
            while (isActive) {
                val ahora = SystemClock.elapsedRealtime()
                _estado.update { s ->
                    if (s.terminado) return@update s
                    val restante = finDescanso?.let { ((it - ahora + 999) / 1000).toInt() }
                    if (restante != null && restante <= 0) finDescanso = null
                    s.copy(segundos = (ahora - inicio) / 1000, descansoRestante = restante?.takeIf { it > 0 })
                }
                delay(250)
            }
        }
    }

    /** Tocar la burbuja de una serie la marca; tocar una ya hecha deshace hasta ahí. */
    fun tocarSerie(ejercicio: Int, serie: Int) {
        val hechas = _estado.value.seriesHechas.getOrNull(ejercicio) ?: return
        val nuevas = if (serie < hechas) serie else serie + 1
        _estado.update { s -> s.copy(seriesHechas = s.seriesHechas.toMutableList().also { it[ejercicio] = nuevas }) }
        if (nuevas > hechas && !_estado.value.todoHecho) empezarDescanso(_estado.value.descansoElegido)
    }

    fun elegirDescanso(segundos: Int) = _estado.update { it.copy(descansoElegido = segundos) }

    fun sumarDescanso(segundos: Int) {
        finDescanso = (finDescanso ?: SystemClock.elapsedRealtime()) + segundos * 1000L
    }

    fun saltarDescanso() {
        finDescanso = null
        _estado.update { it.copy(descansoRestante = null) }
    }

    fun terminar() {
        val usuario = auth.uid ?: return
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            runCatching { repo.marcarCompletada(usuario, id, Fechas.clave(Fechas.hoy()), true) }
            finDescanso = null
            _estado.update { it.copy(guardando = false, terminado = true, descansoRestante = null) }
        }
    }

    private fun empezarDescanso(segundos: Int) {
        finDescanso = SystemClock.elapsedRealtime() + segundos * 1000L
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { SesionViewModel(createSavedStateHandle(), contenedor.auth, contenedor.rutinas) }
        }
    }
}
