package dev.adalbertodev.anitabi.ui.lists

import dev.adalbertodev.anitabi.graphql.AnimeListsQuery
import dev.adalbertodev.anitabi.graphql.type.MediaListStatus

fun AnimeListsQuery.Entry.toUiModel() : AnimeListEntry? {
    val media = this.media ?: return null

    return AnimeListEntry(
        entryId = id,
        mediaId = media.id,
        title = media.title?.userPreferred ?: "(sin título)",
        coverUrl = media.coverImage?.large,
        progress = progress ?: 0,
        totalEpisodes = media.episodes,
        status = status?.toEntryStatus() ?: EntryStatus.WATCHING,
        updatedAt = updatedAt ?: 0
    )
}

fun MediaListStatus.toEntryStatus(): EntryStatus? = when (this) {
    MediaListStatus.CURRENT -> EntryStatus.WATCHING
    MediaListStatus.REPEATING -> EntryStatus.REPEATING
    MediaListStatus.COMPLETED -> EntryStatus.COMPLETED
    MediaListStatus.PAUSED -> EntryStatus.PAUSED
    MediaListStatus.DROPPED -> EntryStatus.DROPPED
    MediaListStatus.PLANNING -> EntryStatus.PLANNING
    else -> null
}