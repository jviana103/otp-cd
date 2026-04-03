package pt.isel.datascan.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import pt.isel.R
import pt.isel.datascan.domain.TransportationType
import pt.isel.datascan.viewmodel.DataScanViewModel
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.DataScanUiState
import pt.isel.ui.components.ConfirmationDialog
import pt.isel.ui.components.RatingDialog
import pt.isel.ui.components.TransportSelectionBox
import pt.isel.helpers.formatTime
import pt.isel.settings.domain.repository.MockSettingsRepository

@Composable
fun DataScanScreen(
    viewModel: DataScanViewModel,
    onStartService: (Int) -> Unit,
    onStopService: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var showStopConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!state.isRiding) {
                val selectedType by viewModel.selectedTransport.collectAsState()

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.label_select_transport),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TransportSelectionBox(
                            type = TransportationType.METRO,
                            isSelected = selectedType == TransportationType.METRO,
                            iconRes = R.drawable.ic_subway,
                            label = stringResource(R.string.label_metro),
                            modifier = Modifier.weight(1f),
                            onSelect = { viewModel.selectTransport(TransportationType.METRO) }
                        )

                        TransportSelectionBox(
                            type = TransportationType.TRAIN,
                            isSelected = selectedType == TransportationType.TRAIN,
                            iconRes = R.drawable.ic_train,
                            label = stringResource(R.string.label_train),
                            modifier = Modifier.weight(1f),
                            onSelect = { viewModel.selectTransport(TransportationType.TRAIN) }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = { viewModel.startRide() },
                        enabled = selectedType != null,
                        modifier = Modifier.height(56.dp).fillMaxWidth(0.8f),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(stringResource(R.string.button_start_collection))
                    }
                }
            } else {
                Text(
                    text =
                        if (state.isPaused) stringResource(R.string.label_trip_paused)
                        else stringResource(R.string.label_time_remaining_header),
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = formatTime(state.secondsRemaining),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (state.isPaused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (!state.isPaused) {
                        Text(
                            text = stringResource(R.string.label_collecting_evidence),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
/*
                Text(stringResource(R.string.label_current_occupancy, state.currentSubjectiveRating))

                Text(stringResource(R.string.label_bluetooth_devices, state.lastRead?.bluetoothCount ?: 0))

                Text(stringResource(R.string.label_wifi_devices, state.lastRead?.wifiCount ?: 0))

                Text(stringResource(R.string.label_current_location, state.lastRead?.latitude ?: 0.0, state.lastRead?.longitude ?: 0.0))
*/
                Row(
                    modifier = Modifier.fillMaxWidth(0.8f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.togglePause(context) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            if (state.isPaused) stringResource(R.string.btn_resume)
                            else stringResource(R.string.btn_pause))
                    }

                    Button(
                        onClick = { showUpdateDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.btn_change_occupancy))
                    }
                }

                Button(
                    onClick = { showStopConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text(stringResource(R.string.btn_stop_trip))
                }
            }
        }

        if (showUpdateDialog) {
            RatingDialog(
                initialRating = state.currentSubjectiveRating,
                confirmText = stringResource(R.string.btn_confirm),
                onConfirm = { newRating ->
                    viewModel.updateOngoingRating(context, newRating)
                    showUpdateDialog = false
                },
                onDismiss = { showUpdateDialog = false }
            )
        }

        if (state.finishedTripIdToConfirm != null) {
            ConfirmationDialog(
                title = stringResource(R.string.dialog_stayed_inside_title),
                message = stringResource(R.string.dialog_stayed_inside_message),
                confirmText = stringResource(R.string.btn_yes),
                dismissText = stringResource(R.string.btn_no),
                confirmButtonColor = MaterialTheme.colorScheme.primary,
                isDismissDestructive = true,
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
                onConfirm = { viewModel.handleTripConfirmation(context, stayedInside = true) },
                onDismiss = { viewModel.handleTripConfirmation(context, stayedInside = false) }
            )
        }

        if (showStopConfirmation) {
            ConfirmationDialog(
                title = stringResource(R.string.dialog_finish_trip_title),
                message = stringResource(R.string.dialog_finish_trip_message),
                confirmText = stringResource(R.string.btn_confirm_stop),
                isConfirmDestructive = true,
                onConfirm = {
                    showStopConfirmation = false
                    onStopService()
                },
                onDismiss = { showStopConfirmation = false }
            )
        }

        if (state.isAwaitingInitialRating) {
            RatingDialog(
                onConfirm = onStartService,
                onDismiss = {
                    viewModel.cancelStart()
                    showUpdateDialog = false
                }
            )
        }
    }
}

@Preview
@Composable
fun DataScanScreenPreview() {
    val mockRepo = MockSettingsRepository()
    DataScanScreen(
        viewModel = DataScanViewModel(mockRepo),
        onStartService = {},
        onStopService = {}
    )
}

@Preview
@Composable
fun DataScanScreenPreviewInTravel() {
    val mockRepo = MockSettingsRepository()
    val viewModel = DataScanViewModel(mockRepo)
    viewModel.uiState = MutableStateFlow(
        DataScanUiState(
            isRiding = true,
            secondsRemaining = DEFAULT_TIMEOUT,
        ))
    DataScanScreen(
        viewModel = viewModel,
        onStartService = {},
        onStopService = {}
    )
}