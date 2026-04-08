package pt.isel.settings.screen

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.R
import pt.isel.datascan.viewmodel.state.DEFAULT_TIMEOUT
import pt.isel.datascan.viewmodel.state.IS_TEST_TRIP
import pt.isel.datascan.viewmodel.state.NOTIFICATION_REMINDER_INTERVAL
import pt.isel.settings.domain.repository.MockSettingsRepository
import pt.isel.settings.viewmodel.SettingsViewModel
import kotlin.math.roundToInt

@SuppressLint("LocalContextConfigurationRead")
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val travelTime by viewModel.timeout.collectAsState(DEFAULT_TIMEOUT)
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

        val context = LocalContext.current

        val appLocales = AppCompatDelegate.getApplicationLocales().toLanguageTags()

        val currentLanguage = appLocales.ifEmpty {
            context.resources.configuration.locales[0].language
        }

        val isEnglish = currentLanguage.startsWith("en")
        var menuExpanded by remember { mutableStateOf(false) }

        SettingsSection(title = stringResource(R.string.select_language)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.language_selection))

                Box {
                    TextButton(onClick = { menuExpanded = true }) {
                        val displayText = if (isEnglish)
                            stringResource(R.string.english)
                        else
                            stringResource(R.string.portuguese)

                        Text(text = displayText)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.english)) },
                            onClick = {
                                viewModel.changeLanguage("en")
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.portuguese)) },
                            onClick = {
                                viewModel.changeLanguage("pt")
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }

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

        val hyperlinkedString = buildAnnotatedString {
            val fullText = stringResource(R.string.about_text)
            val linkText = "https://github.com/jviana103/otp-cd"
            val startIndex = fullText.indexOf(linkText)
            val endIndex = startIndex + linkText.length

            append(fullText)

            addLink(
                LinkAnnotation.Url(
                    url = "https://github.com/jviana103/otp-cd",
                    styles = TextLinkStyles(
                        style = SpanStyle(
                                color = Color(0xFF6BB6FF),
                            textDecoration = TextDecoration.Underline
                        )
                    )
                ),
                start = startIndex,
                end = endIndex
            )
        }

        SettingsSection(title = stringResource(R.string.section_about)) {
            Text(
                text = hyperlinkedString,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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