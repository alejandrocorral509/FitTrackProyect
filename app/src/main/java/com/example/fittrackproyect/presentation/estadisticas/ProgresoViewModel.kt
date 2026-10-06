package com.example.fittrackproyect.presentation.estadisticas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.repository.AguaRepository
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.DietaRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import com.example.fittrackproyect.data.repository.RutinaRepository
import com.example.fittrackproyect.domain.Racha
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class Periodo(val dias: Int, val etiqueta: String) {
    SEMANA(7, "7 días"),
    MES(30, "30 días")
}

data class ProgresoUiState(
    val cargando: Boolean = true,
    val periodo: Periodo = Periodo.SEMANA,
    val etiquetas: List<String> = emptyList(),
    val calorias: List<Int> = emptyList(),
    val vasos: List<Int> = emptyList(),
    val entrenos: List<Int> = emptyList(),
    val caloriasObjetivo: Int = 2000,
    val vasosObjetivo: Int = 8,
    val rachaActual: Int = 0,
    val mejorRacha: Int = 0,
    val totalEntrenos: Int = 0
) {
    /** Media solo de los días con algo registrado, para no hundirla con días vacíos. */
    val mediaCalorias get() = calorias.filter { it > 0 }.takeIf { it.isNotEmpty() }?.average()?.toInt() ?: 0
    val mediaVasos get() = vasos.filter { it > 0 }.takeIf { it.isNotEmpty() }?.average() ?: 0.0
    val diasEntrenados get() = entrenos.count { it > 0 }
    val diasObjetivoAgua get() = vasos.count { it >= vasosObjetivo }
}

class ProgresoViewModel(
    private val auth: AuthRepository,
    private val perfiles: PerfilRepository,
    private val agua: AguaRepository,
    private val dieta: DietaRepository,
    private val rutinas: RutinaRepository
) : ViewModel() {

    private val _estado = MutableStateFlow(ProgresoUiState())
    val estado: StateFlow<ProgresoUiState> = _estado.asStateFlow()

    init { cargar(Periodo.SEMANA) }

    fun elegir(periodo: Periodo) {
        if (periodo != _estado.value.periodo) cargar(periodo)
    }

    private fun cargar(periodo: Periodo) {
        val uid = auth.uid ?: return
        _estado.update { it.copy(cargando = true, periodo = periodo) }
        viewModelScope.launch {
            val dias = Fechas.ultimosDias(periodo.dias)
            val perfil = runCatching { perfiles.obtener(uid) }.getOrNull()
            // Las lecturas de cada día se lanzan a la vez para que cargue rápido
            val calorias = dias.map { async { runCatching { dieta.calorias(uid, Fechas.clave(it)) }.getOrDefault(0) } }
            val vasos = dias.map { async { runCatching { agua.vasos(uid, Fechas.clave(it)) }.getOrDefault(0) } }
            val lista = runCatching { rutinas.observar(uid).first() }.getOrDefault(emptyList())
            val historial = lista.flatMap { it.historial }
            val entrenadosPorDia = historial.groupingBy { it }.eachCount()
            val diasEntrenados = historial.mapNotNull(Fechas::desdeClave).toSet()

            _estado.value = ProgresoUiState(
                cargando = false,
                periodo = periodo,
                etiquetas = etiquetas(dias, periodo),
                calorias = calorias.awaitAll(),
                vasos = vasos.awaitAll(),
                entrenos = dias.map { entrenadosPorDia[Fechas.clave(it)] ?: 0 },
                caloriasObjetivo = perfil?.caloriasObjetivo ?: 2000,
                vasosObjetivo = perfil?.vasosObjetivo ?: 8,
                rachaActual = Racha.diasSeguidos(diasEntrenados, Fechas.hoy()),
                mejorRacha = Racha.mejor(diasEntrenados),
                totalEntrenos = historial.size
            )
        }
    }

    /** En la vista de 30 días solo se rotula uno de cada cinco para que se lea bien. */
    private fun etiquetas(dias: List<LocalDate>, periodo: Periodo) = when (periodo) {
        Periodo.SEMANA -> dias.map(Fechas::inicial)
        Periodo.MES -> dias.mapIndexed { i, dia -> if ((dias.lastIndex - i) % 5 == 0) "${dia.dayOfMonth}" else "" }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                ProgresoViewModel(contenedor.auth, contenedor.perfil, contenedor.agua, contenedor.dieta, contenedor.rutinas)
            }
        }
    }
}
