package net.bkmachine.shopapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import net.bkmachine.shopapp.data.local.DeviceIdentityManager
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationRequest
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest

interface ToolsService {
    suspend fun pickTool(toolPickRequest: ToolPickRequest): HttpResponse
    suspend fun toolInfo(scanCode: String): HttpResponse
    suspend fun updateTool(id: String, amount: Int): HttpResponse
    suspend fun registerDevice(request: DeviceRegistrationRequest): HttpResponse

    companion object {
        fun create(deviceIdentityManager: DeviceIdentityManager): ToolsService {
            return ToolsServiceImpl(KtorClient.create(deviceIdentityManager))
        }
    }
}
