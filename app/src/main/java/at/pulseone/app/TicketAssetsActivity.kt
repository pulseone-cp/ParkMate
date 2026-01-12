package at.pulseone.app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class TicketAssetsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_assets)

        val signatureUri = intent.getParcelableExtra<Uri>("SIGNATURE_URI")
        val pdfUri = intent.getParcelableExtra<Uri>("PDF_URI")

        val signatureImageView: ImageView = findViewById(R.id.signature_image_view)
        val pdfRecyclerView: RecyclerView = findViewById(R.id.pdf_recycler_view)

        if (signatureUri != null) {
            val inputStream = contentResolver.openInputStream(signatureUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            signatureImageView.setImageBitmap(bitmap)
        }

        if (pdfUri != null) {
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
        }
    }
}