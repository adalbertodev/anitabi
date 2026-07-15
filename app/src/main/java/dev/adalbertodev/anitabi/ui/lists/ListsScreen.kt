package dev.adalbertodev.anitabi.ui.lists

import android.R
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ListsScreen(viewModel: ListsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
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
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            Log.d("Padding", padding.calculateLeftPadding(LocalLayoutDirection.current).value.toString())
            when (val s = state) {
                ListUiState.Loading -> CircularProgressIndicator(
                    Modifier.align(Alignment.Center)
                )

                ListUiState.Error -> Text("No se pudieron cargar tus listas")

                is ListUiState.Success -> {
                    if(s.entries.isEmpty()) {
                        Text(
                            text = s.activeFilter.emptyMessage,
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        LazyColumn {
                            items(s.entries, key = { it.entryId }) { entry ->
                                AnimeListCard(entry)
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