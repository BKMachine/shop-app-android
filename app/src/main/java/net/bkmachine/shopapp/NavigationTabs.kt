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
                onClick = {
                    MyViewModel.setMessage(null)
                },
                modifier = Modifier.weight(1f),
            )
            HomeButton(
                text = "Re-Stock",
                icon = Icons.Outlined.Refresh,
                onClick = {
                    MyViewModel.setMessage("Not yet implemented.")
                },
                modifier = Modifier.weight(1f),
            )
            HomeButton(
                text = "Info",
                icon = Icons.Outlined.Info,
                onClick = {
                    MyViewModel.setMessage("Not yet implemented.")
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun HomeButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color: Color = if (text == MyViewModel.headerText) {
        Purple80
    } else {
        Purple40
    }
    Button(
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RectangleShape,
        modifier = modifier,
        onClick = {
            MyViewModel.setHeader(text)
            MyViewModel.setMessage(null)
            MyViewModel.setResult(null)
            onClick()
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
