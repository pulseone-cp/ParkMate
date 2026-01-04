package at.pulseone.app

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class PrintingManager(private val context: Context) {

    private val PRINTER_WIDTH_PX = 384 // 58mm at 203dpi

    suspend fun printTicket(ticket: ParkingTicket) {
        val settingsManager = SettingsManager(context)
        val printerAddress = settingsManager.printerTarget

        if (printerAddress.isNullOrBlank()) {
            // Handle case where no printer is set
            return
        }

        val bitmap = createTicketBitmap(ticket)

        withContext(Dispatchers.IO) {
            try {
                val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
                val device = bluetoothAdapter?.getRemoteDevice(printerAddress)

                if (device == null) {
                    // Handle case where device is not found
                    return@withContext
                }

                val socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))

                socket?.connect()
                val outputStream: OutputStream? = socket?.outputStream

                if (outputStream != null) {
                    val command = EscPosCommand()
                    command.addInitializePrinter()
                    command.addRastBitImage(bitmap, PRINTER_WIDTH_PX)
                    command.addPrintAndFeedLines(3)
                    command.addCut(EscPosCommand.CUT_FEED)
                    outputStream.write(command.getCommand())
                }

                socket?.close()
            } catch (e: IOException) {
                e.printStackTrace()
                // Handle exceptions
            } catch (e: SecurityException) {
                e.printStackTrace()
                // Handle exceptions for missing Bluetooth permissions
            }
        }
    }

    fun createTicketBitmap(ticket: ParkingTicket): Bitmap {
        val themedContext = ContextThemeWrapper(context, R.style.Theme_ParkMate_Print)
        val inflater = LayoutInflater.from(themedContext)
        val view = inflater.inflate(R.layout.ticket_layout, null)

        populateTicketView(view, ticket)

        view.measure(View.MeasureSpec.makeMeasureSpec(PRINTER_WIDTH_PX, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED))
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private fun populateTicketView(view: View, ticket: ParkingTicket) {
        val settingsManager = SettingsManager(context)
        val ticketTitleTextView: TextView = view.findViewById(R.id.ticket_title)
        val licensePlateTextView: TextView = view.findViewById(R.id.ticket_license_plate)
        val timestampTextView: TextView = view.findViewById(R.id.ticket_timestamp)
        val validUntilTextView: TextView = view.findViewById(R.id.ticket_valid_until_text_view)
        val qrCodeImageView: ImageView = view.findViewById(R.id.qr_code_image_view)
        val printedAtTextView: TextView = view.findViewById(R.id.printed_at_text_view)
        val imprintTextView: TextView = view.findViewById(R.id.imprint_text_view)

        val sdfDateTime = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        val title = settingsManager.welcomeMessageHeading
        ticketTitleTextView.text = if (!title.isNullOrBlank()) title else context.getString(R.string.ticket_title)

        licensePlateTextView.text = context.getString(R.string.ticket_label_license_plate, ticket.licensePlate)
        timestampTextView.text = context.getString(R.string.ticket_label_time, sdfDateTime.format(ticket.timestamp))

        val validityHours = settingsManager.ticketValidityHours
        val calendar = Calendar.getInstance()
        calendar.time = ticket.timestamp
        calendar.add(Calendar.HOUR_OF_DAY, validityHours)
        validUntilTextView.text = context.getString(R.string.ticket_label_valid_until, sdfDateTime.format(calendar.time))

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

// Basic ESC/POS command implementation
class EscPosCommand {
    private val command = mutableListOf<Byte>()

    fun addInitializePrinter() {
        command.add(0x1B)
        command.add(0x40)
    }

    fun addRastBitImage(image: Bitmap, width: Int) {
        val grayscaleBitmap = toGrayscale(image)
        val pixels = IntArray(grayscaleBitmap.width * grayscaleBitmap.height)
        grayscaleBitmap.getPixels(pixels, 0, grayscaleBitmap.width, 0, 0, grayscaleBitmap.width, grayscaleBitmap.height)

        val bytes = ByteArray(grayscaleBitmap.width * grayscaleBitmap.height / 8)
        var byteIndex = 0
        var bitIndex = 7

        for (pixel in pixels) {
            if (Color.red(pixel) < 128) { // Assuming white background
                bytes[byteIndex] = (bytes[byteIndex].toInt() or (1 shl bitIndex)).toByte()
            }
            bitIndex--
            if (bitIndex < 0) {
                bitIndex = 7
                byteIndex++
            }
        }
        
        command.add(0x1D) // GS
        command.add(0x76) // v
        command.add(0x30) // 0
        command.add(0x00) // m
        command.add((width / 8 % 256).toByte()) // xL
        command.add((width / 8 / 256).toByte()) // xH
        command.add((grayscaleBitmap.height % 256).toByte()) // yL
        command.add((grayscaleBitmap.height / 256).toByte()) // yH
        command.addAll(bytes.toList())
    }

    fun addPrintAndFeedLines(n: Int) {
        command.add(0x1B)
        command.add(0x64)
        command.add(n.toByte())
    }

    fun addCut(cutType: Byte) {
        command.add(0x1D)
        command.add(0x56)
        command.add(cutType)
    }

    fun getCommand(): ByteArray {
        return command.toByteArray()
    }

    private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
        val height: Int = bmpOriginal.height
        val width: Int = bmpOriginal.width
        val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = android.graphics.Canvas(bmpGrayscale)
        val paint = android.graphics.Paint()
        val cm = android.graphics.ColorMatrix()
        cm.setSaturation(0f)
        val f = android.graphics.ColorMatrixColorFilter(cm)
        paint.colorFilter = f
        c.drawBitmap(bmpOriginal, 0f, 0f, paint)
        return bmpGrayscale
    }

    companion object {
        const val CUT_FEED: Byte = 1
    }
}