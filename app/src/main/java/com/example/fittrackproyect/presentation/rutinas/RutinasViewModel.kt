package com.example.fittrackproyect.presentation.rutinas

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.model.Ejercicio
import com.example.fittrackproyect.data.model.Plantilla
import com.example.fittrackproyect.data.model.Rutina
import com.example.fittrackproyect.data.model.plantillas
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.RutinaRepository
import com.example.fittrackproyect.domain.Racha
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// ---------- Lista de rutinas ----------

data class DiaSemana(val fecha: LocalDate, val entrenado: Boolean)

data class RutinasUiState(
    val cargando: Boolean = true,
    val rutinas: List<Rutina> = emptyList(),
    val filtro: String? = null,
    val semana: List<DiaSemana> = emptyList(),
    val racha: Int = 0
) {
    val hoy get() = Fechas.clave(Fechas.hoy())
    val categoriasUsadas get() = rutinas.map { it.categoria }.distinct()
    val visibles get() = if (filtro == null) rutinas else rutinas.filter { it.categoria == filtro }
}

class RutinasViewModel(
    auth: AuthRepository,
    private val repo: RutinaRepository
) : ViewModel() {

    private val uid = auth.uid
    private val filtro = MutableStateFlow<String?>(null)

    val estado: StateFlow<RutinasUiState> = combine(
        uid?.let { repo.observar(it) } ?: emptyFlow(),
        filtro
    ) { rutinas, filtroActual ->
        val dias = rutinas.flatMap { it.historial }.mapNotNull(Fechas::desdeClave).toSet()
        RutinasUiState(
            cargando = false,
            rutinas = rutinas,
            filtro = filtroActual?.takeIf { f -> rutinas.any { it.categoria == f } },
            semana = Fechas.ultimosDias(7).map { DiaSemana(it, it in dias) },
            racha = Racha.diasSeguidos(dias, Fechas.hoy())
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RutinasUiState())

    fun filtrar(categoria: String?) { filtro.value = categoria }

    fun alternarHecha(rutina: Rutina) {
        val usuario = uid ?: return
        val hoy = Fechas.clave(Fechas.hoy())
        viewModelScope.launch { runCatching { repo.marcarCompletada(usuario, rutina.id, hoy, !rutina.completadaEl(hoy)) } }
    }

    fun eliminar(rutina: Rutina) {
        val usuario = uid ?: return
        viewModelScope.launch { runCatching { repo.eliminar(usuario, rutina.id) } }
    }

    fun restaurar(rutina: Rutina) {
        val usuario = uid ?: return
        viewModelScope.launch { runCatching { repo.crear(usuario, rutina.nombre, rutina.categoria, rutina.ejercicios) } }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { RutinasViewModel(contenedor.auth, contenedor.rutinas) }
        }
    }
}

// ---------- Crear y editar ----------

data class EditorUiState(
    val editando: Boolean = false,
    val cargando: Boolean = false,
    val nombre: String = "",
    val categoria: String = "Full Body",
    val ejercicios: List<Ejercicio> = emptyList(),
    val nuevoNombre: String = "",
    val nuevasSeries: String = "",
    val nuevasReps: String = "",
    val nuevoPeso: String = "",
    val guardando: Boolean = false,
    val error: String? = null
) {
    val puedeGuardar get() = nombre.isNotBlank() && ejercicios.isNotEmpty()
}

