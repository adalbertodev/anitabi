package dev.adalbertodev.anitabi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.adalbertodev.anitabi.data.SessionStore
import dev.adalbertodev.anitabi.ui.lists.ListUiState
import dev.adalbertodev.anitabi.ui.lists.ListsViewModel
import dev.adalbertodev.anitabi.ui.lists.ViewerUiState
import dev.adalbertodev.anitabi.ui.lists.ViewerViewModel
import dev.adalbertodev.anitabi.ui.login.LoginScreen
import dev.adalbertodev.anitabi.ui.search.SearchViewModel
import kotlinx.coroutines.flow.map

sealed interface Session {
    data object Loading : Session
    data object LoggedOut : Session
    data class LoggedIn(val token: String) : Session
}

@Composable
fun AniTabiApp(sessionStore: SessionStore) {
    val session by remember {
        sessionStore.token
            .map { if (it === null) Session.LoggedOut else Session.LoggedIn(it) }
    }.collectAsState(initial = Session.Loading)

    when(session) {
        Session.Loading -> Box(Modifier.fillMaxSize())
        Session.LoggedOut -> LoginScreen()
        is Session.LoggedIn -> ListPlaceholderScreen()
    }
}

@Composable
fun ListPlaceholderScreen(viewModel: ListsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Mis listas — placeholder")
    }

    when(val s = state) {
        ListUiState.Loading -> Box(Modifier.fillMaxSize())
        ListUiState.Error -> Box(Modifier.fillMaxSize())
        is ListUiState.Success -> LazyColumn {
            items(s.entries, key = {it.entryId}) { entry ->
                Text("${entry.title} — ${entry.progress}/${entry.totalEpisodes ?: "?"} [${entry.status}]")
            }
        }
    }
}