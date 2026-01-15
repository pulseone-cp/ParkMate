package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText

class DataGovernanceFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data_governance, container, false)

        settingsManager = SettingsManager(requireContext())

        val autoDeleteSwitch: SwitchMaterial = view.findViewById(R.id.auto_delete_switch)
        val auditDeletionSwitch: SwitchMaterial = view.findViewById(R.id.audit_deletion_switch)
        val retentionPeriodEditText: TextInputEditText = view.findViewById(R.id.retention_period_edit_text)
        val saveButton: Button = view.findViewById(R.id.save_governance_button)

        autoDeleteSwitch.isChecked = settingsManager.autoDeleteEnabled
        auditDeletionSwitch.isChecked = settingsManager.auditDeletionEnabled
        retentionPeriodEditText.setText(settingsManager.autoDeleteDays.toString())

        saveButton.setOnClickListener {
            val daysStr = retentionPeriodEditText.text.toString()
            if (daysStr.isBlank()) {
                Toast.makeText(requireContext(), "Please enter retention period", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val days = daysStr.toIntOrNull()
            if (days == null || days <= 0) {
                Toast.makeText(requireContext(), "Please enter a valid number of days", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            settingsManager.autoDeleteEnabled = autoDeleteSwitch.isChecked
            settingsManager.autoDeleteDays = days
            settingsManager.auditDeletionEnabled = auditDeletionSwitch.isChecked

            Toast.makeText(requireContext(), R.string.toast_settings_saved, Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }

        return view
    }
}