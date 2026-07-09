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
    private val _results = MutableStateFlow<List<String>>(emptyList())
    val results: StateFlow<List<String>> = _results

    fun search(text: String) {
        viewModelScope.launch {
            val response = ApolloProvider.client
                .query(SearchAnimeQuery(Optional.present(text)))
                .execute()

            _results.value = response.data?.Page?.media
                ?.mapNotNull { it?.title?.userPreferred }
                ?: emptyList()
        }
    }
}