package dev.adalbertodev.anitabi.data

enum class EntryStatus(val label: String) {
    WATCHING("Viendo"), REPEATING("Repitiendo"), COMPLETED("Completado"),
    PAUSED("Pausado"), DROPPED("Descartado"), PLANNING("Por ver")
}