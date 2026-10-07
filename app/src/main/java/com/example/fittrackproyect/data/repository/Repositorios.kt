package com.example.fittrackproyect.data.repository

import android.net.Uri
import com.example.fittrackproyect.data.model.Comida
import com.example.fittrackproyect.data.model.Ejercicio
import com.example.fittrackproyect.data.model.Perfil
import com.example.fittrackproyect.data.model.Rutina
import com.example.fittrackproyect.data.model.TipoComida
import com.example.fittrackproyect.domain.NivelActividad
import com.example.fittrackproyect.domain.Nutricion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/*
 * Toda la comunicación con Firebase está aquí. Las pantallas y los ViewModels
 * no saben nada de Firestore: solo piden datos y reciben modelos de la app.
 *
 * Estructura en Firestore:
 *   users/{uid}                               perfil
 *   users/{uid}/agua/{yyyy-MM-dd}             vasos del día
 *   users/{uid}/dieta/{yyyy-MM-dd}/comidas    alimentos del día
 *   users/{uid}/rutinas/{id}                  rutinas y fechas en las que se completaron
 */

class AuthRepository(private val auth: FirebaseAuth) {

    val uid: String? get() = auth.currentUser?.uid

    val email: String? get() = auth.currentUser?.email

    val nombre: String
        get() = auth.currentUser?.displayName?.substringBefore(" ")?.takeIf { it.isNotBlank() }
            ?: auth.currentUser?.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Campeón"

    suspend fun iniciarSesion(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun registrarse(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
    }

    suspend fun iniciarSesionConGoogle(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    fun cerrarSesion() = auth.signOut()
}

class PerfilRepository(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private fun doc(uid: String) = db.collection("users").document(uid)

    fun observar(uid: String): Flow<Perfil?> = callbackFlow {
        val registro = doc(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.takeIf { it.exists() }?.aPerfil())
        }
        awaitClose { registro.remove() }
    }

    suspend fun obtener(uid: String): Perfil? = doc(uid).get().await().takeIf { it.exists() }?.aPerfil()

    suspend fun guardar(uid: String, perfil: Perfil) {
        val peso = requireNotNull(perfil.peso)
        val estatura = requireNotNull(perfil.estatura)
        val edad = requireNotNull(perfil.edad)
        val calorias = Nutricion.caloriasDiarias(peso, estatura, edad, perfil.esHombre, perfil.nivelActividad)
        val macros = Nutricion.macrosObjetivo(peso, calorias)
        val datos = mapOf(
            "peso" to peso,
            "estatura" to estatura,
            "edad" to edad,
            "sexo" to if (perfil.esHombre) "Hombre" else "Mujer",
            "nivelActividad" to perfil.nivelActividad.etiqueta,
            "imc" to Nutricion.imc(peso, estatura),
            "objetivoCalorias" to calorias,
            "proteinasObj" to macros.proteinas,
            "carbosObj" to macros.carbos,
            "grasasObj" to macros.grasas
        )
        doc(uid).set(datos, SetOptions.merge()).await()
    }

    suspend fun subirFoto(uid: String, imagen: Uri): String {
        val ref = storage.reference.child("profiles/$uid/avatar.jpg")
        // Las reglas de Storage solo aceptan imágenes: se indica el tipo explícitamente
        val metadatos = StorageMetadata.Builder().setContentType("image/jpeg").build()
        ref.putFile(imagen, metadatos).await()
        val url = ref.downloadUrl.await().toString()
        doc(uid).set(mapOf("fotoUrl" to url), SetOptions.merge()).await()
        return url
    }

    private fun DocumentSnapshot.aPerfil() = Perfil(
        peso = getDouble("peso"),
        estatura = getDouble("estatura"),
        edad = getLong("edad")?.toInt(),
        esHombre = getString("sexo") != "Mujer",
        nivelActividad = NivelActividad.desdeEtiqueta(getString("nivelActividad")),
        objetivoCalorias = getLong("objetivoCalorias")?.toInt(),
        fotoUrl = getString("fotoUrl")
    )
}

class AguaRepository(private val db: FirebaseFirestore) {
    private fun doc(uid: String, fecha: String) =
        db.collection("users").document(uid).collection("agua").document(fecha)

    fun observar(uid: String, fecha: String): Flow<Int> = callbackFlow {
        val registro = doc(uid, fecha).addSnapshotListener { snap, _ ->
            trySend(snap?.getLong("vasos")?.toInt() ?: 0)
        }
        awaitClose { registro.remove() }
    }

    suspend fun vasos(uid: String, fecha: String): Int =
        doc(uid, fecha).get().await().getLong("vasos")?.toInt() ?: 0

    suspend fun guardar(uid: String, fecha: String, vasos: Int) {
        doc(uid, fecha).set(mapOf("vasos" to vasos.coerceAtLeast(0).toLong(), "fecha" to fecha)).await()
    }
}

class DietaRepository(private val db: FirebaseFirestore) {
    private fun comidas(uid: String, fecha: String) =
        db.collection("users").document(uid).collection("dieta").document(fecha).collection("comidas")

