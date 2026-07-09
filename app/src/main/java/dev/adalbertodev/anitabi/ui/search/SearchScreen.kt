package dev.adalbertodev.anitabi.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel()) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.results.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
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

        LazyColumn {
            items(results) { title ->
                Text(text = title, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}