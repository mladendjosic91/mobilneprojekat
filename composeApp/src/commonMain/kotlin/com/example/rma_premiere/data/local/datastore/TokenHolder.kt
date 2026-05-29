package com.example.rma_premiere.data.local.datastore

object TokenHolder {
    @Volatile
    var token: String? = null
}
