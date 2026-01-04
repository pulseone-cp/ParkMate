package at.pulseone.app

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
                val jsonPayload = gson.toJson(ticket)

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