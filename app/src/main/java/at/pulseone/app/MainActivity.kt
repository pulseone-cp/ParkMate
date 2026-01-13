package at.pulseone.app

import android.app.Activity
import android.content.Intent
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.text.InputFilter
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Date
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var nameEditText: TextInputEditText
    private lateinit var nameInputLayout: TextInputLayout
    private lateinit var surnameEditText: TextInputEditText
    private lateinit var surnameInputLayout: TextInputLayout
    private lateinit var companyEditText: TextInputEditText
    private lateinit var companyInputLayout: TextInputLayout
    private lateinit var licensePlateEditText: TextInputEditText
    private lateinit var licensePlateInputLayout: TextInputLayout
    private lateinit var departmentAutoComplete: AutoCompleteTextView
    private lateinit var departmentMenu: TextInputLayout
    private lateinit var confirmButton: Button
    private lateinit var testPrintButton: Button
    private lateinit var welcomeHeadingTextView: TextView
    private lateinit var welcomeBodyTextView: TextView
    private lateinit var loadingOverlay: View
    private lateinit var settingsManager: SettingsManager
    private lateinit var repository: ParkingTicketRepository
    private lateinit var printingManager: PrintingManager
    private lateinit var auditManager: AuditManager
    private var departments: List<String> = emptyList()

    private var pendingTicketData: PendingTicketData? = null

    data class PendingTicketData(
        val name: String,
        val surname: String,
        val company: String?,
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

                if (signaturePath != null && pdfPath != null && signatureBounds != null) {
                    val signedPdfPath = PdfSignatureUtils.renderSignatureOnPdf(this, pdfPath, signaturePath, signatureBounds)
                    if (signedPdfPath != null) {
                        pdfPath = signedPdfPath
                    }
                }

                createAndPrintTicket(
                    data.name,
                    data.surname,
                    data.company,
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
        nameInputLayout = findViewById(R.id.name_input_layout)
        surnameEditText = findViewById(R.id.surname_edit_text)
        surnameInputLayout = findViewById(R.id.surname_input_layout)
        companyEditText = findViewById(R.id.company_edit_text)
        companyInputLayout = findViewById(R.id.company_input_layout)
        licensePlateEditText = findViewById(R.id.license_plate_edit_text)
        licensePlateInputLayout = findViewById(R.id.license_plate_input_layout)
        departmentAutoComplete = findViewById(R.id.department_autocomplete)
        departmentMenu = findViewById(R.id.department_menu)
        confirmButton = findViewById(R.id.confirm_button)
        testPrintButton = findViewById(R.id.test_print_button)
        welcomeHeadingTextView = findViewById(R.id.welcome_heading_text_view)
        welcomeBodyTextView = findViewById(R.id.welcome_body_text_view)
        loadingOverlay = findViewById(R.id.loading_overlay)

        loadWelcomeMessage()
        loadDepartments()
        setupLicensePlateInputFilter()
        setupFormFields()

        confirmButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val surname = surnameEditText.text.toString()
            val company = companyEditText.text.toString()
            val licensePlate = licensePlateEditText.text.toString()
            val department = departmentAutoComplete.text.toString()
            val defaultDepartment = settingsManager.defaultDepartment

            if (validateForm()) {
                if (licensePlate.isBlank()) {
                    if (settingsManager.allowNoLicensePlate) {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_no_license_plate_title)
                            .setMessage(R.string.dialog_no_license_plate_message)
                            .setPositiveButton(R.string.button_yes) { _, _ ->
                                checkAgreementAndProceed(name, surname, company, "", department, departments, defaultDepartment)
                            }
                            .setNegativeButton(R.string.button_no, null)
                            .show()
                    } else {
                        Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    checkAgreementAndProceed(name, surname, company, licensePlate, department, departments, defaultDepartment)
                }
            } else {
                Toast.makeText(this, R.string.toast_fill_required_fields, Toast.LENGTH_SHORT).show()
            }
        }

        testPrintButton.setOnClickListener {
            if (departments.isEmpty()) {
                Toast.makeText(this, R.string.toast_add_department_first, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val randomName = listOf("John", "Jane", "Peter", "Mary", "Chris", "Sarah").random()
            val randomSurname = listOf("Smith", "Doe", "Jones", "Williams", "Brown", "Davis").random()
            val randomCompany = listOf("Acme Inc.", "Wayne Enterprises", "Stark Industries", "Cyberdyne Systems").random()
            val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
            val randomLicense = (1..8)
                .map { allowedChars.random() }
                .joinToString("")
                .chunked(4)
                .joinToString("-")
            val randomDepartment = departments.random()

            nameEditText.setText(randomName)
            surnameEditText.setText(randomSurname)
            companyEditText.setText(randomCompany)
            licensePlateEditText.setText(randomLicense)
            departmentAutoComplete.setText(randomDepartment, false)

            confirmButton.performClick()
        }
    }

    override fun onResume() {
        super.onResume()
        loadWelcomeMessage()
        loadDepartments()
    }

    private fun loadDepartments() {
        departments = settingsManager.departments?.toList() ?: emptyList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, departments)
        departmentAutoComplete.setAdapter(adapter)

        val defaultDepartment = settingsManager.defaultDepartment
        if (defaultDepartment != null && departments.contains(defaultDepartment)) {
            if (departmentAutoComplete.text.isNullOrBlank()) {
                departmentAutoComplete.setText(defaultDepartment, false)
            }
        }
    }

    private fun createAndPrintTicket(
        name: String,
        surname: String,
        company: String?,
        licensePlate: String,
        department: String,
        departments: List<String>,
        defaultDepartment: String?,
        signaturePath: String? = null,
        pdfPath: String? = null
    ) {
        loadingOverlay.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val timestamp = Date()
                val calendar = Calendar.getInstance()
                calendar.time = timestamp
                calendar.add(Calendar.HOUR_OF_DAY, settingsManager.ticketValidityHours)
                val validUntil = calendar.time

                val ticket = ParkingTicket(
                    name = name,
                    surname = surname,
                    company = company,
                    licensePlate = licensePlate,
                    department = department,
                    timestamp = timestamp,
                    validFrom = timestamp,
                    validUntil = validUntil,
                    signaturePath = signaturePath,
                    pdfPath = pdfPath
                )
                val newTicket = repository.addTicket(ticket)

                if (settingsManager.liveAuditEnabled && !settingsManager.liveAuditEndpoint.isNullOrBlank()) {
                    val success = auditManager.reportTicket(newTicket, settingsManager.liveAuditEndpoint!!)
                    if (success) {
                        repository.updateTicket(newTicket.copy(isReported = true))
                    } else {
                        Toast.makeText(this@MainActivity, R.string.toast_audit_failed, Toast.LENGTH_SHORT).show()
                    }
                }

                // Print in a parallel thread (coroutine) "hoping for the best"
                lifecycleScope.launch(Dispatchers.IO) {
                    val printSuccess = printingManager.printTicket(newTicket)
                    if (!printSuccess) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, R.string.toast_printing_failed, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                // Clear fields
                nameEditText.text?.clear()
                surnameEditText.text?.clear()
                companyEditText.text?.clear()
                licensePlateEditText.text?.clear()
                if (defaultDepartment != null && departments.contains(defaultDepartment)) {
                    departmentAutoComplete.setText(defaultDepartment, false)
                } else {
                    departmentAutoComplete.text?.clear()
                }
            } finally {
                loadingOverlay.visibility = View.GONE
            }
        }
    }

    private fun checkAgreementAndProceed(
        name: String,
        surname: String,
        company: String?,
        licensePlate: String,
        department: String,
        departments: List<String>,
        defaultDepartment: String?
    ) {
        val pdfPath = settingsManager.getDepartmentPdfPath(department)
        if (pdfPath != null) {
            val pdfFile = File(pdfPath)
            if (pdfFile.exists()) {
                pendingTicketData = PendingTicketData(name, surname, company, licensePlate, department, departments, defaultDepartment)
                val pdfUri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", pdfFile)
                val intent = Intent(this, AgreementActivity::class.java)
                intent.putExtra("PDF_URI", pdfUri)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                agreementLauncher.launch(intent)
            } else {
                createAndPrintTicket(name, surname, company, licensePlate, department, departments, defaultDepartment)
            }
        } else {
            createAndPrintTicket(name, surname, company, licensePlate, department, departments, defaultDepartment)
        }
    }

    private fun setupFormFields() {
        nameInputLayout.visibility = if (settingsManager.isNameEnabled) View.VISIBLE else View.GONE
        if (settingsManager.isNameRequired) {
            nameInputLayout.hint = SpannableStringBuilder("* ").apply { 
                setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)), 0, 1, 0)
                append(getString(R.string.hint_name))
            }
        }

        surnameInputLayout.visibility = if (settingsManager.isSurnameEnabled) View.VISIBLE else View.GONE
        if (settingsManager.isSurnameRequired) {
            surnameInputLayout.hint = SpannableStringBuilder("* ").apply { 
                setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)), 0, 1, 0)
                append(getString(R.string.hint_surname))
            }
        }

        companyInputLayout.visibility = if (settingsManager.isCompanyEnabled) View.VISIBLE else View.GONE
        if (settingsManager.isCompanyRequired) {
            companyInputLayout.hint = SpannableStringBuilder("* ").apply { 
                setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)), 0, 1, 0)
                append(getString(R.string.hint_company))
            }
        }

        licensePlateInputLayout.visibility = if (settingsManager.isLicensePlateEnabled) View.VISIBLE else View.GONE
        if (settingsManager.isLicensePlateRequired) {
            licensePlateInputLayout.hint = SpannableStringBuilder("* ").apply { 
                setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)), 0, 1, 0)
                append(getString(R.string.hint_license_plate))
            }
        }

        departmentMenu.visibility = if (settingsManager.isDepartmentEnabled) View.VISIBLE else View.GONE
        if (settingsManager.isDepartmentRequired) {
            departmentMenu.hint = SpannableStringBuilder("* ").apply { 
                setSpan(ForegroundColorSpan(ContextCompat.getColor(this@MainActivity, android.R.color.holo_red_dark)), 0, 1, 0)
                append(getString(R.string.hint_department))
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        if (settingsManager.isNameRequired && nameEditText.text.isNullOrBlank()) {
            nameInputLayout.error = getString(R.string.error_name_required)
            isValid = false
        } else {
            nameInputLayout.error = null
        }

        if (settingsManager.isSurnameRequired && surnameEditText.text.isNullOrBlank()) {
            surnameInputLayout.error = getString(R.string.error_surname_required)
            isValid = false
        } else {
            surnameInputLayout.error = null
        }

        if (settingsManager.isCompanyRequired && companyEditText.text.isNullOrBlank()) {
            companyInputLayout.error = getString(R.string.error_company_required)
            isValid = false
        } else {
            companyInputLayout.error = null
        }

        if (settingsManager.isLicensePlateRequired && licensePlateEditText.text.isNullOrBlank()) {
            licensePlateInputLayout.error = getString(R.string.error_license_plate_required)
            isValid = false
        } else {
            licensePlateInputLayout.error = null
        }

        if (settingsManager.isDepartmentRequired && departmentAutoComplete.text.isNullOrBlank()) {
            departmentMenu.error = getString(R.string.error_department_required)
            isValid = false
        } else {
            departmentMenu.error = null
        }

        return isValid
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
        } else {
            welcomeHeadingTextView.visibility = View.GONE
        }

        if (!body.isNullOrBlank()) {
            welcomeBodyTextView.text = body
            welcomeBodyTextView.visibility = View.VISIBLE
        } else {
            welcomeBodyTextView.visibility = View.GONE
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

        builder.setPositiveButton(R.string.button_ok) { dialog, _ ->
            val password = passwordInput.text.toString()
            if (password == settingsManager.adminPin) {
                val intent = Intent(this, AdminActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, R.string.toast_incorrect_password, Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton(R.string.button_cancel) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}