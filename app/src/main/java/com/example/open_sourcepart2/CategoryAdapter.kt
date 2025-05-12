package com.example.open_sourcepart2


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.open_sourcepart2.databinding.ItemCategoryBinding
import java.text.NumberFormat
import java.util.*

class CategoryAdapter(
    private var categories: List<Category>,
    private val onEditClick: (Category) -> Unit,
    private val onDeleteClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.tvCategoryName.text = category.name
            binding.tvCategoryBudget.text = "Budget: ${currencyFormat.format(category.budget)}"

            // Set category initial and background color
            val initial = category.name.take(2).uppercase()
            binding.tvCategoryInitial.text = initial

            try {
                binding.tvCategoryInitial.setBackgroundColor(Color.parseColor(category.color))
            } catch (e: Exception) {
                binding.tvCategoryInitial.setBackgroundColor(Color.parseColor("#B22222"))
            }

            binding.btnEditCategory.setOnClickListener {
                onEditClick(category)
            }

            binding.btnDeleteCategory.setOnClickListener {
                onDeleteClick(category)
            }
        }
    }
}