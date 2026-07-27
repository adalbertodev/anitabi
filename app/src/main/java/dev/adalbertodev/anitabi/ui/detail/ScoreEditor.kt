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

data class NumericScoreConfig(
    val max: Double,
    val allowDecimal: Boolean,
    val maxLength: Int
)

@Composable
fun ScoreEditor(
    format: ScoreUiFormat,
    score: Double,
    enabled: Boolean,
    onScoreConfirmed: (Double) -> Unit
) {
    when (format) {
        ScoreUiFormat.POINT_100 ->
            NumericScoreEditor(
                NumericScoreConfig(100.0, false, 3),
                score,
                enabled,
                onScoreConfirmed
            )

        ScoreUiFormat.POINT_10_DECIMAL ->
            NumericScoreEditor(NumericScoreConfig(10.0, true, 4), score, enabled, onScoreConfirmed)

        ScoreUiFormat.POINT_10 ->
            NumericScoreEditor(NumericScoreConfig(10.0, false, 2), score, enabled, onScoreConfirmed)

        ScoreUiFormat.POINT_5 ->
            NumericScoreEditor(NumericScoreConfig(5.0, false, 1), score, enabled, onScoreConfirmed)

        ScoreUiFormat.POINT_3 ->
            NumericScoreEditor(NumericScoreConfig(3.0, false, 1), score, enabled, onScoreConfirmed)
    }
}

@Composable
private fun NumericScoreEditor(
    config: NumericScoreConfig,
    score: Double,
    enabled: Boolean,
    onScoreConfirmed: (Double) -> Unit
) {
    var text by remember(score) {
        mutableStateOf(if (score == 0.0) "" else formatScore(score, config.allowDecimal))
    }
    val parsed = text.toDoubleOrNull()
    val isValid = text.isEmpty() || (parsed != null && parsed in 0.0..config.max)
    val currentText = if (score == 0.0) "" else formatScore(score, config.allowDecimal)

    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { input ->
                val pattern =
                    if (config.allowDecimal) Regex("^\\d*(\\.\\d?)?$") else Regex("^\\d*$")
                if (input.length <= config.maxLength && input.matches(pattern)) text = input
            },
            modifier = Modifier.width(112.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (config.allowDecimal) KeyboardType.Decimal else KeyboardType.Number
            ),
            isError = !isValid,
            singleLine = true,
            label = { Text("Nota") },
            placeholder = { Text("—") }
        )

        Spacer(Modifier.width(8.dp))

        Text("/ ${formatScore(config.max, false)}")

        Spacer(Modifier.width(8.dp))

        IconButton(
            onClick = {onScoreConfirmed(if (text.isEmpty()) 0.0 else parsed ?: return@IconButton)},
            enabled = enabled && isValid && text != currentText
        ) {
            Icon(Icons.Default.Check, contentDescription = "Guardar nota")
        }
    }
}

private fun formatScore(value: Double, allowDecimal: Boolean): String =
    if (allowDecimal && value % 1.0 != 0.0) value.toString()
    else value.toInt().toString()