package com.example.fittrackproyect.domain

import kotlin.math.roundToInt

/** Niveles de actividad y su multiplicador para calcular el gasto calórico diario. */
enum class NivelActividad(val etiqueta: String, val multiplicador: Double) {
    SEDENTARIO("Sedentario (poco o ningún ejercicio)", 1.2),
    LIGERO("Ligero (1-3 días/semana)", 1.375),
    MODERADO("Moderado (3-5 días/semana)", 1.55),
    ACTIVO("Activo (6-7 días/semana)", 1.725),
    MUY_ACTIVO("Muy activo (ejercicio intenso 2x/día)", 1.9);

    companion object {
        fun desdeEtiqueta(etiqueta: String?): NivelActividad =
            entries.find { it.etiqueta == etiqueta } ?: MODERADO
    }
}

enum class CategoriaImc(val etiqueta: String) {
    BAJO_PESO("Bajo peso"),
    NORMAL("Peso normal"),
    SOBREPESO("Sobrepeso"),
    OBESIDAD("Obesidad")
}

data class Macros(val proteinas: Int, val carbos: Int, val grasas: Int)

/** Cálculos de nutrición. Son funciones puras para poder probarlas con tests unitarios. */
object Nutricion {

    /** Índice de masa corporal: peso (kg) / estatura (m)². */
    fun imc(pesoKg: Double, estaturaCm: Double): Double {
        val metros = estaturaCm / 100
        return pesoKg / (metros * metros)
    }

    fun categoriaImc(imc: Double): CategoriaImc = when {
        imc < 18.5 -> CategoriaImc.BAJO_PESO
        imc < 25.0 -> CategoriaImc.NORMAL
        imc < 30.0 -> CategoriaImc.SOBREPESO
        else -> CategoriaImc.OBESIDAD
    }

    /** Gasto calórico diario (TDEE) con la fórmula de Mifflin-St Jeor. */
    fun caloriasDiarias(
        pesoKg: Double,
        estaturaCm: Double,
        edad: Int,
        esHombre: Boolean,
        nivel: NivelActividad
    ): Int {
        val metabolismoBasal = 10 * pesoKg + 6.25 * estaturaCm - 5 * edad + if (esHombre) 5 else -161
        return (metabolismoBasal * nivel.multiplicador).toInt()
    }

    /** 2 g de proteína por kg, 25 % de las calorías en grasa y el resto en carbohidratos. */
    fun macrosObjetivo(pesoKg: Double, calorias: Int): Macros {
        val proteinas = (pesoKg * 2).toInt()
        val grasas = (calorias * 0.25 / 9).toInt()
        val carbos = ((calorias - proteinas * 4 - grasas * 9) / 4).coerceAtLeast(0)
        return Macros(proteinas, carbos, grasas)
    }

    /** Vasos de 250 ml recomendados al día: unos 33 ml por kg de peso. */
    fun vasosObjetivo(pesoKg: Double?): Int {
        if (pesoKg == null || pesoKg <= 0) return 8
        return (pesoKg * 0.033 / 0.25).roundToInt().coerceIn(6, 15)
    }
}
