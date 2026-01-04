package at.pulseone.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var surnameEditText: TextInputEditText
    private lateinit var licensePlateEditText: TextInputEditText
    private lateinit var departmentAutoComplete: AutoCompleteTextView
    private lateinit var confirmButton: Button
    private lateinit var testPrintButton: Button
    private lateinit var welcomeHeadingTextView: TextView
    private lateinit var welcomeBodyTextView: TextView
    private lateinit var settingsManager: SettingsManager
    private lateinit var repository: ParkingTicketRepository
    private lateinit var printingManager: PrintingManager
    private lateinit var auditManager: AuditManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsManager = SettingsManager(this)
        repository = ParkingTicketRepository(application)
        printingManager = PrintingManager(this)
        auditManager = AuditManager()

        nameEditText = findViewById(R.id.name_edit_text)
        surnameEditText = findViewById(R.id.surname_edit_text)
        licensePlateEditText = findViewById(R.id.license_plate_edit_text)
        departmentAutoComplete = findViewById(R.id.department_autocomplete)
        confirmButton = findViewById(R.id.confirm_button)
        testPrintButton = findViewById(R.id.test_print_button)
        welcomeHeadingTextView = findViewById(R.id.welcome_heading_text_view)
        welcomeBodyTextView = findViewById(R.id.welcome_body_text_view)

        loadWelcomeMessage()
        setupLicensePlateInputFilter()

        val departments = settingsManager.departments?.toList() ?: emptyList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departments)
        departmentAutoComplete.setAdapter(adapter)

        val defaultDepartment = settingsManager.defaultDepartment
        if (defaultDepartment != null && departments.contains(defaultDepartment)) {
            departmentAutoComplete.setText(defaultDepartment, false)
        }

        confirmButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val licensePlate = licensePlateEditText.text.toString()
            val department = departmentAutoComplete.text.toString()

            if (name.isNotBlank() && surname.isNotBlank() && licensePlate.isNotBlank() && department.isNotBlank()) {
                lifecycleScope.launch {
                    val ticket = ParkingTicket(name = name, surname = surname, licensePlate = licensePlate, department = department, timestamp = Date())
                    val newTicket = repository.addTicket(ticket)
                    printingManager.printTicket(newTicket)

                    if (settingsManager.liveAuditEnabled && !settingsManager.liveAuditEndpoint.isNullOrBlank()) {
                        val success = auditManager.reportTicket(newTicket, settingsManager.liveAuditEndpoint!!)
                        if (success) {
                            repository.updateTicket(newTicket.copy(isReported = true))
                        }
                    }

                    // Clear fields
                    nameEditText.text?.clear()
                    surnameEditText.text?.clear()
                    licensePlateEditText.text?.clear()
                    if (defaultDepartment != null && departments.contains(defaultDepartment)) {
                        departmentAutoComplete.setText(defaultDepartment, false)
                    } else {
                        departmentAutoComplete.text?.clear()
                    }
                }
            } else {
                Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show()
            }
        }

        testPrintButton.setOnClickListener {
            if (departments.isEmpty()) {
                Toast.makeText(this, "Please add a department first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val randomName = listOf("John", "Jane", "Peter", "Mary", "Chris", "Sarah").random()
            val randomSurname = listOf("Smith", "Doe", "Jones", "Williams", "Brown", "Davis").random()
            val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val randomLicense = (1..8)
                .map { allowedChars.random() }
                .joinToString("")
                .chunked(4)
                .joinToString("-")
            val randomDepartment = departments.random()

            nameEditText.setText(randomName)
            surnameEditText.setText(randomSurname)
            licensePlateEditText.setText(randomLicense)
            departmentAutoComplete.setText(randomDepartment, false)

            confirmButton.performClick()
        }
    }

    private fun setupLicensePlateInputFilter() {
        val filter = InputFilter { source, start, end, dest, dstart, dend ->
            val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-"
            var keepOriginal = true
            val sb = StringBuilder(end - start)
            for (i in start until end) {
                val c = source[i]
                if (allowedChars.contains(c, ignoreCase = true)) {
                    sb.append(c)
                } else {
                    keepOriginal = false
                }
            }
            if (keepOriginal) {
                null
            } else {
                if (source is String) {
                    sb.toString().uppercase()
                } else {
                    sb
                }
            }
        }
        licensePlateEditText.filters = arrayOf(filter, InputFilter.AllCaps())
    }

    private fun loadWelcomeMessage() {
        val heading = settingsManager.welcomeMessageHeading
        val body = settingsManager.welcomeMessageBody

        if (!heading.isNullOrBlank()) {
            welcomeHeadingTextView.text = heading
            welcomeHeadingTextView.visibility = View.VISIBLE
        }

        if (!body.isNullOrBlank()) {
            welcomeBodyTextView.text = body
            welcomeBodyTextView.visibility = View.VISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_admin -> {
                showAdminPasswordDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAdminPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.admin_access_title)

        val passwordInput = EditText(this)
        passwordInput.inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
        builder.setView(passwordInput)

        builder.setPositiveButton("OK") { dialog, _ ->
            val password = passwordInput.text.toString()
            if (password == settingsManager.adminPin) {
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, R.string.toast_incorrect_password, Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}