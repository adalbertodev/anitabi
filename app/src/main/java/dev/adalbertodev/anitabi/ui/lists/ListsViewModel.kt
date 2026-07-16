package dev.adalbertodev.anitabi.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.graphql.AnimeListsQuery
import dev.adalbertodev.anitabi.graphql.SaveProgressMutation
import dev.adalbertodev.anitabi.graphql.ViewerQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ListUiState {
    data object Loading : ListUiState
    data class Success(
        val entries: List<AnimeListEntry>,
        val activeFilter: ListFilter
    ) : ListUiState

    data object Error : ListUiState
}

class ListsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState> = _uiState

    private var allEntries: List<AnimeListEntry> = emptyList()
    private var activeFilter: ListFilter = ListFilter.WATCHING

    init {
        viewModelScope.launch {
            val viewerId = ApolloProvider.client.query(ViewerQuery()).execute().data?.Viewer?.id

            if (viewerId == null) {
                _uiState.value = ListUiState.Error
                return@launch
            }

            val response = ApolloProvider.client.query(AnimeListsQuery(userId = viewerId)).execute()

            val lists = response.data?.MediaListCollection?.lists

            if (lists != null) {
                allEntries = lists.asSequence()
                    .filter { it?.isCustomList != true }
                    .flatMap { it?.entries.orEmpty().asSequence() }
                    .mapNotNull { it?.toUiModel() }
                    .distinctBy { it.entryId }
                    .toList()

                applyFilter()
            }
        }
    }

    fun setFilter(filter: ListFilter) {
        activeFilter = filter
        applyFilter()
    }

    fun incrementProgress(entry: AnimeListEntry) {
        viewModelScope.launch {
            val response = ApolloProvider.client
                .mutation(SaveProgressMutation(
                    entryId = Optional.present(entry.entryId),
                    progress = Optional.present(entry.progress + 1)
                ))
                .execute()

            val saved = response.data?.SaveMediaListEntry

            if(saved != null) {
                allEntries = allEntries.map {
                    if(it.entryId == entry.entryId) it.copy(
                        progress = saved.progress ?: it.progress,
                        updatedAt = saved.updatedAt ?: it.updatedAt
                    ) else it
                }
            }

            applyFilter()
        }
    }

    private fun applyFilter() {
        _uiState.value = ListUiState.Success(
            entries = allEntries
                .filter { it.matches(activeFilter) }
                .sortedByDescending { it.updatedAt },
            activeFilter = activeFilter
        )
    }

    private fun AnimeListEntry.matches(filter: ListFilter) = when (filter) {
        ListFilter.ALL -> true
        ListFilter.WATCHING -> status == EntryStatus.WATCHING || status == EntryStatus.REPEATING
        ListFilter.COMPLETED -> status == EntryStatus.COMPLETED
        ListFilter.PAUSED -> status == EntryStatus.PAUSED
        ListFilter.DROPPED -> status == EntryStatus.DROPPED
        ListFilter.PLANNING -> status == EntryStatus.PLANNING
    }
}