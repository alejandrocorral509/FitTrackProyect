package com.example.fittrackproyect.navegacion

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fittrackproyect.presentation.agua.AguaScreen
import com.example.fittrackproyect.presentation.auth.BienvenidaScreen
import com.example.fittrackproyect.presentation.auth.DestinoTrasEntrar
import com.example.fittrackproyect.presentation.auth.LoginScreen
import com.example.fittrackproyect.presentation.auth.RegistroScreen
import com.example.fittrackproyect.presentation.dieta.BuscarAlimentoScreen
import com.example.fittrackproyect.presentation.dieta.DietaScreen
import com.example.fittrackproyect.presentation.estadisticas.EstadisticasScreen
import com.example.fittrackproyect.presentation.home.HomeScreen
import com.example.fittrackproyect.presentation.profile.ProfileScreen
import com.example.fittrackproyect.presentation.rutinas.CrearRutinaScreen
import com.example.fittrackproyect.presentation.rutinas.EditarRutinaScreen
import com.example.fittrackproyect.presentation.rutinas.PlantillasScreen
import com.example.fittrackproyect.presentation.rutinas.RutinasScreen
import com.google.firebase.auth.FirebaseAuth

object Rutas {
    const val BIENVENIDA = "bienvenida"
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val COMPLETAR_PERFIL = "completar-perfil"

    // Pestañas de la barra inferior
    const val INICIO = "inicio"
    const val ENTRENO = "entreno"
    const val NUTRICION = "nutricion"
    const val PROGRESO = "progreso"
    const val PERFIL = "perfil"

    const val AGUA = "agua"
    const val PLANTILLAS = "plantillas"
    const val NUEVA_RUTINA = "rutina/nueva"
    const val EDITAR_RUTINA = "rutina/{id}/editar"
    const val BUSCAR_ALIMENTO = "buscar/{tipo}"

    fun editarRutina(id: String) = "rutina/$id/editar"
    fun buscarAlimento(tipo: String) = "buscar/$tipo"
}

private data class Pestana(val ruta: String, val texto: String, val icono: ImageVector, val iconoActivo: ImageVector)

