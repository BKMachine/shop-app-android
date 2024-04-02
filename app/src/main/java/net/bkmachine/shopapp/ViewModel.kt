package net.bkmachine.shopapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    var showStockTextField by mutableStateOf(false)
        private set

    var tool by mutableStateOf<ToolResponse?>(null)
    var mText by mutableStateOf("")

    fun setShowStock(bool: Boolean) {
        showStockTextField = bool
    }

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
            "Re-Stock" -> stockTool(scanCode)
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
        val t = tool?.copy()
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
                val description = toolResponse.description
                val stock = toolResponse.stock
                val onReorder = toolResponse.onOrder
                val autoReorder = toolResponse.autoReorder
                val reorderThreshold = toolResponse.reorderThreshold
                MyViewModel.backgroundColor = DarkGreen
                var text = "$scanCode\n$description\nPicked successfully.\n$stock remaining."
                if (onReorder) text += "\nOrder placed."
                else if (autoReorder && stock <= reorderThreshold) text += "\nFlagged for re-order."
                MyViewModel.setResult(text)
            }

            400 -> {
                val toolResponse = response.body<ToolResponse>();
                val description = toolResponse.description
                val stock = toolResponse.stock
                val onReorder = toolResponse.onOrder
                val autoReorder = toolResponse.autoReorder
                val reorderThreshold = toolResponse.reorderThreshold
                MyViewModel.backgroundColor = DarkRed
                var text = "$scanCode\n$description\nNo stock remaining."
                if (onReorder) text += "\nOrder placed."
                else if (autoReorder && stock <= reorderThreshold) text += "\nFlagged for re-order."
                MyViewModel.setResult(text)
            }

            404 -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult("$scanCode\nTool not found.")
            }

            else -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult(response.status.toString())
            }
        }
        delay(1000)
        MyViewModel.setMessage(null)
        MyViewModel.backgroundColor = Background
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
                val description = toolResponse.description
                val stock = toolResponse.stock
                val onReorder = toolResponse.onOrder
                val autoReorder = toolResponse.autoReorder
                val reorderThreshold = toolResponse.reorderThreshold
                val location = toolResponse.location
                val position = toolResponse.position
                MyViewModel.backgroundColor = DarkGreen
                var text = "$scanCode\n$description"
                if (location != null) {
                    text += "\n$location"
                    if (position !== null) {
                        text += " - $position"
                    }
                }
                text += "\n$stock in stock."
                if (onReorder) text += "\nOrder placed."
                else if (autoReorder && stock <= reorderThreshold) text += "\nFlagged for re-order."
                MyViewModel.setResult(text)
            }

            404 -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult("$scanCode\nTool not found.")
            }

            else -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult(response.status.toString())
            }
        }
        delay(1000)
        MyViewModel.setMessage(null)
        MyViewModel.backgroundColor = Background
    }
}


@OptIn(DelicateCoroutinesApi::class)
fun stockTool(scanCode: String) {
    if (job?.isActive == true) job?.cancel("A new scan was made.")
    job = GlobalScope.launch {
        MyViewModel.setMessage("Processing...")
        MyViewModel.setResult(null)
        MyViewModel.tool = null
        val response: HttpResponse = client.toolInfo(scanCode)
        println(response)

        when (response.status.value) {
            200 -> {
                val toolResponse = response.body<ToolResponse>();
                val item = toolResponse.item
                val description = toolResponse.description
                val stock = toolResponse.stock
                val onReorder = toolResponse.onOrder
                val autoReorder = toolResponse.autoReorder
                val reorderThreshold = toolResponse.reorderThreshold
                val location = toolResponse.location
                val position = toolResponse.position
                MyViewModel.backgroundColor = DarkGreen
                MyViewModel.tool = toolResponse
                var text = "$item\n$description"
                if (location != null) {
                    text += "\n$location"
                    if (position !== null) {
                        text += " - $position"
                    }
                }
                text += "\n$stock in stock."
                if (onReorder) text += "\nOrder placed."
                else if (autoReorder && stock <= reorderThreshold) text += "\nFlagged for re-order."
                MyViewModel.setResult(text)
                MyViewModel.setShowStock(true)
                MyViewModel.setMessage("Enter amount to adjust stock:")
            }

            404 -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult("$scanCode\nTool not found.")
                MyViewModel.setMessage(null)
            }

            else -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult(response.status.toString())
                MyViewModel.setMessage(null)
            }
        }
        delay(1000)
        MyViewModel.backgroundColor = Background
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
                val item = toolResponse.item
                val description = toolResponse.description
                val stock = toolResponse.stock
                val onReorder = toolResponse.onOrder
                val autoReorder = toolResponse.autoReorder
                val reorderThreshold = toolResponse.reorderThreshold
                val location = toolResponse.location
                val position = toolResponse.position
                MyViewModel.backgroundColor = DarkGreen
                MyViewModel.tool = toolResponse
                var text = "$item\n$description"
                if (location != null) {
                    text += "\n$location"
                    if (position !== null) {
                        text += " - $position"
                    }
                }
                text += "\n$stock in stock."
                if (onReorder) text += "\nOrder placed."
                else if (autoReorder && stock <= reorderThreshold) text += "\nFlagged for re-order."
                MyViewModel.setResult(text)
                MyViewModel.mText = ""
                MyViewModel.setMessage(null)
                MyViewModel.setShowStock(false)
            }

            404 -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult("Tool not found.")
                MyViewModel.setMessage(null)
            }

            else -> {
                MyViewModel.backgroundColor = DarkRed
                MyViewModel.setResult(response.status.toString())
                MyViewModel.setMessage(null)
            }
        }
        delay(1000)
        MyViewModel.backgroundColor = Background
        delay(4000)
        MyViewModel.setResult(null)
    }
}

val MyViewModel = AppViewModel()
