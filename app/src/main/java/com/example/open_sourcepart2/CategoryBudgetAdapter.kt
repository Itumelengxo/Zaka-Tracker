package com.example.open_sourcepart2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.open_sourcepart2.databinding.ItemCategoryBudgetBinding
import java.text.NumberFormat
import java.util.*



class CategoryBudgetAdapter(private var categories: List<CategorySummary>) :
    RecyclerView.Adapter<CategoryBudgetAdapter.ViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    class ViewHolder(val binding: ItemCategoryBudgetBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBudgetBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.binding.tvCategoryName.text = category.name
        holder.binding.tvCategorySpent.text = currencyFormat.format(category.totalSpent)

        // Calculate percentage if budget exists
        if (category.budget > 0) {
            val percentage = ((category.totalSpent / category.budget) * 100).toInt().coerceAtMost(100)
            holder.binding.progressCategory.progress = percentage
            holder.binding.tvCategoryBudget.text = "of ${currencyFormat.format(category.budget)}"
        } else {
            holder.binding.progressCategory.progress = 0
            holder.binding.tvCategoryBudget.text = "No budget set"
        }
    }

    override fun getItemCount(): Int = categories.size

    // This is the method that needs to be fixed - it should be updateCategories, not updateCategorie
    fun updateCategories(newCategories: List<CategorySummary>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}