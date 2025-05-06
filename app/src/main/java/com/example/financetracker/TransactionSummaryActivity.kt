package com.example.financetracker

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.financetracker.utils.NotificationHelper
import com.example.financetracker.utils.SharedPrefManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView

class TransactionSummaryActivity : AppCompatActivity() {

    private lateinit var etBudgetInput: EditText
    private lateinit var btnSaveBudget: Button
    private lateinit var tvSpent: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var pieChart: PieChart
    private lateinit var bottomNav: BottomNavigationView

    private lateinit var prefManager: SharedPrefManager

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_summary)
        supportActionBar?.hide()

        prefManager = SharedPrefManager(this)

        // Bind views
        etBudgetInput = findViewById(R.id.etBudgetInput)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
        tvSpent = findViewById(R.id.tvSpent)
        tvRemaining = findViewById(R.id.tvRemaining)
        progressBar = findViewById(R.id.progressBar)
        pieChart = findViewById(R.id.pieChart)
        bottomNav = findViewById(R.id.bottom_nav)

        setupBudgetInput()
        setupChart()
        setupBottomNav()

        NotificationHelper.createChannel(this)

    }

    private fun setupBudgetInput() {
        val savedBudget = prefManager.getBudget()?.toInt() ?: 0
        etBudgetInput.setText(savedBudget.toString())

        btnSaveBudget.setOnClickListener {
            val budget = etBudgetInput.text.toString().toFloatOrNull()
            if (budget != null && budget > 0f) {
                prefManager.saveBudget(budget)
                Toast.makeText(this, "Budget saved!", Toast.LENGTH_SHORT).show()
                updateSpendingView()
            } else {
                Toast.makeText(this, "Please enter a valid budget amount", Toast.LENGTH_SHORT).show()
            }
        }

        etBudgetInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                btnSaveBudget.performClick()
                true
            } else false
        }

        updateSpendingView()
    }

    private fun updateSpendingView() {
        val transactions = prefManager.getTransactions()
        val totalSpent = transactions.filter { it.type == "Expense" }.sumOf { it.amount.toDouble() }.toFloat()
        val budget = prefManager.getBudget() ?: 0f
        val remaining = budget - totalSpent
        val percentage = if (budget > 0) ((totalSpent / budget) * 100).toInt().coerceIn(0, 100) else 0

        if (percentage >= 100) {
            NotificationHelper.showNotification(this, "Budget Exceeded", "You have exceeded your monthly budget.")
        } else if (percentage >= 80) {
            NotificationHelper.showNotification(this, "Budget Alert", "You've used over 80% of your budget.")
        }


        tvSpent.text = "Spent: LKR %.2f".format(totalSpent)
        tvRemaining.text = "Remaining: LKR %.2f".format(remaining)
        progressBar.progress = percentage


        when {
            percentage >= 100 -> {
                Toast.makeText(this, "Budget Exceeded!", Toast.LENGTH_LONG).show()
            }
            percentage >= 80 -> {
                Toast.makeText(this, "You're close to exceeding your budget!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupChart() {
        val transactions = prefManager.getTransactions().filter { it.type == "Expense" }
        val grouped = transactions.groupBy { it.category }
            .mapValues { it.value.sumOf { txn -> txn.amount } }

        val entries = grouped.map { (cat, total) -> PieEntry(total.toFloat(), cat) }
        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                Color.parseColor("#BB86FC"),
                Color.parseColor("#F06292"),
                Color.parseColor("#42A5F5"),
                Color.parseColor("#66BB6A"),
                Color.parseColor("#FFA726")
            )
            valueTextColor = Color.WHITE
            valueTextSize = 12f
        }

        pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(Color.WHITE)
            centerText = "Spending by Category"
            setCenterTextColor(Color.WHITE)
            animateY(1000)
            invalidate()
        }
    }

    private fun setupBottomNav() {
        bottomNav.selectedItemId = R.id.nav_home
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
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
