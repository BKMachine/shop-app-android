package net.bkmachine.shopapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.bkmachine.shopapp.data.remote.ToolsService
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest
import net.bkmachine.shopapp.data.remote.dto.ToolResponse
import net.bkmachine.shopapp.ui.theme.Background
import net.bkmachine.shopapp.ui.theme.DarkGreen
import net.bkmachine.shopapp.ui.theme.DarkRed

const val defaultMessage = "Ready to scan..."

class AppViewModel : ViewModel() {
    private val client = ToolsService.create()
    private var job: Job? = null
    
    var isUpdating by mutableStateOf(false)
        private set

    var headerText by mutableStateOf("Pick Tool")
        private set
    var userMessage by mutableStateOf(defaultMessage)
        private set
    var resultMessage by mutableStateOf("")
        private set
    var backgroundColor by mutableStateOf(Background)
        private set
    var showStockTextField by mutableStateOf(false)
        private set
    private var lastTool by mutableStateOf<ToolResponse?>(null)
    var mTextField by mutableStateOf(
        TextFieldValue(text = "", selection = TextRange(0))
    )
        private set

    fun setHeader(text: String) {
        headerText = text
    }

    fun setMessage(message: String?) {
        userMessage = message ?: defaultMessage
    }

    fun setResult(message: String?) {
        resultMessage = message ?: ""
    }

    fun setBackground(color: Color) {
        backgroundColor = color
    }

    fun setShowStock(bool: Boolean) {
        showStockTextField = bool
    }

    fun setTool(tool: ToolResponse?) {
        lastTool = tool
    }

    fun setTextField(textFieldValue: TextFieldValue) {
        mTextField = textFieldValue
    }

    fun setTextField(text: String) {
        mTextField = TextFieldValue(text = text, selection = TextRange(text.length))
    }

    fun handleScan(scanCode: String) {
        Log.d("AppViewModel", "Handling scan: $scanCode in mode: $headerText")
        when (headerText) {
            "Pick Tool" -> pickTool(scanCode)
            "Re-Stock" -> reStockTool(scanCode)
            "Info" -> toolInfo(scanCode)
        }
    }

    private fun handleError(t: Throwable, functionName: String) {
        Log.e("AppViewModel", "Error in $functionName: ${t.message}", t)
        setBackground(DarkRed)
        setResult("Error: ${t.localizedMessage ?: t.toString()}")
        setMessage("Connection failed")
    }

    private fun pickTool(scanCode: String) {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                setMessage("Processing...")
                setResult(null)
                val response: HttpResponse = client.pickTool(ToolPickRequest(scanCode))
                
                when (response.status.value) {
                    200 -> {
                        val toolResponse = response.body<ToolResponse>()
                        setBackground(DarkGreen)
                        setResult(formatResultMessage(toolResponse))
                    }
                    400 -> {
                        val toolResponse = response.body<ToolResponse>()
                        setBackground(DarkRed)
                        setResult(formatResultMessage(toolResponse, 400))
                    }
                    404 -> toolNotFound(scanCode)
                    else -> serverErrorMessage(response)
                }
                delay(1000)
                setBackground(Background)
                setMessage(null)
                delay(4000)
                setResult(null)
            } catch (t: Throwable) {
                handleError(t, "pickTool")
            }
        }
    }

    private fun toolInfo(scanCode: String) {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                setMessage("Processing...")
                setResult(null)
                val response: HttpResponse = client.toolInfo(scanCode)
                
                when (response.status.value) {
                    200 -> {
                        val toolResponse = response.body<ToolResponse>()
                        setBackground(DarkGreen)
                        setResult(formatResultMessage(toolResponse))
                    }
                    404 -> toolNotFound(scanCode)
                    else -> serverErrorMessage(response)
                }
                delay(1000)
                setBackground(Background)
                setMessage(null)
            } catch (t: Throwable) {
                handleError(t, "toolInfo")
            }
        }
    }

    private fun reStockTool(scanCode: String) {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                setMessage("Processing...")
                setResult(null)
                setTool(null)
                val response: HttpResponse = client.toolInfo(scanCode)
                
                when (response.status.value) {
                    200 -> {
                        val toolResponse = response.body<ToolResponse>()
                        setBackground(DarkGreen)
                        setResult(formatResultMessage(toolResponse))
                        setMessage(" ")
                        setShowStock(true)
                    }
                    404 -> toolNotFound(scanCode)
                    else -> serverErrorMessage(response)
                }
                delay(1000)
                setBackground(Background)
            } catch (t: Throwable) {
                handleError(t, "reStockTool")
            }
        }
    }

    fun updateStock(amount: String) {
        val num = amount.toIntOrNull() ?: run {
            setMessage("Not a number.")
            return
        }
        if (num == 0 || isUpdating) return
        
        val t = lastTool ?: return
        if (t.stock + num < 0) {
            setMessage("Not enough stock.")
            return
        }

        job?.cancel()
        job = viewModelScope.launch {
            try {
                isUpdating = true
                setMessage("Processing...")
                val response: HttpResponse = client.updateTool(t._id, num)
                isUpdating = false

                when (response.status.value) {
                    200 -> {
                        val toolResponse = response.body<ToolResponse>()
                        setBackground(DarkGreen)
                        setResult(formatResultMessage(toolResponse))
                        setTextField("")
                        setShowStock(false)
                    }
                    404 -> toolNotFound("")
                    else -> serverErrorMessage(response)
                }
                delay(1000)
                setBackground(Background)
                setMessage(null)
                delay(4000)
                setResult(null)
            } catch (t: Throwable) {
                isUpdating = false
                handleError(t, "updateTool")
            }
        }
    }

    private fun formatResultMessage(tool: ToolResponse, status: Int = 200): String {
        setTool(tool)
        var text = "${tool.item ?: "Unknown"}\n${tool.description}"
        if (status == 200) {
            tool.location?.let {
                text += "\n$it"
                tool.position?.let { pos -> text += " - $pos" }
            }
            text += "\n${tool.stock} in stock."
        } else if (status == 400) {
            text += "\nNo stock remaining."
        }
        if (tool.onOrder) text += "\nOrder placed."
        else if (tool.autoReorder && tool.stock <= tool.reorderThreshold) text += "\nFlagged for re-order."
        return text
    }

    private fun toolNotFound(scanCode: String) {
        setBackground(DarkRed)
        setResult(if (scanCode.isBlank()) "Tool not found." else "$scanCode\nTool not found.")
    }

    private fun serverErrorMessage(response: HttpResponse) {
        setBackground(DarkRed)
        setResult("Server error: ${response.status}")
    }
}

val MyViewModel = AppViewModel()
