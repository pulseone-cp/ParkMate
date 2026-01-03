package at.pulseone.app

import android.app.Application

class ParkingTicketRepository(application: Application) {

    private val parkingTicketDao = AppDatabase.getDatabase(application).parkingTicketDao()

    suspend fun addTicket(ticket: ParkingTicket) {
        parkingTicketDao.insert(ticket)
    }

    suspend fun getTickets(): List<ParkingTicket> {
        return parkingTicketDao.getAll()
    }

    suspend fun searchTickets(query: String): List<ParkingTicket> {
        return parkingTicketDao.search("%" + query + "%")
    }
}