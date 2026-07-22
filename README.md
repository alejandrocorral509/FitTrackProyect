# 🏋️ FitTrack — App Android de seguimiento fitness

Aplicación Android nativa para seguimiento de rutinas, hidratación, dieta y descanso.
Desarrollada con **Kotlin**, **Jetpack Compose** y arquitectura **MVVM**, con
autenticación y persistencia en la nube mediante Firebase.

## ✨ Funcionalidades
- 🔐 Autenticación con email/contraseña y Google Sign-In (Firebase Auth).
- 🏃 Registro y seguimiento de rutinas de entrenamiento.
- 💧 Tracker de hidratación diaria.
- 🥗 Control de dieta y registro de comidas.
- 😴 Seguimiento del descanso.
- ☁️ Sincronización en tiempo real con Cloud Firestore.

## 🛠️ Tecnologías
| Capa | Tecnología |
|------|------------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose |
| Arquitectura | MVVM (Model–View–ViewModel) |
| Autenticación | Firebase Authentication |
| Base de datos | Cloud Firestore |
| Build | Gradle (Kotlin DSL) |

## 🏗️ Arquitectura
Patrón **MVVM**: modelos y acceso a Firestore (**Model**), lógica y estado con
`StateFlow` (**ViewModel**), y pantallas declarativas en Jetpack Compose que
observan el estado (**View**).

## 📸 Capturas
<!-- Añade 2-3 capturas reales en una carpeta /screenshots -->
| Login | Rutinas | Hidratación |
|-------|---------|-------------|
| ![Login](screenshots/login.png) | ![Rutinas](screenshots/rutinas.png) | ![Agua](screenshots/agua.png) |

## 🚀 Cómo ejecutarlo
1. `git clone https://github.com/alejandrocorral509/FitTrackProyect.git`
2. Ábrelo en **Android Studio** (Hedgehog o superior).
3. Crea un proyecto en [Firebase](https://console.firebase.google.com), activa
   **Authentication** (Email + Google) y **Firestore**, y coloca tu
   `google-services.json` en `app/`.
4. Sincroniza Gradle y ejecuta en un dispositivo/emulador (Android 8.0+).

## 👤 Autor
**Alejandro Corral Carrasco** — Desarrollador Full-Stack Junior
[Portfolio](https://alejandrocorral.es) · [LinkedIn](https://www.linkedin.com/in/alejandro-corral-carrasco-8664b72a3/) · [GitHub](https://github.com/alejandrocorral509)