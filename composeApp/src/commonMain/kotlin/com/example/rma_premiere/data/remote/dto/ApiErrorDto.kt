package com.example.rma_premiere.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorDto(
    val error: String? = null,
    val httpCode: Int? = null,
    val message: String? = null,
    val description: String? = null,
    val suggestion: String? = null
)
