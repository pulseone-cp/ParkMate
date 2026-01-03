package at.pulseone.app

import java.util.Date

data class ValidationRecord(
    val guid: String,
    val status: String,
    val validatedAt: Date,
    val expiresAt: Date?
)