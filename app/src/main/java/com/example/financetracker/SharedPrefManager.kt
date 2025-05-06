package com.example.financetracker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.financetracker.model.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class SharedPrefManager(context: Context) {

    companion object {
        private const val PREF_NAME = "finance_tracker_prefs"
        private const val KEY_TRANSACTIONS = "transactions"
    }

    private val sharedPref: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    private val gson = Gson()

    private fun getTransactionKeyForUser(): String {
        val user = getCurrentUserName() ?: return KEY_TRANSACTIONS
        return "$KEY_TRANSACTIONS$user"
    }

    fun saveTransaction(transaction: Transaction) {
        val currentList = getTransactions().toMutableList()
        currentList.add(0, transaction)
        val json = gson.toJson(currentList)
        sharedPref.edit().putString(getTransactionKeyForUser(), json).apply()
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPref.getString(getTransactionKeyForUser(), null)
        if (json.isNullOrBlank()) return emptyList()

        return try {
            val type = object : TypeToken<List<Transaction>>() {}.type
            gson.fromJson<List<Transaction>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun deleteTransaction(target: Transaction) {
        val transactions = getTransactions().filter { it.id != target.id }
        saveAll(transactions)
    }

    fun updateTransaction(updated: Transaction) {
        val transactions = getTransactions().toMutableList()
        val index = transactions.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            transactions[index] = updated
            saveAll(transactions)
        }
    }

    fun saveAll(list: List<Transaction>) {
        val json = gson.toJson(list)
        sharedPref.edit().putString(getTransactionKeyForUser(), json).apply()
    }

    fun clearAll() {
        sharedPref.edit().remove(getTransactionKeyForUser()).apply()
    }

    fun saveBudget(budget: Float) {
        sharedPref.edit().putFloat("monthly_budget", budget).apply()
    }

    fun getBudget(): Float? {
        val value = sharedPref.getFloat("monthly_budget", -1f)
        return if (value > 0) value else null
    }

    fun setDarkMode(enabled: Boolean) {
        sharedPref.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun isDarkMode(): Boolean {
        return sharedPref.getBoolean("dark_mode", true)
    }

    fun setLanguage(lang: String) {
        sharedPref.edit().putString("language", lang).apply()
    }

    fun getLanguage(): String {
        return sharedPref.getString("language", "en") ?: "en"
    }

    fun getCurrentUserName(): String? {
        return sharedPref.getString("logged_in_user_name", null)
    }

    fun registerUser(username: String, password: String): Boolean {
        val usersMap = getUserMap().toMutableMap()
        if (usersMap.containsKey(username)) return false
        usersMap[username] = password
        val json = gson.toJson(usersMap)
        sharedPref.edit().putString("user_credentials", json).apply()
        return true
    }

    fun isValidLogin(username: String, password: String): Boolean {
        val usersMap = getUserMap()
        return usersMap[username] == password
    }

    fun updatePassword(username: String, newPassword: String) {
        val usersMap = getUserMap().toMutableMap()
        usersMap[username] = newPassword
        val json = gson.toJson(usersMap)
        sharedPref.edit().putString("user_credentials", json).apply()
    }

    fun deleteUser(username: String) {
        val usersMap = getUserMap().toMutableMap()
        usersMap.remove(username)
        val json = gson.toJson(usersMap)
        sharedPref.edit().putString("user_credentials", json).apply()

        sharedPref.edit()
            .remove("logged_in_user_name")
            .remove("$KEY_TRANSACTIONS$username")
            .apply()
    }

    private fun getUserMap(): Map<String, String> {
        val json = sharedPref.getString("user_credentials", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type)
    }

    fun setLoggedInUser(username: String) {
        sharedPref.edit().putString("logged_in_user_name", username).apply()
    }

    fun exportTransactionsToFile(context: Context): File? {
        return try {
            val transactions = getTransactions()
            val json = Gson().toJson(transactions)
            val file = File(context.filesDir, "transaction_backup.json")
            file.writeText(json)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun importTransactionsFromFile(context: Context): Boolean {
        return try {
            val file = File(context.filesDir, "transaction_backup.json")
            if (!file.exists()) return false

            val json = file.readText()
            val type = object : TypeToken<List<Transaction>>() {}.type
            val importedList: List<Transaction> = Gson().fromJson(json, type)
            saveAll(importedList)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
