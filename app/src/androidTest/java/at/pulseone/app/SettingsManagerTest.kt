package at.pulseone.app

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsManagerTest {

    private lateinit var settingsManager: SettingsManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sharedPreferences = context.getSharedPreferences("test_prefs", Context.MODE_PRIVATE)
        editor = sharedPreferences.edit()
        
        val mockContext = mockk<Context>()
        every { mockContext.getSharedPreferences(any(), any()) } returns sharedPreferences

        settingsManager = SettingsManager(mockContext)
    }

    @Test
    fun testAdminPin() {
        settingsManager.adminPin = "4321"
        assert(settingsManager.adminPin == "4321")
    }
}