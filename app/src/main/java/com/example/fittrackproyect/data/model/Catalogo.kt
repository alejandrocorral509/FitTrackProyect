package com.example.fittrackproyect.data.model

/** Datos fijos de la app: categorías, plantillas de rutina y alimentos frecuentes. */

data class Categoria(val nombre: String, val emoji: String, val color: Long)

val categorias = listOf(
    Categoria("Full Body", "🔥", 0xFF4ADE80),
    Categoria("Pecho", "💪", 0xFFFF6B35),
    Categoria("Espalda", "🏋️", 0xFF60A5FA),
    Categoria("Piernas", "🦵", 0xFFFBBF24),
    Categoria("Hombros", "🎯", 0xFFEC4899),
    Categoria("Bíceps", "💪", 0xFFA78BFA),
    Categoria("Tríceps", "⚡", 0xFFF87171),
    Categoria("Abdomen", "🧱", 0xFFFF8C42),
    Categoria("Cardio", "🏃", 0xFF22D3EE),
)

fun categoria(nombre: String): Categoria =
    categorias.find { it.nombre == nombre } ?: Categoria(nombre, "💪", 0xFF4ADE80)

data class Plantilla(
    val nombre: String,
    val categoria: String,
    val descripcion: String,
    val ejercicios: List<Ejercicio>
)

private fun ej(nombre: String, series: Int, reps: Int) = Ejercicio(nombre, series, reps)

val plantillas = listOf(
    Plantilla(
        "Principiantes", "Full Body", "Rutina suave para empezar sin material, solo con tu peso corporal",
        listOf(ej("Sentadilla con peso corporal", 3, 15), ej("Flexiones", 3, 10), ej("Zancadas", 3, 12), ej("Plancha", 3, 30), ej("Crunch abdominal", 3, 15))
    ),
    Plantilla(
        "Full Body Básico", "Full Body", "Todo el cuerpo en una sesión con los grandes movimientos",
        listOf(ej("Press banca", 4, 10), ej("Sentadilla", 4, 10), ej("Peso muerto", 3, 8), ej("Dominadas", 3, 8), ej("Press militar", 3, 10), ej("Remo con barra", 3, 10))
    ),
    Plantilla(
        "Pecho y Tríceps", "Pecho", "Empuje horizontal para ganar fuerza y volumen en el pecho",
        listOf(ej("Press banca plano", 4, 10), ej("Press banca inclinado", 3, 12), ej("Aperturas con mancuernas", 3, 12), ej("Fondos en paralelas", 3, 15), ej("Extensión de tríceps", 3, 12), ej("Press cerrado", 3, 10))
    ),
    Plantilla(
        "Espalda y Bíceps", "Espalda", "Tirones para una espalda ancha y brazos fuertes",
        listOf(ej("Dominadas", 4, 8), ej("Remo con barra", 4, 10), ej("Jalón al pecho", 3, 12), ej("Remo con mancuerna", 3, 12), ej("Curl con barra", 3, 12), ej("Curl martillo", 3, 12))
    ),
    Plantilla(
        "Pierna Completa", "Piernas", "Cuádriceps, femorales, glúteos y gemelos",
        listOf(ej("Sentadilla", 4, 10), ej("Prensa de piernas", 4, 12), ej("Extensión de cuádriceps", 3, 15), ej("Curl de isquiotibiales", 3, 15), ej("Hip thrust", 3, 12), ej("Pantorrillas de pie", 4, 20))
    ),
    Plantilla(
        "Hombros 3D", "Hombros", "Las tres cabezas del deltoides para unos hombros redondos",
        listOf(ej("Press militar", 4, 10), ej("Elevaciones laterales", 4, 15), ej("Elevaciones frontales", 3, 12), ej("Pájaros", 3, 12), ej("Press Arnold", 3, 10), ej("Encogimientos", 3, 15))
    ),
    Plantilla(
        "Brazos", "Bíceps", "Bíceps y tríceps en superserie",
        listOf(ej("Curl con barra", 4, 12), ej("Curl con mancuernas", 3, 12), ej("Curl martillo", 3, 12), ej("Extensión de tríceps", 4, 12), ej("Press cerrado", 3, 10), ej("Patadas de tríceps", 3, 15))
    ),
    Plantilla(
        "Core de Acero", "Abdomen", "Abdomen y zona media para estabilidad",
        listOf(ej("Crunch abdominal", 4, 20), ej("Plancha", 4, 45), ej("Elevación de piernas", 3, 15), ej("Russian twist", 3, 20), ej("Crunch inverso", 3, 15), ej("Mountain climbers", 3, 30))
    ),
    Plantilla(
        "HIIT Quemagrasa", "Cardio", "Intervalos de alta intensidad para quemar calorías",
        listOf(ej("Burpees", 4, 15), ej("Jump squats", 3, 20), ej("Mountain climbers", 3, 30), ej("Saltos en estrella", 3, 30), ej("Sprint en el sitio", 3, 30))
    ),
)

