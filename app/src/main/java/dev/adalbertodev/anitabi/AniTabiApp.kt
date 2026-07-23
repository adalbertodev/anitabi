package dev.adalbertodev.anitabi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.adalbertodev.anitabi.data.SessionStore
import dev.adalbertodev.anitabi.ui.detail.DetailScreen
import dev.adalbertodev.anitabi.ui.lists.ListsScreen
import dev.adalbertodev.anitabi.ui.login.LoginScreen
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

    when (session) {
        Session.Loading -> Box(Modifier.fillMaxSize())
        Session.LoggedOut -> LoginScreen()
        is Session.LoggedIn -> AppNavHost()
    }
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "lists") {
        composable("lists") {
            ListsScreen(
                onEntryClick = { mediaId -> navController.navigate("detail/$mediaId") }
            )
        }

        composable(
            route = "detail/{mediaId}",
            arguments = listOf(navArgument("mediaId") { type = NavType.IntType })
        ) {
            DetailScreen()
        }
    }
}