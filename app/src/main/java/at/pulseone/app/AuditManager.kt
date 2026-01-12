package at.pulseone.app

import android.util.Base64
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class AuditManager {

    suspend fun reportTicket(ticket: ParkingTicket, endpointUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(endpointUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.doOutput = true

                val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create()

                val signatureBase64 = ticket.signaturePath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
                    } else null
                }

                val pdfBase64 = ticket.pdfPath?.let { path ->
                    val file = File(path)
                    if (file.exists()) {
                        Base64.encodeToString(file.readBytes(), Base64.NO_WRAP)
                    } else null
                }

                val payload = mutableMapOf<String, Any?>(
                    "ticket" to ticket,
                    "signatureImage" to signatureBase64,
                    "signedDocument" to pdfBase64
                )

                val jsonPayload = gson.toJson(payload)

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(jsonPayload)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                connection.disconnect()

                responseCode == HttpURLConnection.HTTP_OK
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}