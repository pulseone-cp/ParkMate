package at.pulseone.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText

class PrintingFragment : Fragment() {

    private val printers = arrayOf("Printer 1", "Printer 2", "Printer 3") // Dummy data
    private lateinit var settingsManager: SettingsManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_printing, container, false)

        settingsManager = SettingsManager(requireContext())

        val printerSpinner: Spinner = view.findViewById(R.id.printer_spinner)
        val setDefaultPrinterButton: Button = view.findViewById(R.id.set_default_printer_button)
        val imprintEditText: TextInputEditText = view.findViewById(R.id.imprint_edit_text)
        val saveImprintButton: Button = view.findViewById(R.id.save_imprint_button)

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, printers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        printerSpinner.adapter = adapter

        val defaultPrinter = settingsManager.defaultPrinter
        if (defaultPrinter != null) {
            val spinnerPosition = adapter.getPosition(defaultPrinter)
            printerSpinner.setSelection(spinnerPosition)
        }

        imprintEditText.setText(settingsManager.imprintText)

        setDefaultPrinterButton.setOnClickListener {
            val selectedPrinter = printerSpinner.selectedItem.toString()
            settingsManager.defaultPrinter = selectedPrinter
            Toast.makeText(context, getString(R.string.toast_printer_set_default, selectedPrinter), Toast.LENGTH_SHORT).show()
        }

        saveImprintButton.setOnClickListener {
            val newImprintText = imprintEditText.text.toString()
            settingsManager.imprintText = newImprintText
            Toast.makeText(context, R.string.toast_imprint_saved, Toast.LENGTH_SHORT).show()
        }

        return view
    }
}