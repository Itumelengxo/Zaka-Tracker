package com.example.open_sourcepart2

// ui/expenses/ExpenseAdapter.kt

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.open_sourcepart2.databinding.ItemExpenseBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<ExpenseWithCategory>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    fun updateExpenses(newExpenses: List<ExpenseWithCategory>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: ExpenseWithCategory) {
            binding.tvExpenseDescription.text = expense.description
            binding.tvExpenseAmount.text = currencyFormat.format(expense.amount)

            // Format date
            val date = try {
                dateFormat.parse(expense.date)?.let { displayDateFormat.format(it) } ?: expense.date
            } catch (e: Exception) {
                expense.date
            }

            binding.tvExpenseDetails.text = "${expense.categoryName} • $date"

            // Set category initial and background color
            val initial = expense.categoryName.take(2).uppercase()
            binding.tvCategoryInitial.text = initial

            try {
                binding.tvCategoryInitial.setBackgroundColor(Color.parseColor(expense.categoryColor))
            } catch (e: Exception) {
                binding.tvCategoryInitial.setBackgroundColor(Color.parseColor("#B22222"))
            }
        }
    }
}