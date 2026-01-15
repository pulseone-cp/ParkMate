package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val usersButton: Button = view.findViewById(R.id.users_button)
        val displayButton: Button = view.findViewById(R.id.display_button)
        val liveAuditButton: Button = view.findViewById(R.id.live_audit_button)
        val signatureButton: Button = view.findViewById(R.id.signature_button)
        val formFieldsButton: Button = view.findViewById(R.id.form_fields_button)
        val dataGovernanceButton: Button = view.findViewById(R.id.data_governance_button)
        val testButton: Button = view.findViewById(R.id.test_button)

        usersButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UsersFragment())
                .addToBackStack(null)
                .commit()
        }

        displayButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DisplayFragment())
                .addToBackStack(null)
                .commit()
        }

        liveAuditButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LiveAuditFragment())
                .addToBackStack(null)
                .commit()
        }

        signatureButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SignatureSettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        formFieldsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, FormFieldsFragment())
                .addToBackStack(null)
                .commit()
        }

        dataGovernanceButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DataGovernanceFragment())
                .addToBackStack(null)
                .commit()
        }

        testButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TestFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}