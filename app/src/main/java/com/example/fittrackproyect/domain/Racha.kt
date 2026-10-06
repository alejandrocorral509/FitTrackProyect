package com.example.fittrackproyect.domain

import java.time.LocalDate

/** Calcula cuántos días seguidos se ha entrenado. */
object Racha {

    /**
     * Cuenta los días consecutivos con entreno hasta hoy.
     * Si hoy todavía no se ha entrenado, la racha sigue viva desde ayer.
     */
    fun diasSeguidos(diasEntrenados: Set<LocalDate>, hoy: LocalDate): Int {
        var dia = if (hoy in diasEntrenados) hoy else hoy.minusDays(1)
        var racha = 0
        while (dia in diasEntrenados) {
            racha++
            dia = dia.minusDays(1)
        }
        return racha
    }

    /** La racha más larga de todo el historial. */
    fun mejor(diasEntrenados: Set<LocalDate>): Int {
        var mejor = 0
        diasEntrenados.forEach { dia ->
            // Solo se empieza a contar desde el primer día de cada racha
            if (dia.minusDays(1) !in diasEntrenados) {
                var actual = dia
                var largo = 0
                while (actual in diasEntrenados) {
                    largo++
                    actual = actual.plusDays(1)
                }
                mejor = maxOf(mejor, largo)
            }
        }
        return mejor
    }
}
