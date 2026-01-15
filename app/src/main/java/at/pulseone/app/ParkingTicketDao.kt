package at.pulseone.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ParkingTicketDao {

    @Insert
    suspend fun insert(ticket: ParkingTicket): Long

    @Update
    suspend fun update(ticket: ParkingTicket)

    @Query("SELECT * FROM parking_tickets ORDER BY timestamp DESC")
    suspend fun getAll(): List<ParkingTicket>

    @Query("SELECT * FROM parking_tickets WHERE name LIKE :query OR surname LIKE :query OR licensePlate LIKE :query ORDER BY timestamp DESC")
    suspend fun search(query: String): List<ParkingTicket>

    @Query("SELECT * FROM parking_tickets WHERE guid = :guid")
    suspend fun findByGuid(guid: String): ParkingTicket?

    @Query("SELECT * FROM parking_tickets WHERE timestamp < :date")
    suspend fun getOlderThan(date: java.util.Date): List<ParkingTicket>

    @Query("DELETE FROM parking_tickets WHERE timestamp < :date")
    suspend fun deleteOlderThan(date: java.util.Date)
}