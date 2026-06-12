package com.example.fittrackproyect.presentation.rutinas

data class RutinaPlantilla(
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val ejercicios: List<Map<String, String>>
)

val plantillasRutinas = listOf(
    RutinaPlantilla(
        nombre = "Principiantes",
        categoria = "Full Body",
        descripcion = "Rutina suave para empezar sin material, solo peso corporal",
        ejercicios = listOf(
            mapOf("nombre" to "Sentadilla con peso corporal", "series" to "3", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Flexiones", "series" to "3", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Zancadas", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Plancha", "series" to "3", "repeticiones" to "30", "peso" to ""),
            mapOf("nombre" to "Crunch abdominal", "series" to "3", "repeticiones" to "15", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "Full Body Básico",
        categoria = "Full Body",
        descripcion = "Trabaja todo el cuerpo en una sesión con los grandes movimientos",
        ejercicios = listOf(
            mapOf("nombre" to "Press banca", "series" to "4", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Sentadilla", "series" to "4", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Peso muerto", "series" to "3", "repeticiones" to "8", "peso" to ""),
            mapOf("nombre" to "Dominadas", "series" to "3", "repeticiones" to "8", "peso" to ""),
            mapOf("nombre" to "Press militar", "series" to "3", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Remo con barra", "series" to "3", "repeticiones" to "10", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "Pecho y Tríceps",
        categoria = "Pecho",
        descripcion = "Día de empuje: pecho y tríceps en la misma sesión",
        ejercicios = listOf(
            mapOf("nombre" to "Press banca plano", "series" to "4", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Press banca inclinado", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Aperturas con mancuernas", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Fondos en paralelas", "series" to "3", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Extensión de tríceps", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Press cerrado", "series" to "3", "repeticiones" to "10", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "Espalda y Bíceps",
        categoria = "Espalda",
        descripcion = "Día de tirón: espalda y bíceps en la misma sesión",
        ejercicios = listOf(
            mapOf("nombre" to "Dominadas", "series" to "4", "repeticiones" to "8", "peso" to ""),
            mapOf("nombre" to "Remo con barra", "series" to "4", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Jalón al pecho", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Remo con mancuerna", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Curl con barra", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Curl martillo", "series" to "3", "repeticiones" to "12", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "Piernas Completa",
        categoria = "Piernas",
        descripcion = "Trabajo completo de cuádriceps, isquios, glúteos y pantorrillas",
        ejercicios = listOf(
            mapOf("nombre" to "Sentadilla", "series" to "4", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Prensa de piernas", "series" to "4", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Extensión de cuádriceps", "series" to "3", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Curl de isquiotibiales", "series" to "3", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Hip thrust", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Pantorrillas de pie", "series" to "4", "repeticiones" to "20", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "Hombros y Trapecio",
        categoria = "Hombros",
        descripcion = "Deltoides completo: anterior, lateral y posterior",
        ejercicios = listOf(
            mapOf("nombre" to "Press militar", "series" to "4", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Elevaciones laterales", "series" to "4", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Elevaciones frontales", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Pájaros", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Press Arnold", "series" to "3", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Encogimientos", "series" to "3", "repeticiones" to "15", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "Brazos Completos",
        categoria = "Bíceps",
        descripcion = "Sesión dedicada a bíceps y tríceps",
        ejercicios = listOf(
            mapOf("nombre" to "Curl con barra", "series" to "4", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Curl con mancuernas", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Curl martillo", "series" to "3", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Extensión de tríceps", "series" to "4", "repeticiones" to "12", "peso" to ""),
            mapOf("nombre" to "Press cerrado", "series" to "3", "repeticiones" to "10", "peso" to ""),
            mapOf("nombre" to "Patadas de tríceps", "series" to "3", "repeticiones" to "15", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "Core y Abdomen",
        categoria = "Abdomen",
        descripcion = "Trabajo de abdomen y estabilidad del core",
        ejercicios = listOf(
            mapOf("nombre" to "Crunch abdominal", "series" to "4", "repeticiones" to "20", "peso" to ""),
            mapOf("nombre" to "Plancha", "series" to "4", "repeticiones" to "45", "peso" to ""),
            mapOf("nombre" to "Elevación de piernas", "series" to "3", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Russian twist", "series" to "3", "repeticiones" to "20", "peso" to ""),
            mapOf("nombre" to "Crunch inverso", "series" to "3", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Mountain climbers", "series" to "3", "repeticiones" to "30", "peso" to ""),
        )
    ),
    RutinaPlantilla(
        nombre = "HIIT Cardio",
        categoria = "Cardio",
        descripcion = "Entrenamiento de alta intensidad para quemar calorías",
        ejercicios = listOf(
            mapOf("nombre" to "Burpees", "series" to "4", "repeticiones" to "15", "peso" to ""),
            mapOf("nombre" to "Jump squats", "series" to "3", "repeticiones" to "20", "peso" to ""),
            mapOf("nombre" to "Mountain climbers", "series" to "3", "repeticiones" to "30", "peso" to ""),
            mapOf("nombre" to "Saltos en estrella", "series" to "3", "repeticiones" to "30", "peso" to ""),
            mapOf("nombre" to "Sprint en el sitio", "series" to "3", "repeticiones" to "30", "peso" to ""),
        )
    ),
)

val ejerciciosSugeridos = listOf(
    "Press banca", "Press banca inclinado", "Press banca declinado",
    "Aperturas con mancuernas", "Fondos en paralelas", "Press con mancuernas",
    "Cruce de poleas", "Flexiones", "Push-up",
    "Dominadas", "Jalón al pecho", "Remo con barra", "Remo con mancuerna",
    "Remo en polea baja", "Peso muerto", "Hiperextensiones", "Encogimientos",
    "Sentadilla", "Sentadilla búlgara", "Sentadilla hack", "Sentadilla con peso corporal",
    "Prensa de piernas", "Extensión de cuádriceps", "Curl de isquiotibiales",
    "Peso muerto rumano", "Zancadas", "Hip thrust", "Pantorrillas de pie", "Pantorrillas sentado",
    "Press militar", "Press Arnold", "Elevaciones laterales", "Elevaciones frontales",
    "Pájaros", "Face pull", "Press tras nuca",
    "Curl con barra", "Curl con mancuernas", "Curl martillo", "Curl concentrado",
    "Curl en polea", "Curl predicador",
    "Extensión de tríceps", "Press cerrado", "Fondos en banco", "Patadas de tríceps",
    "Tríceps en polea", "Rompecráneos",
    "Crunch abdominal", "Crunch inverso", "Plancha", "Elevación de piernas",
    "Russian twist", "Mountain climbers", "Rueda abdominal",
    "Burpees", "Jump squats", "Saltos en estrella", "Sprint en el sitio",
    "Cuerda para saltar", "Bicicleta estática",
    "Clean y press", "Swing con kettlebell", "Box jumps", "Battle ropes",
)
