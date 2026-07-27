package dev.adalbertodev.anitabi.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.api.Optional
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.data.EntryStatus
import dev.adalbertodev.anitabi.data.EntryUpdate
import dev.adalbertodev.anitabi.data.EntryUpdates
import dev.adalbertodev.anitabi.graphql.MediaDetailQuery
import dev.adalbertodev.anitabi.graphql.SaveEntryMutation
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
            val data = ApolloProvider.client
                .query(MediaDetailQuery(mediaId = mediaId))
                .execute()
                .data

            _uiState.value = if (data?.Media != null) {
                DetailUiState.Success(data.toUiModel())
            } else {
                DetailUiState.Error
            }
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

                EntryUpdates.publish(
                    EntryUpdate(
                        entryId = entry.entryId,
                        status = updated.status,
                        progress = updated.progress,
                        updatedAt = saved.updatedAt ?: nowEpochSeconds()
                    )
                )
            } else {
                _errorMessage.value = "No se pudo cambiar el estado."
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

                EntryUpdates.publish(
                    EntryUpdate(
                        entryId = entry.entryId,
                        status = updated.status,
                        progress = updated.progress,
                        updatedAt = saved.updatedAt ?: nowEpochSeconds()
                    )
                )
            } else {
                _errorMessage.value = "No se pudo guardar el progreso."
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
                    score = saved.score ?: newScore,
                    status = saved.status?.toEntryStatus() ?: entry.status
                )

                _uiState.value = DetailUiState.Success(current.copy(entry = updated))
            } else {
                _errorMessage.value = "No se pudo guardar la nota."
            }

            _isSaving.value = false
        }
    }

    private fun nowEpochSeconds() = (System.currentTimeMillis() / 1000).toInt()

    fun onErrorShown() {
        _errorMessage.value = null
    }
}