    fun observar(uid: String, fecha: String): Flow<List<Comida>> = callbackFlow {
        val registro = comidas(uid, fecha).addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.map { it.aComida() } ?: emptyList())
        }
        awaitClose { registro.remove() }
    }

    suspend fun calorias(uid: String, fecha: String): Int =
        comidas(uid, fecha).get().await().documents.sumOf { it.getLong("calorias")?.toInt() ?: 0 }

    suspend fun anadir(uid: String, fecha: String, comida: Comida) {
        comidas(uid, fecha).add(
            mapOf(
                "nombre" to comida.nombre,
                "calorias" to comida.calorias,
                "proteinas" to comida.proteinas,
                "carbos" to comida.carbos,
                "grasas" to comida.grasas,
                "tipo" to comida.tipo.etiqueta
            )
        ).await()
    }

    suspend fun eliminar(uid: String, fecha: String, id: String) {
        comidas(uid, fecha).document(id).delete().await()
    }

    private fun DocumentSnapshot.aComida() = Comida(
        id = id,
        nombre = getString("nombre") ?: "",
        calorias = getLong("calorias")?.toInt() ?: 0,
        proteinas = getLong("proteinas")?.toInt() ?: 0,
        carbos = getLong("carbos")?.toInt() ?: 0,
        grasas = getLong("grasas")?.toInt() ?: 0,
        tipo = TipoComida.desdeEtiqueta(getString("tipo"))
    )
}

class RutinaRepository(private val db: FirebaseFirestore) {
    private fun rutinas(uid: String) = db.collection("users").document(uid).collection("rutinas")

    fun observar(uid: String): Flow<List<Rutina>> = callbackFlow {
        val registro = rutinas(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.map { it.aRutina() }?.sortedBy { it.nombre.lowercase() } ?: emptyList())
        }
        awaitClose { registro.remove() }
    }

    suspend fun obtener(uid: String, id: String): Rutina? =
        rutinas(uid).document(id).get().await().takeIf { it.exists() }?.aRutina()

    suspend fun crear(uid: String, nombre: String, categoria: String, ejercicios: List<Ejercicio>) {
        rutinas(uid).add(
            mapOf(
                "nombre" to nombre.trim(),
                "categoria" to categoria,
                "ejercicios" to ejercicios.map { it.aMapa() },
                "historial" to emptyList<String>()
            )
        ).await()
    }

    suspend fun actualizar(uid: String, id: String, nombre: String, categoria: String, ejercicios: List<Ejercicio>) {
        rutinas(uid).document(id).update(
            mapOf(
                "nombre" to nombre.trim(),
                "categoria" to categoria,
                "ejercicios" to ejercicios.map { it.aMapa() }
            )
        ).await()
    }

    suspend fun eliminar(uid: String, id: String) {
        rutinas(uid).document(id).delete().await()
    }

    /** Marca o desmarca la rutina como hecha en una fecha, guardando el historial. */
    suspend fun marcarCompletada(uid: String, id: String, fecha: String, completada: Boolean) {
        val cambio = if (completada) FieldValue.arrayUnion(fecha) else FieldValue.arrayRemove(fecha)
        rutinas(uid).document(id).update(
            mapOf(
                "historial" to cambio,
                // Campo antiguo: se mantiene para no romper datos guardados con versiones anteriores
                "completadaFecha" to if (completada) fecha else ""
            )
        ).await()
    }

    private fun Ejercicio.aMapa() = mapOf(
        "nombre" to nombre,
        "series" to series.toString(),
        "repeticiones" to repeticiones.toString(),
        "peso" to if (pesoKg > 0) pesoKg.toString() else ""
    )

    private fun DocumentSnapshot.aRutina(): Rutina {
        val ejercicios = (get("ejercicios") as? List<*>).orEmpty().mapNotNull { item ->
            val mapa = item as? Map<*, *> ?: return@mapNotNull null
            Ejercicio(
                nombre = mapa["nombre"]?.toString().orEmpty(),
                series = mapa["series"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0,
                repeticiones = mapa["repeticiones"]?.toString()?.toDoubleOrNull()?.toInt() ?: 0,
                pesoKg = mapa["peso"]?.toString()?.toDoubleOrNull() ?: 0.0
            )
        }
        // Versiones anteriores solo guardaban la última fecha en "completadaFecha"
        val historial = (get("historial") as? List<*>).orEmpty().map { it.toString() }.toMutableSet()
        getString("completadaFecha")?.takeIf { it.isNotBlank() }?.let { historial += it }
        return Rutina(
            id = id,
            nombre = getString("nombre").orEmpty(),
            categoria = getString("categoria") ?: "Full Body",
            ejercicios = ejercicios,
            historial = historial
        )
    }
}
