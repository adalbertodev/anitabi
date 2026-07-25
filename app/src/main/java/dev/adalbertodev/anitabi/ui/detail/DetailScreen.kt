package dev.adalbertodev.anitabi.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.adalbertodev.anitabi.data.EntryStatus

@Composable
fun DetailScreen(viewModel: DetailViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val s = state) {
                DetailUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                DetailUiState.Error -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se pudo cargar el anime.")
                }

                is DetailUiState.Success -> DetailContent(s.detail, isSaving, viewModel)
            }
        }
    }
}

@Composable
fun DetailContent(detail: MediaDetail, isSaving: Boolean, viewModel: DetailViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AsyncImage(
            model = detail.coverUrl,
            contentDescription = detail.title,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .align(Alignment.CenterHorizontally)
                .aspectRatio(2f / 3f),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(16.dp))

        Text(detail.title, style = MaterialTheme.typography.headlineSmall)

        Spacer(Modifier.height(8.dp))

        detail.entry?.let { entry ->
            StatusSelector(
                current = entry.status,
                enabled = !isSaving,
                onStatusSelected = viewModel::setStatus
            )
            ProgressEditor(
                progress = entry.progress,
                totalEpisodes = detail.totalEpisodes,
                enabled = !isSaving,
                onProgressConfirmed = viewModel::setProgress
            )
            Text("Puntuación: ${entry.score}")

            Spacer(Modifier.height(16.dp))
        }

        Text(detail.synopsis, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StatusSelector(
    current: EntryStatus,
    enabled: Boolean,
    onStatusSelected: (EntryStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
            Text(current.label)

            if (!enabled) {
                Spacer(Modifier.width(8.dp))
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EntryStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = { onStatusSelected(status); expanded = false }
                )
            }
        }
    }
}