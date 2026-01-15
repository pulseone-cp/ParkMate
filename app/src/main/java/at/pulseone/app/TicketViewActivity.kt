package at.pulseone.app

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class TicketViewActivity : AppCompatActivity() {

    private lateinit var repository: ParkingTicketRepository
    private lateinit var printingManager: PrintingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_view)

        repository = ParkingTicketRepository(application)
        printingManager = PrintingManager(this)

        val guid = intent.getStringExtra("ticket_guid")
        if (guid != null) {
            lifecycleScope.launch {
                val ticket = repository.findTicketByGuid(guid)
                if (ticket != null) {
                    populateTicketData(ticket)
                    val bitmap = printingManager.createTicketBitmap(ticket)
                    val imageView: ImageView = findViewById(R.id.ticket_image_view)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun populateTicketData(ticket: ParkingTicket) {
        val sdfDateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

        findViewById<TextView>(R.id.tv_license_plate).text =
            if (ticket.licensePlate.isBlank()) "BESUCHER" else ticket.licensePlate

        findViewById<TextView>(R.id.tv_name).text = "${ticket.name} ${ticket.surname}"

        val tvCompany = findViewById<TextView>(R.id.tv_company)
        if (!ticket.company.isNullOrBlank()) {
            tvCompany.text = ticket.company
            tvCompany.visibility = View.VISIBLE
        } else {
            tvCompany.visibility = View.GONE
        }

        findViewById<TextView>(R.id.tv_department).text = getString(R.string.hint_department) + ": " + ticket.department
        findViewById<TextView>(R.id.tv_timestamp).text = getString(R.string.ticket_label_time, sdfDateTime.format(ticket.timestamp))
        findViewById<TextView>(R.id.tv_validity).text =
            "Valid: ${sdfDateTime.format(ticket.validFrom)} - ${sdfDateTime.format(ticket.validUntil)}"

        val tvStatus = findViewById<TextView>(R.id.tv_status)
        if (ticket.isReported) {
            tvStatus.text = "Status: Reported"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.green))
        } else {
            tvStatus.text = "Status: Pending Upload"
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.red))
        }
    }
}