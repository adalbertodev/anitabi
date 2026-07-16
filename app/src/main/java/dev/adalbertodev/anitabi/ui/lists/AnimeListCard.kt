package dev.adalbertodev.anitabi.ui.lists

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun AnimeListCard(entry: AnimeListEntry, onIncrement : (AnimeListEntry) -> Unit) {
    Card(modifier = Modifier
        .fillMaxSize()
        .padding(vertical = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = entry.coverUrl,
                contentDescription = entry.title,
                modifier = Modifier
                    .width(64.dp)
                    .height(90.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f)) {
                Text(
                    text = entry.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Ep. ${entry.progress} / ${entry.totalEpisodes ?: "?"}",
                    style = MaterialTheme.typography.bodySmall
                )

                if(entry.status == EntryStatus.REPEATING) {
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Repitiendo",
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            IconButton(onClick = { onIncrement(entry) }, enabled = entry.totalEpisodes == null || entry.progress < entry.totalEpisodes) {
                Icon(Icons.Default.Add, contentDescription = "+1 episodio")
            }
        }
    }
}