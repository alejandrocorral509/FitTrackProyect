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

    // Se usa el buscador de texto de Open Food Facts (search-a-licious): el endpoint
    // api/v2/search ignora el texto y devuelve productos populares de cualquier país.
    // Se filtra por productos vendidos en España.
    suspend fun buscarOnline(texto: String): List<Alimento> = withContext(Dispatchers.IO) {
        val consulta = URLEncoder.encode("${texto.trim()} countries_tags:\"en:spain\"", "UTF-8")
        val url = URL(
            "https://search.openfoodfacts.org/search?q=$consulta&langs=es&page_size=25" +
                "&fields=product_name,product_name_es,brands,nutriments"
        )
        val conexion = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("User-Agent", "FitTrack/2.0 (Android)")
            connectTimeout = 6000
            readTimeout = 10000
        }
        try {
            if (conexion.responseCode != 200) return@withContext emptyList()
            val productos = JSONObject(conexion.inputStream.bufferedReader().readText()).getJSONArray("hits")
            (0 until productos.length()).mapNotNull { i ->
                val producto = productos.getJSONObject(i)
                val nombre = listOf("product_name_es", "product_name")
                    .map { producto.optString(it).trim() }
                    .firstOrNull { it.isNotBlank() } ?: return@mapNotNull null
                // La marca ayuda a distinguir productos con el mismo nombre
                val marca = producto.optJSONArray("brands")?.optString(0)?.trim().orEmpty()
                val nutrientes = producto.optJSONObject("nutriments") ?: return@mapNotNull null
                val kcal = nutrientes.optDouble("energy-kcal_100g", -1.0).takeIf { it >= 0 } ?: return@mapNotNull null
                Alimento(
                    nombre = (if (marca.isBlank()) nombre else "$nombre · $marca").take(50),
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
