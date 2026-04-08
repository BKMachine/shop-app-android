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
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.bkmachine.shopapp.data.local.DeviceIdentityManager
import net.bkmachine.shopapp.data.remote.ApiService
import net.bkmachine.shopapp.data.remote.HttpRoutes
import net.bkmachine.shopapp.data.remote.dto.ApiErrorResponse
import net.bkmachine.shopapp.data.remote.dto.DeviceInfo
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationRequest
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationResponse
import net.bkmachine.shopapp.data.remote.dto.PartResponse
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest
import net.bkmachine.shopapp.data.remote.dto.ToolResponse
import net.bkmachine.shopapp.ui.theme.Background

enum class AppMode {
    MAIN, TOOLS, PARTS, IMAGES
}

const val defaultMessage = "Ready to scan..."
private const val IDLE_TIMEOUT_MS = 300000L // 5 minutes

class MainViewModel : ViewModel() {
    private var _api: ApiService? = null
    private val api: ApiService get() = _api!!
    private var _deviceIdentityManager: DeviceIdentityManager? = null
    private val deviceIdentityManager: DeviceIdentityManager get() = _deviceIdentityManager!!

    private var job: Job? = null
    private var idleJob: Job? = null
    
    var appMode by mutableStateOf(AppMode.MAIN)
        private set

    // Theme State
    var isDarkTheme by mutableStateOf(true)
        private set

    // Common State
    var backgroundColor by mutableStateOf(Background)
        private set
    var userMessage by mutableStateOf(defaultMessage)
        private set
    var resultMessage by mutableStateOf("")
        private set
    var isUpdating by mutableStateOf(false)
        private set

    // Flash State
    var backgroundFlash by mutableStateOf<FlashType?>(null)
        private set
    enum class FlashType { SUCCESS, FAILURE }

    // Tools State
    var toolHeaderText by mutableStateOf("Pick Tool")
        private set
    var showToolStockTextField by mutableStateOf(false)
        private set
    var lastTool by mutableStateOf<ToolResponse?>(null)
        private set
    var toolTextField by mutableStateOf(TextFieldValue(text = "", selection = TextRange(0)))
        private set

    // Parts State
    var lastPart by mutableStateOf<PartResponse?>(null)
        private set

    // Image Uploader State
    var isUploadingImage by mutableStateOf(false)
        private set
    var imageUploadStatus by mutableStateOf<String?>(null)
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
    
    // Initial Verification State
    var isCheckingRegistration by mutableStateOf(false)
        private set
    var networkErrorMessage by mutableStateOf<String?>(null)
        private set
    var confirmedDisplayName by mutableStateOf<String?>(null)
        private set
    var deviceIdSuffix by mutableStateOf("")
        private set

    private var pendingAction: (suspend () -> Unit)? = null

    fun init(context: Context) {
        if (_deviceIdentityManager == null) {
            _deviceIdentityManager = DeviceIdentityManager(context.applicationContext)
            _api = ApiService.create(deviceIdentityManager!!)
            
            val fullId = deviceIdentityManager.getOrCreateDeviceId()
            deviceIdSuffix = if (fullId.length > 8) fullId.takeLast(8) else fullId

            val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            
            // Load last app mode
            val lastMode = prefs.getString("last_mode", AppMode.MAIN.name)
            appMode = AppMode.valueOf(lastMode ?: AppMode.MAIN.name)

            // Load theme
            isDarkTheme = prefs.getBoolean("is_dark_theme", true)
            updateBackgroundColorForTheme()

            // Check registration via the /me endpoint
            checkDeviceStatus()
        }
    }

    fun checkDeviceStatus() {
        viewModelScope.launch {
            isCheckingRegistration = true
            networkErrorMessage = null
            try {
                val response = api.getDeviceMe()
                
                when (response.status.value) {
                    200 -> {
                        val isJson = response.contentType()?.match(ContentType.Application.Json) == true
                        if (isJson) {
                            try {
                                // Based on user feedback, response is wrapped: { device: { ... } }
                                val regResponse = response.body<DeviceRegistrationResponse>()
                                updateConfirmedName(regResponse.device.displayName)
                                isBlocked = regResponse.device.blocked
                                if (isBlocked) {
                                    resultMessage = "This device is blocked."
                                }
                                showRegistrationScreen = false
                            } catch (e: Exception) {
                                try {
                                    // Fallback if it's not wrapped
                                    val deviceInfo = response.body<DeviceInfo>()
                                    updateConfirmedName(deviceInfo.displayName)
                                    isBlocked = deviceInfo.blocked
                                    showRegistrationScreen = false
                                } catch (e2: Exception) {
                                    Log.e("MainViewModel", "Failed to parse /me response", e)
                                }
                            }
                        }
                    }
                    403, 404 -> {
                        val isJson = response.contentType()?.match(ContentType.Application.Json) == true
                        if (isJson) {
                            val errorBody = response.body<ApiErrorResponse>()
                            if (errorBody.error == "device_blocked" || errorBody.error == "device_not_approved") {
                                isBlocked = true
                                resultMessage = errorBody.message
                            } else {
                                showRegistrationScreen = true
                            }
                        } else {
                            showRegistrationScreen = true
                        }
                    }
                    else -> {
                        networkErrorMessage = "Server error: ${response.status.value}. Ensure you are on the BK LAN."
                    }
                }
            } catch (t: Throwable) {
                Log.e("MainViewModel", "Status check failed", t)
                networkErrorMessage = "Cannot connect to server. Ensure you are on the BK LAN."
            } finally {
                isCheckingRegistration = false
            }
        }
    }

