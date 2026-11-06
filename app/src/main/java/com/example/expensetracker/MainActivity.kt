package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class MainActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var rgPayment: RadioGroup
    private lateinit var tvTotal: TextView
    private lateinit var tvBudget: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnClear: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private var budget: Double = 0.0
    private val REQUEST_STATS = 100
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private lateinit var emptySpaceListener: FrameLayout
    private var selectedPosition: Int = -1
    private val expenses = mutableListOf<Expense>()
    private var total: Double = 0.0
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PDFBoxResourceLoader.init(applicationContext)
        setContentView(R.layout.activity_main)

        initViews()
        setupDrawer()
        dbHelper = DatabaseHelper(this)
        setupRecyclerView()
        loadExpensesFromDb()

        btnAdd.setOnClickListener { addExpense() }
        btnClear.setOnClickListener { clearAll() }
    }
    private fun loadExpensesFromDb() {
        expenses.clear()
        expenses.addAll(dbHelper.getAllExpenses())
        total = expenses.sumOf { it.amount }
        updateTotal()
        adapter.notifyDataSetChanged()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etAmount = findViewById(R.id.etAmount)
        rgPayment = findViewById(R.id.rgPayment)
        tvTotal = findViewById(R.id.tvTotal)
        tvBudget = findViewById(R.id.tvBudget)
        btnAdd = findViewById(R.id.btnAdd)
        btnClear = findViewById(R.id.btnClear)
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        recyclerView = findViewById(R.id.recyclerView)
        emptySpaceListener = findViewById(R.id.emptySpaceListener)
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(expenses) { position, view ->
            selectedPosition = position
            view.showContextMenu()
        }

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        registerForContextMenu(recyclerView)

        registerForContextMenu(emptySpaceListener)
        emptySpaceListener.setOnLongClickListener { view ->
            selectedPosition = -1
            view.showContextMenu()
            true
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.menu_item_context, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                showAddDialog()
                return true
            }
            R.id.action_edit -> {
                if (selectedPosition != -1 && selectedPosition < expenses.size) {
                    showEditDialog(selectedPosition)
                } else {
                    Toast.makeText(this, "Выберите карточку для редактирования", Toast.LENGTH_SHORT).show()
                }
                return true
            }
            R.id.action_delete -> {
                if (selectedPosition != -1 && selectedPosition < expenses.size) {
                    deleteExpense(selectedPosition)
                } else {
                    Toast.makeText(this, "Выберите карточку для удаления", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onContextItemSelected(item)
    }
    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val etNameDialog = dialogView.findViewById<EditText>(R.id.etDialogName)
        val etAmountDialog = dialogView.findViewById<EditText>(R.id.etDialogAmount)
        val rgPaymentDialog = dialogView.findViewById<RadioGroup>(R.id.rgDialogPayment)

        AlertDialog.Builder(this)
            .setTitle("Новый расход")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val name = etNameDialog.text.toString().trim()
                val amountStr = etAmountDialog.text.toString().trim()
                val amount = amountStr.toDoubleOrNull()

                if (name.isNotEmpty() && amount != null && amount > 0) {
                    val checkedId = rgPaymentDialog.checkedRadioButtonId
                    val rb = dialogView.findViewById<RadioButton>(checkedId)
                    val payment = rb.text.toString()

                    val paymentId = if (payment == "Карта") 2 else 1
                    dbHelper.addExpense(name, amount, paymentId)

                    loadExpensesFromDb()
                    updateBudgetDisplay()
                }
                else {
                    Toast.makeText(this, "Заполните поля корректно", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun showEditDialog(position: Int) {
        val expense = expenses[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense, null)
        val etNameDialog = dialogView.findViewById<EditText>(R.id.etDialogName)
        val etAmountDialog = dialogView.findViewById<EditText>(R.id.etDialogAmount)
        val rgPaymentDialog = dialogView.findViewById<RadioGroup>(R.id.rgDialogPayment)

        etNameDialog.setText(expense.name)
        etAmountDialog.setText(expense.amount.toString())
        if (expense.payment == "Карта") {
            rgPaymentDialog.check(R.id.rbDialogCard)
        } else {
            rgPaymentDialog.check(R.id.rbDialogCash)
        }

        AlertDialog.Builder(this)
            .setTitle("Редактировать расход")
            .setView(dialogView)
            .setPositiveButton("Сохранить") { _, _ ->
                val newName = etNameDialog.text.toString().trim()
                val newAmountStr = etAmountDialog.text.toString().trim()
                val newAmount = newAmountStr.toDoubleOrNull()

                if (newName.isNotEmpty() && newAmount != null && newAmount > 0) {
                    val checkedId = rgPaymentDialog.checkedRadioButtonId
                    val rb = dialogView.findViewById<RadioButton>(checkedId)
                    val newPayment = rb.text.toString()

                    val paymentId = if (newPayment == "Карта") 2 else 1
                    dbHelper.updateExpense(expense.name, newName, newAmount, paymentId)

                    loadExpensesFromDb()
                    updateBudgetDisplay()
                    Toast.makeText(this, "Расход обновлен", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Заполните поля корректно", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun deleteExpense(position: Int) {
        val expense = expenses[position]
        AlertDialog.Builder(this)
            .setTitle("Удалить расход?")
            .setMessage("Вы уверены, что хотите удалить '${expense.name}'?")
            .setPositiveButton("Удалить") { _, _ ->
                dbHelper.deleteExpense(expense.name)

                loadExpensesFromDb()
                updateBudgetDisplay()
                Toast.makeText(this, "Расход удален", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
    private fun setupDrawer() {
        val toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_main -> Toast.makeText(this, "Вы уже на главной", Toast.LENGTH_SHORT).show()
                R.id.nav_statistics -> openStatistics()

                R.id.nav_export_csv -> DataExporter.exportToCsv(this, expenses)
                R.id.nav_export_xls -> DataExporter.exportToExcel(this, expenses)
                R.id.nav_export_pdf -> DataExporter.exportToPdf(this, expenses)

                R.id.nav_import_csv -> {
                    dbHelper.clearExpensesTable()
                    DataExporter.importFromCsv(this, dbHelper)
                    loadExpensesFromDb()
                }
                R.id.nav_import_xls -> {
                    dbHelper.clearExpensesTable()
                    DataExporter.importFromExcel(this, dbHelper)
                    loadExpensesFromDb()
                }
                R.id.nav_import_pdf -> {
                    dbHelper.clearExpensesTable()
                    DataExporter.importFromPdf(this, dbHelper)
                    loadExpensesFromDb()
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
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, getString(R.string.error_amount_positive), Toast.LENGTH_SHORT).show()
            return
        }

        val checkedId = rgPayment.checkedRadioButtonId
        val payment = findViewById<RadioButton>(checkedId).text.toString()

        val paymentId = if (payment == "Карта") 2 else 1
        dbHelper.addExpense(name, amount, paymentId)

        loadExpensesFromDb()
        updateBudgetDisplay()

        etName.setText("")
        etAmount.setText("")
    }

    private fun updateTotal() {
        tvTotal.text = getString(R.string.total_format, total)
    }

    private fun clearAll() {
        expenses.clear()
        total = 0.0
        etName.setText("")
        etAmount.setText("")
        rgPayment.check(R.id.rbCash)
        updateTotal()
        adapter.notifyDataSetChanged()
        updateBudgetDisplay()
    }

    data class Expense(val name: String, val amount: Double, val payment: String)
}