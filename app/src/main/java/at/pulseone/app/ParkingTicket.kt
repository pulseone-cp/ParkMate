package at.pulseone.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "parking_tickets")
data class ParkingTicket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val surname: String,
    val licensePlate: String,
    val department: String,
    val timestamp: Date,
    val guid: String = UUID.randomUUID().toString(),
    val isReported: Boolean = false,
    val validFrom: Date,
    val validUntil: Date,
    val signaturePath: String? = null,
    val pdfPath: String? = null
)