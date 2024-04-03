package net.bkmachine.shopapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.bkmachine.shopapp.data.remote.ToolsService
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest
import net.bkmachine.shopapp.data.remote.dto.ToolResponse
import net.bkmachine.shopapp.ui.theme.Background
import net.bkmachine.shopapp.ui.theme.DarkGreen
import net.bkmachine.shopapp.ui.theme.DarkRed

const val defaultMessage = "Ready to scan..."
private val client = ToolsService.create();
var isUpdating by mutableStateOf(false)

class AppViewModel : ViewModel() {
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
        TextFieldValue(
            text = "",
            selection = TextRange(0)
        )
    )
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

    fun setResult(message: String?) {
        resultMessage = if (message.isNullOrEmpty()) {
            ""
        } else {
            message
        }
    }

    fun setBackground(color: Color) {
        backgroundColor = color
    }

    fun setShowStock(bool: Boolean) {
        showStockTextField = bool
    }

    fun setTool(tool: ToolResponse?) {
        lastTool = tool;
    }

    fun setTextField(textFieldValue: TextFieldValue) {
        mTextField = textFieldValue
    }

    fun setTextField(text: String) {
        mTextField = TextFieldValue(
            text = text,
            selection = TextRange(text.length)
        )
    }

    fun handleScan(scanCode: String) {
        Log.d("SCAN_CODE", scanCode)

        when (headerText) {
            "Pick Tool" -> pickTool(scanCode)
            "Re-Stock" -> reStockTool(scanCode)
            "Info" -> toolInfo(scanCode)
        }
    }

    fun updateStock(amount: String) {
        if (amount.isBlank()) {
            Log.d("Stock Amount", "Blank String")
            return;
        }
        val num: Int
        try {
            num = Integer.parseInt(amount)
        } catch (e: NumberFormatException) {
            setMessage("Not a number.")
            return;
        }
        if (num == 0) {
            Log.d("Stock Amount", "0")
            return;
        }
        if (isUpdating) return
        val t = lastTool?.copy()
        if (t != null) {
            val newStock = t.stock + num;
            if (newStock < 0) {
                setMessage("Not enough stock.")
                return
            }
            updateTool(t, num)
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
        val body = ToolPickRequest(scanCode)
        val response: HttpResponse = client.pickTool(body)
        println(response)

        when (response.status.value) {
            200 -> {
                val toolResponse = response.body<ToolResponse>();
                MyViewModel.setBackground(DarkGreen)
                MyViewModel.setResult(formatResultMessage(toolResponse))
            }

            400 -> {
                val toolResponse = response.body<ToolResponse>();
                MyViewModel.setBackground(DarkRed)
                MyViewModel.setResult(formatResultMessage(toolResponse, 400))
            }

            404 -> {
                toolNotFound(scanCode)
            }

            else -> {
                serverErrorMessage(response)
            }
        }
        delay(1000)
        MyViewModel.setBackground(Background)
        MyViewModel.setMessage(null)
        delay(4000)
        MyViewModel.setResult(null)
    }
}


@OptIn(DelicateCoroutinesApi::class)
fun toolInfo(scanCode: String) {
    if (job?.isActive == true) job?.cancel("A new scan was made.")
    job = GlobalScope.launch {
        MyViewModel.setMessage("Processing...")
        MyViewModel.setResult(null)
        val response: HttpResponse = client.toolInfo(scanCode)
        println(response)

        when (response.status.value) {
            200 -> {
                val toolResponse = response.body<ToolResponse>();
                MyViewModel.setBackground(DarkGreen)
                MyViewModel.setResult(formatResultMessage(toolResponse))
            }

            404 -> {
                toolNotFound(scanCode)
            }

            else -> {
                serverErrorMessage(response)
            }
        }
        delay(1000)
        MyViewModel.setBackground(Background)
        MyViewModel.setMessage(null)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun reStockTool(scanCode: String) {
    if (job?.isActive == true) job?.cancel("A new scan was made.")
    job = GlobalScope.launch {
        MyViewModel.setMessage("Processing...")
        MyViewModel.setResult(null)
        MyViewModel.setTool(null)
        val response: HttpResponse = client.toolInfo(scanCode)
        println(response)

        when (response.status.value) {
            200 -> {
                val toolResponse = response.body<ToolResponse>();
                MyViewModel.setBackground(DarkGreen)
                MyViewModel.setResult(formatResultMessage(toolResponse))
                MyViewModel.setMessage(" ")
                MyViewModel.setShowStock(true)
            }

            404 -> {
                toolNotFound(scanCode)
            }

            else -> {
                serverErrorMessage(response)
            }
        }
        delay(1000)
        MyViewModel.setBackground(Background)
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun updateTool(tool: ToolResponse, amount: Int) {
    if (job?.isActive == true) return;
    isUpdating = true;
    job = GlobalScope.launch {
        MyViewModel.setMessage("Processing...")
        isUpdating = false;
        val response: HttpResponse = client.updateTool(tool._id, amount)
        println(response)

        when (response.status.value) {
            200 -> {
                val toolResponse = response.body<ToolResponse>();
                MyViewModel.setBackground(DarkGreen)
                MyViewModel.setResult(formatResultMessage(toolResponse))
                MyViewModel.setTextField("")
                MyViewModel.setShowStock(false)
            }

            404 -> {
                toolNotFound("")
            }

            else -> {
                serverErrorMessage(response)
            }
        }
        delay(1000)
        MyViewModel.setBackground(Background)
        MyViewModel.setMessage(null)
        delay(4000)
        MyViewModel.setResult(null)
    }
}

fun formatResultMessage(tool: ToolResponse, status: Int = 200): String {
    MyViewModel.setTool(tool)
    val item = tool.item
    val description = tool.description
    val stock = tool.stock
    val onReorder = tool.onOrder
    val autoReorder = tool.autoReorder
    val reorderThreshold = tool.reorderThreshold
    val location = tool.location
    val position = tool.position
    var text = "$item\n$description"
    if (status == 200) {

        if (location != null) {
            text += "\n$location"
            if (position !== null) {
                text += " - $position"
            }
        }
        text += "\n$stock in stock."
    } else if (status == 400) {
        text += "\nNo stock remaining."
    }
    if (onReorder) text += "\nOrder placed."
    else if (autoReorder && stock <= reorderThreshold) text += "\nFlagged for re-order."
    return text
}

fun toolNotFound(scanCode: String) {
    MyViewModel.setBackground(DarkRed)
    val text = if (scanCode.isBlank()) "Tool not found." else "$scanCode\nTool not found."
    MyViewModel.setResult(text)
}

fun serverErrorMessage(response: HttpResponse) {
    MyViewModel.setBackground(DarkRed)
    MyViewModel.setResult(response.status.toString())
}

val MyViewModel = AppViewModel()
