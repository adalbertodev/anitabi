package dev.adalbertodev.anitabi.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage

@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel(), onResultClick: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val keyboard = LocalSoftwareKeyboardController.current

    fun launchSearch() {
        if (query.isNotBlank()) {
            viewModel.search(query)
            keyboard?.hide()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar anime...") },
            singleLine = true,
            trailingIcon = {
                IconButton(onClick = ::launchSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar")
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { launchSearch() })
        )

        Spacer(Modifier.height(8.dp))

        when (val state = uiState) {
            SearchUiState.Idle -> Text("Busca un anime para empezar")

            SearchUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            is SearchUiState.Error -> Text(state.message)

            is SearchUiState.Success -> {
                if (state.results.isEmpty()) {
                    Text("No hay resultados para tu búsqueda")
                } else {
                    LazyColumn {
                        items(state.results, key = { it.id }) { anime ->
                            AnimeCard(anime = anime, onClick = onResultClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeCard(anime: AnimeSearchResult, onClick: (Int) -> Unit) {
    Card(
        onClick = { onClick(anime.id) },
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = anime.coverUrl,
                contentDescription = anime.title,
                modifier = Modifier
                    .width(64.dp)
                    .height(90.dp),
                contentScale = ContentScale.Crop
            )

            Text(text = anime.title, modifier = Modifier.padding(horizontal = 12.dp))
        }
    }
}