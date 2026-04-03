package pt.isel.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
    var selectedRating by remember { mutableIntStateOf(initialRating) }
    var showInfo by remember { mutableStateOf(false) }

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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Text(
                            text ="$selectedRating: " +
                                when(selectedRating) {
                                    1 -> stringResource(id = R.string.rating_desc_1)
                                    2 -> stringResource(id = R.string.rating_desc_2)
                                    3 -> stringResource(id = R.string.rating_desc_3)
                                    4 -> stringResource(id = R.string.rating_desc_4)
                                    5 -> stringResource(id = R.string.rating_desc_5)
                                    else -> ""
                                },
                            style = MaterialTheme.typography.bodyMedium,


                        )
                    }
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
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text(stringResource(R.string.dialog_info_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.rating_desc_1), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.rating_desc_2), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.rating_desc_3), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.rating_desc_4), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.rating_desc_5), style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text(stringResource(R.string.button_confirm))
                }
            }
        )
    }

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