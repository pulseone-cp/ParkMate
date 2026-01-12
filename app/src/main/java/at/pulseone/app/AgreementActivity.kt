package at.pulseone.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File

class AgreementActivity : AppCompatActivity() {

    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null

    private val signatureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, result.data)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agreement)

        val pdfPath = intent.getStringExtra("PDF_PATH")
        val agreeButton: Button = findViewById(R.id.agree_button)
        val pdfPagesContainer: ZoomableLinearLayout = findViewById(R.id.pdf_pages_container)

        val abortActivityButton: Button = findViewById(R.id.abort_activity_button)

        if (pdfPath != null) {
            displayPdf(pdfPath, pdfPagesContainer)
        }
        
        agreeButton.setOnClickListener {
            val intent = Intent(this, SignatureActivity::class.java)
            signatureLauncher.launch(intent)
        }

        abortActivityButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun displayPdf(path: String, container: ZoomableLinearLayout) {
        try {
            val file = File(path)
            parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor!!)

            for (i in 0 until pdfRenderer!!.pageCount) {
                val page = pdfRenderer!!.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                val imageView = ImageView(this)
                imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, 16)
                }
                imageView.adjustViewBounds = true
                imageView.setImageBitmap(bitmap)
                container.addView(imageView)
                
                page.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }
}
