package net.bkmachine.shopapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationRequest
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest
import net.bkmachine.shopapp.data.remote.dto.ToolStockRequest

class ToolsServiceImpl(
    private val client: HttpClient
) : ToolsService {
    override suspend fun pickTool(toolPickRequest: ToolPickRequest): HttpResponse {
        return client.put(HttpRoutes.PICK_TOOL) {
            contentType(ContentType.Application.Json)
            setBody(toolPickRequest)
        }
    }

    override suspend fun toolInfo(scanCode: String): HttpResponse {
        val encodedCode = scanCode.encodeURLPath()
        val url = HttpRoutes.TOOL_INFO.replace(":scanCode", encodedCode)
        return client.get(url)
    }

    override suspend fun updateTool(id: String, amount: Int): HttpResponse {
        val body = ToolStockRequest(id, amount)
        return client.put(HttpRoutes.TOOL_STOCK) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    override suspend fun registerDevice(request: DeviceRegistrationRequest): HttpResponse {
        return client.post(HttpRoutes.REGISTER_DEVICE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
