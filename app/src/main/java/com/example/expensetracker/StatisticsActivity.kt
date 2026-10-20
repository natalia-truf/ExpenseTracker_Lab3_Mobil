package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class StatisticsActivity : AppCompatActivity() {

    private lateinit var tvStatsTotal: TextView
    private lateinit var tvStatsCount: TextView
    private lateinit var tvCurrentBudget: TextView
    private lateinit var etBudget: EditText
    private lateinit var btnSaveBudget: Button

    private var budget: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        supportActionBar?.title = getString(R.string.stats_title)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()

        val receivedTotal = intent.getDoubleExtra("EXTRA_TOTAL", 0.0)
        val receivedCount = intent.getIntExtra("EXTRA_COUNT", 0)
        budget = intent.getDoubleExtra("EXTRA_BUDGET", 0.0)

        tvStatsTotal.text = String.format(Locale.getDefault(), "%.2f руб.", receivedTotal)
        tvStatsCount.text = receivedCount.toString()

        if (budget > 0) {
            tvCurrentBudget.text = getString(R.string.budget_current, budget)
        }

        btnSaveBudget.setOnClickListener {
            saveBudgetAndReturn()
        }
    }

    private fun initViews() {
        tvStatsTotal = findViewById(R.id.tvStatsTotal)
        tvStatsCount = findViewById(R.id.tvStatsCount)
        tvCurrentBudget = findViewById(R.id.tvCurrentBudget)
        etBudget = findViewById(R.id.etBudget)
        btnSaveBudget = findViewById(R.id.btnSaveBudget)
    }

    private fun saveBudgetAndReturn() {
        val budgetStr = etBudget.text.toString().trim()

        if (budgetStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.budget_error), Toast.LENGTH_SHORT).show()
            return
        }

        val newBudget = budgetStr.toDoubleOrNull()
        if (newBudget == null || newBudget <= 0) {
            Toast.makeText(this, getString(R.string.budget_error), Toast.LENGTH_SHORT).show()
            return
        }

        val resultIntent = Intent()
        resultIntent.putExtra("EXTRA_NEW_BUDGET", newBudget)
        setResult(RESULT_OK, resultIntent)

        Toast.makeText(
            this,
            getString(R.string.budget_saved, newBudget),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}