package com.example.fittrackproyect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.example.fittrackproyect.presentation.dieta.BuscarAlimentoScreen
import com.example.fittrackproyect.presentation.home.HomeScreen
import com.example.fittrackproyect.presentation.initial.InitialScreen
import com.example.fittrackproyect.presentation.login.LoginScreen
import com.example.fittrackproyect.presentation.profile.ProfileScreen
import com.example.fittrackproyect.presentation.estadisticas.EstadisticasScreen
import com.example.fittrackproyect.presentation.rutinas.CrearRutinaScreen
import com.example.fittrackproyect.presentation.rutinas.EditarRutinaScreen
import com.example.fittrackproyect.presentation.rutinas.PlantillasScreen
import com.example.fittrackproyect.presentation.rutinas.RutinasScreen
import com.example.fittrackproyect.presentation.signup.SignUpScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.fittrackproyect.presentation.agua.AguaScreen
import com.example.fittrackproyect.presentation.dieta.DietaScreen
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun NavigationWrapper(navHostController: NavHostController, auth: FirebaseAuth) {

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user != null) {
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val destination = if (!doc.exists() || doc.getDouble("peso") == null) "profile" else "home"
                    navHostController.navigate(destination) {
                        popUpTo("initial") { inclusive = true }
                    }
                }
                .addOnFailureListener {
                    navHostController.navigate("home") {
                        popUpTo("initial") { inclusive = true }
                    }
                }
        }
    }

    NavHost(navController = navHostController, startDestination = "initial") {
        composable("initial") {
            InitialScreen(
                auth = auth,
                navigateToLogin = { navHostController.navigate("login") },
                navigateToSignUp = { navHostController.navigate("signUp") },
                navigateToHome = {
                    navHostController.navigate("home") {
                        popUpTo("initial") { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(
                auth = auth,
                navigateToHome = {
                    navHostController.navigate("home") {
                        popUpTo("initial") { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("signUp") {
            SignUpScreen(
                auth = auth,
                navigateToHome = {
                    // New users go to profile first for onboarding
                    navHostController.navigate("profile") {
                        popUpTo("initial") { inclusive = true }
                    }
                },
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("home") {
            HomeScreen(
                auth = auth,
                navigateToProfile = { navHostController.navigate("profile") },
                navigateToRutinas = { navHostController.navigate("rutinas") },
                navigateToAgua = { navHostController.navigate("agua") },
                navigateToDieta = { navHostController.navigate("dieta") },
                navigateToInitial = {
                    navHostController.navigate("initial") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                navigateToEstadisticas = { navHostController.navigate("estadisticas") }
            )
        }
        composable("profile") {
            ProfileScreen(
                auth = auth,
                navigateBack = { navHostController.popBackStack() },
                navigateToHome = {
                    navHostController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("rutinas") {
            RutinasScreen(
                auth = auth,
                navigateBack = { navHostController.popBackStack() },
                navigateToCrearRutina = { navHostController.navigate("crearRutina") },
                navigateToEditarRutina = { id -> navHostController.navigate("editarRutina/$id") },
                navigateToPlantillas = { navHostController.navigate("plantillas") }
            )
        }
        composable("plantillas") {
            PlantillasScreen(
                auth = auth,
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("crearRutina") {
            CrearRutinaScreen(
                auth = auth,
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("editarRutina/{rutinaId}") { backStackEntry ->
            val rutinaId = backStackEntry.arguments?.getString("rutinaId") ?: return@composable
            EditarRutinaScreen(
                auth = auth,
                rutinaId = rutinaId,
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("estadisticas") {
            EstadisticasScreen(
                auth = auth,
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("agua") {
            AguaScreen(
                auth = auth,
                navigateBack = { navHostController.popBackStack() }
            )
        }
        composable("dieta") {
            DietaScreen(
                auth = auth,
                navigateBack = { navHostController.popBackStack() },
                navigateToBuscar = { tipoComida ->
                    navHostController.navigate("buscarAlimento/$tipoComida")
                }
            )
        }
        composable("buscarAlimento/{tipoComida}") { backStackEntry ->
            val tipoComida = backStackEntry.arguments?.getString("tipoComida") ?: "Desayuno"
            BuscarAlimentoScreen(
                auth = auth,
                tipoComida = tipoComida,
                navigateBack = { navHostController.popBackStack() }
            )
        }
    }
}
