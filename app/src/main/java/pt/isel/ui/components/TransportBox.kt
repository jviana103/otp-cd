package pt.isel.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pt.isel.datascan.domain.TransportationType

@Composable
fun TransportSelectionBox(
    type: TransportationType,
    isSelected: Boolean,
    iconRes: Int,
    label: String,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, name = "Selected State")
@Composable
fun TransportSelectionBoxPreviewSelected() {
    MaterialTheme {
        TransportSelectionBox(
            type = TransportationType.TRAIN,
            label = "Comboio",
            iconRes = android.R.drawable.ic_dialog_info,
            isSelected = true,
            modifier = Modifier.width(150.dp),
            onSelect = { }
        )
    }
}

@Preview(showBackground = true, name = "Unselected State")
@Composable
fun TransportSelectionBoxPreviewUnselected() {
    MaterialTheme {
        TransportSelectionBox(
            type = TransportationType.METRO,
            label = "Metro",
            iconRes = android.R.drawable.ic_dialog_info,
            isSelected = false,
            modifier = Modifier.width(150.dp),
            onSelect = { }
        )
    }
}