package net.bkmachine.shopapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.bkmachine.shopapp.ui.theme.ShopAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Home()
                }
            }
        }
    }
}

@Composable
fun Home(modifier: Modifier = Modifier) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
        ) {

            Button(
                onClick = { ComponentActivity().setContent { PickTool()}
            }) {
                Text("Pick Tool")
            }
            Button(onClick = { /*TODO*/ }) {
                Text("Re-stock")
            }
            Button(onClick = { /*TODO*/ }) {
                Text("Status")
            }
        }
}

@Preview(showBackground = true)
@Composable
fun HomePreview() {
    ShopAppTheme {
        Home()
    }
}

@Composable
fun PickTool(modifier: Modifier = Modifier) {
    Surface {
        Text("Ready to scan...")
    }
}