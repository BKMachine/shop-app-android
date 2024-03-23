package net.bkmachine.shopapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest

class ToolsServiceImpl(
    private val client: HttpClient
) : ToolsService {
    override suspend fun pickTool(toolPickRequest: ToolPickRequest): HttpResponse {
        return client.put(HttpRoutes.PICK_TOOL) {
            contentType(ContentType.Application.Json)
            setBody(toolPickRequest)
        }
    }
}
