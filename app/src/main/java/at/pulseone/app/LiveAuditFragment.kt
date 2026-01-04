package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LiveAuditFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var auditManager: AuditManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_live_audit, container, false)

        settingsManager = SettingsManager(requireContext())
        auditManager = AuditManager()

        val liveAuditSwitch: SwitchCompat = view.findViewById(R.id.live_audit_switch)
        val liveAuditEndpointEditText: TextInputEditText = view.findViewById(R.id.live_audit_endpoint_edit_text)
        val saveAuditButton: Button = view.findViewById(R.id.save_audit_button)
        val testEndpointButton: Button = view.findViewById(R.id.test_endpoint_button)

        liveAuditSwitch.isChecked = settingsManager.liveAuditEnabled
        liveAuditEndpointEditText.setText(settingsManager.liveAuditEndpoint)

        saveAuditButton.setOnClickListener {
            settingsManager.liveAuditEnabled = liveAuditSwitch.isChecked
            settingsManager.liveAuditEndpoint = liveAuditEndpointEditText.text.toString()
            Toast.makeText(context, "Audit settings saved", Toast.LENGTH_SHORT).show()
        }

        testEndpointButton.setOnClickListener {
            val endpointUrl = liveAuditEndpointEditText.text.toString()
            if (endpointUrl.isNotBlank()) {
                lifecycleScope.launch {
                    val success = auditManager.reportTicket(ParkingTicket(name = "Test", surname = "Ticket", licensePlate = "TEST-000", department = "Test", timestamp = java.util.Date()), endpointUrl)
                    if (success) {
                        Toast.makeText(context, "Endpoint test successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Endpoint test failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(context, "Please enter an endpoint URL", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}