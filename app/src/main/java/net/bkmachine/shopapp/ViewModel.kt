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
import kotlinx.coroutines.runBlocking

const val defaultMessage = "Ready to scan..."

class AppViewModel : ViewModel() {
    var headerText by mutableStateOf("Pick Tool")
        private set
    var userMessage by mutableStateOf(defaultMessage)
        private set

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

    fun handleScan(scanCode: String) {
        Log.d("SCAN_CODE", scanCode)
        setMessage(scanCode)

        when (headerText) {
            "Pick Tool" -> pickTool(scanCode)
        }
    }
}

fun pickTool(scanCode: String) = runBlocking {
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
    println(response.status)
    MyViewModel.setMessage(response.status.toString())
    client.close()
}

val MyViewModel = AppViewModel()