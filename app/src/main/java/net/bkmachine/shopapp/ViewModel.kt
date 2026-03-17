package net.bkmachine.shopapp

import android.content.Context
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
import net.bkmachine.shopapp.data.local.DeviceIdentityManager
import net.bkmachine.shopapp.data.remote.ToolsService
import net.bkmachine.shopapp.data.remote.dto.ApiErrorResponse
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationRequest
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest
import net.bkmachine.shopapp.data.remote.dto.ToolResponse
import net.bkmachine.shopapp.ui.theme.Background
import net.bkmachine.shopapp.ui.theme.DarkGreen
import net.bkmachine.shopapp.ui.theme.DarkRed

const val defaultMessage = "Ready to scan..."
private const val IDLE_TIMEOUT_MS = 300000L // 5 minutes

class AppViewModel : ViewModel() {
    private var _client: ToolsService? = null
    private val client: ToolsService get() = _client!!
    private var _deviceIdentityManager: DeviceIdentityManager? = null
    private val deviceIdentityManager: DeviceIdentityManager get() = _deviceIdentityManager!!

    private var job: Job? = null
    private var idleJob: Job? = null
    
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

    // Device Registration State
    var showRegistrationScreen by mutableStateOf(false)
        private set
    var registrationDisplayName by mutableStateOf("")
    var registrationError by mutableStateOf<String?>(null)
    var isRegistering by mutableStateOf(false)
        private set
    var isBlocked by mutableStateOf(false)
        private set

    private var pendingAction: (suspend () -> Unit)? = null

    fun init(context: Context) {
        if (_deviceIdentityManager == null) {
            _deviceIdentityManager = DeviceIdentityManager(context.applicationContext)
            _client = ToolsService.create(deviceIdentityManager)
        }
    }

    init {
        resetIdleTimer()
    }

    fun resetIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(IDLE_TIMEOUT_MS)
            if (headerText != "Pick Tool") {
                Log.d("AppViewModel", "Idle timeout reached, resetting to Pick Tool")
                resetToDefault()
            }
        }
    }

    fun resetToDefault() {
        headerText = "Pick Tool"
        setMessage(null)
        setResult(null)
        setShowStock(false)
        setBackground(Background)
        setTextField("")
        setTool(null)
    }

    fun selectTab(text: String) {
        headerText = text
        setMessage(null)
        setResult(null)
        setShowStock(false)
        setBackground(Background)
        setTextField("")
        resetIdleTimer()
    }

    fun setHeader(text: String) {
        headerText = text
        resetIdleTimer()
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
        resetIdleTimer()
    }

    fun setTextField(text: String) {
        mTextField = TextFieldValue(text = text, selection = TextRange(text.length))
        resetIdleTimer()
    }

    fun handleScan(scanCode: String) {
        Log.d("AppViewModel", "Handling scan: $scanCode in mode: $headerText")
        resetIdleTimer()
        when (headerText) {
            "Pick Tool" -> pickTool(scanCode)
            "Re-Stock" -> reStockTool(scanCode)
            "Info" -> toolInfo(scanCode)
        }
    }

    private suspend fun handleResponse(
        response: HttpResponse, 
        onSuccess: suspend () -> Unit,
        retryAction: suspend () -> Unit
    ) {
        when (response.status.value) {
            in 200..299 -> onSuccess()
            403 -> {
                val errorBody = response.body<ApiErrorResponse>()
                when (errorBody.error) {
                    "device_not_registered" -> {
                        pendingAction = retryAction
                        showRegistrationScreen = true
                    }
                    "device_blocked", "device_not_approved" -> {
                        isBlocked = true
                        setBackground(DarkRed)
                        setResult(errorBody.message)
                    }
                    else -> serverErrorMessage(response)
                }
            }
            404 -> {
                // Handle 404 separately in each function if needed, 
                // but for now we'll let the caller handle it if it's not a common error.
                serverErrorMessage(response)
            }
            else -> serverErrorMessage(response)
        }
    }

    fun registerDevice() {
        if (registrationDisplayName.isBlank()) {
            registrationError = "Display name cannot be empty"
            return
        }
        viewModelScope.launch {
            isRegistering = true
            registrationError = null
            try {
                val request = DeviceRegistrationRequest(
                    deviceId = deviceIdentityManager.getOrCreateDeviceId(),
                    displayName = registrationDisplayName,
                    deviceType = "android"
                )
                val response = client.registerDevice(request)
                if (response.status.value == 201 || response.status.value == 409) {
                    deviceIdentityManager.saveDisplayName(registrationDisplayName)
                    showRegistrationScreen = false
                    pendingAction?.invoke()
                    pendingAction = null
                } else {
                    val errorBody = response.body<ApiErrorResponse>()
                    registrationError = errorBody.message
                }
            } catch (t: Throwable) {
                registrationError = "Registration failed: ${t.localizedMessage}"
            } finally {
                isRegistering = false
            }
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
                
                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    setBackground(DarkGreen)
                    setResult(formatResultMessage(toolResponse))
                }, retryAction = { pickTool(scanCode) })

                if (response.status.value == 400) {
                     val toolResponse = response.body<ToolResponse>()
                     setBackground(DarkRed)
                     setResult(formatResultMessage(toolResponse, 400))
                } else if (response.status.value == 404) {
                    toolNotFound(scanCode)
                }

                delay(1000)
                if (!isBlocked) setBackground(Background)
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
                
                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    setBackground(DarkGreen)
                    setResult(formatResultMessage(toolResponse))
                }, retryAction = { toolInfo(scanCode) })

                if (response.status.value == 404) {
                    toolNotFound(scanCode)
                }

                delay(1000)
                if (!isBlocked) setBackground(Background)
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
                
                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    setBackground(DarkGreen)
                    setResult(formatResultMessage(toolResponse))
                    setMessage(" ")
                    setShowStock(true)
                }, retryAction = { reStockTool(scanCode) })

                if (response.status.value == 404) {
                    toolNotFound(scanCode)
                }

                delay(1000)
                if (!isBlocked) setBackground(Background)
            } catch (t: Throwable) {
                handleError(t, "reStockTool")
            }
        }
    }

    fun updateStock(amount: String) {
        resetIdleTimer()
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

                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    setBackground(DarkGreen)
                    setResult(formatResultMessage(toolResponse))
                    setTextField("")
                    setShowStock(false)
                }, retryAction = { updateStock(amount) })

                if (response.status.value == 404) {
                    toolNotFound("")
                }

                delay(1000)
                if (!isBlocked) setBackground(Background)
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
