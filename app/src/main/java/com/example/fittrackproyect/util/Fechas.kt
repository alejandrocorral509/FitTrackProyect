package com.example.fittrackproyect.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Las fechas se guardan en Firestore como texto "yyyy-MM-dd" (por ejemplo, "2026-10-06"). */
object Fechas {
    private val formatoClave = DateTimeFormatter.ISO_LOCAL_DATE
    private val es = Locale("es", "ES")

    fun hoy(): LocalDate = LocalDate.now()

    fun clave(fecha: LocalDate): String = fecha.format(formatoClave)

    fun desdeClave(clave: String): LocalDate? = runCatching { LocalDate.parse(clave, formatoClave) }.getOrNull()

    /** Los últimos [dias] días, del más antiguo a hoy. */
    fun ultimosDias(dias: Int, hasta: LocalDate = hoy()): List<LocalDate> =
        (dias - 1 downTo 0).map { hasta.minusDays(it.toLong()) }

    /** "Lunes, 6 de octubre" */
    fun largo(fecha: LocalDate): String {
        val diaSemana = fecha.dayOfWeek.getDisplayName(TextStyle.FULL, es).replaceFirstChar { it.uppercase() }
        val mes = fecha.month.getDisplayName(TextStyle.FULL, es)
        return "$diaSemana, ${fecha.dayOfMonth} de $mes"
    }

    /** "Hoy", "Ayer" o "lun 5 oct" */
    fun relativo(fecha: LocalDate, hoy: LocalDate = hoy()): String = when (fecha) {
        hoy -> "Hoy"
        hoy.minusDays(1) -> "Ayer"
        else -> {
            val diaSemana = fecha.dayOfWeek.getDisplayName(TextStyle.SHORT, es).replaceFirstChar { it.uppercase() }
            val mes = fecha.month.getDisplayName(TextStyle.SHORT, es)
            "$diaSemana ${fecha.dayOfMonth} $mes"
        }
    }

    /** Inicial del día de la semana: L, M, X, J, V, S, D */
    fun inicial(fecha: LocalDate): String =
        listOf("L", "M", "X", "J", "V", "S", "D")[fecha.dayOfWeek.value - 1]
}
