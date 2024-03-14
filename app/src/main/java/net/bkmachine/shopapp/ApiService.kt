package net.bkmachine.shopapp

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.put
import io.ktor.http.ContentType
import io.ktor.http.contentType

interface ApiService {
    suspend fun pickTool(scanCode: ToolPickRequest): ToolPickResponse

    companion object {
        fun create(): ApiService {
            return ApiServiceImpl(
                client = HttpClient(Android) {
                    install(Logging) {
                        level = LogLevel.INFO
                    }
                    //install(JsonFeature) {
                    //   serializer = KotlinxSerializer()
                    //}
                }
            )
        }
    }
}

class ApiServiceImpl(
    private val client: HttpClient
) : ApiService {
    override suspend fun pickTool(scanCode: ToolPickRequest): ToolPickResponse {
        return try {
            client.put<ToolPickResponse> {
                url(HttpRoutes.PICK_TOOL)
                contentType(ContentType.Application.Json)
                body = scanCode
            }
        } catch (e: Exception) {
            // Do Something
        }
        //return client.put<ToolPickResponse> {
        // url(HttpRoutes.PICK_TOOL)
        //contentType(ContentType.Application.Json)
        // setBody(ToolPickRequest(scanCode))
        //}
    }
}
