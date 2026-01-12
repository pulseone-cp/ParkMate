package at.pulseone.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

object PdfSignatureUtils {

    fun renderSignatureOnPdf(context: Context, pdfPath: String, signaturePath: String): String? {
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
            
            // 5 cm in points (1 inch = 72 points, 1 inch = 2.54 cm)
            // 1 cm = 72 / 2.54 = 28.3465 points
            val widthInPoints = 5f * 28.3465f
            val aspectRatio = signatureBitmap.height.toFloat() / signatureBitmap.width.toFloat()
            val heightInPoints = widthInPoints * aspectRatio

            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                
                // Create a page info
                val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, i).create()
                val pdfPage = pdfDocument.startPage(pageInfo)
                val canvas = pdfPage.canvas

                // Render original PDF page content to bitmap first, then draw it to canvas
                // Note: PdfRenderer renders to Bitmap. To draw it on PdfDocument's canvas, 
                // we render it to a bitmap that matches the page size.
                val pageBitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(pageBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
                
                canvas.drawBitmap(pageBitmap, 0f, 0f, null)
                
                // Calculate position for signature (bottom right)
                val x = page.width - widthInPoints - 20f // 20pt margin
                val y = page.height - heightInPoints - 20f // 20pt margin
                
                val destRect = RectF(x, y, x + widthInPoints, y + heightInPoints)
                canvas.drawBitmap(signatureBitmap, null, destRect, null)

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
