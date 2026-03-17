package net.bkmachine.shopapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import net.bkmachine.shopapp.data.remote.dto.DeviceRegistrationRequest

class ApiService(private val client: HttpClient) {
    suspend fun registerDevice(request: DeviceRegistrationRequest): HttpResponse {
        return client.post(HttpRoutes.REGISTER_DEVICE) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }
}
