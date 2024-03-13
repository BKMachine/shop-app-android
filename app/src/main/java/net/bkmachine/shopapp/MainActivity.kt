package net.bkmachine.shopapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.bkmachine.shopapp.ui.theme.ShopAppTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            BottomAppBar(
                                actions = {
                                    IconButton(onClick = {}) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Pick Tool"
                                        )
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(
                                            imageVector = Icons.Default.FavoriteBorder,
                                            contentDescription = "Re-Stock"
                                        )
                                    }
                                    IconButton(onClick = {}) {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Details"
                                        )
                                    }
                                }
                            )
                        }
                    )
                    {

                    }
                    /*Image(
                        painter = painterResource(id = R.drawable.bk_logo),
                        contentDescription = "BK Machine Logo",
                        modifier = Modifier.offset(y = (-65).dp)
                    )
                    NavigationTabs()*/
                }
            }
        }
    }
}