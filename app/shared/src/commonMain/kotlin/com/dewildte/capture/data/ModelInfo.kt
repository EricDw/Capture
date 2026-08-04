package com.dewildte.capture.data

import kotlinx.serialization.Serializable

@Serializable
data class ModelInfo(
    val name: String,
    val uri: String
)
