package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.switchmaterial.SwitchMaterial

class FormFieldsFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_form_fields, container, false)
        settingsManager = SettingsManager(requireContext())

        val nameEnabledSwitch: SwitchMaterial = view.findViewById(R.id.name_enabled_switch)
        val nameRequiredCheckbox: MaterialCheckBox = view.findViewById(R.id.name_required_checkbox)
        val surnameEnabledSwitch: SwitchMaterial = view.findViewById(R.id.surname_enabled_switch)
        val surnameRequiredCheckbox: MaterialCheckBox = view.findViewById(R.id.surname_required_checkbox)
        val companyEnabledSwitch: SwitchMaterial = view.findViewById(R.id.company_enabled_switch)
        val companyRequiredCheckbox: MaterialCheckBox = view.findViewById(R.id.company_required_checkbox)
        val licensePlateEnabledSwitch: SwitchMaterial = view.findViewById(R.id.license_plate_enabled_switch)
        val licensePlateRequiredCheckbox: MaterialCheckBox = view.findViewById(R.id.license_plate_required_checkbox)
        val departmentEnabledSwitch: SwitchMaterial = view.findViewById(R.id.department_enabled_switch)
        val departmentRequiredCheckbox: MaterialCheckBox = view.findViewById(R.id.department_required_checkbox)

        nameEnabledSwitch.isChecked = settingsManager.isNameEnabled
        nameRequiredCheckbox.isChecked = settingsManager.isNameRequired
        surnameEnabledSwitch.isChecked = settingsManager.isSurnameEnabled
        surnameRequiredCheckbox.isChecked = settingsManager.isSurnameRequired
        companyEnabledSwitch.isChecked = settingsManager.isCompanyEnabled
        companyRequiredCheckbox.isChecked = settingsManager.isCompanyRequired
        licensePlateEnabledSwitch.isChecked = settingsManager.isLicensePlateEnabled
        licensePlateRequiredCheckbox.isChecked = settingsManager.isLicensePlateRequired
        departmentEnabledSwitch.isChecked = settingsManager.isDepartmentEnabled
        departmentRequiredCheckbox.isChecked = settingsManager.isDepartmentRequired

        nameEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> settingsManager.isNameEnabled = isChecked }
        nameRequiredCheckbox.setOnCheckedChangeListener { _, isChecked -> settingsManager.isNameRequired = isChecked }
        surnameEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> settingsManager.isSurnameEnabled = isChecked }
        surnameRequiredCheckbox.setOnCheckedChangeListener { _, isChecked -> settingsManager.isSurnameRequired = isChecked }
        companyEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> settingsManager.isCompanyEnabled = isChecked }
        companyRequiredCheckbox.setOnCheckedChangeListener { _, isChecked -> settingsManager.isCompanyRequired = isChecked }
        licensePlateEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> settingsManager.isLicensePlateEnabled = isChecked }
        licensePlateRequiredCheckbox.setOnCheckedChangeListener { _, isChecked -> settingsManager.isLicensePlateRequired = isChecked }
        departmentEnabledSwitch.setOnCheckedChangeListener { _, isChecked -> settingsManager.isDepartmentEnabled = isChecked }
        departmentRequiredCheckbox.setOnCheckedChangeListener { _, isChecked -> settingsManager.isDepartmentRequired = isChecked }

        return view
    }
}