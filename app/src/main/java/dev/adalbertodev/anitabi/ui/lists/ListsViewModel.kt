package dev.adalbertodev.anitabi.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.data.EntryEvent
import dev.adalbertodev.anitabi.data.EntryEvents
import dev.adalbertodev.anitabi.data.EntryStatus
import dev.adalbertodev.anitabi.graphql.AnimeListsQuery
import dev.adalbertodev.anitabi.graphql.SaveEntryMutation
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

data class CompletionEvent(val entryId: Int, val title: String)

class ListsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ListUiState>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState> = _uiState

    private var allEntries: List<AnimeListEntry> = emptyList()
    private var activeFilter: ListFilter = ListFilter.WATCHING

    private var _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val debounceJobs = mutableMapOf<Int, Job>()
    private val burstSnapshots = mutableMapOf<Int, AnimeListEntry>()

    private val completionSnapshots = mutableMapOf<Int, AnimeListEntry>()
    private val _completionEvent = MutableStateFlow<CompletionEvent?>(null)
    val completionEvent: StateFlow<CompletionEvent?> = _completionEvent

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing


    init {
        viewModelScope.launch {
            EntryEvents.events.collect { event ->
                when (event) {
                    is EntryEvent.Updated -> allEntries = allEntries.map {
                        if (it.entryId == event.entryId)
                            it.copy(
                                status = event.status,
                                progress = event.progress,
                                updatedAt = event.updatedAt
                            )
                        else it
                    }

                    is EntryEvent.Created -> {
                        if (allEntries.none { it.entryId == event.entry.entryId }) {
                            allEntries = allEntries + event.entry
                        }
                    }
                }

                applyFilter()
            }
        }

        viewModelScope.launch {
            _uiState.value = ListUiState.Loading
            loadEntries()
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true

            debounceJobs.keys.toList().forEach { entryId ->
                debounceJobs[entryId]?.cancel()
                sendProgress(entryId)
            }

            loadEntries()
            _isRefreshing.value = false
        }
    }

    private suspend fun loadEntries() {
        val viewerId = ApolloProvider.client.query(ViewerQuery()).execute().data?.Viewer?.id

        val lists = viewerId?.let {
            ApolloProvider.client.query(AnimeListsQuery(userId = it))
                .execute().data?.MediaListCollection?.lists
        }

        if (lists != null) {
            allEntries = lists.asSequence()
                .filter { it?.isCustomList != true }
                .flatMap { it?.entries.orEmpty().asSequence() }
                .mapNotNull { it?.toUiModel() }
                .distinctBy { it.entryId }
                .toList()

            applyFilter()
        } else {
            if (allEntries.isEmpty()) {
                _uiState.value = ListUiState.Error
            } else {
                _errorMessage.value = "No se pudo actualizar."
            }
        }
    }

    fun onErrorShown() {
        _errorMessage.value = null
    }

    fun onCompletionShown() {
        _completionEvent.value = null
    }

    fun setFilter(filter: ListFilter) {
        activeFilter = filter
        applyFilter()
    }

    fun incrementProgress(entryId: Int) {
        val current = allEntries.firstOrNull { it.entryId == entryId } ?: return

        if (burstSnapshots[entryId] == null) {
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
        val target = allEntries.firstOrNull { it.entryId == entryId } ?: return

        val response = ApolloProvider.client
            .mutation(
                SaveEntryMutation(
                    entryId = Optional.present(entryId),
                    progress = Optional.present(target.progress)
                )
            )
            .execute()

        val saved = response.data?.SaveMediaListEntry

        if (saved != null) {
            val newStatus = saved.status?.toEntryStatus() ?: target.status

            replaceEntry(
                target.copy(
                    progress = saved.progress ?: target.progress,
                    updatedAt = saved.updatedAt ?: target.updatedAt,
                    status = newStatus
                )
            )

            if (newStatus == EntryStatus.COMPLETED && snapshot.status != EntryStatus.COMPLETED) {
                completionSnapshots[entryId] = snapshot
                _completionEvent.value = CompletionEvent(entryId, target.title)
            }
        } else {
            replaceEntry(snapshot)
            _errorMessage.value = "No se pudo guardar. Progreso revertido."
        }

        debounceJobs.remove(entryId)
        applyFilter()
    }

    fun undoCompletion(entryId: Int) {
        val snapshot = completionSnapshots.remove(entryId) ?: return
        val completed = allEntries.firstOrNull { it.entryId == entryId } ?: return

        replaceEntry(snapshot.copy(updatedAt = nowEpochSeconds()))
        applyFilter()

        viewModelScope.launch {
            val response = ApolloProvider.client
                .mutation(
                    SaveEntryMutation(
                        entryId = Optional.present(entryId),
                        progress = Optional.present(snapshot.progress),
                        status = Optional.present(snapshot.status.toMediaListStatus())
                    )
                )
                .execute()

            val saved = response.data?.SaveMediaListEntry

            if (saved != null) {
                replaceEntry(
                    snapshot.copy(
                        progress = saved.progress ?: snapshot.progress,
                        updatedAt = saved.updatedAt ?: nowEpochSeconds(),
                        status = saved.status?.toEntryStatus() ?: snapshot.status
                    )
                )
            } else {
                replaceEntry(completed)
                _errorMessage.value = "No se pudo deshacer."
            }

            applyFilter()
        }
    }

    private fun nowEpochSeconds() = (System.currentTimeMillis() / 1000).toInt()

    private fun replaceEntry(updated: AnimeListEntry) {
        allEntries = allEntries.map { if (it.entryId == updated.entryId) updated else it }
    }

    private fun applyFilter() {
        _uiState.value = ListUiState.Success(
            entries = allEntries
                .filter { it.matches(activeFilter) }
                .sortedByDescending { it.sortKey },
            activeFilter = activeFilter
        )
    }

    fun onCompletionDismissed(entryId: Int) {
        completionSnapshots.remove(entryId)
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