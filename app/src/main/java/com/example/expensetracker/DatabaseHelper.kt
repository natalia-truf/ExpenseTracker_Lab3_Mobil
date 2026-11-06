package com.example.expensetracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_NAME = "expense_tracker.db"
        const val DATABASE_VERSION = 1

        const val TABLE_PAYMENTS = "payments"
        const val COL_PAYMENT_ID = "id"
        const val COL_PAYMENT_NAME = "name"

        const val TABLE_EXPENSES = "expenses"
        const val COL_EXPENSE_ID = "id"
        const val COL_EXPENSE_NAME = "name"
        const val COL_EXPENSE_AMOUNT = "amount"
        const val COL_EXPENSE_PAYMENT_ID = "payment_id"
    }
    override fun onCreate(db: SQLiteDatabase?) {
        val createPaymentsTable = """
            CREATE TABLE $TABLE_PAYMENTS (
                $COL_PAYMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_PAYMENT_NAME TEXT NOT NULL
            )
        """.trimIndent()

        val createExpensesTable = """
            CREATE TABLE $TABLE_EXPENSES (
                $COL_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_EXPENSE_NAME TEXT NOT NULL,
                $COL_EXPENSE_AMOUNT REAL NOT NULL,
                $COL_EXPENSE_PAYMENT_ID INTEGER,
                FOREIGN KEY ($COL_EXPENSE_PAYMENT_ID) REFERENCES $TABLE_PAYMENTS($COL_PAYMENT_ID)
            )
        """.trimIndent()

        db?.execSQL(createPaymentsTable)
        db?.execSQL(createExpensesTable)

        val values1 = ContentValues().apply { put(COL_PAYMENT_NAME, "Наличные") }
        db?.insert(TABLE_PAYMENTS, null, values1)

        val values2 = ContentValues().apply { put(COL_PAYMENT_NAME, "Карта") }
        db?.insert(TABLE_PAYMENTS, null, values2)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_PAYMENTS")
        onCreate(db)
    }
    fun addExpense(name: String, amount: Double, paymentId: Int): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_EXPENSE_NAME, name)
            put(COL_EXPENSE_AMOUNT, amount)
            put(COL_EXPENSE_PAYMENT_ID, paymentId)
        }
        val id = db.insert(TABLE_EXPENSES, null, values)
        db.close()
        return id
    }
    fun getAllExpenses(): List<MainActivity.Expense> {
        val list = mutableListOf<MainActivity.Expense>()
        val db = this.readableDatabase
        val query = """
            SELECT expenses.$COL_EXPENSE_ID, expenses.$COL_EXPENSE_NAME, expenses.$COL_EXPENSE_AMOUNT, payments.$COL_PAYMENT_NAME 
            FROM $TABLE_EXPENSES expenses
            JOIN $TABLE_PAYMENTS payments ON expenses.$COL_EXPENSE_PAYMENT_ID = payments.$COL_PAYMENT_ID
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(1)
                val amount = cursor.getDouble(2)
                val payment = cursor.getString(3)
                list.add(MainActivity.Expense(name, amount, payment))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }
    fun updateExpense(oldName: String, newName: String, newAmount: Double, newPaymentId: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_EXPENSE_NAME, newName)
            put(COL_EXPENSE_AMOUNT, newAmount)
            put(COL_EXPENSE_PAYMENT_ID, newPaymentId)
        }

        db.update(TABLE_EXPENSES, values, "$COL_EXPENSE_NAME = ?", arrayOf(oldName))
        db.close()
    }
    fun deleteExpense(name: String) {
        val db = this.writableDatabase
        db.delete(TABLE_EXPENSES, "$COL_EXPENSE_NAME = ?", arrayOf(name))
        db.close()
    }
    fun clearExpensesTable() {
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $TABLE_EXPENSES")
        db.close()
    }
}