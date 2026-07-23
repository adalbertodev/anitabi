package dev.adalbertodev.anitabi.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.graphql.MediaDetailQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val mediaId: Int = checkNotNull(savedStateHandle["mediaId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState

    init {
        viewModelScope.launch {
            val media = ApolloProvider.client
                .query(MediaDetailQuery(mediaId = mediaId))
                .execute()
                .data?.Media

            _uiState.value = if(media != null) {
                DetailUiState.Success(media.toUiModel())
            } else {
                DetailUiState.Error
            }
        }
    }
}