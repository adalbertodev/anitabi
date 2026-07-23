package dev.adalbertodev.anitabi.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage

@Composable
fun DetailScreen(viewModel: DetailViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    when(val s = state) {
        DetailUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        DetailUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No se pudo cargar el anime.")
        }

        is DetailUiState.Success -> DetailContent(s.detail)
    }
}

@Composable
fun DetailContent(detail: MediaDetail) {
    Column(Modifier.fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp)
    ) {
        AsyncImage(
            model = detail.coverUrl,
            contentDescription = detail.title,
            modifier = Modifier.fillMaxWidth(0.6f)
                .align(Alignment.CenterHorizontally)
                .aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        Text(detail.title, style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(8.dp))

        detail.entry?.let { entry ->
            Text("Estado: ${entry.status}")
            Text("Progreso: ${entry.progress} / ${detail.totalEpisodes ?: "?"}")
            Text("Puntuación: ${entry.score}")

            Spacer(Modifier.height(16.dp))
        }

        Text(detail.synopsis, style = MaterialTheme.typography.bodyMedium)
    }
}