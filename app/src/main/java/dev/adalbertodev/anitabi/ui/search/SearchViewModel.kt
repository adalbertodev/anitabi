package dev.adalbertodev.anitabi.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.graphql.SearchAnimeQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState

    fun search(text: String) {
        viewModelScope.launch {
            _uiState.value = SearchUiState.Loading

            val response = ApolloProvider.client
                .query(SearchAnimeQuery(Optional.present(text)))
                .execute()

            val media = response.data?.Page?.media

            _uiState.value = if (media != null) {
                SearchUiState.Success(
                    media.mapNotNull { item ->
                        item?.let {
                            AnimeSearchResult(
                                id = it.id,
                                title = it.title?.userPreferred ?: "Sin titulo",
                                coverUrl = it.coverImage?.large
                            )
                        }
                    }
                )
            } else {
                SearchUiState.Error("No se pudo completar la búsqueda")
            }
        }
    }
}