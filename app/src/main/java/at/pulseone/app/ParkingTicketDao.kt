package at.pulseone.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ParkingTicketDao {

    @Insert
    suspend fun insert(ticket: ParkingTicket)

    @Query("SELECT * FROM parking_tickets ORDER BY timestamp DESC")
    suspend fun getAll(): List<ParkingTicket>

    @Query("SELECT * FROM parking_tickets WHERE name LIKE :query OR surname LIKE :query OR licensePlate LIKE :query ORDER BY timestamp DESC")
    suspend fun search(query: String): List<ParkingTicket>
}