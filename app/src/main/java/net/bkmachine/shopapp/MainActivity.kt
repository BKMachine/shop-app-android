package net.bkmachine.shopapp

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.bkmachine.shopapp.ui.theme.ShopAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel = MyViewModel
    private val barcodeScannerReceiver = BarcodeScannerReceiver()

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Global exception handler to provide feedback on crash
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("ShopAppCrash", "Uncaught exception in thread ${thread.name}", throwable)
            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    "Application Error: ${throwable.localizedMessage ?: throwable.toString()}",
                    Toast.LENGTH_LONG
                ).show()
            }
            // Give time for Toast to show (though it might still be killed quickly)
            try { Thread.sleep(2000) } catch (e: InterruptedException) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        startReceivers()

        setContent {
            ShopAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(), color = viewModel.backgroundColor
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
                                .padding(top = 20.dp)
                        )
                        if (!viewModel.showStockTextField || viewModel.headerText != "Re-Stock") {
                            Text(
                                text = viewModel.userMessage,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            val focusRequester = remember { FocusRequester() }
                            if (viewModel.showStockTextField && viewModel.headerText == "Re-Stock") {
                                LaunchedEffect(viewModel.showStockTextField) {
                                    if (viewModel.showStockTextField) {
                                        try {
                                            focusRequester.requestFocus()
                                        } catch (e: Exception) {
                                            Log.e("MainActivity", "Focus request failed", e)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(0.dp))
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                ) {
                                    CompositionLocalProvider(
                                        LocalTextInputService provides null
                                    ) {
                                        Text(
                                            text = viewModel.userMessage,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                        TextField(
                                            value = MyViewModel.mTextField,
                                            label = {
                                                Text(
                                                    text = "Stock Adjustment Amount",
                                                    modifier = Modifier.fillMaxWidth(),
                                                    textAlign = TextAlign.Center
                                                )
                                            },
                                            singleLine = true,
                                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                                            modifier = Modifier
                                                .fillMaxWidth(0.6f)
                                                .focusRequester(focusRequester)
                                                .onKeyEvent {
                                                    if (it.nativeKeyEvent.keyCode == 66) {
                                                        viewModel.updateStock(MyViewModel.mTextField.text)
                                                    }
                                                    false
                                                },
                                            onValueChange = {
                                                MyViewModel.setTextField(it)
                                            })
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (viewModel.mTextField.text.isBlank()) return@Button
                                                val num = viewModel.mTextField.text.toIntOrNull()
                                                if (num == null) {
                                                    viewModel.setMessage("Not a number.")
                                                    return@Button
                                                }
                                                if (num == 0) return@Button
                                                val text = (-num).toString()
                                                viewModel.setTextField(text)
                                                focusRequester.requestFocus()
                                            },
                                            modifier = Modifier
                                        ) {
                                            Text(text = "+/-", modifier = Modifier)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Button(
                                            onClick = { viewModel.updateStock(MyViewModel.mTextField.text) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(text = "Enter")
                                        }
                                    }
                                }
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.bk_logo),
                                    contentDescription = "BK Machine Logo",
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                )
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                Text(
                                    text = viewModel.resultMessage,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.offset(y = (-8).dp)
                                )
                            }
                            NavigationTabs()
                        }
                    }
                }
            }
        }
    }

    private fun startReceivers() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    barcodeScannerReceiver,
                    IntentFilter(barcodeScannerReceiver.QR_ACTION),
                    RECEIVER_EXPORTED
                )
            } else {
                // For older versions, we still might want to specify exported if we know it comes from outside
                // but standard registerReceiver is usually fine.
                registerReceiver(
                    barcodeScannerReceiver, IntentFilter(barcodeScannerReceiver.QR_ACTION)
                )
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Receiver registration failed", e)
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(barcodeScannerReceiver)
        } catch (e: Exception) {
            Log.e("MainActivity", "Receiver unregistration failed", e)
        }
    }

    override fun onResume() {
        super.onResume()
        startReceivers()
    }
}