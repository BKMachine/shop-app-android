package net.bkmachine.shopapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.bkmachine.shopapp.ui.theme.ShopAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AppViewModel>()

    private val client: ApiService.create();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //registerReceiver(receiver, IntentFilter(Intent.ACTION_SEND))

        setContent {
            ShopAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
                            modifier = Modifier.fillMaxWidth(0.7f)
                        )
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxHeight()
                        )
                        {
                            Text(
                                text = "Ready to scan...",
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            )
                            Button(onClick = {
                                pickTool("62147")
                            }) {
                                Text("Test")
                            }
                            NavigationTabs(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //unregisterReceiver(receiver);
    }
}

fun pickTool(scanCode: String) {
    Log.d("DEBUG", scanCode)

    // val header: HashMap<String, String> = hashMapOf()

    // Fuel.get(url).header(header).body.string()
}

const val QR_ACTION: String = "scan.rcv.message"
const val QR_EXTRA: String = "barcodeData"

private val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("DEBUG", intent?.action.toString())
        /*try {
        // Timber.d("Get intent ${intent.action}")
        if (QR_ACTION == intent.action) {
            if (intent.hasExtra(QR_EXTRA)) {
                val code = intent.getStringExtra(QR_EXTRA)
                // Timber.d("New QR code $code")
                pickTool(code.toString())
            }
        }
    } catch (t: Throwable) {
        // handle errors
    }*/
    }
}
