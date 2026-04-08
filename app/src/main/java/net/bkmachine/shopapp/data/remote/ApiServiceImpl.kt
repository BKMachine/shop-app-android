package net.bkmachine.shopapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.encodeURLPath
import net.bkmachine.shopapp.data.local.DeviceIdentityManager
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationRequest
import net.bkmachine.shopapp.data.remote.dto.PartStockUpdateRequest
import net.bkmachine.shopapp.data.remote.dto.ToolPickRequest
import net.bkmachine.shopapp.data.remote.dto.ToolStockRequest

class ApiServiceImpl(
    private val client: HttpClient,
    private val deviceIdentityManager: DeviceIdentityManager
) : ApiService {

    // Tools
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

    // Parts
    override suspend fun partInfo(scanCode: String): HttpResponse {
        val encodedCode = scanCode.encodeURLPath()
        val url = HttpRoutes.PART_INFO.replace(":scanCode", encodedCode)
        return client.get(url)
    }

    override suspend fun updatePartStock(id: String, amount: Int): HttpResponse {
        val body = PartStockUpdateRequest(id, amount)
        return client.put(HttpRoutes.PART_STOCK) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
    }

    // Images
    override suspend fun uploadImage(imageBytes: ByteArray, fileName: String): HttpResponse {
        return client.post(HttpRoutes.IMAGE_UPLOAD) {
            setBody(MultiPartFormDataContent(
                formData {
                    append("image", imageBytes, Headers.build {
                        append(HttpHeaders.ContentType, "image/jpeg")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                }
            ))
        }
    }

    override suspend fun getRecentUploads(): HttpResponse {
        return client.get(HttpRoutes.RECENT_UPLOADS)
    }

    // Common
    override suspend fun registerDevice(request: DeviceRegistrationRequest): HttpResponse {
        return client.post(HttpRoutes.REGISTER_DEVICE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    override suspend fun getDeviceMe(): HttpResponse {
        return client.get(HttpRoutes.GET_DEVICE_ME)
    }
}
