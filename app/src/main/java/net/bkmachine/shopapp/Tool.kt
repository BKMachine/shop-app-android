package net.bkmachine.shopapp

import kotlinx.serialization.Serializable

@Serializable
data class Tool(
    val _id: String,
    val description: String,
    val vendor: Vendor?,
    val item: String?,
    val barcode: String?,
    val stock: Int,
    val img: String?,
    val category: String,
    val coating: String?,
    val flutes: Int?,
    val autoReorder: Boolean,
    val reorderQty: Int,
    val reorderThreshold: Int,
    val productLink: String?,
    val techDataLink: String?,
    val cost: Int?,
    val onOrder: Boolean,
    val orderedOn: String?,
    val location: String?,
    val position: String?,
)

@Serializable
data class Vendor(
    val name: String,
    val logo: String?,
    val homepage: String?,
    val coatings: List<String>?,
)

@Serializable
data class ToolPickResponse(
    val message: String,
)

@Serializable
data class ToolPickRequest(
    val scanCode: String,
)