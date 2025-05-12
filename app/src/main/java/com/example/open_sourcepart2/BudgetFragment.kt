package com.example.open_sourcepart2

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// ui/budget/BudgetFragment.kt
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.databinding.FragmentBudgetBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var categoryBudgetAdapter: CategoryBudgetAdapter

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance("ZAR")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupUI()
        loadData()
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes") { _, _ ->
                    // Clear session
                    sessionManager.logout()

                    // Navigate to login activity
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }


    private fun setupUI() {
        binding.rvCategoryBudgets.layoutManager = LinearLayoutManager(requireContext())
        categoryBudgetAdapter = CategoryBudgetAdapter(emptyList())
        binding.rvCategoryBudgets.adapter = categoryBudgetAdapter

        binding.btnSetBudget.setOnClickListener {
            showSetBudgetDialog()
        }

        // Set current month
        binding.tvBudgetPeriod.text = monthYearFormat.format(Date())
    }

    private fun loadData() {
        val user = sessionManager.getUserDetails() ?: return

        // Get monthly budget
        val calendar = Calendar.getInstance()
        val currentMonth = "monthly" // We're using a simple approach here
        val budget = databaseHelper.getBudgetByPeriod(user.id, currentMonth)

        // Get category summaries
        val categoryExpenseSummaries = databaseHelper.getTotalExpensesByCategory(user.id)

        val categorySummaries = categoryExpenseSummaries.map {
            CategorySummary(
                id = it.categoryId,
                name = it.categoryName,
                totalSpent = it.totalSpent,
                budget = it.budget
            )
        }



        if (categorySummaries.isEmpty()) {
            binding.tvNoCategories.visibility = View.VISIBLE
            binding.rvCategoryBudgets.visibility = View.GONE
        } else {
            binding.tvNoCategories.visibility = View.GONE
            binding.rvCategoryBudgets.visibility = View.VISIBLE
            categoryBudgetAdapter.updateCategories(categorySummaries)
        }

        // Calculate total spent
        val totalSpent = categorySummaries.sumOf { it.totalSpent }

        // Update UI
        if (budget != null) {
            binding.tvMonthlyBudgetAmount.text = currencyFormat.format(budget.amount)

            val remaining = budget.amount - totalSpent
            val percentSpent = if (budget.amount > 0) (totalSpent / budget.amount * 100).toInt() else 0

            binding.progressBudget.progress = percentSpent.coerceAtMost(100)
            binding.tvSpentAmount.text = "Spent: ${currencyFormat.format(totalSpent)}"
            binding.tvRemainingAmount.text = "Remaining: ${currencyFormat.format(remaining)}"
        } else {
            binding.tvMonthlyBudgetAmount.text = currencyFormat.format(0)
            binding.progressBudget.progress = 0
            binding.tvSpentAmount.text = "Spent: ${currencyFormat.format(totalSpent)}"
            binding.tvRemainingAmount.text = "Remaining: ${currencyFormat.format(0)}"
        }
    }

    private fun showSetBudgetDialog() {
        val dialogBinding = DialogSetBudgetBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Setup period spinner
        val periods = arrayOf("Monthly", "Weekly", "Yearly")
        val periodAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            periods
        )
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerPeriod.adapter = periodAdapter

        // Pre-fill with existing budget if any
        val user = sessionManager.getUserDetails() ?: return
        val currentMonth = "monthly"
        val budget = databaseHelper.getBudgetByPeriod(user.id, currentMonth)

        if (budget != null) {
            dialogBinding.etBudgetAmount.setText(budget.amount.toString())
            val periodIndex = when (budget.period) {
                "monthly" -> 0
                "weekly" -> 1
                "yearly" -> 2
                else -> 0
            }
            dialogBinding.spinnerPeriod.setSelection(periodIndex)
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val amountStr = dialogBinding.etBudgetAmount.text.toString().trim()

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter a budget amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val periodPosition = dialogBinding.spinnerPeriod.selectedItemPosition
                val periodStr = when (periodPosition) {
                    0 -> "monthly"
                    1 -> "weekly"
                    2 -> "yearly"
                    else -> "monthly"
                }

                // Calculate start and end dates
                val calendar = Calendar.getInstance()
                val startDate: String
                val endDate: String

                when (periodStr) {
                    "weekly" -> {
                        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                        startDate = dateFormat.format(calendar.time)
                        calendar.add(Calendar.DAY_OF_WEEK, 6)
                        endDate = dateFormat.format(calendar.time)
                    }
                    "monthly" -> {
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        startDate = dateFormat.format(calendar.time)
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                        endDate = dateFormat.format(calendar.time)
                    }
                    "yearly" -> {
                        calendar.set(Calendar.DAY_OF_YEAR, 1)
                        startDate = dateFormat.format(calendar.time)
                        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                        endDate = dateFormat.format(calendar.time)
                    }
                    else -> {
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        startDate = dateFormat.format(calendar.time)
                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                        endDate = dateFormat.format(calendar.time)
                    }
                }

                if (budget != null) {
                    // Update existing budget
                    val updatedBudget = Budget(
                        id = budget.id,
                        amount = amount,
                        period = periodStr,
                        startDate = startDate,
                        endDate = endDate,
                        userId = user.id
                    )

                    val result = databaseHelper.updateBudget(updatedBudget)
                    if (result > 0) {
                        Toast.makeText(requireContext(), "Budget updated successfully", Toast.LENGTH_SHORT).show()
                        loadData()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update budget", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Add new budget
                    val newBudget = Budget(
                        amount = amount,
                        period = periodStr,
                        startDate = startDate,
                        endDate = endDate,
                        userId = user.id
                    )

                    val id = databaseHelper.addBudget(newBudget)
                    if (id > 0) {
                        Toast.makeText(requireContext(), "Budget set successfully", Toast.LENGTH_SHORT).show()
                        loadData()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to set budget", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Invalid budget amount", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}