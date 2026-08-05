package dev.adalbertodev.anitabi.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.data.EntryEvent
import dev.adalbertodev.anitabi.data.EntryEvents
import dev.adalbertodev.anitabi.data.EntryStatus
import dev.adalbertodev.anitabi.data.ErrorKind
import dev.adalbertodev.anitabi.data.errorKindOrNull
import dev.adalbertodev.anitabi.graphql.MediaDetailQuery
import dev.adalbertodev.anitabi.graphql.SaveEntryMutation
import dev.adalbertodev.anitabi.ui.lists.AnimeListEntry
import dev.adalbertodev.anitabi.ui.lists.toEntryStatus
import dev.adalbertodev.anitabi.ui.lists.toMediaListStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val mediaId: Int = checkNotNull(savedStateHandle["mediaId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        viewModelScope.launch {
            load()
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading
            load()
        }
    }

    private suspend fun load() {
        val response = ApolloProvider.client
            .query(MediaDetailQuery(mediaId = mediaId))
            .execute()
        val data = response.data

        _uiState.value = if (data?.Media != null) {
            DetailUiState.Success(data.toUiModel())
        } else {
            DetailUiState.Error(response.errorKindOrNull() ?: ErrorKind.SERVER)
        }
    }

    fun addToList(status: EntryStatus) {
        val current = (_uiState.value as? DetailUiState.Success)?.detail ?: return
        if (current.entry != null || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true

            val response = ApolloProvider.client
                .mutation(
                    SaveEntryMutation(
                        mediaId = Optional.present(current.mediaId),
                        status = Optional.present(status.toMediaListStatus())
                    )
                )
                .execute()

            val saved = response.data?.SaveMediaListEntry

            if (saved != null) {
                val newEntry = MyListEntry(
                    entryId = saved.id,
                    status = saved.status?.toEntryStatus() ?: status,
                    progress = saved.progress ?: 0,
                    score = saved.score ?: 0.0,
                    notes = saved.notes
                )

                _uiState.value = DetailUiState.Success(current.copy(entry = newEntry))

                EntryEvents.publish(
                    EntryEvent.Created(
                        AnimeListEntry(
                            entryId = saved.id,
                            mediaId = current.mediaId,
                            title = current.title,
                            coverUrl = current.coverUrl,
                            progress = newEntry.progress,
                            totalEpisodes = current.totalEpisodes,
                            status = newEntry.status,
                            updatedAt = saved.updatedAt ?: nowEpochSeconds(),
                            sortKey = saved.updatedAt ?: nowEpochSeconds()
                        )
                    )
                )
            } else {
                _errorMessage.value = when (response.errorKindOrNull()) {
                    ErrorKind.NETWORK -> "Sin conexión. No se pudo añadir a tu lista."
                    ErrorKind.RATE_LIMIT -> "Demasiadas peticiones. No se pudo añadir a tu lista."
                    else -> "No se pudo añadir a tu lista."
                }
            }

            _isSaving.value = false
        }
    }

    fun setStatus(newStatus: EntryStatus) {
        val current = (_uiState.value as? DetailUiState.Success)?.detail ?: return
        val entry = current.entry ?: return

        if (entry.status == newStatus || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true

            val response = ApolloProvider.client
                .mutation(
                    SaveEntryMutation(
                        entryId = Optional.present(entry.entryId),
                        status = Optional.present(newStatus.toMediaListStatus())
                    )
                )
                .execute()

            val saved = response.data?.SaveMediaListEntry

            if (saved != null) {
                val updated = entry.copy(
                    status = saved.status?.toEntryStatus() ?: newStatus,
                    progress = saved.progress ?: entry.progress
                )

                _uiState.value = DetailUiState.Success(current.copy(entry = updated))

                EntryEvents.publish(
                    EntryEvent.Updated(
                        entryId = entry.entryId,
                        status = updated.status,
                        progress = updated.progress,
                        updatedAt = saved.updatedAt ?: nowEpochSeconds()
                    )
                )
            } else {
                _errorMessage.value = when (response.errorKindOrNull()) {
                    ErrorKind.NETWORK -> "Sin conexión. No se pudo cambiar el estado."
                    ErrorKind.RATE_LIMIT -> "Demasiadas peticiones. No se pudo cambiar el estado."
                    else -> "No se pudo cambiar el estado."
                }
            }

            _isSaving.value = false
        }
    }

    fun setProgress(newProgress: Int) {
        val current = (_uiState.value as? DetailUiState.Success)?.detail ?: return
        val entry = current.entry ?: return

        if (entry.progress == newProgress || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true

            val response = ApolloProvider.client
                .mutation(
                    SaveEntryMutation(
                        entryId = Optional.present(entry.entryId),
                        progress = Optional.present(newProgress)
                    )
                )
                .execute()

            val saved = response.data?.SaveMediaListEntry

            if (saved != null) {
                val updated = entry.copy(
                    progress = saved.progress ?: newProgress,
                    status = saved.status?.toEntryStatus() ?: entry.status
                )

                _uiState.value = DetailUiState.Success(current.copy(entry = updated))

                EntryEvents.publish(
                    EntryEvent.Updated(
                        entryId = entry.entryId,
                        status = updated.status,
                        progress = updated.progress,
                        updatedAt = saved.updatedAt ?: nowEpochSeconds()
                    )
                )
            } else {
                _errorMessage.value = when (response.errorKindOrNull()) {
                    ErrorKind.NETWORK -> "Sin conexión. No se pudo guardar el progreso."
                    ErrorKind.RATE_LIMIT -> "Demasiadas peticiones. No se pudo guardar el progreso."
                    else -> "No se pudo guardar el progreso."
                }
            }

            _isSaving.value = false
        }
    }

    fun setScore(newScore: Double) {
        val current = (_uiState.value as? DetailUiState.Success)?.detail ?: return
        val entry = current.entry ?: return

        if (entry.score == newScore || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true

            val response = ApolloProvider.client
                .mutation(
                    SaveEntryMutation(
                        entryId = Optional.present(entry.entryId),
                        score = Optional.present(newScore)
                    )
                )
                .execute()

            val saved = response.data?.SaveMediaListEntry

            if (saved != null) {
                val updated = entry.copy(
                    score = saved.score ?: newScore
                )

                _uiState.value = DetailUiState.Success(current.copy(entry = updated))
            } else {
                _errorMessage.value = when (response.errorKindOrNull()) {
                    ErrorKind.NETWORK -> "Sin conexión. No se pudo guardar la nota."
                    ErrorKind.RATE_LIMIT -> "Demasiadas peticiones. No se pudo guardar la nota."
                    else -> "No se pudo guardar la nota."
                }
            }

            _isSaving.value = false
        }
    }

    fun setNotes(newNotes: String) {
        val current = (_uiState.value as? DetailUiState.Success)?.detail ?: return
        val entry = current.entry ?: return

        if (entry.notes.orEmpty() == newNotes || _isSaving.value) return

        viewModelScope.launch {
            _isSaving.value = true

            val response = ApolloProvider.client
                .mutation(
                    SaveEntryMutation(
                        entryId = Optional.present(entry.entryId),
                        notes = Optional.present(newNotes)
                    )
                )
                .execute()

            val saved = response.data?.SaveMediaListEntry

            if (saved != null) {
                val updated = entry.copy(
                    notes = saved.notes ?: newNotes
                )

                _uiState.value = DetailUiState.Success(current.copy(entry = updated))
            } else {
                _errorMessage.value = when (response.errorKindOrNull()) {
                    ErrorKind.NETWORK -> "Sin conexión. No se pudo guardar las notas."
                    ErrorKind.RATE_LIMIT -> "Demasiadas peticiones. No se pudo guardar las notas."
                    else -> "No se pudo guardar las notas."
                }
            }

            _isSaving.value = false
        }
    }

    private fun nowEpochSeconds() = (System.currentTimeMillis() / 1000).toInt()

    fun onErrorShown() {
        _errorMessage.value = null
    }
}