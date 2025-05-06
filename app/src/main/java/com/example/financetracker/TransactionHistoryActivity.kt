package com.example.financetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.adapter.TransactionAdapter
import com.example.financetracker.model.Transaction
import com.example.financetracker.utils.SharedPrefManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.tabs.TabLayout

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var rvTransactions: RecyclerView
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: TransactionAdapter
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var bottomNav: BottomNavigationView

    private var fullTransactionList: MutableList<Transaction> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)
        supportActionBar?.hide()

        // Bind views
        rvTransactions = findViewById(R.id.rvTransactions)
        tabLayout = findViewById(R.id.tabLayout)
        sharedPrefManager = SharedPrefManager(this)

        setupTransactionData()
        setupTabFilter()

        bottomNav = findViewById(R.id.bottom_nav)
        bottomNav.selectedItemId = R.id.nav_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_history -> true
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
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


    private fun setupTransactionData() {
        fullTransactionList = sharedPrefManager.getTransactions().toMutableList()

        adapter = TransactionAdapter(
            fullTransactionList,
            this,
            onEdit = { txn -> openEditTransaction(txn) },
            onDelete = { txn ->
                sharedPrefManager.deleteTransaction(txn)
                fullTransactionList = sharedPrefManager.getTransactions().toMutableList()
                adapter.filterList(fullTransactionList)
                Toast.makeText(this, "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
        )

        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = adapter
    }

    private fun setupTabFilter() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val label = tab?.text.toString()
                when (label) {
                    "All" -> adapter.filterList(fullTransactionList)
                    "Expenses" -> {
                        val filtered = fullTransactionList.filter { it.type == "Expense" }
                        adapter.filterList(filtered)
                    }
                    "Receives" -> {
                        val filtered = fullTransactionList.filter { it.type == "Income" }
                        adapter.filterList(filtered)
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }


    private fun openEditTransaction(transaction: Transaction) {
        val intent = Intent(this, AddTransactionActivity::class.java)
        intent.putExtra("editMode", true)
        intent.putExtra("transactionId", transaction.id)
        startActivity(intent)
    }


    override fun onResume() { // Refresh list after editing
        super.onResume()
        fullTransactionList = sharedPrefManager.getTransactions().toMutableList()
        adapter.filterList(fullTransactionList)
    }
}
