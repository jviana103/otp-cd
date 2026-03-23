package pt.isel.settings.screen

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.R
import pt.isel.datascan.viewmodel.state.DEFAULT_INTERVAL
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP
import pt.isel.datascan.viewmodel.state.NOTIFICATION_REMINDER_INTERVAL
import pt.isel.settings.domain.repository.MockSettingsRepository
import pt.isel.settings.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val travelTime by viewModel.timeout.collectAsState(DEFAULT_TIMEOUT)
    val intervalTime by viewModel.interval.collectAsState(DEFAULT_INTERVAL)
    val notificationTime by viewModel.notificationInterval.collectAsState(NOTIFICATION_REMINDER_INTERVAL)
    val isTestTrip by viewModel.isTestTrip.collectAsState(IS_TEST_TRIP)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        SettingsSection(title = stringResource(R.string.section_trip)) {
            SettingsSlider(
                label = stringResource(R.string.label_travel_time),
                value = travelTime.toFloat() / 60,
                valueRange = 1f..60f,
                steps = 59,
                onValueChange = {
                    viewModel.updateTimeout((it.roundToInt() * 60))
                },
                valueDisplay = stringResource(R.string.unit_minutes, travelTime / 60)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSlider(
                label = stringResource(R.string.label_scan_interval),
                value = intervalTime.toFloat(),
                valueRange = 5f..120f,
                steps = 115,
                onValueChange = {
                    viewModel.updateInterval(it.roundToInt())
                },
                valueDisplay = stringResource(R.string.unit_seconds, intervalTime)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsSlider(
                label = stringResource(R.string.label_notification_interval),
                value = notificationTime.toFloat(),
                valueRange = 30f..300f,
                steps = 26,
                onValueChange = {
                    viewModel.updateNotificationInterval(it.roundToInt())
                },
                valueDisplay = stringResource(R.string.unit_seconds, notificationTime)
            )
        }

        SettingsSection(title = stringResource(R.string.section_development)) {
            SettingsToggle(
                label = stringResource(R.string.label_test_trip),
                description = stringResource(R.string.desc_test_trip),
                checked = isTestTrip,
                onCheckedChange = {
                    viewModel.updateIsTestTrip(it)
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

@Preview
@Composable
fun SettingsScreenPreview() {
    val mockRepo = MockSettingsRepository()
    SettingsScreen(viewModel = SettingsViewModel(mockRepo))
}