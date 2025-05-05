package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.utils.SettingsManager
import com.example.financetracker.utils.SharedPrefManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileActivity : AppCompatActivity() {

    private lateinit var switchTheme: Switch
    private lateinit var spinnerLanguage: Spinner
    private lateinit var btnLogout: Button
    private lateinit var btnExport: Button
    private lateinit var btnImport: Button
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeleteAccount: Button
    private lateinit var etOldPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tvUserName: TextView
    private lateinit var settingsManager: SettingsManager
    private lateinit var prefManager: SharedPrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()

        settingsManager = SettingsManager(this)
        prefManager = SharedPrefManager(this)

        // Bind views
        switchTheme = findViewById(R.id.switchTheme)
        spinnerLanguage = findViewById(R.id.spLanguage)
        btnLogout = findViewById(R.id.btnLogout)
        btnExport = findViewById(R.id.btnExport)
        btnImport = findViewById(R.id.btnImport)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        etOldPassword = findViewById(R.id.etOldPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        bottomNav = findViewById(R.id.bottom_nav)
        tvUserName = findViewById(R.id.tvUserName)

        // Display current user
        val currentName = prefManager.getCurrentUserName()
        tvUserName.text = currentName ?: "User"

        setupLanguageSpinner()
        setupThemeToggle()
        setupLogout()
        setupBottomNav()
        setupBackupButtons()
        setupPasswordChange()
        setupAccountDeletion()
    }

    private fun setupPasswordChange() {
        btnChangePassword.setOnClickListener {
            val oldPwd = etOldPassword.text.toString()
            val newPwd = etNewPassword.text.toString()
            val username = prefManager.getCurrentUserName()

            if (username != null) {
                if (!prefManager.isValidLogin(username, oldPwd)) {
                    Toast.makeText(this, "Incorrect current password!", Toast.LENGTH_SHORT).show()
                } else if (newPwd.length < 4) {
                    Toast.makeText(this, "New password must be at least 4 characters.", Toast.LENGTH_SHORT).show()
                } else {
                    prefManager.updatePassword(username, newPwd)
                    Toast.makeText(this, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    etOldPassword.text.clear()
                    etNewPassword.text.clear()
                }
            }
        }
    }

    private fun setupAccountDeletion() {
        btnDeleteAccount.setOnClickListener {
            val username = prefManager.getCurrentUserName()
            if (username != null) {
                prefManager.deleteUser(username)
                Toast.makeText(this, "Account deleted successfully", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, RegisterActivity::class.java))
                finish()
            }
        }
    }

    private fun setupLanguageSpinner() {
        val languages = listOf("English", "Sinhala", "Tamil")
        val codes = listOf("en", "si", "ta")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = adapter

        val currentCode = settingsManager.getLanguage()
        val selectedIndex = codes.indexOf(currentCode).takeIf { it >= 0 } ?: 0
        spinnerLanguage.setSelection(selectedIndex)

        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val selectedCode = codes[pos]
                if (selectedCode != currentCode) {
                    settingsManager.setLanguage(selectedCode)
                    recreate()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupThemeToggle() {
        switchTheme.isChecked = settingsManager.isDarkMode()
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.setDarkMode(isChecked)
            recreate()
        }
    }

    private fun setupLogout() {
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupBackupButtons() {
        btnExport.setOnClickListener {
            val file = prefManager.exportTransactionsToFile(this)
            val message = if (file != null) "Backup saved to ${file.absolutePath}" else "Failed to export data"
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        btnImport.setOnClickListener {
            val success = prefManager.importTransactionsFromFile(this)
            Toast.makeText(this, if (success) "Data restored" else "Failed to restore", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, TransactionHistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> true
                else -> false
            }
        }
    }
}
