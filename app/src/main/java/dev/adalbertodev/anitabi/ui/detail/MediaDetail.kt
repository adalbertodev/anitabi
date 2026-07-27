package dev.adalbertodev.anitabi.ui.detail

import dev.adalbertodev.anitabi.data.EntryStatus

data class MediaDetail(
    val mediaId: Int,
    val title: String,
    val coverUrl: String?,
    val synopsis: String,
    val totalEpisodes: Int?,
    val entry: MyListEntry?,
    val scoreFormat: ScoreUiFormat
)

data class MyListEntry(
    val entryId: Int,
    val status: EntryStatus,
    val progress: Int,
    val score: Double?,
    val notes: String?
)

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val detail: MediaDetail) : DetailUiState
    data object Error : DetailUiState
}
