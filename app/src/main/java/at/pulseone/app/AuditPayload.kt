package at.pulseone.app

data class AuditPayload(
    val ticket: ParkingTicket,
    val signature: String? = null,
    val document: String? = null
)
