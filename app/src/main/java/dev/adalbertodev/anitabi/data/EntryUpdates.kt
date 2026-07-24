package dev.adalbertodev.anitabi.data

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

data class EntryUpdate(
    val entryId: Int,
    val status: EntryStatus,
    val progress: Int,
    val updatedAt: Int
)

object EntryUpdates {
    private val _updates = MutableSharedFlow<EntryUpdate>(extraBufferCapacity = 16)
    val updates: SharedFlow<EntryUpdate> = _updates

    fun publish(update: EntryUpdate) {
        _updates.tryEmit(update)
    }
}