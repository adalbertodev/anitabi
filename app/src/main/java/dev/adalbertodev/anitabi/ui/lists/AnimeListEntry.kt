package dev.adalbertodev.anitabi.ui.lists

import dev.adalbertodev.anitabi.data.EntryStatus

data class AnimeListEntry(
    val entryId: Int,
    val mediaId: Int,
    val title: String,
    val coverUrl: String?,
    val progress: Int,
    val totalEpisodes: Int?,
    val status: EntryStatus,
    val updatedAt: Int
)