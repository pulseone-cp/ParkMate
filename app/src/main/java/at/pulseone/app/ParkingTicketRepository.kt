package at.pulseone.app

import android.app.Application

class ParkingTicketRepository(application: Application) {

    private val parkingTicketDao = AppDatabase.getDatabase(application).parkingTicketDao()

    suspend fun addTicket(ticket: ParkingTicket): ParkingTicket {
        val newId = parkingTicketDao.insert(ticket)
        return ticket.copy(id = newId.toInt())
    }

    suspend fun updateTicket(ticket: ParkingTicket) {
        parkingTicketDao.update(ticket)
    }

    suspend fun getTickets(): List<ParkingTicket> {
        return parkingTicketDao.getAll()
    }

    suspend fun searchTickets(query: String): List<ParkingTicket> {
        return parkingTicketDao.search("%" + query + "%")
    }

    suspend fun findTicketByGuid(guid: String): ParkingTicket? {
        return parkingTicketDao.findByGuid(guid)
    }
}