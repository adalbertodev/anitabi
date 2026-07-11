package dev.adalbertodev.anitabi.ui.lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.adalbertodev.anitabi.data.ApolloProvider
import dev.adalbertodev.anitabi.graphql.ViewerQuery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface ViewerUiState {
    data object Loading: ViewerUiState
    data class Success(val name: String, val scoreFormat: String) : ViewerUiState
    data object Error : ViewerUiState
}

class ViewerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<ViewerUiState>(ViewerUiState.Loading)
    val uiState: StateFlow<ViewerUiState> = _uiState

    init {
        viewModelScope.launch {
            val response = ApolloProvider.client.query(ViewerQuery()).execute()
            val viewer = response.data?.Viewer

            _uiState.value = if(viewer != null) {
                ViewerUiState.Success(
                    name = viewer.name,
                    scoreFormat = viewer.mediaListOptions?.scoreFormat?.name ?: "desconocido"
                )
            } else {
                ViewerUiState.Error
            }
        }
    }
}