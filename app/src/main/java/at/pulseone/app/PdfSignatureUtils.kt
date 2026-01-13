package at.pulseone.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

object PdfSignatureUtils {

    fun renderSignatureOnPdf(context: Context, pdfPath: String, signaturePath: String, signatureBounds: RectF): String? {
        val pdfFile = File(pdfPath)
        val signatureFile = File(signaturePath)

        if (!pdfFile.exists() || !signatureFile.exists()) return null

        var parcelFileDescriptor: ParcelFileDescriptor? = null
        var pdfRenderer: PdfRenderer? = null
        val pdfDocument = PdfDocument()

        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(parcelFileDescriptor)

            val signatureBitmap = BitmapFactory.decodeFile(signaturePath)

            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i).create()
                val pdfPage = pdfDocument.startPage(pageInfo)
                val canvas = pdfPage.canvas

                val pageBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                pageBitmap.eraseColor(Color.WHITE)
                page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                canvas.drawBitmap(pageBitmap, 0f, 0f, null)

                if (i == pdfRenderer.pageCount - 1) { // Only on the last page
                    canvas.drawBitmap(signatureBitmap, null, signatureBounds, null)
                }

                pdfDocument.finishPage(pdfPage)
                page.close()
                pageBitmap.recycle()
            }

            val signedPdfFile = File(context.filesDir, "signed_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(signedPdfFile)
            pdfDocument.writeTo(outputStream)
            outputStream.close()

            signatureBitmap.recycle()

            return signedPdfFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            try {
                pdfDocument.close()
                pdfRenderer?.close()
                parcelFileDescriptor?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}