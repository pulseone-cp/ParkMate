package at.pulseone.app

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("ParkMateSettings", Context.MODE_PRIVATE)

    var adminPin: String?
        get() = sharedPreferences.getString("admin_pin", "1234")
        set(value) = sharedPreferences.edit().putString("admin_pin", value).apply()

    var printerTarget: String?
        get() = sharedPreferences.getString("printer_target", null)
        set(value) = sharedPreferences.edit().putString("printer_target", value).apply()

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

    var ticketValidityHours: Int
        get() = sharedPreferences.getInt("ticket_validity_hours", 8)
        set(value) = sharedPreferences.edit().putInt("ticket_validity_hours", value).apply()

    var liveAuditEnabled: Boolean
        get() = sharedPreferences.getBoolean("live_audit_enabled", false)
        set(value) = sharedPreferences.edit().putBoolean("live_audit_enabled", value).apply()

    var liveAuditEndpoint: String?
        get() = sharedPreferences.getString("live_audit_endpoint", null)
        set(value) = sharedPreferences.edit().putString("live_audit_endpoint", value).apply()
}