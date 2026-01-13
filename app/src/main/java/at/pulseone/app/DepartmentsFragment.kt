package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream

class DepartmentsFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var departmentsAdapter: DepartmentsAdapter
    private lateinit var departments: MutableList<String>
    private var departmentForPdf: String? = null

    private val pdfPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val department = departmentForPdf ?: return@let
            val inputStream = requireContext().contentResolver.openInputStream(it)
            val file = File(requireContext().filesDir, "agreement_${department}.pdf")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            settingsManager.setDepartmentPdfPath(department, file.absolutePath)
            departmentsAdapter.notifyDataSetChanged()
            Toast.makeText(context, getString(R.string.toast_pdf_uploaded, department), Toast.LENGTH_SHORT).show()
        }
    }

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
        departmentsAdapter = DepartmentsAdapter(
            departments,
            settingsManager.defaultDepartment,
            settingsManager,
            { department ->
                // Remove department
                departments.remove(department)
                settingsManager.departments = departments.toSet()
                departmentsAdapter.notifyDataSetChanged()
                Toast.makeText(context, getString(R.string.toast_department_removed, department), Toast.LENGTH_SHORT).show()
            },
            { department ->
                // Set default department
                settingsManager.defaultDepartment = department
                departmentsAdapter.setDefaultDepartment(department)
                Toast.makeText(context, getString(R.string.toast_department_set_default, department), Toast.LENGTH_SHORT).show()
            },
            { department ->
                // Upload PDF
                departmentForPdf = department
                pdfPickerLauncher.launch("application/pdf")
            }
        )
        departmentsRecyclerView.adapter = departmentsAdapter

        addDepartmentButton.setOnClickListener {
            val newDepartment = newDepartmentEditText.text.toString()
            if (newDepartment.isNotBlank()) {
                if (departments.contains(newDepartment)) {
                    Toast.makeText(context, R.string.error_dept_exists, Toast.LENGTH_SHORT).show()
                } else {
                    departments.add(newDepartment)
                    settingsManager.departments = departments.toSet()
                    departmentsAdapter.notifyDataSetChanged()
                    newDepartmentEditText.text?.clear()
                }
            } else {
                Toast.makeText(context, R.string.error_dept_empty, Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}