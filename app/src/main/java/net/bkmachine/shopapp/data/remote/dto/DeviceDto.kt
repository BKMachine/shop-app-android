package net.bkmachine.shopapp.data.remote.dto

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
    val id: String,
    val deviceId: String,
    val displayName: String,
    val deviceType: String,
    val approved: Boolean,
    val blocked: Boolean
)

@Serializable
data class ApiErrorResponse(
    val error: String,
    val message: String
)
