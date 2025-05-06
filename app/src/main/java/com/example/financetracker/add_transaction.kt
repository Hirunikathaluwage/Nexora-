package com.example.financetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.financetracker.model.Transaction
import com.example.financetracker.utils.SharedPrefManager
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etAmount: EditText
    private lateinit var etCustomCategory: EditText
    private lateinit var spCurrency: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var btnSelectDate: Button
    private lateinit var btnSaveTransaction: Button

    private lateinit var prefManager: SharedPrefManager

    private var selectedCategory = ""
    private var selectedCurrency = "LKR"
    private var selectedDate = ""
    private var editingTransactionId: Int? = null  // Track if editing

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        supportActionBar?.hide()

        prefManager = SharedPrefManager(this)

        etTitle = findViewById(R.id.etTitle)
        etAmount = findViewById(R.id.etAmount)
        etCustomCategory = findViewById(R.id.etCustomCategory)
        spCurrency = findViewById(R.id.spCurrency)
        rgType = findViewById(R.id.rgType)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnSaveTransaction = findViewById(R.id.btnSaveTransaction)

        setupCurrencySpinner()
        setupCategoryCards()
        setupDatePicker()

        // Check if editing
        val isEditMode = intent.getBooleanExtra("editMode", false)
        val transactionId = intent.getIntExtra("transactionId", -1)

        if (isEditMode && transactionId != -1) {
            val transaction = prefManager.getTransactions().find { it.id == transactionId }
            if (transaction != null) {
                editingTransactionId = transactionId
                populateFields(transaction)
            }
        }

        setupSaveButton()
    }

    private fun populateFields(transaction: Transaction) {
        etTitle.setText(transaction.title)
        etAmount.setText(transaction.amount.toString())
        selectedDate = transaction.timestamp
        btnSelectDate.text = transaction.timestamp
        selectedCurrency = transaction.currency
        val currencyList = listOf("LKR", "USD", "Pounds","AUD","EURO")
        spCurrency.setSelection(currencyList.indexOf(transaction.currency))

        // Type toggle
        when (transaction.type) {
            "Income" -> rgType.check(R.id.rbIncome)
            "Expense" -> rgType.check(R.id.rbExpense)
        }

        // Category selection
        selectedCategory = transaction.category
        when (transaction.category) {
            "Food" -> findViewById<CardView>(R.id.cardFood).performClick()
            "Travel" -> findViewById<CardView>(R.id.cardTravel).performClick()
            "Education" -> findViewById<CardView>(R.id.cardEducation).performClick()
            else -> {
                findViewById<CardView>(R.id.cardOther).performClick()
                etCustomCategory.setText(transaction.category)
            }
        }
    }

    private fun setupCurrencySpinner() {
        val options = listOf("LKR", "Dollar", "Pounds")
        spCurrency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        spCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedCurrency = options[pos]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupCategoryCards() {
        val cardFood = findViewById<CardView>(R.id.cardFood)
        val cardTravel = findViewById<CardView>(R.id.cardTravel)
        val cardEducation = findViewById<CardView>(R.id.cardEducation)
        val cardOther = findViewById<CardView>(R.id.cardOther)

        val allCards = listOf(cardFood, cardTravel, cardEducation, cardOther)

        fun resetCardColors() {
            allCards.forEach { it.setCardBackgroundColor(0xFF2D2D2D.toInt()) }
        }

        fun selectCard(card: CardView, category: String) {
            resetCardColors()
            card.setCardBackgroundColor(0xFF03DAC5.toInt())
            selectedCategory = category
            etCustomCategory.visibility = if (category == "Other") View.VISIBLE else View.GONE
        }

        cardFood.setOnClickListener { selectCard(cardFood, "Food") }
        cardTravel.setOnClickListener { selectCard(cardTravel, "Travel") }
        cardEducation.setOnClickListener { selectCard(cardEducation, "Education") }
        cardOther.setOnClickListener { selectCard(cardOther, "Other") }
    }

    private fun setupDatePicker() {
        btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    selectedDate = "$d/${m + 1}/$y"
                    btnSelectDate.text = selectedDate
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        btnSaveTransaction.setOnClickListener {
            val title = etTitle.text.toString()
            val amount = etAmount.text.toString().toFloatOrNull() ?: 0f
            val type = when (rgType.checkedRadioButtonId) {
                R.id.rbIncome -> "Income"
                R.id.rbExpense -> "Expense"
                else -> ""
            }
            val category = if (selectedCategory == "Other")
                etCustomCategory.text.toString().ifBlank { "Other" }
            else selectedCategory

            if (title.isBlank() || type.isBlank() || selectedDate.isBlank()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val transaction = Transaction(
                id = editingTransactionId ?: System.currentTimeMillis().toInt(),
                title = title,
                amount = amount.toDouble(),
                type = type,
                category = category,
                currency = selectedCurrency,
                timestamp = selectedDate
            )

            if (editingTransactionId != null) {
                prefManager.updateTransaction(transaction)
                Toast.makeText(this, "Transaction updated!", Toast.LENGTH_SHORT).show()
            } else {
                prefManager.saveTransaction(transaction)
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
}
