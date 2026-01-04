package at.pulseone.app

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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
                    val bitmap = printingManager.createTicketBitmap(ticket)
                    val imageView: ImageView = findViewById(R.id.ticket_image_view)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }
}