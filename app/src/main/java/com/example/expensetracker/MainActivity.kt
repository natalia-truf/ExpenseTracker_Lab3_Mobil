package com.example.expensetracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    // Ссылки на элементы экрана
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var rgPayment: RadioGroup
    private lateinit var tvTotal: TextView
    private lateinit var tvList: TextView
    private lateinit var btnAdd: Button
    private lateinit var btnClear: Button

    // Список расходов
    private val expenses = mutableListOf<Expense>()

    // Общая сумма
    private var total: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Находим элементы по id
        etName = findViewById(R.id.etName)
        etAmount = findViewById(R.id.etAmount)
        rgPayment = findViewById(R.id.rgPayment)
        tvTotal = findViewById(R.id.tvTotal)
        tvList = findViewById(R.id.tvList)
        btnAdd = findViewById(R.id.btnAdd)
        btnClear = findViewById(R.id.btnClear)

        // Нажатие на кнопку "Добавить расход"
        btnAdd.setOnClickListener {
            addExpense()
        }

        // Нажатие на кнопку "Очистить всё"
        btnClear.setOnClickListener {
            clearAll()
        }
    }

    // Метод добавления расхода
    private fun addExpense() {
        val name = etName.text.toString().trim()
        val amountStr = etAmount.text.toString().trim()

        // Проверка: название не пустое
        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_name), Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка: сумма не пустая
        if (amountStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_amount), Toast.LENGTH_SHORT).show()
            return
        }

        // Преобразуем строку в число
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, getString(R.string.error_amount_positive), Toast.LENGTH_SHORT).show()
            return
        }

        // Определяем способ оплаты через RadioButton (I3)
        val checkedId = rgPayment.checkedRadioButtonId
        val rb = findViewById<RadioButton>(checkedId)
        val payment = rb.text.toString()

        // Создаём объект расхода и добавляем в список
        expenses.add(Expense(name, amount, payment))

        // Пересчитываем итоговую сумму
        total += amount

        // Обновляем интерфейс
        updateTotal()
        updateList()

        // Очищаем поля ввода
        etName.setText("")
        etAmount.setText("")

        // Показываем Toast (O2)
        Toast.makeText(
            this,
            getString(R.string.added_format, name, amount, payment),
            Toast.LENGTH_LONG
        ).show()
    }

    // Обновление надписи "Итого"
    private fun updateTotal() {
        tvTotal.text = getString(R.string.total_format, total)
    }

    // Обновление списка расходов
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

    // Очистка всех данных
    private fun clearAll() {
        expenses.clear()
        total = 0.0
        etName.setText("")
        etAmount.setText("")
        rgPayment.check(R.id.rbCash)
        updateTotal()
        updateList()
        Toast.makeText(this, "Все данные очищены", Toast.LENGTH_SHORT).show()
    }

    // Модель расхода
    data class Expense(
        val name: String,
        val amount: Double,
        val payment: String
    )
}