private val pestanas = listOf(
    Pestana(Rutas.INICIO, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    Pestana(Rutas.ENTRENO, "Entreno", Icons.Outlined.FitnessCenter, Icons.Filled.FitnessCenter),
    Pestana(Rutas.NUTRICION, "Nutrición", Icons.Outlined.Restaurant, Icons.Filled.Restaurant),
    Pestana(Rutas.PROGRESO, "Progreso", Icons.Outlined.ShowChart, Icons.Filled.ShowChart),
    Pestana(Rutas.PERFIL, "Perfil", Icons.Outlined.Person, Icons.Filled.Person),
)

/** Cambia de pestaña sin apilar pantallas y conservando el estado de cada una. */
fun NavHostController.irAPestana(ruta: String) = navigate(ruta) {
    popUpTo(Rutas.INICIO) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/** Navega a una ruta borrando todo lo anterior (al entrar o salir de la cuenta). */
fun NavHostController.reiniciarEn(ruta: String) = navigate(ruta) {
    popUpTo(0) { inclusive = true }
}

@Composable
fun NavegacionApp(destinoInicial: String, nav: NavHostController = rememberNavController()) {
    val entrada by nav.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    val conBarra = pestanas.any { it.ruta == rutaActual }

    val alEntrar: (DestinoTrasEntrar) -> Unit = { destino ->
        nav.reiniciarEn(if (destino == DestinoTrasEntrar.INICIO) Rutas.INICIO else Rutas.COMPLETAR_PERFIL)
    }
    // Las pantallas antiguas todavía reciben FirebaseAuth; se irán migrando a ViewModels
    val auth = FirebaseAuth.getInstance()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(conBarra, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
                    pestanas.forEach { pestana ->
                        val activa = pestana.ruta == rutaActual
                        NavigationBarItem(
                            selected = activa,
                            onClick = { nav.irAPestana(pestana.ruta) },
                            icon = { Icon(if (activa) pestana.iconoActivo else pestana.icono, contentDescription = pestana.texto) },
                            label = { Text(pestana.texto) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = nav,
            startDestination = destinoInicial,
            modifier = Modifier.padding(relleno).consumeWindowInsets(relleno)
        ) {
            composable(Rutas.BIENVENIDA) {
                BienvenidaScreen(
                    alIrARegistro = { nav.navigate(Rutas.REGISTRO) },
                    alIrALogin = { nav.navigate(Rutas.LOGIN) },
                    alEntrar = alEntrar
                )
            }
            composable(Rutas.LOGIN) {
                LoginScreen(
                    alVolver = { nav.popBackStack() },
                    alIrARegistro = { nav.navigate(Rutas.REGISTRO) { popUpTo(Rutas.BIENVENIDA) } },
                    alEntrar = alEntrar
                )
            }
            composable(Rutas.REGISTRO) {
                RegistroScreen(
                    alVolver = { nav.popBackStack() },
                    alIrALogin = { nav.navigate(Rutas.LOGIN) { popUpTo(Rutas.BIENVENIDA) } },
                    alEntrar = alEntrar
                )
            }
            composable(Rutas.COMPLETAR_PERFIL) {
                ProfileScreen(auth = auth, navigateBack = {}, navigateToHome = { nav.reiniciarEn(Rutas.INICIO) })
            }

            composable(Rutas.INICIO) {
                HomeScreen(
                    auth = auth,
                    navigateToProfile = { nav.irAPestana(Rutas.PERFIL) },
                    navigateToRutinas = { nav.irAPestana(Rutas.ENTRENO) },
                    navigateToAgua = { nav.navigate(Rutas.AGUA) },
                    navigateToDieta = { nav.irAPestana(Rutas.NUTRICION) },
                    navigateToInitial = { nav.reiniciarEn(Rutas.BIENVENIDA) },
                    navigateToEstadisticas = { nav.irAPestana(Rutas.PROGRESO) }
                )
            }
            composable(Rutas.ENTRENO) {
                RutinasScreen(
                    auth = auth,
                    navigateBack = { nav.irAPestana(Rutas.INICIO) },
                    navigateToCrearRutina = { nav.navigate(Rutas.NUEVA_RUTINA) },
                    navigateToEditarRutina = { id -> nav.navigate(Rutas.editarRutina(id)) },
                    navigateToPlantillas = { nav.navigate(Rutas.PLANTILLAS) }
                )
            }
            composable(Rutas.NUTRICION) {
                DietaScreen(
                    auth = auth,
                    navigateBack = { nav.irAPestana(Rutas.INICIO) },
                    navigateToBuscar = { tipo -> nav.navigate(Rutas.buscarAlimento(tipo)) }
                )
            }
            composable(Rutas.PROGRESO) {
                EstadisticasScreen(auth = auth, navigateBack = { nav.irAPestana(Rutas.INICIO) })
            }
            composable(Rutas.PERFIL) {
                ProfileScreen(auth = auth, navigateBack = { nav.irAPestana(Rutas.INICIO) }, navigateToHome = { nav.irAPestana(Rutas.INICIO) })
            }

            composable(Rutas.AGUA) {
                AguaScreen(auth = auth, navigateBack = { nav.popBackStack() })
            }
            composable(Rutas.PLANTILLAS) {
                PlantillasScreen(auth = auth, navigateBack = { nav.popBackStack() })
            }
            composable(Rutas.NUEVA_RUTINA) {
                CrearRutinaScreen(auth = auth, navigateBack = { nav.popBackStack() })
            }
            composable(Rutas.EDITAR_RUTINA) { entradaPila ->
                val id = entradaPila.arguments?.getString("id") ?: return@composable
                EditarRutinaScreen(auth = auth, rutinaId = id, navigateBack = { nav.popBackStack() })
            }
            composable(Rutas.BUSCAR_ALIMENTO) { entradaPila ->
                val tipo = entradaPila.arguments?.getString("tipo") ?: "Desayuno"
                BuscarAlimentoScreen(auth = auth, tipoComida = tipo, navigateBack = { nav.popBackStack() })
            }
        }
    }
}
