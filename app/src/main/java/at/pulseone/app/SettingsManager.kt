package at.pulseone.app

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ParkMateSettings", Context.MODE_PRIVATE)

    var adminPin: String?
        get() = sharedPreferences.getString("admin_pin", "1234")
        set(value) = sharedPreferences.edit().putString("admin_pin", value).apply()

    var defaultPrinter: String?
        get() = sharedPreferences.getString("default_printer", null)
        set(value) = sharedPreferences.edit().putString("default_printer", value).apply()

    var departments: Set<String>?
        get() = sharedPreferences.getStringSet("departments", setOf("Sales", "Marketing", "Development", "HR"))
        set(value) = sharedPreferences.edit().putStringSet("departments", value).apply()

    var defaultDepartment: String?
        get() = sharedPreferences.getString("default_department", "Sales")
        set(value) = sharedPreferences.edit().putString("default_department", value).apply()

    var imprintText: String?
        get() = sharedPreferences.getString("imprint_text", null)
        set(value) = sharedPreferences.edit().putString("imprint_text", value).apply()

    var welcomeMessageHeading: String?
        get() = sharedPreferences.getString("welcome_heading", null)
        set(value) = sharedPreferences.edit().putString("welcome_heading", value).apply()

    var welcomeMessageBody: String?
        get() = sharedPreferences.getString("welcome_body", null)
        set(value) = sharedPreferences.edit().putString("welcome_body", value).apply()

    var ticketValidityDays: Int
        get() = sharedPreferences.getInt("ticket_validity_days", 1)
        set(value) = sharedPreferences.edit().putInt("ticket_validity_days", value).apply()
}