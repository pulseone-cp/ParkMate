package at.pulseone.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.io.File

class TicketAssetsActivity : AppCompatActivity() {

    private lateinit var repository: ParkingTicketRepository
    private lateinit var printingManager: PrintingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_assets)

        repository = ParkingTicketRepository(application)
        printingManager = PrintingManager(this)

        val ticketGuid = intent.getStringExtra("TICKET_GUID")
        val signatureUri = intent.getParcelableExtra<Uri>("SIGNATURE_URI")
        val pdfUri = intent.getParcelableExtra<Uri>("PDF_URI")

        val ticketImageView: ImageView = findViewById(R.id.ticket_image_view)
        val ticketHeader: TextView = findViewById(R.id.ticket_header)
        val signatureImageView: ImageView = findViewById(R.id.signature_image_view)
        val signatureHeader: TextView = findViewById(R.id.signature_header)
        val pdfRecyclerView: RecyclerView = findViewById(R.id.pdf_recycler_view)
        val pdfHeader: TextView = findViewById(R.id.pdf_header)

        if (ticketGuid != null) {
            lifecycleScope.launch {
                val ticket = repository.findTicketByGuid(ticketGuid)
                if (ticket != null) {
                    val bitmap = printingManager.createTicketBitmap(ticket)
                    ticketImageView.setImageBitmap(bitmap)
                    ticketHeader.visibility = View.VISIBLE
                    ticketImageView.visibility = View.VISIBLE
                } else {
                    ticketHeader.visibility = View.GONE
                    ticketImageView.visibility = View.GONE
                }
            }
        } else {
            ticketHeader.visibility = View.GONE
            ticketImageView.visibility = View.GONE
        }

        if (signatureUri != null) {
            try {
                val inputStream = contentResolver.openInputStream(signatureUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                signatureImageView.setImageBitmap(bitmap)
                signatureHeader.visibility = View.VISIBLE
                signatureImageView.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                signatureHeader.visibility = View.GONE
                signatureImageView.visibility = View.GONE
            }
        } else {
            signatureHeader.visibility = View.GONE
            signatureImageView.visibility = View.GONE
        }

        if (pdfUri != null) {
            try {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(pdfUri, "r")
                val pdfRenderer = PdfRenderer(parcelFileDescriptor!!)
                val bitmaps = mutableListOf<Bitmap>()

                for (i in 0 until pdfRenderer.pageCount) {
                    val page = pdfRenderer.openPage(i)
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }
                pdfRecyclerView.layoutManager = LinearLayoutManager(this)
                pdfRecyclerView.adapter = PdfPagesAdapter(bitmaps)
                pdfRenderer.close()
                parcelFileDescriptor.close()

                pdfHeader.visibility = View.VISIBLE
                pdfRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
                pdfHeader.visibility = View.GONE
                pdfRecyclerView.visibility = View.GONE
            }
        } else {
            pdfHeader.visibility = View.GONE
            pdfRecyclerView.visibility = View.GONE
        }
    }
}