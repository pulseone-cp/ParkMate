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

        return view
    }
}