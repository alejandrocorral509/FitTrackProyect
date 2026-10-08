package com.example.fittrackproyect.util

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import java.io.ByteArrayOutputStream
import kotlin.math.min

/**
 * Prepara la foto de perfil para guardarla dentro del documento del usuario en Firestore
 * (Storage solo está disponible en el plan de pago). Se recorta en cuadrado y se reduce
 * a 256 × 256 px en JPEG, así ocupa unos 20-30 KB, muy por debajo del límite de 1 MB
 * por documento.
 */
object Imagenes {

    private const val LADO = 256
    private const val CALIDAD = 80

    /** Devuelve la foto lista para guardar, en Base64. */
    fun fotoDePerfil(resolver: ContentResolver, imagen: Uri): String {
        val original = leer(resolver, imagen)
        val ladoRecorte = min(original.width, original.height)
        val cuadrada = Bitmap.createBitmap(
            original,
            (original.width - ladoRecorte) / 2,
            (original.height - ladoRecorte) / 2,
            ladoRecorte,
            ladoRecorte
        )
        val pequena = Bitmap.createScaledBitmap(cuadrada, LADO, LADO, true)
        val bytes = ByteArrayOutputStream().use { salida ->
            pequena.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida)
            salida.toByteArray()
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /** Convierte la foto guardada en bytes para mostrarla. */
    fun aBytes(foto: String): ByteArray? = runCatching { Base64.decode(foto, Base64.NO_WRAP) }.getOrNull()

    private fun leer(resolver: ContentResolver, imagen: Uri): Bitmap =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // ImageDecoder ya gira la foto según la orientación con la que se hizo
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(resolver, imagen)) { decodificador, info, _ ->
                decodificador.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val muestra = (min(info.size.width, info.size.height) / LADO).coerceAtLeast(1)
                decodificador.setTargetSampleSize(muestra)
            }
        } else {
            // Android 8: se lee primero solo el tamaño para no cargar la foto entera en memoria
            val tamano = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(imagen).use { BitmapFactory.decodeStream(it, null, tamano) }
            val opciones = BitmapFactory.Options().apply {
                inSampleSize = (min(tamano.outWidth, tamano.outHeight) / LADO).coerceAtLeast(1)
            }
            resolver.openInputStream(imagen).use { BitmapFactory.decodeStream(it, null, opciones) }
                ?: error("No se ha podido leer la imagen")
        }
}
