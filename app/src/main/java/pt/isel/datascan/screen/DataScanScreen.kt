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

    val selectedType by viewModel.selectedTransport.collectAsState()

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
                        text = "Selecione o meio de transporte:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --- METRO BOX ---
                        TransportSelectionBox(
                            type = TransportationType.METRO,
                            isSelected = selectedType == TransportationType.METRO,
                            iconRes = R.drawable.ic_train,
                            label = "Metro",
                            modifier = Modifier.weight(1f),
                            onSelect = { viewModel.selectTransport(TransportationType.METRO) }
                        )

                        // --- TRAIN BOX ---
                        TransportSelectionBox(
                            type = TransportationType.TRAIN,
                            isSelected = selectedType == TransportationType.TRAIN,
                            iconRes = R.drawable.ic_train,
                            label = "Comboio",
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
                        Text("Iniciar Recolha")
                    }
                }
            } else {
                Text(
                    text = "Tempo Restante",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = formatTime(state.secondsRemaining),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "A recolher evidências sem fio...",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text("Ocupação atual: ${state.currentSubjectiveRating}")

                Text("Dispositivos Bluetooth: ${state.lastRead?.bluetoothCount}")

                Text("Dispositivos Wi-Fi: ${state.lastRead?.wifiCount}")

                Text("Localização atual: ${state.lastRead?.latitude}, ${state.lastRead?.longitude}")

                Button(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Alterar Lotação")
                }

                Button(
                    onClick = { showStopConfirmation = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Parar Viagem")
                }
            }
        }

        if (showUpdateDialog) {
            RatingDialog(
                initialRating = state.currentSubjectiveRating,
                onConfirm = { newRating ->
                    viewModel.updateOngoingRating(context, newRating)
                    showUpdateDialog = false
                },
                onDismiss = { showUpdateDialog = false }
            )
        }

        if (showStopConfirmation) {
            ConfirmationDialog(
                title = "Finalizar Viagem?",
                message = "Deseja terminar a viagem?",
                confirmText = "Sim, Parar",
                isDestructive = true,
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
                    showUpdateDialog = false;
                }
            )
        }
    }
}

fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}

@Preview
@Composable
fun DataScanScreenPreview() {
    DataScanScreen(
        viewModel = DataScanViewModel(),
        onStartService = {},
        onStopService = {}
    )
}

@Preview
@Composable
fun DataScanScreenPreviewInTravel() {
    val viewModel = DataScanViewModel()
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