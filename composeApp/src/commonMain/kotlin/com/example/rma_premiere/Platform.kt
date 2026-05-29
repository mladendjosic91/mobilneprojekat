package com.example.rma_premiere

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform