package dev.adalbertodev.anitabi.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage

@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row {
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f)
            )

            Button(onClick = { viewModel.search(query) }) {
                Text("Buscar...")
            }
        }

        when (val state = uiState) {
            SearchUiState.Idle -> Text("Busca un anime para empezar")
            SearchUiState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .padding(24.dp)
                    .align(Alignment.CenterHorizontally)
            )

            is SearchUiState.Error -> Text(state.message)
            is SearchUiState.Success -> LazyColumn {
                items(state.results, key = { it.id }) { anime ->
                    AnimeCard(anime)
                }
            }
        }
    }
}

@Composable
fun AnimeCard(anime: AnimeSearchResult) {
    Card(modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 4.dp)) {
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