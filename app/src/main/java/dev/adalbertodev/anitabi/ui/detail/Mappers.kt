package dev.adalbertodev.anitabi.ui.detail

import dev.adalbertodev.anitabi.graphql.MediaDetailQuery
import dev.adalbertodev.anitabi.ui.lists.EntryStatus
import dev.adalbertodev.anitabi.ui.lists.toEntryStatus

fun MediaDetailQuery.Media.toUiModel(): MediaDetail {
    return MediaDetail(
        mediaId = this.id,
        title = this.title?.userPreferred ?: "No hay título",
        coverUrl = this.coverImage?.extraLarge,
        synopsis = this.description?.stripHtml() ?: "No hay descripción",
        totalEpisodes = this.episodes,
        entry = if (this.mediaListEntry != null) {
            MyListEntry(
                entryId = this.mediaListEntry.id,
                status = this.mediaListEntry.status?.toEntryStatus() ?: EntryStatus.WATCHING ,
                progress = this.mediaListEntry.progress ?: 0,
                score = this.mediaListEntry.score,
                notes = this.mediaListEntry.notes
            )
        } else {
            null
        }
    )
}

private fun String.stripHtml(): String =
    replace(Regex("<br\\s*/?>"), "\n").replace(Regex("<[^>]*>"), "")