package at.pulseone.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {

    private val historyFragment = HistoryFragment()
    private val validateFragment = ValidateFragment()
    private val printingFragment = PrintingFragment()
    private val departmentsFragment = DepartmentsFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val bottomNavigation: BottomNavigationView = findViewById(R.id.bottom_navigation)

        setCurrentFragment(historyFragment)

        bottomNavigation.setOnItemSelectedListener { // Use the modern listener
            when (it.itemId) {
                R.id.nav_history -> setCurrentFragment(historyFragment)
                R.id.nav_validate -> setCurrentFragment(validateFragment)
                R.id.nav_printing -> setCurrentFragment(printingFragment)
                R.id.nav_departments -> setCurrentFragment(departmentsFragment)
                R.id.nav_settings -> setCurrentFragment(settingsFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_exit_admin -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}