    // Deprecated: verifyRegistration now replaced by checkDeviceStatus
    fun verifyRegistration(displayName: String) {
        checkDeviceStatus()
    }

    private fun updateConfirmedName(newName: String) {
        if (confirmedDisplayName != newName) {
            Log.d("MainViewModel", "Syncing device name from DB: $newName")
            confirmedDisplayName = newName
            deviceIdentityManager.saveDisplayName(newName)
        }
    }

    private fun saveAppMode(mode: AppMode, context: Context) {
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit().putString("last_mode", mode.name).apply()
    }

    fun toggleTheme(context: Context) {
        isDarkTheme = !isDarkTheme
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("is_dark_theme", isDarkTheme).apply()
        updateBackgroundColorForTheme()
    }

    private fun updateBackgroundColorForTheme() {
        backgroundColor = if (isDarkTheme) Background else Color.White
    }

    fun selectAppMode(mode: AppMode, context: Context) {
        if (showRegistrationScreen || isBlocked || networkErrorMessage != null) return
        appMode = mode
        saveAppMode(mode, context)
        resetToDefault()
    }

    fun resetIdleTimer() {
        idleJob?.cancel()
        idleJob = viewModelScope.launch {
            delay(IDLE_TIMEOUT_MS)
            if (appMode == AppMode.TOOLS && toolHeaderText != "Pick Tool") {
                resetToDefault()
            } else if (appMode == AppMode.PARTS && lastPart != null) {
                resetToDefault()
            }
        }
    }

    fun resetToDefault() {
        updateBackgroundColorForTheme()
        userMessage = defaultMessage
        resultMessage = ""
        isUpdating = false
        backgroundFlash = null
        
        // Reset Tools
        toolHeaderText = "Pick Tool"
        showToolStockTextField = false
        lastTool = null
        toolTextField = TextFieldValue("")
        
        // Reset Parts
        lastPart = null
        
        // Reset Images
        isUploadingImage = false
        imageUploadStatus = null
        
        resetIdleTimer()
    }

    private fun triggerFlash(type: FlashType) {
        viewModelScope.launch {
            backgroundFlash = type
            delay(800)
            backgroundFlash = null
        }
    }

    fun setToolTab(text: String) {
        if (showRegistrationScreen || isBlocked || networkErrorMessage != null) return
        toolHeaderText = text
        userMessage = defaultMessage
        resultMessage = ""
        showToolStockTextField = false
        updateBackgroundColorForTheme()
        toolTextField = TextFieldValue("")
        resetIdleTimer()
    }

    fun handleScan(scanCode: String) {
        if (showRegistrationScreen || isBlocked || networkErrorMessage != null || isCheckingRegistration) {
            Log.d("MainViewModel", "Ignoring scan: $scanCode (Blocked or not initialized)")
            return
        }
        Log.d("MainViewModel", "Handling scan: $scanCode in appMode: $appMode")
        resetIdleTimer()
        when (appMode) {
            AppMode.TOOLS -> {
                when (toolHeaderText) {
                    "Pick Tool" -> pickTool(scanCode)
                    "Re-Stock" -> reStockTool(scanCode)
                    "Info" -> toolInfo(scanCode)
                }
            }
            AppMode.PARTS -> {
                handlePartScan(scanCode)
            }
            else -> {}
        }
    }

    fun getImageUrl(relPath: String?): String? {
        if (relPath.isNullOrEmpty()) return null
        if (relPath.startsWith("http://") || relPath.startsWith("https://")) {
            return relPath
        }
        val cleanPath = if (relPath.startsWith("/")) relPath else "/$relPath"
        return "${HttpRoutes.BASE_IMAGE_URL}$cleanPath"
    }

    // --- Common Logic ---

