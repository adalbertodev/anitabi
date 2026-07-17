package dev.adalbertodev.anitabi.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.graphql.AnimeListsQuery
import dev.adalbertodev.anitabi.graphql.SaveProgressMutation
import dev.adalbertodev.anitabi.graphql.ViewerQuery
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

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

    private var _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val debounceJobs = mutableMapOf<Int, Job>()
    private val burstSnapshots = mutableMapOf<Int, AnimeListEntry>()

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

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun setFilter(filter: ListFilter) {
        activeFilter = filter
        applyFilter()
    }

    fun incrementProgress(entryId: Int) {
        val current = allEntries.firstOrNull { it.entryId == entryId } ?: return

        if(burstSnapshots[entryId] == null) {
            burstSnapshots[entryId] = current
        }

        val optimistic = current.copy(
            progress = current.progress + 1,
            updatedAt = nowEpochSeconds()
        )

        replaceEntry(optimistic)
        applyFilter()

        debounceJobs[entryId]?.cancel()
        debounceJobs[entryId] = viewModelScope.launch {
            delay(1_000.milliseconds)

            sendProgress(entryId)
        }
    }

    private suspend fun sendProgress(entryId: Int) {
        val snapshot = burstSnapshots.remove(entryId) ?: return
        val target = allEntries.firstOrNull {it.entryId == entryId} ?: return

        val response = ApolloProvider.client
            .mutation(
                SaveProgressMutation(
                    entryId = Optional.present(entryId),
                    progress = Optional.present(target.progress)
                )
            )
            .execute()

        val saved = response.data?.SaveMediaListEntry

        if (saved != null) {
            replaceEntry(
                target.copy(
                    progress = saved.progress ?: target.progress,
                    updatedAt = saved.updatedAt ?: target.updatedAt
                )
            )
        } else {
            replaceEntry(snapshot)
            _errorMessage.value = "No se pudo guardar. Progreso revertido."
        }

        debounceJobs.remove(entryId)
        applyFilter()
    }

    private fun nowEpochSeconds() = (System.currentTimeMillis() / 1000).toInt()

    private fun replaceEntry(updated: AnimeListEntry) {
        allEntries = allEntries.map { if (it.entryId == updated.entryId) updated else it }
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