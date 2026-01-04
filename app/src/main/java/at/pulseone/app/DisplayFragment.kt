package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class DisplayFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_display, container, false)

        settingsManager = SettingsManager(requireContext())

        val welcomeHeadingEditText: TextInputEditText = view.findViewById(R.id.welcome_heading_edit_text)
        val welcomeBodyEditText: TextInputEditText = view.findViewById(R.id.welcome_body_edit_text)
        val saveWelcomeMessageButton: Button = view.findViewById(R.id.save_welcome_message_button)
        val ticketValidityEditText: TextInputEditText = view.findViewById(R.id.ticket_validity_edit_text)
        val saveValidityButton: Button = view.findViewById(R.id.save_validity_button)
        val liveAuditSwitch: SwitchCompat = view.findViewById(R.id.live_audit_switch)
        val liveAuditEndpointEditText: TextInputEditText = view.findViewById(R.id.live_audit_endpoint_edit_text)
        val saveAuditButton: Button = view.findViewById(R.id.save_audit_button)

        welcomeHeadingEditText.setText(settingsManager.welcomeMessageHeading)
        welcomeBodyEditText.setText(settingsManager.welcomeMessageBody)
        ticketValidityEditText.setText(settingsManager.ticketValidityHours.toString())
        liveAuditSwitch.isChecked = settingsManager.liveAuditEnabled
        liveAuditEndpointEditText.setText(settingsManager.liveAuditEndpoint)

        saveWelcomeMessageButton.setOnClickListener {
            settingsManager.welcomeMessageHeading = welcomeHeadingEditText.text.toString()
            settingsManager.welcomeMessageBody = welcomeBodyEditText.text.toString()
            Toast.makeText(context, R.string.toast_welcome_message_saved, Toast.LENGTH_SHORT).show()
        }

        saveValidityButton.setOnClickListener {
            val validityHours = ticketValidityEditText.text.toString().toIntOrNull()
            if (validityHours != null && validityHours in 1..999) {
                settingsManager.ticketValidityHours = validityHours
                Toast.makeText(context, "Ticket validity saved", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please enter a number between 1 and 999", Toast.LENGTH_SHORT).show()
            }
        }

        saveAuditButton.setOnClickListener {
            settingsManager.liveAuditEnabled = liveAuditSwitch.isChecked
            settingsManager.liveAuditEndpoint = liveAuditEndpointEditText.text.toString()
            Toast.makeText(context, "Audit settings saved", Toast.LENGTH_SHORT).show()
        }

        return view
    }
}