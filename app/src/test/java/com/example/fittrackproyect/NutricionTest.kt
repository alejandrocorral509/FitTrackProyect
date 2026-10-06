package com.example.fittrackproyect

import com.example.fittrackproyect.domain.CategoriaImc
import com.example.fittrackproyect.domain.NivelActividad
import com.example.fittrackproyect.domain.Nutricion
import com.example.fittrackproyect.domain.Racha
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class NutricionTest {

    @Test
    fun `imc de 70 kg y 175 cm es 22,9`() {
        assertEquals(22.86, Nutricion.imc(70.0, 175.0), 0.01)
    }

    @Test
    fun `categorias de imc en sus limites`() {
        assertEquals(CategoriaImc.BAJO_PESO, Nutricion.categoriaImc(18.4))
        assertEquals(CategoriaImc.NORMAL, Nutricion.categoriaImc(18.5))
        assertEquals(CategoriaImc.SOBREPESO, Nutricion.categoriaImc(25.0))
        assertEquals(CategoriaImc.OBESIDAD, Nutricion.categoriaImc(30.0))
    }

    @Test
    fun `calorias con mifflin st jeor para un hombre moderadamente activo`() {
        // Metabolismo basal: 10*70 + 6.25*175 - 5*25 + 5 = 1673.75 → x1.55 = 2594
        assertEquals(2594, Nutricion.caloriasDiarias(70.0, 175.0, 25, esHombre = true, nivel = NivelActividad.MODERADO))
    }

    @Test
    fun `una mujer tiene 166 kcal menos de metabolismo basal con los mismos datos`() {
        val hombre = Nutricion.caloriasDiarias(60.0, 165.0, 30, true, NivelActividad.SEDENTARIO)
        val mujer = Nutricion.caloriasDiarias(60.0, 165.0, 30, false, NivelActividad.SEDENTARIO)
        assertEquals((166 * 1.2).toInt(), hombre - mujer, 1)
    }

    @Test
    fun `los macros reparten las calorias del objetivo`() {
        val macros = Nutricion.macrosObjetivo(pesoKg = 70.0, calorias = 2500)
        assertEquals(140, macros.proteinas)
        assertEquals(69, macros.grasas)
        val caloriasMacros = macros.proteinas * 4 + macros.carbos * 4 + macros.grasas * 9
        assertEquals(2500, caloriasMacros, 9)
    }

    @Test
    fun `el objetivo de agua depende del peso y tiene limites`() {
        assertEquals(8, Nutricion.vasosObjetivo(null))
        assertEquals(9, Nutricion.vasosObjetivo(70.0))
        assertEquals(6, Nutricion.vasosObjetivo(30.0))
        assertEquals(15, Nutricion.vasosObjetivo(200.0))
    }

    @Test
    fun `la racha cuenta dias seguidos y sigue viva si hoy aun no se ha entrenado`() {
        val hoy = LocalDate.of(2026, 10, 6)
        val dias = setOf(hoy.minusDays(1), hoy.minusDays(2), hoy.minusDays(3), hoy.minusDays(5))
        assertEquals(3, Racha.diasSeguidos(dias, hoy))
        assertEquals(4, Racha.diasSeguidos(dias + hoy, hoy))
        assertEquals(0, Racha.diasSeguidos(setOf(hoy.minusDays(3)), hoy))
    }

    private fun assertEquals(esperado: Int, real: Int, margen: Int) =
        org.junit.Assert.assertEquals(esperado.toDouble(), real.toDouble(), margen.toDouble())
}
