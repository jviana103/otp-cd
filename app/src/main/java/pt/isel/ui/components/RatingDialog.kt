package pt.isel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.R
import pt.isel.datascan.viewmodel.state.DEFAULT_SUBJ_RATING

@Composable
fun RatingDialog(
    initialRating: Int = DEFAULT_SUBJ_RATING,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRating by remember { mutableStateOf(initialRating) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.dialog_occupancy_title)) },
        text = {
            Column {
                Text(text = stringResource(id = R.string.dialog_occupancy_question))
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (1..5).forEach { rating ->
                        FilterChip(
                            selected = selectedRating == rating,
                            onClick = { selectedRating = rating },
                            label = { Text(rating.toString()) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stringResource(id = R.string.label_occupancy_empty), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(id = R.string.label_occupancy_full), style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedRating) }) {
                Text(stringResource(id = R.string.button_start_trip))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.button_cancel))
            }
        }
    )
}

@Preview
@Composable
fun RatingDialogPreview() {
    RatingDialog(
        initialRating = DEFAULT_SUBJ_RATING,
        onConfirm = {},
        onDismiss = {}
    )
}