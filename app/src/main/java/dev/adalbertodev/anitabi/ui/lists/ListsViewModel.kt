package dev.adalbertodev.anitabi.ui.lists

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.graphql.AnimeListsQuery
import dev.adalbertodev.anitabi.graphql.ViewerQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ListUiState {
    data object Loading: ListUiState
    data class Success(val entries: List<AnimeListEntry>) : ListUiState
    data object Error: ListUiState
}

class ListsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState : StateFlow<ListUiState> = _uiState

    init {
        viewModelScope.launch {
            val viewerId = ApolloProvider.client.query(ViewerQuery()).execute().data?.Viewer?.id

            if(viewerId == null) {
                _uiState.value = ListUiState.Error
                return@launch
            }

            val response = ApolloProvider.client.query(AnimeListsQuery(userId = viewerId)).execute()

            val lists = response.data?.MediaListCollection?.lists

            _uiState.value = if (lists != null) {
                ListUiState.Success(
                    lists.asSequence()
                        .filter { it?.isCustomList != true }
                        .flatMap { it?.entries.orEmpty().asSequence() }
                        .mapNotNull { it?.toUiModel() }
                        .distinctBy { it.entryId }
                        .sortedByDescending { it.updatedAt }
                        .toList()
                )
            } else {
                ListUiState.Error
            }
        }
    }
}