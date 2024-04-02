package net.bkmachine.shopapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ToolStockRequest(
    val id: String,
    val amount: Int
)
