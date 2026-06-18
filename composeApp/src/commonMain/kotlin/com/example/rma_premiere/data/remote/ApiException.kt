package com.example.rma_premiere.data.remote

import java.io.IOException


class ApiException(
    val statusCode: Int,
    val serverMessage: String?
) : Exception("HTTP $statusCode${serverMessage?.let { ": $it" } ?: ""}")


val Throwable.isNetworkError: Boolean
    get() = this is IOException || (cause?.isNetworkError == true)
