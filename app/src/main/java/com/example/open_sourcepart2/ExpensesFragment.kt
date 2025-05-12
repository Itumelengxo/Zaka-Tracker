package com.example.open_sourcepart2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.open_sourcepart2.databinding.FragmentExpensesBinding

// ui/expenses/ExpensesFragment.kt

import android.app.DatePickerDialog
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.databinding.DialogAddExpenseBinding
import java.text.SimpleDateFormat
import java.util.*

class ExpensesFragment : Fragment() {

    private var _binding: FragmentExpensesBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var expenseAdapter: ExpenseAdapter

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    private var categories = listOf<Category>()
    private var selectedDate: Date = Calendar.getInstance().time

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        databaseHelper = DatabaseHelper(requireContext())
        sessionManager = SessionManager(requireContext())

        setupUI()
        loadData()
    }

    private fun setupUI() {
        binding.rvExpenses.layoutManager = LinearLayoutManager(requireContext())
        expenseAdapter = ExpenseAdapter(emptyList())
        binding.rvExpenses.adapter = expenseAdapter

        // Setup date spinner
        val dateOptions = arrayOf("All Time", "Today", "This Week", "This Month", "This Year")
        val dateAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, dateOptions)
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerDate.adapter = dateAdapter

        binding.fabAddExpense.setOnClickListener {
            showAddExpenseDialog()
        }

        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }
    }

    private fun loadData() {
        val user = sessionManager.getUserDetails() ?: return

        // Load categories for spinner
        categories = databaseHelper.getAllCategories(user.id)
        val categoryNames = mutableListOf("All Categories")
        categoryNames.addAll(categories.map { it.name })

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = categoryAdapter

        // Load expenses
        val expenses = databaseHelper.getAllExpenses(user.id)
        if (expenses.isEmpty()) {
            binding.tvNoExpenses.visibility = View.VISIBLE
            binding.rvExpenses.visibility = View.GONE
        } else {
            binding.tvNoExpenses.visibility = View.GONE
            binding.rvExpenses.visibility = View.VISIBLE
            expenseAdapter.updateExpenses(expenses)
        }
    }

    private fun showAddExpenseDialog() {
        val dialogBinding = DialogAddExpenseBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Setup category spinner
        val categoryNames = categories.map { it.name }
        if (categoryNames.isEmpty()) {
            Toast.makeText(requireContext(), "Please add categories first", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        )
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerCategory.adapter = categoryAdapter

        // Setup date selection
        dialogBinding.btnSelectDate.text = "Date: ${displayDateFormat.format(selectedDate)}"
        dialogBinding.btnSelectDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.time
                    dialogBinding.btnSelectDate.text = "Date: ${displayDateFormat.format(selectedDate)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val amountStr = dialogBinding.etAmount.text.toString()
            val description = dialogBinding.etDescription.text.toString()
            val categoryPosition = dialogBinding.spinnerCategory.selectedItemPosition

            if (amountStr.isEmpty() || description.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val user = sessionManager.getUserDetails() ?: return@setOnClickListener
                val category = categories[categoryPosition]

                val expense = Expense(
                    amount = amount,
                    description = description,
                    date = dateFormat.format(selectedDate),
                    categoryId = category.id,
                    userId = user.id
                )

                val id = databaseHelper.addExpense(expense)
                if (id > 0) {
                    Toast.makeText(requireContext(), "Expense added successfully", Toast.LENGTH_SHORT).show()
                    loadData() // Reload data
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "Failed to add expense", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun applyFilters() {
        val user = sessionManager.getUserDetails() ?: return
        val categoryPosition = binding.spinnerCategory.selectedItemPosition
        val datePosition = binding.spinnerDate.selectedItemPosition

        // Get all expenses
        var filteredExpenses = databaseHelper.getAllExpenses(user.id)

        // Apply category filter
        if (categoryPosition > 0) { // 0 is "All Categories"
            val category = categories[categoryPosition - 1]
            filteredExpenses = filteredExpenses.filter { it.categoryId == category.id }
        }

        // Apply date filter
        val calendar = Calendar.getInstance()
        when (datePosition) {
            1 -> { // Today
                val today = dateFormat.format(calendar.time)
                filteredExpenses = filteredExpenses.filter { it.date == today }
            }
            2 -> { // This Week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val weekStart = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                val weekEnd = dateFormat.format(calendar.time)
                filteredExpenses = filteredExpenses.filter { it.date in weekStart..weekEnd }
            }
            3 -> { // This Month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val monthStart = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val monthEnd = dateFormat.format(calendar.time)
                filteredExpenses = filteredExpenses.filter { it.date in monthStart..monthEnd }
            }
            4 -> { // This Year
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                val yearStart = dateFormat.format(calendar.time)
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                val yearEnd = dateFormat.format(calendar.time)
                filteredExpenses = filteredExpenses.filter { it.date in yearStart..yearEnd }
            }
        }

        // Update UI
        if (filteredExpenses.isEmpty()) {
            binding.tvNoExpenses.visibility = View.VISIBLE
            binding.rvExpenses.visibility = View.GONE
        } else {
            binding.tvNoExpenses.visibility = View.GONE
            binding.rvExpenses.visibility = View.VISIBLE
            expenseAdapter.updateExpenses(filteredExpenses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}