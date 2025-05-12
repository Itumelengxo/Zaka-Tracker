package com.example.open_sourcepart2

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

// ui/home/HomeFragment.kt
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.open_sourcepart2.databinding.FragmentHomeBinding
import java.text.NumberFormat
import java.util.*
// Add these imports at the top of your file
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var sessionManager: SessionManager
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
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
        val user = sessionManager.getUserDetails()
        binding.tvWelcome.text = "Welcome, ${user?.name ?: "User"}"

        binding.rvRecentExpenses.layoutManager = LinearLayoutManager(requireContext())
        expenseAdapter = ExpenseAdapter(emptyList())
        binding.rvRecentExpenses.adapter = expenseAdapter

        // Inside your fragment class, implement the button click listeners
        binding.btnIncome.setOnClickListener {
            showAddIncomeDialog()
        }

        binding.btnOutcome.setOnClickListener {
            // Navigate to expenses fragment
            // If you're using Navigation Component:

            // If you're not using Navigation Component:
            // val transaction = requireActivity().supportFragmentManager.beginTransaction()
            // transaction.replace(R.id.fragment_container, ExpensesFragment())
            // transaction.addToBackStack(null)
            // transaction.commit()
        }
    }




    private fun loadData() {
        val user = sessionManager.getUserDetails() ?: return

        // Get recent expenses
        val expenses = databaseHelper.getAllExpenses(user.id).take(5)
        if (expenses.isEmpty()) {
            binding.tvNoExpenses.visibility = View.VISIBLE
            binding.rvRecentExpenses.visibility = View.GONE
        } else {
            binding.tvNoExpenses.visibility = View.GONE
            binding.rvRecentExpenses.visibility = View.VISIBLE
            expenseAdapter.updateExpenses(expenses)
        }

        // Calculate total balance
        val totalExpenses = expenses.sumOf { it.amount }
        val formatter = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("ZAR")
        }
        binding.tvTotalBalance.text = formatter.format(totalExpenses)
    }


    private fun showAddIncomeDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_expense, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        // Hide the category spinner for income dialog
        spinnerCategory.visibility = View.GONE

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            val description = etDescription.text.toString().trim()

            if (amountStr.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val amount = amountStr.toDouble()
                val user = sessionManager.getUserDetails()

                if (user != null) {
                    databaseHelper.ensureIncomeTableExists()

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val currentDate = dateFormat.format(Date())

                    val income = Income(
                        amount = amount,
                        source = description, // Using description as source
                        note = "", // Empty note since we don't have this field
                        date = currentDate,
                        userId = user.id
                    )

                    val id = databaseHelper.addIncome(income)

                    if (id > 0) {
                        Toast.makeText(requireContext(), "Income added successfully", Toast.LENGTH_SHORT).show()
                        loadData()
                        dialog.dismiss()
                    } else {
                        Toast.makeText(requireContext(), "Failed to add income", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}