val ejerciciosSugeridos = listOf(
    "Press banca", "Press banca inclinado", "Press banca declinado",
    "Aperturas con mancuernas", "Fondos en paralelas", "Press con mancuernas",
    "Cruce de poleas", "Flexiones",
    "Dominadas", "Jalón al pecho", "Remo con barra", "Remo con mancuerna",
    "Remo en polea baja", "Peso muerto", "Hiperextensiones", "Encogimientos",
    "Sentadilla", "Sentadilla búlgara", "Sentadilla hack", "Sentadilla con peso corporal",
    "Prensa de piernas", "Extensión de cuádriceps", "Curl de isquiotibiales",
    "Peso muerto rumano", "Zancadas", "Hip thrust", "Pantorrillas de pie", "Pantorrillas sentado",
    "Press militar", "Press Arnold", "Elevaciones laterales", "Elevaciones frontales",
    "Pájaros", "Face pull",
    "Curl con barra", "Curl con mancuernas", "Curl martillo", "Curl concentrado",
    "Curl en polea", "Curl predicador",
    "Extensión de tríceps", "Press cerrado", "Fondos en banco", "Patadas de tríceps",
    "Tríceps en polea", "Rompecráneos",
    "Crunch abdominal", "Crunch inverso", "Plancha", "Elevación de piernas",
    "Russian twist", "Mountain climbers", "Rueda abdominal",
    "Burpees", "Jump squats", "Saltos en estrella", "Sprint en el sitio",
    "Cuerda para saltar", "Bicicleta estática",
    "Swing con kettlebell", "Box jumps", "Battle ropes",
)

val alimentosFrecuentes = listOf(
    Alimento("Pechuga de pollo", 165.0, 31.0, 0.0, 4.0),
    Alimento("Arroz cocido", 130.0, 3.0, 28.0, 0.0),
    Alimento("Huevo entero", 155.0, 13.0, 1.0, 11.0),
    Alimento("Claras de huevo", 52.0, 11.0, 1.0, 0.0),
    Alimento("Avena", 389.0, 17.0, 66.0, 7.0),
    Alimento("Plátano", 89.0, 1.0, 23.0, 0.0),
    Alimento("Manzana", 52.0, 0.0, 14.0, 0.0),
    Alimento("Naranja", 47.0, 1.0, 12.0, 0.0),
    Alimento("Leche entera", 61.0, 3.0, 5.0, 3.0),
    Alimento("Yogur natural", 59.0, 10.0, 3.0, 1.0),
    Alimento("Queso fresco", 98.0, 11.0, 3.0, 4.0),
    Alimento("Pan integral", 247.0, 13.0, 41.0, 3.0),
    Alimento("Pasta cocida", 131.0, 5.0, 25.0, 1.0),
    Alimento("Quinoa cocida", 120.0, 4.0, 21.0, 2.0),
    Alimento("Batata / boniato", 86.0, 2.0, 20.0, 0.0),
    Alimento("Atún en lata", 116.0, 26.0, 0.0, 1.0),
    Alimento("Salmón", 208.0, 20.0, 0.0, 13.0),
    Alimento("Pechuga de pavo", 135.0, 30.0, 0.0, 1.0),
    Alimento("Carne de ternera", 250.0, 26.0, 0.0, 17.0),
    Alimento("Lentejas cocidas", 116.0, 9.0, 20.0, 0.0),
    Alimento("Garbanzo cocido", 164.0, 9.0, 27.0, 3.0),
    Alimento("Brócoli", 34.0, 3.0, 7.0, 0.0),
    Alimento("Espinacas", 23.0, 3.0, 4.0, 0.0),
    Alimento("Lechuga", 15.0, 1.0, 2.0, 0.0),
    Alimento("Tomate", 18.0, 1.0, 4.0, 0.0),
    Alimento("Aguacate", 160.0, 2.0, 9.0, 15.0),
    Alimento("Almendras", 579.0, 21.0, 22.0, 50.0),
    Alimento("Aceite de oliva", 884.0, 0.0, 0.0, 100.0),
)