    private suspend fun handleResponse(
        response: HttpResponse, 
        onSuccess: suspend () -> Unit,
        retryAction: suspend () -> Unit
    ) {
        when (response.status.value) {
            in 200..299 -> onSuccess()
            403 -> {
                val isJson = response.contentType()?.match(ContentType.Application.Json) == true
                if (isJson) {
                    try {
                        val errorBody = response.body<ApiErrorResponse>()
                        when (errorBody.error) {
                            "device_not_registered" -> {
                                pendingAction = retryAction
                                showRegistrationScreen = true
                            }
                            "device_blocked", "device_not_approved" -> {
                                isBlocked = true
                                triggerFlash(FlashType.FAILURE)
                                resultMessage = errorBody.message
                            }
                            else -> serverErrorMessage(response)
                        }
                    } catch (e: Exception) {
                        networkErrorMessage = "Access Restricted. Please ensure you are on the BK LAN."
                    }
                } else {
                    networkErrorMessage = "Access Restricted. Please ensure you are on the BK LAN."
                }
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
                val response = api.registerDevice(request)
                if (response.status.value in 200..299) {
                    val isJson = response.contentType()?.match(ContentType.Application.Json) == true
                    if (isJson) {
                        try {
                            // Wrapped: { device: { ... } }
                            val regResponse = response.body<DeviceRegistrationResponse>()
                            updateConfirmedName(regResponse.device.displayName)
                        } catch (e: Exception) {
                            try {
                                // Direct: { ...DeviceInfo... }
                                val deviceInfo = response.body<DeviceInfo>()
                                updateConfirmedName(deviceInfo.displayName)
                            } catch (e2: Exception) {
                                updateConfirmedName(registrationDisplayName)
                            }
                        }
                    } else {
                        updateConfirmedName(registrationDisplayName)
                    }
                    
                    showRegistrationScreen = false
                    pendingAction?.invoke()
                    pendingAction = null
                } else {
                    val isJson = response.contentType()?.match(ContentType.Application.Json) == true
                    if (isJson) {
                        val errorBody = response.body<ApiErrorResponse>()
                        registrationError = errorBody.message
                    } else {
                        registrationError = "Connection error (${response.status.value}). Ensure you are on the BK LAN."
                    }
                }
            } catch (t: Throwable) {
                registrationError = "Registration failed: ${t.localizedMessage}"
            } finally {
                isRegistering = false
            }
        }
    }

    private fun handleError(t: Throwable, functionName: String) {
        Log.e("MainViewModel", "Error in $functionName: ${t.message}", t)
        triggerFlash(FlashType.FAILURE)
        resultMessage = "Error: ${t.localizedMessage ?: t.toString()}"
        userMessage = "Connection failed"
    }

    private fun serverErrorMessage(response: HttpResponse) {
        triggerFlash(FlashType.FAILURE)
        resultMessage = "Server error: ${response.status}"
    }

    // --- Tools Logic ---

