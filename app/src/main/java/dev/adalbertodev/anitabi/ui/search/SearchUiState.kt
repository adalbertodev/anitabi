package dev.adalbertodev.anitabi.ui.search

data class AnimeSearchResult(
    val id: Int,
    val title: String,
    val coverUrl: String?
)

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val results: List<AnimeSearchResult>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}