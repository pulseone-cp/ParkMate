package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
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

        welcomeHeadingEditText.setText(settingsManager.welcomeMessageHeading)
        welcomeBodyEditText.setText(settingsManager.welcomeMessageBody)

        saveWelcomeMessageButton.setOnClickListener {
            val newHeading = welcomeHeadingEditText.text.toString()
            val newBody = welcomeBodyEditText.text.toString()

            settingsManager.welcomeMessageHeading = newHeading
            settingsManager.welcomeMessageBody = newBody

            Toast.makeText(context, R.string.toast_welcome_message_saved, Toast.LENGTH_SHORT).show()
        }

        return view
    }
}