package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(
    private val expenses: List<MainActivity.Expense>,
    private val onItemLongClick: (Int, View) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvItemAmount)
        val tvPayment: TextView = itemView.findViewById(R.id.tvItemPayment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvName.text = expense.name
        holder.tvAmount.text = "Сумма: %.2f руб.".format(expense.amount)
        holder.tvPayment.text = "Оплата: ${expense.payment}"

        holder.itemView.setOnLongClickListener { view ->
            onItemLongClick(position, view)
            true
        }
    }

    override fun getItemCount(): Int = expenses.size
}