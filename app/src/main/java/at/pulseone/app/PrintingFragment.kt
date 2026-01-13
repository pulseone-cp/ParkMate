package at.pulseone.app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class PrintingFragment : Fragment() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var printerSpinner: Spinner
    private val printerList = mutableListOf<Pair<String, String>>()
    private lateinit var adapter: ArrayAdapter<String>

    private val requestBluetoothPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            loadPairedPrinters()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_printing, container, false)

        settingsManager = SettingsManager(requireContext())

        printerSpinner = view.findViewById(R.id.printer_spinner)
        val discoverPrintersButton: Button = view.findViewById(R.id.discover_printers_button)
        discoverPrintersButton.setText(R.string.button_refresh_printers)
        discoverPrintersButton.setOnClickListener { loadPairedPrinters() }

        val setDefaultPrinterButton: Button = view.findViewById(R.id.set_default_printer_button)
        val imprintEditText: TextInputEditText = view.findViewById(R.id.imprint_edit_text)
        val saveImprintButton: Button = view.findViewById(R.id.save_imprint_button)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, printerList.map { it.first })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        printerSpinner.adapter = adapter

        setDefaultPrinterButton.setOnClickListener { saveSelectedPrinter() }

        imprintEditText.setText(settingsManager.imprintText)
        saveImprintButton.setOnClickListener {
            settingsManager.imprintText = imprintEditText.text.toString()
            Toast.makeText(context, R.string.toast_imprint_saved, Toast.LENGTH_SHORT).show()
        }

        checkAndLoadPrinters()

        return view
    }

    private fun checkAndLoadPrinters() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                loadPairedPrinters()
            } else {
                requestBluetoothPermission.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            loadPairedPrinters()
        }
    }

    private fun loadPairedPrinters() {
        val bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(context, R.string.toast_bluetooth_unavailable, Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(context, R.string.toast_enable_bluetooth, Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
             return
        }

        val pairedDevices = bluetoothAdapter.bondedDevices
        printerList.clear()
        printerList.addAll(pairedDevices.map { it.name to it.address })
        
        adapter.clear()
        adapter.addAll(printerList.map { it.first })
        adapter.notifyDataSetChanged()

        val defaultPrinterAddress = settingsManager.printerTarget
        if (defaultPrinterAddress != null) {
            val defaultPrinter = printerList.find { it.second == defaultPrinterAddress }
            if (defaultPrinter != null) {
                printerSpinner.setSelection(adapter.getPosition(defaultPrinter.first))
            }
        }
    }

    private fun saveSelectedPrinter() {
        val selectedPrinterName = printerSpinner.selectedItem as? String
        if (selectedPrinterName != null) {
            val selectedPrinter = printerList.find { it.first == selectedPrinterName }
            if (selectedPrinter != null) {
                settingsManager.printerTarget = selectedPrinter.second
                Toast.makeText(context, getString(R.string.toast_printer_set_default, selectedPrinterName), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, R.string.toast_printer_save_error, Toast.LENGTH_SHORT).show()
            }
        }
    }
}