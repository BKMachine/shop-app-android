package net.bkmachine.shopapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ToolPickRequest(
    val scanCode: String
)
