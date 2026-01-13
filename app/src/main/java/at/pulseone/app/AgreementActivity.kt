package at.pulseone.app

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class AgreementActivity : AppCompatActivity() {

    private var pdfRenderer: PdfRenderer? = null
    private var parcelFileDescriptor: ParcelFileDescriptor? = null
    private var pdfUri: Uri? = null

    private val signatureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            setResult(Activity.RESULT_OK, result.data)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agreement)

        pdfUri = intent.getParcelableExtra<Uri>("PDF_URI")
        val agreeButton: Button = findViewById(R.id.agree_button)
        val pdfPagesContainer: ZoomableLinearLayout = findViewById(R.id.pdf_pages_container)

        val abortActivityButton: Button = findViewById(R.id.abort_activity_button)

        if (pdfUri != null) {
            displayPdf(pdfUri!!, pdfPagesContainer)
        }

        agreeButton.setOnClickListener {
            val intent = Intent(this, SignatureActivity::class.java)
            pdfUri?.let { uri ->
                createPdfPreview(uri)?.let { (previewFile, originalWidth, originalHeight) ->
                    val previewUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", previewFile)
                    intent.putExtra("PDF_PREVIEW_URI", previewUri)
                    intent.putExtra("ORIGINAL_WIDTH", originalWidth)
                    intent.putExtra("ORIGINAL_HEIGHT", originalHeight)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
            signatureLauncher.launch(intent)
        }

        abortActivityButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun createPdfPreview(uri: Uri): Triple<File, Int, Int>? {
        return try {
            val pfd = contentResolver.openFileDescriptor(uri, "r")!!
            val renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount
            if (pageCount == 0) {
                renderer.close()
                pfd.close()
                return null
            }

            val page = renderer.openPage(pageCount - 1) // Get the last page
            val originalWidth = page.width
            val originalHeight = page.height
            val bitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_8888)
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            
            val previewFile = File(cacheDir, "pdf_preview.png")
            val out = FileOutputStream(previewFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            page.close()
            renderer.close()
            pfd.close()

            Triple(previewFile, originalWidth, originalHeight)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayPdf(uri: Uri, container: ZoomableLinearLayout) {
        try {
            parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
            pdfRenderer = PdfRenderer(parcelFileDescriptor!!)

            for (i in 0 until pdfRenderer!!.pageCount) {
                val page = pdfRenderer!!.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
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
            Toast.makeText(this, getString(R.string.toast_pdf_load_error, e.message), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        pdfRenderer?.close()
        parcelFileDescriptor?.close()
    }
}