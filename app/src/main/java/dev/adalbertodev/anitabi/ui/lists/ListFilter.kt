package dev.adalbertodev.anitabi.ui.lists

enum class ListFilter(val label: String) {
    ALL("Todo"),
    WATCHING("Viendo"),
    COMPLETED("Completado"),
    PAUSED("Pausado"),
    DROPPED("Descartado"),
    PLANNING("Planeado")
}