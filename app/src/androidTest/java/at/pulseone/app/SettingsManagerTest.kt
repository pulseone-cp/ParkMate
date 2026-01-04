package at.pulseone.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsManagerTest {

    private lateinit var settingsManager: SettingsManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        // Use a unique name for the test preferences to avoid conflicts
        settingsManager = SettingsManager(context)
        // Clear any previous values
        context.getSharedPreferences("ParkMateSettings", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @After
    fun teardown() {
        // Clean up the created preferences file
        context.getSharedPreferences("ParkMateSettings", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test
    fun testAdminPin_isCorrectlySavedAndRead() {
        // Arrange
        val testPin = "9876"

        // Act
        settingsManager.adminPin = testPin
        val retrievedPin = settingsManager.adminPin

        // Assert
        assertEquals(testPin, retrievedPin)
    }

    @Test
    fun testDefaultDepartment_isCorrectlySavedAndRead() {
        // Arrange
        val testDepartment = "Finance"

        // Act
        settingsManager.defaultDepartment = testDepartment
        val retrievedDepartment = settingsManager.defaultDepartment

        // Assert
        assertEquals(testDepartment, retrievedDepartment)
    }
}