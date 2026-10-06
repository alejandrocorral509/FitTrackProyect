package com.example.fittrackproyect

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.fittrackproyect.data.repository.AguaRepository
import com.example.fittrackproyect.data.repository.AlimentoRepository
import com.example.fittrackproyect.data.repository.AuthRepository
import com.example.fittrackproyect.data.repository.DietaRepository
import com.example.fittrackproyect.data.repository.PerfilRepository
import com.example.fittrackproyect.data.repository.RutinaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class FitTrackApp : Application() {
    val contenedor by lazy { Contenedor() }
}

/** Crea los repositorios una sola vez y los comparte con todos los ViewModels. */
class Contenedor {
    private val db by lazy { FirebaseFirestore.getInstance() }

    val auth by lazy { AuthRepository(FirebaseAuth.getInstance()) }
    val perfil by lazy { PerfilRepository(db, FirebaseStorage.getInstance()) }
    val agua by lazy { AguaRepository(db) }
    val dieta by lazy { DietaRepository(db) }
    val rutinas by lazy { RutinaRepository(db) }
    val alimentos by lazy { AlimentoRepository() }
}

/** Acceso al contenedor desde las factorías de ViewModel. */
val CreationExtras.contenedor: Contenedor
    get() = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as FitTrackApp).contenedor
