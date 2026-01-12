package at.pulseone.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.Date

@RunWith(AndroidJUnit4::class)
class ParkingTicketRepositoryTest {

    private lateinit var parkingTicketDao: ParkingTicketDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        parkingTicketDao = db.parkingTicketDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetTicket() = runBlocking {
        val now = Date()
        val ticket = ParkingTicket(
            name = "John",
            surname = "Doe",
            licensePlate = "1234",
            department = "Sales",
            timestamp = now,
            validFrom = now,
            validUntil = now
        )
        val id = parkingTicketDao.insert(ticket)
        val insertedTicket = ticket.copy(id = id.toInt())
        val allTickets = parkingTicketDao.getAll()
        assert(allTickets.contains(insertedTicket))
    }
}