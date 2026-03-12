package pt.isel.datascan.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import pt.isel.datascan.viewmodel.DataScanViewModel
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.DataScanUiState
import pt.isel.ui.components.RatingDialog
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun DataScanScreen(
    viewModel: DataScanViewModel,
    onStartService: (Int) -> Unit,
    onStopService: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    var showUpdateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!state.isRiding) {
                Text(
                    text = "Pronto para iniciar a recolha?",
                    style = MaterialTheme.typography.headlineSmall
                )
                Button(
                    onClick = { viewModel.startRide() },
                    modifier = Modifier.height(56.dp).fillMaxWidth(0.7f)
                ) {
                    Text("Iniciar Viagem")
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

                Button(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Alterar Lotação")
                }

                Button(
                    onClick = onStopService,
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
                onConfirm = { newRating ->
                    viewModel.updateOngoingRating(context, newRating)
                    showUpdateDialog = false
                },
                onDismiss = { showUpdateDialog = false }
            )
        }

        if (state.isAwaitingInitialRating) {
            RatingDialog(
                onConfirm = onStartService,
                onDismiss = {  }
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