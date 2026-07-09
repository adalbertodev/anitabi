package dev.adalbertodev.anitabi.ui.login

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import dev.adalbertodev.anitabi.auth.AuthConfig

@Composable
fun LoginScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("AniTabi", style = MaterialTheme.typography.headlineLarge)

        Spacer(Modifier.height(32.dp))

        Button(onClick = {
            val customTab = CustomTabsIntent.Builder().build()
            customTab.launchUrl(context, AuthConfig.AUTH_URL.toUri())
        }) {
            Text("Entrar con AniList")
        }
    }
}