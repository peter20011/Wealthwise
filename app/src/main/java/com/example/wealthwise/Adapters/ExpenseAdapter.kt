package com.example.wealthwise.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.wealthwise.DataClass.Expense
import com.example.wealthwise.R

class ExpenseAdapter(private val context: Context, private val expenses: List<Expense>) :
    BaseAdapter() {
    override fun getCount(): Int {
        return expenses.size
    }

    override fun getItem(position: Int): Any {
        return expenses[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.expense_list_item, parent, false)

        val expense = expenses[position]
        val amountTextView = view.findViewById<TextView>(R.id.amountTextView)
        val categoryTextView = view.findViewById<TextView>(R.id.categoryTextView)
        val valueTextView = view.findViewById<TextView>(R.id.valueTextView)
        amountTextView.text = expense.amount.toString()
        categoryTextView.text = expense.category
        valueTextView.text=expense.value
        return view
    }
}