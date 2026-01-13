package at.pulseone.app

import android.util.Base64
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.doOutput = true

                val payload = AuditPayload(
                    ticket = ticket,
                    signature = fileToBase64(ticket.signaturePath),
                    document = fileToBase64(ticket.pdfPath)
                )

                val gson = GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create()
                val json = gson.toJson(payload)
                val bytes = json.toByteArray(Charsets.UTF_8)

                connection.setFixedLengthStreamingMode(bytes.size)
                connection.outputStream.use { os ->
                    os.write(bytes)
                    os.flush()
                }

                val responseCode = connection.responseCode
                connection.disconnect()

                responseCode in 200..299
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun fileToBase64(path: String?): String? {
        if (path == null) return null
        val file = File(path)
        if (!file.exists()) return null
        return try {
            val bytes = file.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}