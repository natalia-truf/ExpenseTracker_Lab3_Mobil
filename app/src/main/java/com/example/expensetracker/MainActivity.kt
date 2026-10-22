package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var rgPayment: RadioGroup
    private lateinit var tvTotal: TextView
    private lateinit var tvList: TextView
    private lateinit var tvBudget: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnClear: Button

    private val expenses = mutableListOf<Expense>()
    private var total: Double = 0.0
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var budget: Double = 0.0
    private val REQUEST_STATS = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupDrawer()

        btnAdd.setOnClickListener { addExpense() }
        btnClear.setOnClickListener { clearAll() }
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etAmount = findViewById(R.id.etAmount)
        rgPayment = findViewById(R.id.rgPayment)
        tvTotal = findViewById(R.id.tvTotal)
        tvList = findViewById(R.id.tvList)
        tvBudget = findViewById(R.id.tvBudget)
        btnAdd = findViewById(R.id.btnAdd)
        btnClear = findViewById(R.id.btnClear)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
    }

    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_main -> {
                    Toast.makeText(this, "Вы уже на главной", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_statistics -> {
                    openStatistics()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun openStatistics() {
        val intent = Intent(this, StatisticsActivity::class.java)
        intent.putExtra("EXTRA_TOTAL", total)
        intent.putExtra("EXTRA_COUNT", expenses.size)
        intent.putExtra("EXTRA_BUDGET", budget)
        @Suppress("DEPRECATION")
        startActivityForResult(intent, REQUEST_STATS)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_STATS && resultCode == RESULT_OK) {
            val newBudget = data?.getDoubleExtra("EXTRA_NEW_BUDGET", 0.0) ?: 0.0
            budget = newBudget
            updateBudgetDisplay()
            Toast.makeText(this, "Лимит бюджета обновлён", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBudgetDisplay() {
        if (budget > 0) {
            val text = "Текущий лимит бюджета: %.2f руб.".format(budget)
            if (total > budget) {
                tvBudget.text = "$text Превышен!"
                tvBudget.setTextColor(0xFFB00020.toInt())
            } else {
                tvBudget.text = text
                tvBudget.setTextColor(0xFF4CAF50.toInt())
            }
        } else {
            tvBudget.text = ""
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addExpense() {
        val name = etName.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_name), Toast.LENGTH_SHORT).show()
            return
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_amount), Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, getString(R.string.error_amount_positive), Toast.LENGTH_SHORT).show()
            return
        }

        val checkedId = rgPayment.checkedRadioButtonId
        val rb = findViewById<RadioButton>(checkedId)
        val payment = rb.text.toString()

        expenses.add(Expense(name, amount, payment))
        total += amount

        updateTotal()
        updateList()
        updateBudgetDisplay()

        etName.setText("")
        etAmount.setText("")

        Toast.makeText(
            this,
            getString(R.string.added_format, name, amount, payment),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun updateTotal() {
        tvTotal.text = getString(R.string.total_format, total)
    }

    private fun updateList() {
        if (expenses.isEmpty()) {
            tvList.text = getString(R.string.list_empty)
            return
        }

        val sb = StringBuilder()
        for (i in expenses.indices) {
            val e = expenses[i]
            sb.append("${i + 1}. ${e.name} — ")
            sb.append(String.format("%.2f", e.amount))
            sb.append(" руб. (${e.payment})\n")
        }
        tvList.text = sb.toString().trim()
    }

    private fun clearAll() {
        expenses.clear()
        total = 0.0
        etName.setText("")
        etAmount.setText("")
        rgPayment.check(R.id.rbCash)
        updateTotal()
        updateList()
        updateBudgetDisplay()
        Toast.makeText(this, "Все данные очищены", Toast.LENGTH_SHORT).show()
    }

    data class Expense(val name: String, val amount: Double, val payment: String)
}