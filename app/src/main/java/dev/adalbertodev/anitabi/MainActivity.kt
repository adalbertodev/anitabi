package dev.adalbertodev.anitabi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import dev.adalbertodev.anitabi.ui.login.LoginScreen
import dev.adalbertodev.anitabi.ui.theme.AniTabiTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAuthRedirect(intent)

        setContent {
            AniTabiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    LoginScreen()
                }
            }
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

        if(token != null) {
            Log.d("AniTabiAuth", "Token recibido, longitud: ${token.length}")
            Toast.makeText(this, "Login correcto ✔", Toast.LENGTH_SHORT).show()
        }
    }
}