package net.bkmachine.shopapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import net.bkmachine.shopapp.data.local.DeviceIdentityManager
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationRequest
import net.bkmachine.shopapp.data.remote.dto.PartStockUpdateRequest
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest

interface ApiService {
    // Tools
    suspend fun pickTool(toolPickRequest: ToolPickRequest): HttpResponse
    suspend fun toolInfo(scanCode: String): HttpResponse
    suspend fun updateTool(id: String, amount: Int): HttpResponse
    
    // Parts
    suspend fun partInfo(scanCode: String): HttpResponse
    suspend fun updatePartStock(id: String, amount: Int): HttpResponse
    
    // Images
    suspend fun uploadImage(imageBytes: ByteArray, fileName: String): HttpResponse
    suspend fun getRecentUploads(): HttpResponse
    
    // Common
    suspend fun registerDevice(request: DeviceRegistrationRequest): HttpResponse
    suspend fun getDeviceMe(): HttpResponse

    companion object {
        fun create(deviceIdentityManager: DeviceIdentityManager): ApiService {
            return ApiServiceImpl(KtorClient.create(deviceIdentityManager), deviceIdentityManager)
        }
    }
}
