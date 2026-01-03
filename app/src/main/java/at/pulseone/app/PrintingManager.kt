package at.pulseone.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrintingManager(private val context: Context) {

    fun printTicket(ticket: ParkingTicket) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = context.getString(R.string.ticket_title) + " ${ticket.licensePlate}"

        val mediaSize = PrintAttributes.MediaSize("roll_50mm", "50mm Thermal Paper", 1969, 10000)
        val printAttributes = PrintAttributes.Builder()
            .setMediaSize(mediaSize)
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()

        val printAdapter = TicketPrintDocumentAdapter(context, ticket)

        printManager.print(jobName, printAdapter, printAttributes)
    }

    private class TicketPrintDocumentAdapter(private val context: Context, private val ticket: ParkingTicket) : PrintDocumentAdapter() {

        private var pageHeight: Int = 0
        private var pageWidth: Int = 0

        override fun onLayout(
            oldAttributes: PrintAttributes?,
            newAttributes: PrintAttributes,
            cancellationSignal: CancellationSignal?,
            callback: LayoutResultCallback,
            extras: Bundle?
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }

            pageWidth = newAttributes.mediaSize!!.widthMils * 72 / 1000

            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.ticket_layout, null)
            populateTicketView(view, ticket)

            val widthSpec = View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            view.measure(widthSpec, heightSpec)
            pageHeight = view.measuredHeight

            val info = PrintDocumentInfo.Builder("ticket.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(1)
                .build()

            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out PageRange>?,
            destination: ParcelFileDescriptor?,
            cancellationSignal: CancellationSignal?,
            callback: WriteResultCallback
        ) {
            if (cancellationSignal?.isCanceled == true) {
                callback.onWriteCancelled()
                return
            }
            
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)

            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.ticket_layout, null)
            populateTicketView(view, ticket)

            val widthSpec = View.MeasureSpec.makeMeasureSpec(pageWidth, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(pageHeight, View.MeasureSpec.EXACTLY)
            view.measure(widthSpec, heightSpec)
            view.layout(0, 0, pageWidth, pageHeight)

            view.draw(page.canvas)

            pdfDocument.finishPage(page)

            try {
                destination?.let {
                    pdfDocument.writeTo(FileOutputStream(it.fileDescriptor))
                }
            } catch (e: IOException) {
                callback.onWriteFailed(e.toString())
            } finally {
                pdfDocument.close()
            }

            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        }
        
        private fun populateTicketView(view: View, ticket: ParkingTicket) {
            val settingsManager = SettingsManager(context)
            val ticketTitleTextView: TextView = view.findViewById(R.id.ticket_title)
            val licensePlateTextView: TextView = view.findViewById(R.id.ticket_license_plate)
            val timestampTextView: TextView = view.findViewById(R.id.ticket_timestamp)
            val qrCodeImageView: ImageView = view.findViewById(R.id.qr_code_image_view)
            val printedAtTextView: TextView = view.findViewById(R.id.printed_at_text_view)
            val imprintTextView: TextView = view.findViewById(R.id.imprint_text_view)

            val sdfDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

            val title = settingsManager.welcomeMessageHeading
            ticketTitleTextView.text = if (!title.isNullOrBlank()) title else context.getString(R.string.ticket_title)
            
            licensePlateTextView.text = context.getString(R.string.ticket_label_license_plate, ticket.licensePlate)
            timestampTextView.text = context.getString(R.string.ticket_label_time, sdfDate.format(ticket.timestamp))
            printedAtTextView.text = context.getString(R.string.ticket_label_printed_at, sdfTime.format(Date()))

            val imprintText = settingsManager.imprintText
            if (!imprintText.isNullOrBlank()) {
                imprintTextView.text = imprintText
                imprintTextView.visibility = View.VISIBLE
            }

            try {
                val multiFormatWriter = MultiFormatWriter()
                val bitMatrix = multiFormatWriter.encode(ticket.guid, BarcodeFormat.QR_CODE, 120, 120)
                val barcodeEncoder = BarcodeEncoder()
                val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
                qrCodeImageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}