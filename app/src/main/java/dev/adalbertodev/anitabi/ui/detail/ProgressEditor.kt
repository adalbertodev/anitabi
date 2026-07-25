package dev.adalbertodev.anitabi.ui.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ProgressEditor(
    progress: Int,
    totalEpisodes: Int?,
    enabled: Boolean,
    onProgressConfirmed: (Int) -> Unit
) {
    var text by remember(progress) { mutableStateOf(progress.toString()) }
    val parsed = text.toIntOrNull()
    val isValid = parsed != null && parsed >= 0 &&
            (totalEpisodes == null || parsed <= totalEpisodes)

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = {input ->
                if(input.length <= 4 && input.all {it.isDigit()}) text = input
            },
            modifier = Modifier.width(96.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = text.isNotEmpty() && !isValid,
            singleLine = true,
            label = { Text("Episodio") }
        )

        Spacer(Modifier.width(8.dp))

        Text("/ ${totalEpisodes ?: "?"}")

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = {parsed?.let(onProgressConfirmed)},
            enabled = enabled && isValid && parsed != progress
        ) {
            Icon(Icons.Default.Check, contentDescription = "Guardar progreso")
        }
    }
}