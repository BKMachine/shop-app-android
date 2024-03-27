package net.bkmachine.shopapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ToolResponse(
    val _id: String,
    val description: String,
    val vendor: Vendor? = null,
    val supplier: Supplier? = null,
    val item: String? = null,
    val barcode: String? = null,
    val stock: Int,
    val img: String? = null,
    val category: String,
    val coating: String? = null,
    val flutes: Int? = null,
    val autoReorder: Boolean,
    val reorderQty: Int,
    val reorderThreshold: Int,
    val productLink: String? = null,
    val techDataLink: String? = null,
    val cost: Double? = null,
    val onOrder: Boolean,
    val orderedOn: String? = null,
    val location: String? = null,
    val position: String? = null,
)

@Serializable
data class Vendor(
    val _id: String,
    val name: String,
    val logo: String? = null,
    val homepage: String? = null,
    val coatings: List<String>? = null,
)

@Serializable
data class Supplier(
    val _id: String,
    val name: String,
    val logo: String? = null,
    val homepage: String? = null,
)
