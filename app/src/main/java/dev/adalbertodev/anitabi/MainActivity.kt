package dev.adalbertodev.anitabi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import dev.adalbertodev.anitabi.data.SessionStore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")

    private lateinit var sessionStore: SessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionStore = SessionStore(applicationContext)
        enableEdgeToEdge()
        handleAuthRedirect(intent)

        setContent {
            AniTabiApp(sessionStore)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAuthRedirect(intent)
    }

    private fun handleAuthRedirect(intent: Intent?) {
        val uri = intent?.data ?: return

        if (uri.scheme != "anitabi") return

        val fragment = uri.fragment ?: return
        val token = fragment
            .split("&")
            .map { it.split("=", limit = 2) }
            .firstOrNull { it.size == 2 && it[0] == "access_token" }
            ?.get(1)

        if (token != null) {
            lifecycleScope.launch { sessionStore.saveToken(token) }
        }
    }
}