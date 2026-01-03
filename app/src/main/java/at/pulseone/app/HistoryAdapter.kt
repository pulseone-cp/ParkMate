package at.pulseone.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val tickets: MutableList<ParkingTicket>,
    private val onReprintClick: (ParkingTicket) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val licensePlateTextView: TextView = itemView.findViewById(R.id.license_plate_text_view)
        val nameTextView: TextView = itemView.findViewById(R.id.name_text_view)
        val departmentTextView: TextView = itemView.findViewById(R.id.department_text_view)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestamp_text_view)

        init {
            itemView.setOnClickListener {
                onReprintClick(tickets[adapterPosition])
            }
        }
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
        holder.timestampTextView.text = ticket.timestamp.toString()
    }

    override fun getItemCount() = tickets.size
}