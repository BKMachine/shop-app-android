package net.bkmachine.shopapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class PartResponse(
    val _id: String,
    val part: String,
    val description: String,
    val stock: Int,
    val location: String? = null,
    val position: String? = null,
    val img: String? = null,
    val customer: CustomerResponse? = null
)

@Serializable
data class CustomerResponse(
    val _id: String,
    val name: String
)

@Serializable
data class PartStockUpdateRequest(
    val id: String,
    val amount: Int
)
