package net.bkmachine.shopapp.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceRegistrationRequest(
    val deviceId: String,
    val displayName: String,
    val deviceType: String
)

@Serializable
data class DeviceRegistrationResponse(
    val device: DeviceInfo
)

@Serializable
data class DeviceInfo(
    @SerialName("_id")
    val id: String? = null,
    val deviceId: String,
    val displayName: String,
    val deviceType: String? = null,
    val approved: Boolean = false,
    val blocked: Boolean = false
)

@Serializable
data class ApiErrorResponse(
    val error: String,
    val message: String
)
