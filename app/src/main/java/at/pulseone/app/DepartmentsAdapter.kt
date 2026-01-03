package at.pulseone.app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DepartmentsAdapter(
    private val departments: List<String>,
    private var defaultDepartment: String?,
    private val onRemoveClick: (String) -> Unit,
    private val onSetDefaultClick: (String) -> Unit
) : RecyclerView.Adapter<DepartmentsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val departmentNameTextView: TextView = itemView.findViewById(R.id.department_name_text_view)
        val defaultIndicatorTextView: TextView = itemView.findViewById(R.id.default_indicator_text_view)
        val removeDepartmentButton: Button = itemView.findViewById(R.id.remove_department_button)
        val setDefaultDepartmentButton: Button = itemView.findViewById(R.id.set_default_department_button)

        init {
            removeDepartmentButton.setOnClickListener {
                onRemoveClick(departments[adapterPosition])
            }
            setDefaultDepartmentButton.setOnClickListener {
                onSetDefaultClick(departments[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.department_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val department = departments[position]
        holder.departmentNameTextView.text = department

        if (department == defaultDepartment) {
            holder.defaultIndicatorTextView.visibility = View.VISIBLE
            holder.setDefaultDepartmentButton.visibility = View.GONE
        } else {
            holder.defaultIndicatorTextView.visibility = View.GONE
            holder.setDefaultDepartmentButton.visibility = View.VISIBLE
        }
        
        holder.removeDepartmentButton.text = holder.itemView.context.getString(R.string.button_remove)
        holder.setDefaultDepartmentButton.text = holder.itemView.context.getString(R.string.button_set_default)
        holder.defaultIndicatorTextView.text = holder.itemView.context.getString(R.string.text_default)
    }

    override fun getItemCount() = departments.size

    fun setDefaultDepartment(department: String) {
        defaultDepartment = department
        notifyDataSetChanged()
    }
}