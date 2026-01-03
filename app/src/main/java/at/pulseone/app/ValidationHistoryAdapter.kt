package at.pulseone.app

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class ValidationHistoryAdapter(private val records: MutableList<ValidationRecord>) : RecyclerView.Adapter<ValidationHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusIcon: ImageView = itemView.findViewById(R.id.status_icon)
        val guidTextView: TextView = itemView.findViewById(R.id.guid_text_view)
        val statusTextView: TextView = itemView.findViewById(R.id.status_text_view)
        val validatedAtTextView: TextView = itemView.findViewById(R.id.validated_at_text_view)
        val expiresAtTextView: TextView = itemView.findViewById(R.id.expires_at_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.validation_history_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = records[position]
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

        holder.guidTextView.text = record.guid
        holder.statusTextView.text = "Status: ${record.status}"
        holder.validatedAtTextView.text = "Validated at: ${sdf.format(record.validatedAt)}"

        if (record.expiresAt != null) {
            holder.expiresAtTextView.text = "Expires at: ${sdf.format(record.expiresAt)}"
            holder.expiresAtTextView.visibility = View.VISIBLE
        } else {
            holder.expiresAtTextView.visibility = View.GONE
        }

        when (record.status) {
            "Valid" -> {
                holder.statusIcon.setImageResource(R.drawable.ic_check_circle)
                holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
            }
            else -> {
                holder.statusIcon.setImageResource(R.drawable.ic_error)
                holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            }
        }
    }

    override fun getItemCount() = records.size

    fun addRecord(record: ValidationRecord) {
        records.add(0, record)
        notifyItemInserted(0)
    }

    fun clear() {
        val size = records.size
        records.clear()
        notifyItemRangeRemoved(0, size)
    }
}