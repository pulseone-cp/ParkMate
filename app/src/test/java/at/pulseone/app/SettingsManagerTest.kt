package at.pulseone.app

import android.content.Context
import android.content.SharedPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SettingsManagerTest {

    private lateinit var settingsManager: SettingsManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setup() {
        sharedPreferences = mockk()
        editor = mockk(relaxed = true)
        val context = mockk<Context>()

        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor

        settingsManager = SettingsManager(context)
    }

    @Test
    fun testAdminPin() {
        every { sharedPreferences.getString("admin_pin", "1234") } returns "4321"
        settingsManager.adminPin = "4321"
        verify { editor.putString("admin_pin", "4321") }
        assert(settingsManager.adminPin == "4321")
    }
}