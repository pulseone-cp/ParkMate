package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class DepartmentsFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var departmentsAdapter: DepartmentsAdapter
    private lateinit var departments: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_departments, container, false)

        settingsManager = SettingsManager(requireContext())
        departments = settingsManager.departments?.toMutableList() ?: mutableListOf()

        val newDepartmentEditText: TextInputEditText = view.findViewById(R.id.new_department_edit_text)
        val addDepartmentButton: Button = view.findViewById(R.id.add_department_button)
        val departmentsRecyclerView: RecyclerView = view.findViewById(R.id.departments_recycler_view)

        departmentsRecyclerView.layoutManager = LinearLayoutManager(context)
        departmentsAdapter = DepartmentsAdapter(departments, settingsManager.defaultDepartment, {
            // Remove department
            departments.remove(it)
            settingsManager.departments = departments.toSet()
            departmentsAdapter.notifyDataSetChanged()
            Toast.makeText(context, getString(R.string.toast_department_removed, it), Toast.LENGTH_SHORT).show()
        }, {
            // Set default department
            settingsManager.defaultDepartment = it
            departmentsAdapter.setDefaultDepartment(it)
            Toast.makeText(context, getString(R.string.toast_department_set_default, it), Toast.LENGTH_SHORT).show()
        })
        departmentsRecyclerView.adapter = departmentsAdapter

        addDepartmentButton.setOnClickListener {
            val newDepartment = newDepartmentEditText.text.toString()
            if (newDepartment.isNotBlank()) {
                departments.add(newDepartment)
                settingsManager.departments = departments.toSet()
                departmentsAdapter.notifyDataSetChanged()
                newDepartmentEditText.text?.clear()
            }
        }

        return view
    }
}