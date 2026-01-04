package at.pulseone.app

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ticket = tickets[position]
        holder.licensePlateTextView.text = ticket.licensePlate
        holder.nameTextView.text = "${ticket.name} ${ticket.surname}"
        holder.departmentTextView.text = ticket.department
        holder.timestampTextView.text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(ticket.timestamp)

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

        if (ticket.isReported) {
            holder.reportedIcon.visibility = View.VISIBLE
        } else {
            holder.reportedIcon.visibility = View.GONE
        }
    }

    override fun getItemCount() = tickets.size
}