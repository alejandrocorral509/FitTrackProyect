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
}
