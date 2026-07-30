package dev.adalbertodev.anitabi.data

import dev.adalbertodev.anitabi.ui.lists.AnimeListEntry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

sealed interface EntryEvent {
    data class Updated(
        val entryId: Int,
        val status: EntryStatus,
        val progress: Int,
        val updatedAt: Int
    ) : EntryEvent

    data class Created(val entry: AnimeListEntry) : EntryEvent
}


object EntryEvents {
    private val _events = MutableSharedFlow<EntryEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<EntryEvent> = _events

    fun publish(event: EntryEvent) {
        _events.tryEmit(event)
    }
}