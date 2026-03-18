package pt.isel.datascan.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP
import pt.isel.datascan.viewmodel.state.NOTIFICATION_REMINDER_INTERVAL
import kotlin.math.roundToInt

@Composable
fun SettingsScreen() {
    var travelTime by remember { mutableFloatStateOf(DEFAULT_TIMEOUT / 60f) }
    var intervalTime by remember { mutableFloatStateOf(DEFAULT_INTERVAL.toFloat()) }
    var notificationTime by remember { mutableFloatStateOf(NOTIFICATION_REMINDER_INTERVAL.toFloat()) }
    var isTestTrip by remember { mutableStateOf(IS_TEST_TRIP) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Configurações",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        SettingsSection(title = "Viagem") {
            SettingsSlider(
                label = "Tempo de viagem",
                value = travelTime,
                valueRange = 1f..60f,
                steps = 59,
                onValueChange = {
                    travelTime = it
                    DEFAULT_TIMEOUT = (it * 60).toInt()
                },
                valueDisplay = "${travelTime.toInt()} min"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSlider(
                label = "Intervalo entre leituras",
                value = intervalTime,
                valueRange = 5f..120f,
                steps = 115,
                onValueChange = {
                    val roundedValue = it.roundToInt()
                    intervalTime = roundedValue.toFloat()
                    DEFAULT_INTERVAL = roundedValue
                },
                valueDisplay = "${intervalTime.toInt()} seg"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSlider(
                label = "Intervalo entre notificações",
                value = notificationTime,
                valueRange = 30f..300f,
                steps = 26,
                onValueChange = {
                    val roundedValue = it.roundToInt()
                    notificationTime = roundedValue.toFloat()
                    NOTIFICATION_REMINDER_INTERVAL = roundedValue
                },
                valueDisplay = "${notificationTime.toInt()} seg"
            )
        }

        SettingsSection(title = "Desenvolvimento") {
            SettingsToggle(
                label = "Viagem de Teste",
                description = "Se ativo, os dados serão guardados na coleção 'viagens_teste'",
                checked = isTestTrip,
                onCheckedChange = {
                    isTestTrip = it
                    IS_TEST_TRIP = it
                }
            )
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    onValueChange: (Float) -> Unit,
    valueDisplay: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
fun SettingsToggle(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
