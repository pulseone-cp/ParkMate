package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class UsersFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_users, container, false)

        settingsManager = SettingsManager(requireContext())

        val currentPinEditText: TextInputEditText = view.findViewById(R.id.current_pin_edit_text)
        val newPinEditText: TextInputEditText = view.findViewById(R.id.new_pin_edit_text)
        val confirmPinEditText: TextInputEditText = view.findViewById(R.id.confirm_pin_edit_text)
        val changePinButton: Button = view.findViewById(R.id.change_pin_button)

        changePinButton.setOnClickListener {
            val currentPin = currentPinEditText.text.toString()
            val newPin = newPinEditText.text.toString()
            val confirmPin = confirmPinEditText.text.toString()

            if (currentPin == settingsManager.adminPin) {
                if (newPin.isNotBlank() && newPin == confirmPin) {
                    settingsManager.adminPin = newPin
                    Toast.makeText(context, R.string.toast_pin_changed, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.toast_pin_not_match, Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, R.string.toast_incorrect_current_pin, Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}