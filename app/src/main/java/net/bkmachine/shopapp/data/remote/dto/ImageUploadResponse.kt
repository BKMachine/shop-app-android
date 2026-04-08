package net.bkmachine.shopapp.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ImageUploadResponse(
    val id: String? = null,
    val url: String? = null,
    val createdAt: String? = null,
    val isMain: Boolean? = null
)
