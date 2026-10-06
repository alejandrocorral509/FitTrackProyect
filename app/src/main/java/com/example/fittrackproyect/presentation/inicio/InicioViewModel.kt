package com.example.fittrackproyect.presentation.inicio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.fittrackproyect.contenedor
import com.example.fittrackproyect.data.model.Comida
import com.example.fittrackproyect.data.model.Perfil
import com.example.fittrackproyect.data.model.Rutina
import com.example.fittrackproyect.data.repository.AguaRepository
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.DietaRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import com.example.fittrackproyect.data.repository.RutinaRepository
import com.example.fittrackproyect.domain.Racha
import com.example.fittrackproyect.util.Fechas
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime

data class InicioUiState(
    val cargando: Boolean = true,
    val saludo: String = "",
    val nombre: String = "",
    val fecha: String = "",
    val fotoUrl: String? = null,
    val perfilCompleto: Boolean = true,
    val calorias: Int = 0,
    val caloriasObjetivo: Int = 2000,
    val proteinas: Int = 0,
    val carbos: Int = 0,
    val grasas: Int = 0,
    val vasos: Int = 0,
    val vasosObjetivo: Int = 8,
    val rutinasHoy: Int = 0,
    val totalRutinas: Int = 0,
    val racha: Int = 0,
    val siguienteRutina: Rutina? = null
) {
    val progresoCalorias get() = calorias.toFloat() / caloriasObjetivo
    val progresoAgua get() = vasos.toFloat() / vasosObjetivo
    val progresoEntreno get() = if (rutinasHoy > 0) 1f else 0f
}

class InicioViewModel(
    private val auth: AuthRepository,
    perfiles: PerfilRepository,
    private val agua: AguaRepository,
    dieta: DietaRepository,
    rutinas: RutinaRepository
) : ViewModel() {

    private val uid = auth.uid
    private val hoy = Fechas.clave(Fechas.hoy())

    val estado: StateFlow<InicioUiState> =
        if (uid == null) flowOf(InicioUiState(cargando = false)).stateIn(viewModelScope, SharingStarted.Eagerly, InicioUiState())
        else combine(
            perfiles.observar(uid),
            agua.observar(uid, hoy),
            dieta.observar(uid, hoy),
            rutinas.observar(uid)
        ) { perfil, vasos, comidas, listaRutinas -> construir(perfil, vasos, comidas, listaRutinas) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InicioUiState())

    private fun construir(perfil: Perfil?, vasos: Int, comidas: List<Comida>, rutinas: List<Rutina>): InicioUiState {
        val diasEntrenados = rutinas.flatMap { it.historial }.mapNotNull(Fechas::desdeClave).toSet()
        return InicioUiState(
            cargando = false,
            saludo = saludo(),
            nombre = auth.nombre,
            fecha = Fechas.largo(Fechas.hoy()),
            fotoUrl = perfil?.fotoUrl,
            perfilCompleto = perfil?.completo == true,
            calorias = comidas.sumOf { it.calorias },
            caloriasObjetivo = perfil?.caloriasObjetivo ?: 2000,
            proteinas = comidas.sumOf { it.proteinas },
            carbos = comidas.sumOf { it.carbos },
            grasas = comidas.sumOf { it.grasas },
            vasos = vasos,
            vasosObjetivo = perfil?.vasosObjetivo ?: 8,
            rutinasHoy = rutinas.count { it.completadaEl(hoy) },
            totalRutinas = rutinas.size,
            racha = Racha.diasSeguidos(diasEntrenados, Fechas.hoy()),
            siguienteRutina = rutinas.filterNot { it.completadaEl(hoy) }
                .minByOrNull { it.historial.maxOrNull() ?: "" }
        )
    }

    fun sumarVaso() {
        val usuario = uid ?: return
        viewModelScope.launch {
            runCatching { agua.guardar(usuario, hoy, estado.value.vasos + 1) }
        }
    }

    private fun saludo(): String = when (LocalTime.now().hour) {
        in 6..13 -> "Buenos días"
        in 14..20 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                InicioViewModel(contenedor.auth, contenedor.perfil, contenedor.agua, contenedor.dieta, contenedor.rutinas)
            }
        }
    }
}
