package pt.isel.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import pt.isel.R

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = stringResource(R.string.btn_confirm),
    dismissText: String = stringResource(R.string.btn_cancel),
    isConfirmDestructive: Boolean = false,
    isDismissDestructive: Boolean = false,
    confirmButtonColor: Color? = null,
    properties: DialogProperties = DialogProperties(),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = properties,
        title = { Text(text = title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(text = message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isConfirmDestructive -> MaterialTheme.colorScheme.error
                        confirmButtonColor != null -> confirmButtonColor
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            if (isDismissDestructive) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(dismissText)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        }
    )
}
