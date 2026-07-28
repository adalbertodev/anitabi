package dev.adalbertodev.anitabi.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotesEditor(
    notes: String?,
    enabled: Boolean,
    onNotesConfirmed: (String) -> Unit
) {
    var text by rememberSaveable(notes) { mutableStateOf(notes.orEmpty()) }
    val currentText = notes.orEmpty()

    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp),
            label = { Text("Notas") },
            placeholder = { Text("Sin notas") }
        )

        Spacer(Modifier.height(4.dp))

        Row {
            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = { onNotesConfirmed(text) },
                enabled = enabled && text != currentText
            ) {
                Icon(Icons.Default.Check, contentDescription = "Guardar notas")
            }
        }
    }
}