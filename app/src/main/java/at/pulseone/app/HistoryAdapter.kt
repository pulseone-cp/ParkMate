package at.pulseone.app

import android.app.Application
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class HistoryAdapter(
    private val tickets: MutableList<ParkingTicket>,
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val licensePlateTextView: TextView = itemView.findViewById(R.id.license_plate_text_view)
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val departmentTextView: TextView = itemView.findViewById(R.id.department_text_view)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)
        val viewButton: Button = itemView.findViewById(R.id.view_button)
        val printButton: Button = itemView.findViewById(R.id.print_button)
        val reportedIcon: ImageView = itemView.findViewById(R.id.reported_icon)
        val uploadButton: ImageButton = itemView.findViewById(R.id.upload_button)
        val signaturePreviewImageView: ImageView = itemView.findViewById(R.id.signature_preview_image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.licensePlateTextView.text = if (ticket.licensePlate.isBlank()) "BESUCHER" else ticket.licensePlate
        holder.nameTextView.text = "${ticket.name} ${ticket.surname}"
        holder.departmentTextView.text = ticket.department
        holder.timestampTextView.text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(ticket.timestamp)

        if (ticket.signaturePath != null) {
            val file = File(ticket.signaturePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                holder.signaturePreviewImageView.setImageBitmap(bitmap)
                holder.signaturePreviewImageView.visibility = View.VISIBLE
                holder.signaturePreviewImageView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, SignatureDetailActivity::class.java)
                    intent.putExtra("SIGNATURE_PATH", ticket.signaturePath)
                    holder.itemView.context.startActivity(intent)
                }
            } else {
                holder.signaturePreviewImageView.visibility = View.GONE
            }
        } else {
            holder.signaturePreviewImageView.visibility = View.GONE
        }

        holder.viewButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, TicketViewActivity::class.java)
            intent.putExtra("ticket_guid", ticket.guid)
            holder.itemView.context.startActivity(intent)
        }

        holder.printButton.setOnClickListener {
            holder.itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                val printingManager = PrintingManager(holder.itemView.context)
                printingManager.printTicket(ticket)
            }
        }

        val settingsManager = SettingsManager(holder.itemView.context)

        if (ticket.isReported) {
            holder.reportedIcon.visibility = View.VISIBLE
            holder.uploadButton.visibility = View.GONE
        } else {
            holder.reportedIcon.visibility = View.GONE
            if (settingsManager.liveAuditEnabled) {
                holder.uploadButton.visibility = View.VISIBLE
            }
        }

        holder.uploadButton.setOnClickListener {
            holder.itemView.findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                val auditManager = AuditManager()
                val endpointUrl = settingsManager.liveAuditEndpoint
                if (!endpointUrl.isNullOrBlank()) {
                    val success = auditManager.reportTicket(ticket, endpointUrl)
                    if (success) {
                        val repository = ParkingTicketRepository(holder.itemView.context.applicationContext as Application)
                        val updatedTicket = ticket.copy(isReported = true)
                        repository.updateTicket(updatedTicket)
                        tickets[position] = updatedTicket
                        notifyItemChanged(position)
                        Toast.makeText(holder.itemView.context, "Ticket reported successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(holder.itemView.context, "Failed to report ticket", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(holder.itemView.context, "Live audit endpoint not set", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount() = tickets.size
}