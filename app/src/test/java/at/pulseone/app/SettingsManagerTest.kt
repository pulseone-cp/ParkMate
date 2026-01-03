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
    private val sharedPreferences: SharedPreferences = mockk(relaxed = true)
    private val editor: SharedPreferences.Editor = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    @Before
    fun setup() {
        every { context.getSharedPreferences(any(), any()) } returns sharedPreferences
        every { sharedPreferences.edit() } returns editor
        settingsManager = SettingsManager(context)
    }

    @Test
    fun `test admin pin can be written and read`() {
        // Arrange
        val testPin = "4321"
        every { sharedPreferences.getString("admin_pin", any()) } returns testPin

        // Act
        settingsManager.adminPin = testPin
        val retrievedPin = settingsManager.adminPin

        // Assert
        verify { editor.putString("admin_pin", testPin) }
        assert(retrievedPin == testPin)
    }
}