package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment

class SignatureSettingsFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signature_settings, container, false)
        settingsManager = SettingsManager(requireContext())

        val radioGroup = view.findViewById<RadioGroup>(R.id.thickness_radio_group)
        val smallRadioButton = view.findViewById<RadioButton>(R.id.small_thickness_radio_button)
        val mediumRadioButton = view.findViewById<RadioButton>(R.id.medium_thickness_radio_button)
        val largeRadioButton = view.findViewById<RadioButton>(R.id.large_thickness_radio_button)

        when (settingsManager.signatureStrokeWidth) {
            5f -> smallRadioButton.isChecked = true
            8f -> mediumRadioButton.isChecked = true
            12f -> largeRadioButton.isChecked = true
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.small_thickness_radio_button -> settingsManager.signatureStrokeWidth = 5f
                R.id.medium_thickness_radio_button -> settingsManager.signatureStrokeWidth = 8f
                R.id.large_thickness_radio_button -> settingsManager.signatureStrokeWidth = 12f
            }
        }

        return view
    }
}