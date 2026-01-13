package at.pulseone.app

import android.app.Activity
import android.content.Intent
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
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

    private var pendingTicketData: PendingTicketData? = null

    data class PendingTicketData(
        val name: String,
        val surname: String,
        val licensePlate: String,
        val department: String,
        val departments: List<String>,
        val defaultDepartment: String?
    )

    private val agreementLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val signaturePath = result.data?.getStringExtra("SIGNATURE_PATH")
            val signatureBounds = result.data?.getParcelableExtra<RectF>("SIGNATURE_BOUNDS")
            pendingTicketData?.let { data ->
                var pdfPath = settingsManager.getDepartmentPdfPath(data.department)
                
                if (settingsManager.renderSignatureOnPdf && signaturePath != null && pdfPath != null && signatureBounds != null) {
                    val signedPdfPath = PdfSignatureUtils.renderSignatureOnPdf(this, pdfPath, signaturePath, signatureBounds)
                    if (signedPdfPath != null) {
                        pdfPath = signedPdfPath
                    }
                }
                
                createAndPrintTicket(
                    data.name,
                    data.surname,
                    data.licensePlate,
                    data.department,
                    data.departments,
                    data.defaultDepartment,
                    signaturePath,
                    pdfPath
                )
            }
        }
        pendingTicketData = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Add StrictMode to detect file URI exposure
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

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

            if (name.isNotBlank() && surname.isNotBlank() && department.isNotBlank()) {
                if (licensePlate.isBlank()) {
                    if (settingsManager.allowNoLicensePlate) {
                        AlertDialog.Builder(this)
                            .setTitle("No License Plate")
                            .setMessage("Did you really not come by car?")
                            .setPositiveButton("Yes") { _, _ ->
                                checkAgreementAndProceed(name, surname, "", department, departments, defaultDepartment)
                            }
                            .setNegativeButton("No", null)
                            .show()
                    } else {
                        Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    checkAgreementAndProceed(name, surname, licensePlate, department, departments, defaultDepartment)
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

    private fun createAndPrintTicket(
        name: String,
        surname: String,
        licensePlate: String,
        department: String,
        departments: List<String>,
        defaultDepartment: String?,
        signaturePath: String? = null,
        pdfPath: String? = null
    ) {
        lifecycleScope.launch {
            val timestamp = Date()
            val calendar = Calendar.getInstance()
            calendar.time = timestamp
            calendar.add(Calendar.HOUR_OF_DAY, settingsManager.ticketValidityHours)
            val validUntil = calendar.time

            val ticket = ParkingTicket(
                name = name,
                surname = surname,
                licensePlate = licensePlate,
                department = department,
                timestamp = timestamp,
                validFrom = timestamp,
                validUntil = validUntil,
                signaturePath = signaturePath,
                pdfPath = pdfPath
            )
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
    }

    private fun checkAgreementAndProceed(
        name: String,
        surname: String,
        licensePlate: String,
        department: String,
        departments: List<String>,
        defaultDepartment: String?
    ) {
        val pdfPath = settingsManager.getDepartmentPdfPath(department)
        if (pdfPath != null) {
            val pdfFile = File(pdfPath)
            if (pdfFile.exists()) {
                pendingTicketData = PendingTicketData(name, surname, licensePlate, department, departments, defaultDepartment)
                val pdfUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", pdfFile)
                val intent = Intent(this, AgreementActivity::class.java)
                intent.putExtra("PDF_URI", pdfUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                agreementLauncher.launch(intent)
            } else {
                createAndPrintTicket(name, surname, licensePlate, department, departments, defaultDepartment)
            }
        } else {
            createAndPrintTicket(name, surname, licensePlate, department, departments, defaultDepartment)
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