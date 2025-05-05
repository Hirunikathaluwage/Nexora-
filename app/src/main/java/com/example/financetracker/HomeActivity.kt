package com.example.financetracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.financetracker.utils.SharedPrefManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvBudgetValue: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var tvIncomeAmount: TextView
    private lateinit var tvExpenseAmount: TextView
    private lateinit var btnAddTransaction: Button
    private lateinit var btnTransactions: Button
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var ivProfile: ImageView

    private lateinit var prefManager: SharedPrefManager

    @SuppressLint("SetTextI18n", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()

        // 🔔 Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        prefManager = SharedPrefManager(this)

        // Bind views
        tvWelcome = findViewById(R.id.tvWelcome)
        ivProfile = findViewById(R.id.ivProfile)
        progressBar = findViewById(R.id.progressBar2)
        tvBudgetValue = findViewById(R.id.tvBudgetValue)
        btnAddTransaction = findViewById(R.id.btnAddTransaction)
        btnTransactions = findViewById(R.id.btnTransactions)
        bottomNav = findViewById(R.id.bottom_nav)
        tvIncomeAmount = findViewById(R.id.tvIncomeAmount)
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount)

        // Set welcome message
        val userName = prefManager.getCurrentUserName() ?: "User"
        tvWelcome.text = "Welcome, $userName"

        // Profile shortcut
        ivProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        // Bottom Nav
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, TransactionHistoryActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }

        // Add Transaction
        btnAddTransaction.setOnClickListener {
            startActivity(Intent(this, AddTransactionActivity::class.java))
        }

        // Category Summary
        btnTransactions.setOnClickListener {
            startActivity(Intent(this, TransactionSummaryActivity::class.java))
        }

        updateSummaryData()
    }

    override fun onResume() {
        super.onResume()
        updateSummaryData()
    }

    private fun updateSummaryData() {
        val allTransactions = prefManager.getTransactions()

        val totalIncome = allTransactions.filter { it.type == "Income" }
            .sumOf { it.amount }

        val totalExpense = allTransactions.filter { it.type == "Expense" }
            .sumOf { it.amount }


        val totalBudget = prefManager.getBudget() ?: 3000f

        tvIncomeAmount.text = "LKR ${totalIncome.toInt()}"
        tvExpenseAmount.text = "LKR ${totalExpense.toInt()}"

        setupBudgetProgress(totalExpense, totalBudget)
    }

    private fun setupBudgetProgress(currentSpending: Double, totalBudget: Float) {
        val spentPercentage = if (totalBudget > 0) {
            ((currentSpending / totalBudget) * 100).toInt().coerceAtMost(100)
        } else 0

        progressBar.progress = spentPercentage
        tvBudgetValue.text = "LKR ${currentSpending.toInt()} of ${totalBudget.toInt()}"

        when {
            spentPercentage >= 100 -> {
                tvBudgetValue.setTextColor(ContextCompat.getColor(this, R.color.red))
                Toast.makeText(this, "⚠️ You have exceeded your monthly budget!", Toast.LENGTH_LONG).show()
            }
            spentPercentage >= 80 -> {
                tvBudgetValue.setTextColor(ContextCompat.getColor(this, R.color.yellow))
                Toast.makeText(this, "⚠️ You are close to reaching your monthly budget!", Toast.LENGTH_SHORT).show()
            }
            else -> {
                tvBudgetValue.setTextColor(ContextCompat.getColor(this, R.color.teal_200))
            }
        }

        progressBar.invalidate() // Force redraw
    }

}
