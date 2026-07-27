package dev.adalbertodev.anitabi.ui.detail

import dev.adalbertodev.anitabi.data.EntryStatus
import dev.adalbertodev.anitabi.graphql.MediaDetailQuery
import dev.adalbertodev.anitabi.graphql.type.ScoreFormat
import dev.adalbertodev.anitabi.ui.lists.toEntryStatus

fun MediaDetailQuery.Data.toUiModel(): MediaDetail {
    val media = Media!!
    val viewer = Viewer

    return MediaDetail(
        mediaId = media.id,
        title = media.title?.userPreferred ?: "No hay título",
        coverUrl = media.coverImage?.extraLarge,
        synopsis = media.description?.stripHtml() ?: "No hay descripción",
        totalEpisodes = media.episodes,
        scoreFormat = when (viewer?.mediaListOptions?.scoreFormat) {
            ScoreFormat.POINT_10_DECIMAL -> ScoreUiFormat.POINT_10_DECIMAL
            ScoreFormat.POINT_10 -> ScoreUiFormat.POINT_10
            ScoreFormat.POINT_100 -> ScoreUiFormat.POINT_100
            ScoreFormat.POINT_5 -> ScoreUiFormat.POINT_5
            ScoreFormat.POINT_3 -> ScoreUiFormat.POINT_3
            else -> ScoreUiFormat.POINT_10
        },
        entry = if (media.mediaListEntry != null) {
            MyListEntry(
                entryId = media.mediaListEntry.id,
                status = media.mediaListEntry.status?.toEntryStatus() ?: EntryStatus.WATCHING,
                progress = media.mediaListEntry.progress ?: 0,
                score = media.mediaListEntry.score,
                notes = media.mediaListEntry.notes
            )
        } else {
            null
        }
    )
}

fun MediaDetailQuery.Viewer.toUIModel(): ScoreUiFormat {
    return when (this.mediaListOptions?.scoreFormat) {
        ScoreFormat.POINT_10_DECIMAL -> ScoreUiFormat.POINT_10_DECIMAL
        ScoreFormat.POINT_10 -> ScoreUiFormat.POINT_10
        ScoreFormat.POINT_100 -> ScoreUiFormat.POINT_100
        ScoreFormat.POINT_5 -> ScoreUiFormat.POINT_5
        ScoreFormat.POINT_3 -> ScoreUiFormat.POINT_3
        else -> ScoreUiFormat.POINT_10
    }
}

private fun String.stripHtml(): String =
    replace(Regex("<br\\s*/?>"), "\n").replace(Regex("<[^>]*>"), "")