package com.example.fittrackproyect.data.repository

import com.example.fittrackproyect.data.model.Alimento
import com.example.fittrackproyect.data.model.alimentosFrecuentes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** Busca alimentos en la lista local y en la API pública de Open Food Facts. */
class AlimentoRepository {

    fun buscarLocal(texto: String): List<Alimento> =
        if (texto.isBlank()) alimentosFrecuentes
        else alimentosFrecuentes.filter { it.nombre.contains(texto.trim(), ignoreCase = true) }

    suspend fun buscarOnline(texto: String): List<Alimento> = withContext(Dispatchers.IO) {
        val consulta = URLEncoder.encode(texto.trim(), "UTF-8")
        val url = URL(
            "https://world.openfoodfacts.org/api/v2/search?search_terms=$consulta" +
                "&fields=product_name,nutriments&page_size=25"
        )
        val conexion = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "FitTrack/2.0 (Android)")
            connectTimeout = 6000
            readTimeout = 10000
        }
        try {
            if (conexion.responseCode != 200) return@withContext emptyList()
            val productos = JSONObject(conexion.inputStream.bufferedReader().readText()).getJSONArray("products")
            (0 until productos.length()).mapNotNull { i ->
                val producto = productos.getJSONObject(i)
                val nombre = producto.optString("product_name").trim().takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val nutrientes = producto.optJSONObject("nutriments") ?: return@mapNotNull null
                val kcal = nutrientes.optDouble("energy-kcal_100g", -1.0).takeIf { it >= 0 } ?: return@mapNotNull null
                Alimento(
                    nombre = nombre.take(50),
                    calorias = kcal,
                    proteinas = nutrientes.optDouble("proteins_100g", 0.0),
                    carbos = nutrientes.optDouble("carbohydrates_100g", 0.0),
                    grasas = nutrientes.optDouble("fat_100g", 0.0)
                )
            }.distinctBy { it.nombre.lowercase() }
        } finally {
            conexion.disconnect()
        }
    }
}
