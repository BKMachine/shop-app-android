package net.bkmachine.shopapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.bkmachine.shopapp.ui.theme.Background
import net.bkmachine.shopapp.ui.theme.DarkGreen
import net.bkmachine.shopapp.ui.theme.DarkRed

const val defaultMessage = "Ready to scan..."

class AppViewModel : ViewModel() {
    var headerText by mutableStateOf("Pick Tool")
        private set
    var userMessage by mutableStateOf(defaultMessage)
        private set

    var resultMessage by mutableStateOf("")
        private set

    var backgroundColor by mutableStateOf(Background)

    fun setHeader(text: String) {
        headerText = text
    }

    fun setMessage(message: String?) {
        userMessage = if (message.isNullOrEmpty()) {
            defaultMessage
        } else {
            message
        }
    }

    fun setResult(message: String?) {
        resultMessage = if (message.isNullOrEmpty()) {
            ""
        } else {
            message
        }
    }

    fun handleScan(scanCode: String) {
        Log.d("SCAN_CODE", scanCode)

        when (headerText) {
            "Pick Tool" -> pickTool(scanCode)
        }
    }
}

var job: Job? = null;

@OptIn(DelicateCoroutinesApi::class)
fun pickTool(scanCode: String) {
    if (job?.isActive == true) job?.cancel("A new scan was made.")
    job = GlobalScope.launch {
        MyViewModel.setMessage("Processing...")
        MyViewModel.setResult(null)
        val client = HttpClient(Android) {
            install(Logging) {
                level = LogLevel.INFO
            }
            install(ContentNegotiation) {
                json()
            }
        }
        val body = ToolPickRequest(scanCode)
        val response: HttpResponse = client.put(HttpRoutes.PICK_TOOL) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        println(response)
        MyViewModel.backgroundColor = DarkRed
        when (response.status.value) {
            200 -> {
                MyViewModel.backgroundColor = DarkGreen
                MyViewModel.setResult("$scanCode\nPicked successfully.")
            }

            400 -> {
                MyViewModel.setResult("$scanCode\nNo stock remaining.")
            }

            404 -> {
                MyViewModel.setResult("$scanCode\nTool not found.")
            }

            else -> {
                MyViewModel.setResult(response.status.toString())
            }
        }
        client.close()
        delay(500)
        MyViewModel.setMessage(null)
        MyViewModel.backgroundColor = Background
        delay(5000)
        MyViewModel.setResult(null)
    }
}

val MyViewModel = AppViewModel()