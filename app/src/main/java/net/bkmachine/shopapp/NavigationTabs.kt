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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import net.bkmachine.shopapp.ui.theme.Purple40
import net.bkmachine.shopapp.ui.theme.Purple80

@Composable
fun NavigationTabs(viewModel: AppViewModel, modifier: Modifier = Modifier) {
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
                viewModel = viewModel,
                icon = Icons.Outlined.ShoppingCart,
                modifier = Modifier.weight(1f),
            )
            HomeButton(
                text = "Re-Stock",
                viewModel = viewModel,
                icon = Icons.Outlined.Refresh,
                modifier = Modifier.weight(1f),
            )
            HomeButton(
                text = "Info",
                viewModel = viewModel,
                icon = Icons.Outlined.Info,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun HomeButton(
    text: String,
    viewModel: AppViewModel,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val color: Color = if (text == viewModel.headerText) {
        Purple80
    } else {
        Purple40
    }
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RectangleShape,
        modifier = modifier,
        onClick = {
            viewModel.setHeader(text)
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
