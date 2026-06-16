package com.example.rma_premiere.data.remote

import java.io.IOException

/**
 * Greska koju server vrati sa HTTP status kodom != 2xx.
 * [serverMessage] je `message` polje iz API error odgovora ako postoji.
 */
class ApiException(
    val statusCode: Int,
    val serverMessage: String?
) : Exception("HTTP $statusCode${serverMessage?.let { ": $it" } ?: ""}")

/**
 * True kada je uzrok pad konekcije (nema mreze, DNS, timeout),
 * a ne odgovor servera — koristi se za offline stanje na ekranima.
 */
val Throwable.isNetworkError: Boolean
    get() = this is IOException || (cause?.isNetworkError == true)
