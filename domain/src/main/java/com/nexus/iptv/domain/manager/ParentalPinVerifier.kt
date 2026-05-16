package com.nexus.iptv.domain.manager

interface ParentalPinVerifier {
    suspend fun verifyParentalPin(pin: String): Boolean
}