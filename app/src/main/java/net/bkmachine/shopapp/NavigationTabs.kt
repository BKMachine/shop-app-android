package net.bkmachine.shopapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import net.bkmachine.shopapp.ui.theme.Purple40
import net.bkmachine.shopapp.ui.theme.Purple80

@Composable
fun NavigationTabs(headerText: MutableState<String>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    )
    {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            HomeButton(
                value = "Pick Tool",
                headerText = headerText,
                modifier = Modifier.weight(1f),
                onClick = {
                    headerText.value = "Pick Tool"
                }
            )
            HomeButton(
                value = "Re-Stock",
                headerText = headerText,
                modifier = Modifier.weight(1f),
                onClick = {
                    headerText.value = "Re-Stock"
                }
            )
            HomeButton(
                value = "Status",
                headerText = headerText,
                modifier = Modifier.weight(1f),
                onClick = {
                    headerText.value = "Status"
                }
            )
        }
    }
}

@Composable
fun HomeButton(
    value: String,
    headerText: MutableState<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color: Color = if (value == headerText.value) {
        Purple80
    } else {
        Purple40
    }
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RectangleShape,
        modifier = modifier.height(50.dp),
        onClick = onClick
    ) {
        Text(value)
    }
}
