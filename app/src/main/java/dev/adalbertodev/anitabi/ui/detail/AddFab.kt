package dev.adalbertodev.anitabi.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.adalbertodev.anitabi.data.EntryStatus

@Composable
fun AddFab(
    enabled: Boolean,
    onStatusSelected: (EntryStatus) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        ExtendedFloatingActionButton(
            onClick = { if (enabled) expanded = true },
            text = { Text("Añadir a mi lista") },
            icon = {
                if (enabled) Icon(Icons.Default.Add, contentDescription = null)
                else CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            EntryStatus.entries.forEach { status ->
                DropdownMenuItem(
                    text = { Text(status.label) },
                    onClick = {
                        onStatusSelected(status)
                        expanded = false
                    }
                )
            }
        }
    }
}