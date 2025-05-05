package com.example.financetracker

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financetracker.adapter.TransactionAdapter
import com.example.financetracker.model.Transaction
import com.example.financetracker.utils.SharedPrefManager

class CategoryTransactionActivity : AppCompatActivity() {

    private lateinit var rvFilteredTransactions: RecyclerView
    private lateinit var sharedPrefManager: SharedPrefManager
    private lateinit var adapter: TransactionAdapter
    private lateinit var tvHeader: TextView

    private var transactionList: List<Transaction> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category_transaction)

        supportActionBar?.hide()

        val selectedCategory = intent.getStringExtra("category") ?: "Unknown"
        sharedPrefManager = SharedPrefManager(this)

        transactionList = sharedPrefManager.getTransactions()
            .filter { it.category.equals(selectedCategory, ignoreCase = true) }

        rvFilteredTransactions = findViewById(R.id.rvFilteredTransactions)
        tvHeader = findViewById(R.id.tvCategoryTitle)

        tvHeader.text = "Transactions for \"$selectedCategory\""

        adapter = TransactionAdapter(
            transactionList,
            this,
            onEdit = {},
            onDelete = {}
        )

        rvFilteredTransactions.layoutManager = LinearLayoutManager(this)
        rvFilteredTransactions.adapter = adapter
    }
}
