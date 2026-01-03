package at.pulseone.app

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "parking_tickets")
data class ParkingTicket(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val surname: String,
    val licensePlate: String,
    val department: String,
    val timestamp: Date
)