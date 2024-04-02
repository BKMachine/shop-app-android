package net.bkmachine.shopapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
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
        val url = HttpRoutes.TOOL_INFO.replace(":scanCode", scanCode)
        return client.get(url) {}
    }

    override suspend fun updateTool(id: String, amount: Int): HttpResponse {
        val url = HttpRoutes.TOOL_STOCK
        val body = ToolStockRequest(id, amount)
        return client.put(url) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }
}