    private fun pickTool(scanCode: String) {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                userMessage = "Processing..."
                resultMessage = ""
                val response = api.pickTool(ToolPickRequest(scanCode))
                
                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    triggerFlash(FlashType.SUCCESS)
                    resultMessage = formatToolResultMessage(toolResponse)
                }, retryAction = { pickTool(scanCode) })

                if (response.status.value == 400) {
                     val toolResponse = response.body<ToolResponse>()
                     triggerFlash(FlashType.FAILURE)
                     resultMessage = formatToolResultMessage(toolResponse, 400)
                } else if (response.status.value == 404) {
                    toolNotFound(scanCode)
                }

                delay(1000)
                userMessage = defaultMessage
                delay(4000)
                resultMessage = ""
            } catch (t: Throwable) {
                handleError(t, "pickTool")
            }
        }
    }

    private fun toolInfo(scanCode: String) {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                userMessage = "Processing..."
                resultMessage = ""
                val response = api.toolInfo(scanCode)
                
                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    triggerFlash(FlashType.SUCCESS)
                    resultMessage = formatToolResultMessage(toolResponse)
                }, retryAction = { toolInfo(scanCode) })

                if (response.status.value == 404) {
                    toolNotFound(scanCode)
                }

                delay(1000)
                userMessage = defaultMessage
            } catch (t: Throwable) {
                handleError(t, "toolInfo")
            }
        }
    }

    private fun reStockTool(scanCode: String) {
        job?.cancel()
        job = viewModelScope.launch {
            try {
                userMessage = "Processing..."
                resultMessage = ""
                lastTool = null
                val response = api.toolInfo(scanCode)
                
                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    triggerFlash(FlashType.SUCCESS)
                    resultMessage = formatToolResultMessage(toolResponse)
                    userMessage = " "
                    showToolStockTextField = true
                }, retryAction = { reStockTool(scanCode) })

                if (response.status.value == 404) {
                    toolNotFound(scanCode)
                }
            } catch (t: Throwable) {
                handleError(t, "reStockTool")
            }
        }
    }

    fun updateToolStock(amount: String) {
        if (showRegistrationScreen || isBlocked || networkErrorMessage != null) return
        val num = amount.toIntOrNull() ?: run {
            userMessage = "Not a number."
            return
        }
        if (num == 0 || isUpdating) return
        
        val t = lastTool ?: return
        if (t.stock + num < 0) {
            userMessage = "Not enough stock."
            return
        }

        job?.cancel()
        job = viewModelScope.launch {
            try {
                isUpdating = true
                userMessage = "Processing..."
                val response = api.updateTool(t._id, num)
                isUpdating = false

                handleResponse(response, onSuccess = {
                    val toolResponse = response.body<ToolResponse>()
                    triggerFlash(FlashType.SUCCESS)
                    resultMessage = formatToolResultMessage(toolResponse)
                    updateToolTextField(TextFieldValue(""))
                    showToolStockTextField = false
                }, retryAction = { updateToolStock(amount) })

                if (response.status.value == 404) {
                    toolNotFound("")
                }

                delay(1000)
                userMessage = defaultMessage
                delay(4000)
                resultMessage = ""
            } catch (t: Throwable) {
                isUpdating = false
                handleError(t, "updateTool")
            }
        }
    }

    private fun formatToolResultMessage(tool: ToolResponse, status: Int = 200): String {
        lastTool = tool
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
        triggerFlash(FlashType.FAILURE)
        resultMessage = if (scanCode.isBlank()) "Tool not found." else "$scanCode\nTool not found."
    }

    fun updateToolTextField(value: TextFieldValue) {
        if (showRegistrationScreen || isBlocked || networkErrorMessage != null) return
        toolTextField = value
        resetIdleTimer()
    }

    // --- Parts Logic ---

    private fun handlePartScan(code: String) {
        val cleanCode = code.removePrefix("bk-part:").removePrefix("part:").trim()
        job?.cancel()
        job = viewModelScope.launch {
            try {
                isUpdating = true
                userMessage = "Processing..."
                resultMessage = ""
                val response = api.partInfo(cleanCode)
                isUpdating = false
                
                handleResponse(response, onSuccess = {
                    val part = response.body<PartResponse>()
                    lastPart = part
                }, retryAction = { handlePartScan(code) })
                
                if (response.status.value == 404) {
                    triggerFlash(FlashType.FAILURE)
                    resultMessage = "Part not found: $cleanCode"
                }

                delay(1000)
                userMessage = defaultMessage
            } catch (t: Throwable) {
                isUpdating = false
                handleError(t, "handlePartScan")
            }
        }
    }

    fun updatePartStock(amount: String) {
        if (showRegistrationScreen || isBlocked || networkErrorMessage != null) return
        val num = amount.toIntOrNull() ?: run {
            userMessage = "Not a number."
            return
        }
        val partId = lastPart?._id ?: return
        
        job?.cancel()
        job = viewModelScope.launch {
            try {
                isUpdating = true
                userMessage = "Processing..."
                val response = api.updatePartStock(partId, num)
                isUpdating = false
                
                handleResponse(response, onSuccess = {
                    val part = response.body<PartResponse>()
                    lastPart = part
                    triggerFlash(FlashType.SUCCESS)
                }, retryAction = { updatePartStock(amount) })

                delay(1000)
                userMessage = defaultMessage
            } catch (t: Throwable) {
                isUpdating = false
                handleError(t, "updatePartStock")
            }
        }
    }

    // --- Image Uploader Logic ---

    fun uploadImage(imageBytes: ByteArray, fileName: String) {
        if (showRegistrationScreen || isBlocked || networkErrorMessage != null) return
        viewModelScope.launch {
            isUploadingImage = true
            imageUploadStatus = "Uploading..."
            try {
                val response = api.uploadImage(imageBytes, fileName)
                if (response.status.value in 200..299) {
                    imageUploadStatus = "Upload successful!"
                    triggerFlash(FlashType.SUCCESS)
                } else {
                    val isJson = response.contentType()?.match(ContentType.Application.Json) == true
                    if (isJson) {
                        imageUploadStatus = "Upload failed: ${response.status}"
                    } else {
                        imageUploadStatus = "Access Restricted. Ensure you are on the BK LAN."
                    }
                    triggerFlash(FlashType.FAILURE)
                }
            } catch (t: Throwable) {
                imageUploadStatus = "Error: ${t.localizedMessage}"
                triggerFlash(FlashType.FAILURE)
            } finally {
                isUploadingImage = false
                delay(3000)
                imageUploadStatus = null
            }
        }
    }
}

val MyViewModel = MainViewModel()
