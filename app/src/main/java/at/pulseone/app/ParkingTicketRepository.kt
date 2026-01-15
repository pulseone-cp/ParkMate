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

    suspend fun deleteOldTickets(days: Int, auditDeletionEnabled: Boolean = false, endpointUrl: String? = null) {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -days)
        val cutoffDate = calendar.time

        if (auditDeletionEnabled && !endpointUrl.isNullOrBlank()) {
            val ticketsToDelete = parkingTicketDao.getOlderThan(cutoffDate)
            val auditManager = AuditManager()
            for (ticket in ticketsToDelete) {
                auditManager.reportDeletion(ticket, endpointUrl)
            }
        }

        parkingTicketDao.deleteOlderThan(cutoffDate)
    }
}