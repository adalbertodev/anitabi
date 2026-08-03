package dev.adalbertodev.anitabi.ui.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    viewModel: ListsViewModel = viewModel(),
    onEntryClick: (Int) -> Unit,
    onSearchClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage by viewModel.errorMessage.collectAsState()
    val completionEvent by viewModel.completionEvent.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onErrorShown()
        }
    }

    LaunchedEffect(completionEvent) {
        completionEvent?.let { event ->
            val result = snackbarHostState.showSnackbar(
                message = "«${event.title}» completado",
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Long
            )

            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.undoCompletion(event.entryId)
                SnackbarResult.Dismissed -> viewModel.onCompletionDismissed(event.entryId)
            }

            viewModel.onCompletionShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AniTabi") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val s = state

            if (s is ListUiState.Success) {
                FilterFab(
                    activeFilter = s.activeFilter,
                    onFilterSelected = viewModel::setFilter
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val s = state) {
                ListUiState.Loading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center)
                )

                ListUiState.Error -> Text("No se pudieron cargar tus listas", color = MaterialTheme.colorScheme.onSurfaceVariant)

                is ListUiState.Success -> PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = viewModel::refresh
                ) {
                    if (s.entries.isEmpty()) {
                        Column(
                            Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(s.activeFilter.emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize()
                        ) {
                            items(s.entries, key = { it.entryId }) { entry ->
                                AnimeListCard(
                                    entry,
                                    onIncrement = viewModel::incrementProgress,
                                    onClick = onEntryClick
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterFab(
    activeFilter: ListFilter,
    onFilterSelected: (ListFilter) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box {
        ExtendedFloatingActionButton(
            onClick = { menuExpanded = true },
            text = { Text(activeFilter.label) },
            icon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }
        )

        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            ListFilter.entries.forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter.label) },
                    onClick = {
                        onFilterSelected(filter)
                        menuExpanded = false
                    }
                )
            }
        }
    }
}