class EditorRutinaViewModel(
    estadoGuardado: SavedStateHandle,
    private val auth: AuthRepository,
    private val repo: RutinaRepository
) : ViewModel() {

    private val id: String? = estadoGuardado["id"]
    private val _estado = MutableStateFlow(EditorUiState(editando = id != null, cargando = id != null))
    val estado: StateFlow<EditorUiState> = _estado.asStateFlow()

    init {
        val usuario = auth.uid
        if (id != null && usuario != null) viewModelScope.launch {
            val rutina = runCatching { repo.obtener(usuario, id) }.getOrNull()
            _estado.update {
                if (rutina == null) it.copy(cargando = false, error = "No se ha encontrado la rutina")
                else it.copy(cargando = false, nombre = rutina.nombre, categoria = rutina.categoria, ejercicios = rutina.ejercicios)
            }
        }
    }

    fun cambiarNombre(v: String) = _estado.update { it.copy(nombre = v.take(40), error = null) }
    fun cambiarCategoria(v: String) = _estado.update { it.copy(categoria = v) }
    fun cambiarNuevoNombre(v: String) = _estado.update { it.copy(nuevoNombre = v.take(40)) }
    fun cambiarSeries(v: String) = _estado.update { it.copy(nuevasSeries = v.filter(Char::isDigit).take(2)) }
    fun cambiarReps(v: String) = _estado.update { it.copy(nuevasReps = v.filter(Char::isDigit).take(3)) }
    fun cambiarPeso(v: String) = _estado.update { it.copy(nuevoPeso = v.filter { c -> c.isDigit() || c == '.' || c == ',' }.take(5)) }

    fun anadirEjercicio() = _estado.update {
        if (it.nuevoNombre.isBlank()) return@update it.copy(error = "Escribe el nombre del ejercicio")
        val ejercicio = Ejercicio(
            nombre = it.nuevoNombre.trim(),
            series = it.nuevasSeries.toIntOrNull() ?: 3,
            repeticiones = it.nuevasReps.toIntOrNull() ?: 10,
            pesoKg = it.nuevoPeso.replace(',', '.').toDoubleOrNull() ?: 0.0
        )
        it.copy(ejercicios = it.ejercicios + ejercicio, nuevoNombre = "", nuevasSeries = "", nuevasReps = "", nuevoPeso = "", error = null)
    }

    fun quitarEjercicio(indice: Int) = _estado.update { it.copy(ejercicios = it.ejercicios.filterIndexed { i, _ -> i != indice }) }

    fun mover(indice: Int, haciaArriba: Boolean) = _estado.update {
        val destino = if (haciaArriba) indice - 1 else indice + 1
        if (destino !in it.ejercicios.indices) return@update it
        val lista = it.ejercicios.toMutableList()
        lista.add(destino, lista.removeAt(indice))
        it.copy(ejercicios = lista)
    }

    fun guardar(alTerminar: () -> Unit) {
        val s = _estado.value
        val usuario = auth.uid ?: return
        if (!s.puedeGuardar) return _estado.update { it.copy(error = "Ponle nombre y añade al menos un ejercicio") }
        _estado.update { it.copy(guardando = true) }
        viewModelScope.launch {
            try {
                if (id == null) repo.crear(usuario, s.nombre, s.categoria, s.ejercicios)
                else repo.actualizar(usuario, id, s.nombre, s.categoria, s.ejercicios)
                alTerminar()
            } catch (_: Exception) {
                _estado.update { it.copy(guardando = false, error = "No se ha podido guardar. Revisa tu conexión") }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { EditorRutinaViewModel(createSavedStateHandle(), contenedor.auth, contenedor.rutinas) }
        }
    }
}

// ---------- Plantillas ----------

class PlantillasViewModel(
    private val auth: AuthRepository,
    private val repo: RutinaRepository
) : ViewModel() {

    private val _anadidas = MutableStateFlow(setOf<String>())
    val anadidas: StateFlow<Set<String>> = _anadidas.asStateFlow()
    val lista: List<Plantilla> = plantillas

    fun anadir(plantilla: Plantilla, alAnadir: () -> Unit) {
        val usuario = auth.uid ?: return
        if (plantilla.nombre in _anadidas.value) return
        _anadidas.update { it + plantilla.nombre }
        viewModelScope.launch {
            runCatching { repo.crear(usuario, plantilla.nombre, plantilla.categoria, plantilla.ejercicios) }
                .onSuccess { alAnadir() }
                .onFailure { _anadidas.update { it - plantilla.nombre } }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { PlantillasViewModel(contenedor.auth, contenedor.rutinas) }
        }
    }
}
