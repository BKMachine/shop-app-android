package net.bkmachine.shopapp

import android.content.Context
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.bkmachine.shopapp.ui.MainSelectionScreen
import net.bkmachine.shopapp.ui.PartsScreen
import net.bkmachine.shopapp.ui.ToolsScreen
import net.bkmachine.shopapp.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel = MyViewModel
    private val barcodeScannerReceiver = BarcodeScannerReceiver()
    private var isReceiverRegistered = false

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.init(this)

        // Global exception handler to provide feedback on crash
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("ShopAppCrash", "Uncaught exception in thread ${thread.name}", throwable)
            
            try {
                getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("last_mode", AppMode.MAIN.name)
                    .commit()
            } catch (e: Exception) {
                Log.e("ShopAppCrash", "Failed to clear last_mode", e)
            }

            runOnUiThread {
                Toast.makeText(
                    applicationContext,
                    "Application Error: ${throwable.localizedMessage ?: throwable.toString()}",
                    Toast.LENGTH_LONG
                ).show()
            }
            try { Thread.sleep(2000) } catch (e: InterruptedException) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }

        setContent {
            // Re-register receiver when switching modes
            LaunchedEffect(viewModel.appMode) {
                if (viewModel.appMode != AppMode.IMAGES) {
                    stopReceivers()
                    startReceivers()
                }
            }

            val primaryColor = when(viewModel.appMode) {
                AppMode.MAIN -> MainOrangePrimary
                AppMode.TOOLS -> ToolPurplePrimary
                AppMode.PARTS -> PartGreenPrimary
                else -> MainOrangePrimary
            }

            ShopAppTheme(darkTheme = viewModel.isDarkTheme, primaryOverride = primaryColor) {
                val flashType = viewModel.backgroundFlash
                val backgroundColor by animateColorAsState(
                    targetValue = when (flashType) {
                        MainViewModel.FlashType.SUCCESS -> Color.Green.copy(alpha = 0.5f)
                        MainViewModel.FlashType.FAILURE -> Color.Red.copy(alpha = 0.5f)
                        null -> viewModel.backgroundColor
                    },
                    animationSpec = tween(durationMillis = 300),
                    label = "FlashBackground"
                )

                Surface(
                    modifier = Modifier.fillMaxSize(), color = backgroundColor
                ) {
                    when {
                        viewModel.isCheckingRegistration -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        viewModel.networkErrorMessage != null -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Connection Error",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = viewModel.networkErrorMessage!!,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(onClick = { 
                                    viewModel.checkDeviceStatus()
                                }) {
                                    Text("Retry Connection")
                                }
                            }
                        }
                        viewModel.showRegistrationScreen -> {
                            RegistrationScreen(viewModel)
                        }
                        viewModel.isBlocked -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Device Access Restricted",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = viewModel.resultMessage.ifBlank { "This device is not approved for access." },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                        else -> {
                            if (viewModel.appMode != AppMode.MAIN) {
                                BackHandler {
                                    viewModel.selectAppMode(AppMode.MAIN, this)
                                }
                            }

                            MainContent(viewModel, onToggleTheme = { viewModel.toggleTheme(this@MainActivity) })
                        }
                    }
                }
            }
        }
    }

    private fun startReceivers() {
        if (isReceiverRegistered) return
        try {
            val filter = IntentFilter(barcodeScannerReceiver.QR_ACTION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(barcodeScannerReceiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                registerReceiver(barcodeScannerReceiver, filter)
            }
            isReceiverRegistered = true
            Log.d("MainActivity", "Scanner receiver registered")
        } catch (e: Exception) {
            Log.e("MainActivity", "Receiver registration failed", e)
        }
    }

    private fun stopReceivers() {
        if (!isReceiverRegistered) return
        try {
            unregisterReceiver(barcodeScannerReceiver)
            isReceiverRegistered = false
            Log.d("MainActivity", "Scanner receiver unregistered")
        } catch (e: Exception) {
            Log.e("MainActivity", "Receiver unregistration failed", e)
        }
    }

    override fun onStop() {
        super.onStop()
        stopReceivers()
    }

    override fun onResume() {
        super.onResume()
        // Refresh receiver
        stopReceivers()
        startReceivers()
        
        // Re-verify registration status via the /me endpoint
        viewModel.checkDeviceStatus()
    }
    
    override fun onDestroy() {
        stopReceivers()
        super.onDestroy()
    }
}

@Composable
fun MainContent(viewModel: MainViewModel, onToggleTheme: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (viewModel.appMode == AppMode.MAIN) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bk_logo),
                    contentDescription = "BK Machine Logo",
                    modifier = Modifier.height(64.dp)
                )
                
                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (viewModel.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = if (viewModel.isDarkTheme) Color.White else Color.Black
                    )
                }
            }
        }
        
        Box(modifier = Modifier.weight(1f)) {
            when (viewModel.appMode) {
                AppMode.MAIN -> MainSelectionScreen(viewModel)
                AppMode.TOOLS -> ToolsScreen(viewModel)
                AppMode.PARTS -> PartsScreen(viewModel)
                else -> MainSelectionScreen(viewModel)
            }
        }
    }
}

@Composable
fun RegistrationScreen(viewModel: MainViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp),
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bk_logo),
                    contentDescription = "BK Machine Logo",
                    modifier = Modifier.height(48.dp)
                )
                
                Text(
                    text = "Device Registration",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "This device is not registered. Please enter a display name to register.",
                    textAlign = TextAlign.Center
                )
                
                TextField(
                    value = viewModel.registrationDisplayName,
                    onValueChange = { viewModel.registrationDisplayName = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("BK-SCAN-01") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.registrationError != null
                )
                
                if (viewModel.registrationError != null) {
                    Text(
                        text = viewModel.registrationError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Button(
                    onClick = { viewModel.registerDevice() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isRegistering
                ) {
                    if (viewModel.isRegistering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Register Device")
                    }
                }
            }
        }
    }
}
