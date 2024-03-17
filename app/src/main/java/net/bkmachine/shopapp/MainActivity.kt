package net.bkmachine.shopapp

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.bkmachine.shopapp.ui.theme.ShopAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel = MyViewModel
    private val barcodeScannerReceiver = BarcodeScannerReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                barcodeScannerReceiver,
                IntentFilter(barcodeScannerReceiver.QR_ACTION),
                RECEIVER_EXPORTED
            )
        } else {
            registerReceiver(
                barcodeScannerReceiver,
                IntentFilter(barcodeScannerReceiver.QR_ACTION)
            )
        }

        setContent {
            ShopAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = viewModel.backgroundColor
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = viewModel.headerText,
                            textAlign = TextAlign.Center,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.bk_logo),
                            contentDescription = "BK Machine Logo",
                            modifier = Modifier.fillMaxWidth(0.6f)
                        )
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxHeight()
                        )
                        {
                            Text(
                                text = viewModel.userMessage,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 20.dp)
                            )
                            if (viewModel.resultMessage != "") {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(Color.DarkGray)
                                        .padding(5.dp)
                                )
                                {
                                    Text(
                                        text = viewModel.resultMessage,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                            NavigationTabs()
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(barcodeScannerReceiver)
    }
}