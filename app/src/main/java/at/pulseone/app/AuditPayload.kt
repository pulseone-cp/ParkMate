package at.pulseone.app

data class AuditPayload(
    val ticket: ParkingTicket,
    val signatureImage: String? = null,
    val signedDocument: String? = null
)