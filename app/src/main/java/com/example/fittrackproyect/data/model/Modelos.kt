package com.example.fittrackproyect.data.model

import com.example.fittrackproyect.domain.NivelActividad
import com.example.fittrackproyect.domain.Nutricion

/** Documento users/{uid} */
data class Perfil(
    val peso: Double? = null,
    val estatura: Double? = null,
    val edad: Int? = null,
    val esHombre: Boolean = true,
    val nivelActividad: NivelActividad = NivelActividad.MODERADO,
    val objetivoCalorias: Int? = null,
    /** Foto de perfil en JPEG codificado en Base64 */
    val foto: String? = null
) {
    val completo: Boolean get() = peso != null && estatura != null && edad != null

    val imc: Double? get() = if (peso != null && estatura != null && estatura > 0) Nutricion.imc(peso, estatura) else null

    val caloriasObjetivo: Int get() = objetivoCalorias?.takeIf { it > 0 } ?: 2000

    val vasosObjetivo: Int get() = Nutricion.vasosObjetivo(peso)
}

/** Documento users/{uid}/dieta/{fecha}/comidas/{id} */
data class Comida(
    val id: String = "",
    val nombre: String = "",
    val calorias: Int = 0,
    val proteinas: Int = 0,
    val carbos: Int = 0,
    val grasas: Int = 0,
    val tipo: TipoComida = TipoComida.DESAYUNO
)

enum class TipoComida(val etiqueta: String) {
    DESAYUNO("Desayuno"),
    ALMUERZO("Almuerzo"),
    CENA("Cena"),
    SNACKS("Snacks");

    companion object {
        fun desdeEtiqueta(etiqueta: String?): TipoComida =
            entries.find { it.etiqueta == etiqueta || it.name == etiqueta } ?: SNACKS
    }
}

/** Valores nutricionales por cada 100 g. */
data class Alimento(
    val nombre: String,
    val calorias: Double,
    val proteinas: Double,
    val carbos: Double,
    val grasas: Double
) {
    fun paraGramos(gramos: Int, tipo: TipoComida): Comida {
        val factor = gramos / 100.0
        return Comida(
            nombre = "$nombre (${gramos}g)",
            calorias = (calorias * factor).toInt(),
            proteinas = (proteinas * factor).toInt(),
            carbos = (carbos * factor).toInt(),
            grasas = (grasas * factor).toInt(),
            tipo = tipo
        )
    }
}

data class Ejercicio(
    val nombre: String = "",
    val series: Int = 0,
    val repeticiones: Int = 0,
    val pesoKg: Double = 0.0
)

/** Documento users/{uid}/rutinas/{id} */
data class Rutina(
    val id: String = "",
    val nombre: String = "",
    val categoria: String = "Full Body",
    val ejercicios: List<Ejercicio> = emptyList(),
    /** Fechas ("yyyy-MM-dd") en las que se completó la rutina. */
    val historial: Set<String> = emptySet()
) {
    fun completadaEl(fecha: String): Boolean = fecha in historial
}
