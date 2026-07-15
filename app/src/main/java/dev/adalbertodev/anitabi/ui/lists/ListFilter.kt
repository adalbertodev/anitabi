package dev.adalbertodev.anitabi.ui.lists

enum class ListFilter(val label: String, val emptyMessage: String) {
    ALL("Todo", "Tu lista de anime está vacía. Busca un título para empezar."),
    WATCHING("Viendo", "No estás viendo nada ahora mismo."),
    COMPLETED("Completado", "Aún no has completado ningún anime."),
    PAUSED("Pausado", "No tienes nada en pausa."),
    DROPPED("Descartado", "No has descartado nada."),
    PLANNING("Planeado", "No tienes nada planeado.")
}