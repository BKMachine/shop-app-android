package net.bkmachine.shopapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NavigationTabs(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
    )
    {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            HomeButton(
                text = "Pick Tool",
                icon = Icons.Outlined.ShoppingCart,
                modifier = Modifier.weight(1f),
            )
            HomeButton(
                text = "Re-Stock",
                icon = Icons.Outlined.Refresh,
                modifier = Modifier.weight(1f),
            )
            HomeButton(
                text = "Info",
                icon = Icons.Outlined.Info,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun HomeButton(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val isSelected = text == MyViewModel.toolHeaderText
    
    // Using theme secondary for unselected state to match the "Tool Stock" screenshot style
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.secondary
    }
    
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RectangleShape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
        modifier = modifier,
        onClick = {
            MyViewModel.setToolTab(text)
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {
            Icon(icon, contentDescription = null)
            Text(text, fontSize = 12.sp)
        }
    }
}
