package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID

class ValidateFragment : Fragment() {

    private lateinit var repository: ParkingTicketRepository
    private lateinit var settingsManager: SettingsManager
    private lateinit var validationHistoryAdapter: ValidationHistoryAdapter

    private val barcodeLauncher = registerForActivityResult(ScanContract()) {
        result ->
            if (!result.contents.isNullOrBlank()) {
                handleScanResult(result.contents)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_validate, container, false)

        repository = ParkingTicketRepository(requireActivity().application)
        settingsManager = SettingsManager(requireContext())

        val scanButton: Button = view.findViewById(R.id.scan_qr_button)
        val clearHistoryButton: Button = view.findViewById(R.id.clear_history_button)
        val recyclerView: RecyclerView = view.findViewById(R.id.validation_history_recycler_view)

        scanButton.setOnClickListener { launchCustomScanner() }
        clearHistoryButton.setOnClickListener { validationHistoryAdapter.clear() }

        validationHistoryAdapter = ValidationHistoryAdapter(mutableListOf())
        recyclerView.adapter = validationHistoryAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        return view
    }

    private fun launchCustomScanner() {
        val options = ScanOptions()
        options.setOrientationLocked(false)
        options.captureActivity = CustomScannerActivity::class.java
        barcodeLauncher.launch(options)
    }

    private fun handleScanResult(contents: String) {
        try {
            val guid = UUID.fromString(contents)
            lifecycleScope.launch {
                val ticket = repository.findTicketByGuid(guid.toString())
                val validationTime = Date()
                var expiresAt: Date? = null
                val validationStatus = when {
                    ticket == null -> "Not Found"
                    else -> {
                        val validityDays = settingsManager.ticketValidityDays
                        val calendar = Calendar.getInstance()
                        calendar.time = ticket.timestamp
                        calendar.add(Calendar.DAY_OF_YEAR, validityDays)
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                        calendar.set(Calendar.SECOND, 59)
                        expiresAt = calendar.time

                        if (validationTime.after(expiresAt)) {
                            "Expired"
                        } else {
                            "Valid"
                        }
                    }
                }
                validationHistoryAdapter.addRecord(ValidationRecord(guid.toString(), validationStatus, validationTime, expiresAt))
            }
        } catch (e: IllegalArgumentException) {
            validationHistoryAdapter.addRecord(ValidationRecord(contents, "Invalid QR Code", Date(), null))
        }
    }
}