package com.example.fittrackproyect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fittrackproyect.navegacion.ArranqueViewModel
import com.example.fittrackproyect.navegacion.NavegacionApp
import com.example.fittrackproyect.ui.theme.FitTrackProyectTheme

class MainActivity : ComponentActivity() {

    private val arranque: ArranqueViewModel by viewModels { ArranqueViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        // El splash se mantiene hasta saber a qué pantalla ir, así no hay saltos al abrir la app
        installSplashScreen().setKeepOnScreenCondition { arranque.destino.value == null }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val destino by arranque.destino.collectAsStateWithLifecycle()
            FitTrackProyectTheme {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    destino?.let { NavegacionApp(destinoInicial = it) }
                }
            }
        }
    }
}
