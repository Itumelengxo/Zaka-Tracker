package com.example.open_sourcepart2

// DatabaseHelper.kt

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.*

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "budget_tracker.db"
        private const val DATABASE_VERSION = 1

        // User table
        private const val TABLE_USERS = "users"
        private const val COLUMN_USER_ID = "id"
        private const val COLUMN_USER_NAME = "name"
        private const val COLUMN_USER_EMAIL = "email"
        private const val COLUMN_USER_PASSWORD = "password"

        // Category table
        private const val TABLE_CATEGORIES = "categories"
        private const val COLUMN_CATEGORY_ID = "id"
        private const val COLUMN_CATEGORY_NAME = "name"
        private const val COLUMN_CATEGORY_COLOR = "color"
        private const val COLUMN_CATEGORY_BUDGET = "budget"
        private const val COLUMN_CATEGORY_USER_ID = "user_id"

        // Expense table
        private const val TABLE_EXPENSES = "expenses"
        private const val COLUMN_EXPENSE_ID = "id"
        private const val COLUMN_EXPENSE_AMOUNT = "amount"
        private const val COLUMN_EXPENSE_DESCRIPTION = "description"
        private const val COLUMN_EXPENSE_DATE = "date"
        private const val COLUMN_EXPENSE_CATEGORY_ID = "category_id"
        private const val COLUMN_EXPENSE_USER_ID = "user_id"
        private const val COLUMN_EXPENSE_IMAGE_PATH = "image_path"

        // Budget table
        private const val TABLE_BUDGETS = "budgets"
        private const val COLUMN_BUDGET_ID = "id"
        private const val COLUMN_BUDGET_AMOUNT = "amount"
        private const val COLUMN_BUDGET_PERIOD = "period"
        private const val COLUMN_BUDGET_START_DATE = "start_date"
        private const val COLUMN_BUDGET_END_DATE = "end_date"
        private const val COLUMN_BUDGET_USER_ID = "user_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create users table
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT,
                $COLUMN_USER_EMAIL TEXT UNIQUE,
                $COLUMN_USER_PASSWORD TEXT
            )
        """.trimIndent()
        db.execSQL(createUserTable)

// Add this to your onCreate method
        db.execSQL("""
    CREATE TABLE $TABLE_INCOME (
        $COLUMN_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_INCOME_AMOUNT REAL NOT NULL,
        $COLUMN_INCOME_SOURCE TEXT NOT NULL,
        $COLUMN_INCOME_NOTE TEXT,
        $COLUMN_INCOME_DATE TEXT NOT NULL,
        $COLUMN_INCOME_USER_ID INTEGER NOT NULL,
        FOREIGN KEY ($COLUMN_INCOME_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
    )
""")

        // Create categories table
        val createCategoryTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT,
                $COLUMN_CATEGORY_COLOR TEXT,
                $COLUMN_CATEGORY_BUDGET REAL,
                $COLUMN_CATEGORY_USER_ID INTEGER,
                FOREIGN KEY($COLUMN_CATEGORY_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        db.execSQL(createCategoryTable)

        // Create expenses table
        val createExpenseTable = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_EXPENSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_EXPENSE_AMOUNT REAL,
                $COLUMN_EXPENSE_DESCRIPTION TEXT,
                $COLUMN_EXPENSE_DATE TEXT,
                $COLUMN_EXPENSE_CATEGORY_ID INTEGER,
                $COLUMN_EXPENSE_USER_ID INTEGER,
                $COLUMN_EXPENSE_IMAGE_PATH TEXT,
                FOREIGN KEY($COLUMN_EXPENSE_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_CATEGORY_ID),
                FOREIGN KEY($COLUMN_EXPENSE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        db.execSQL(createExpenseTable)

        // Create budgets table
        val createBudgetTable = """
            CREATE TABLE $TABLE_BUDGETS (
                $COLUMN_BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_BUDGET_AMOUNT REAL,
                $COLUMN_BUDGET_PERIOD TEXT,
                $COLUMN_BUDGET_START_DATE TEXT,
                $COLUMN_BUDGET_END_DATE TEXT,
                $COLUMN_BUDGET_USER_ID INTEGER,
                FOREIGN KEY($COLUMN_BUDGET_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """.trimIndent()
        db.execSQL(createBudgetTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BUDGETS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EXPENSES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")

        // Create tables again
        onCreate(db)
    }

    // User operations
    fun addUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_USER_NAME, user.name)
        values.put(COLUMN_USER_EMAIL, user.email)
        values.put(COLUMN_USER_PASSWORD, user.password)

        // Insert row
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun getUser(email: String, password: String): User? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID, COLUMN_USER_NAME, COLUMN_USER_EMAIL, COLUMN_USER_PASSWORD),
            "$COLUMN_USER_EMAIL = ? AND $COLUMN_USER_PASSWORD = ?",
            arrayOf(email, password),
            null, null, null
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD))
            )
        }
        cursor.close()
        db.close()
        return user
    }

    fun checkUser(email: String): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_USER_ID),
            "$COLUMN_USER_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // Category operations
    fun addCategory(category: Category): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CATEGORY_NAME, category.name)
        values.put(COLUMN_CATEGORY_COLOR, category.color)
        values.put(COLUMN_CATEGORY_BUDGET, category.budget)
        values.put(COLUMN_CATEGORY_USER_ID, category.userId)

        // Insert row
        val id = db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        return id
    }

    fun updateCategory(category: Category): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_CATEGORY_NAME, category.name)
        values.put(COLUMN_CATEGORY_COLOR, category.color)
        values.put(COLUMN_CATEGORY_BUDGET, category.budget)

        // Update row
        val result = db.update(
            TABLE_CATEGORIES,
            values,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(category.id.toString())
        )
        db.close()
        return result
    }

    fun deleteCategory(categoryId: Long): Int {
        val db = this.writableDatabase
        val result = db.delete(
            TABLE_CATEGORIES,
            "$COLUMN_CATEGORY_ID = ?",
            arrayOf(categoryId.toString())
        )
        db.close()
        return result
    }

    fun getAllCategories(userId: Long): List<Category> {
        val categories = mutableListOf<Category>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_CATEGORIES,
            arrayOf(COLUMN_CATEGORY_ID, COLUMN_CATEGORY_NAME, COLUMN_CATEGORY_COLOR, COLUMN_CATEGORY_BUDGET),
            "$COLUMN_CATEGORY_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            do {
                val category = Category(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                    color = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_COLOR)),
                    budget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_BUDGET)),
                    userId = userId
                )
                categories.add(category)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return categories
    }// Add these constants at the top of your DatabaseHelper class
    private val TABLE_INCOME = "income"
    private val COLUMN_INCOME_ID = "id"
    private val COLUMN_INCOME_AMOUNT = "amount"
    private val COLUMN_INCOME_SOURCE = "source"
    private val COLUMN_INCOME_NOTE = "note"
    private val COLUMN_INCOME_DATE = "date"
    private val COLUMN_INCOME_USER_ID = "user_id"

    // Add this method to your DatabaseHelper class to check if the income table exists
    fun ensureIncomeTableExists() {
        val db = this.writableDatabase

        // Check if the income table exists
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='$TABLE_INCOME'",
            null
        )

        val tableExists = cursor.count > 0
        cursor.close()

        if (!tableExists) {
            // Create the income table if it doesn't exist
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_INCOME (
                $COLUMN_INCOME_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_INCOME_AMOUNT REAL NOT NULL,
                $COLUMN_INCOME_SOURCE TEXT NOT NULL,
                $COLUMN_INCOME_NOTE TEXT,
                $COLUMN_INCOME_DATE TEXT NOT NULL,
                $COLUMN_INCOME_USER_ID INTEGER NOT NULL,
                FOREIGN KEY ($COLUMN_INCOME_USER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """)
            Log.d("DatabaseHelper", "Income table created")
        }

        db.close()
    }


    // Add these methods to your DatabaseHelper class
    fun addIncome(income: Income): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_INCOME_AMOUNT, income.amount)
            put(COLUMN_INCOME_SOURCE, income.source)
            put(COLUMN_INCOME_NOTE, income.note)
            put(COLUMN_INCOME_DATE, income.date)
            put(COLUMN_INCOME_USER_ID, income.userId)
        }

        try {
            val id = db.insert(TABLE_INCOME, null, values)
            Log.d("DatabaseHelper", "Income inserted with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error inserting income", e)
            return -1
        } finally {
            db.close()
        }
    }

    fun getIncomeByUser(userId: Long): List<Income> {
        val incomeList = mutableListOf<Income>()
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_INCOME,
            null,
            "$COLUMN_INCOME_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COLUMN_INCOME_DATE DESC"
        )

        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndexOrThrow(COLUMN_INCOME_ID))
                val amount = getDouble(getColumnIndexOrThrow(COLUMN_INCOME_AMOUNT))
                val source = getString(getColumnIndexOrThrow(COLUMN_INCOME_SOURCE))
                val note = getString(getColumnIndexOrThrow(COLUMN_INCOME_NOTE))
                val date = getString(getColumnIndexOrThrow(COLUMN_INCOME_DATE))

                incomeList.add(Income(id, amount, source, note, date, userId))
            }
        }

        cursor.close()
        db.close()
        return incomeList
    }

    fun getTotalIncomeByUser(userId: Long): Double {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT SUM($COLUMN_INCOME_AMOUNT) FROM $TABLE_INCOME WHERE $COLUMN_INCOME_USER_ID = ?",
            arrayOf(userId.toString())
        )

        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }

        cursor.close()
        db.close()
        return total
    }

    // Expense operations
    fun addExpense(expense: Expense): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_EXPENSE_AMOUNT, expense.amount)
        values.put(COLUMN_EXPENSE_DESCRIPTION, expense.description)
        values.put(COLUMN_EXPENSE_DATE, expense.date)
        values.put(COLUMN_EXPENSE_CATEGORY_ID, expense.categoryId)
        values.put(COLUMN_EXPENSE_USER_ID, expense.userId)
        values.put(COLUMN_EXPENSE_IMAGE_PATH, expense.imagePath)

        // Insert row
        val id = db.insert(TABLE_EXPENSES, null, values)
        db.close()
        return id
    }

    fun updateExpense(expense: Expense): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_EXPENSE_AMOUNT, expense.amount)
        values.put(COLUMN_EXPENSE_DESCRIPTION, expense.description)
        values.put(COLUMN_EXPENSE_DATE, expense.date)
        values.put(COLUMN_EXPENSE_CATEGORY_ID, expense.categoryId)
        values.put(COLUMN_EXPENSE_IMAGE_PATH, expense.imagePath)

        // Update row
        val result = db.update(
            TABLE_EXPENSES,
            values,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expense.id.toString())
        )
        db.close()
        return result
    }

    fun deleteExpense(expenseId: Long): Int {
        val db = this.writableDatabase
        val result = db.delete(
            TABLE_EXPENSES,
            "$COLUMN_EXPENSE_ID = ?",
            arrayOf(expenseId.toString())
        )
        db.close()
        return result
    }

    fun getAllExpenses(userId: Long): List<ExpenseWithCategory> {
        val expenses = mutableListOf<ExpenseWithCategory>()
        val db = this.readableDatabase

        val query = """
            SELECT e.*, c.name as category_name, c.color as category_color
            FROM $TABLE_EXPENSES e
            LEFT JOIN $TABLE_CATEGORIES c ON e.$COLUMN_EXPENSE_CATEGORY_ID = c.$COLUMN_CATEGORY_ID
            WHERE e.$COLUMN_EXPENSE_USER_ID = ?
            ORDER BY e.$COLUMN_EXPENSE_DATE DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val expense = ExpenseWithCategory(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_ID)),
                    amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_AMOUNT)),
                    description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DESCRIPTION)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_DATE)),
                    categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_CATEGORY_ID)),
                    userId = userId,
                    imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EXPENSE_IMAGE_PATH)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category_name")),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow("category_color"))
                )
                expenses.add(expense)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return expenses
    }

    // Budget operations
    fun addBudget(budget: Budget): Long {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_BUDGET_AMOUNT, budget.amount)
        values.put(COLUMN_BUDGET_PERIOD, budget.period)
        values.put(COLUMN_BUDGET_START_DATE, budget.startDate)
        values.put(COLUMN_BUDGET_END_DATE, budget.endDate)
        values.put(COLUMN_BUDGET_USER_ID, budget.userId)

        // Insert row
        val id = db.insert(TABLE_BUDGETS, null, values)
        db.close()
        return id
    }

    fun updateBudget(budget: Budget): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_BUDGET_AMOUNT, budget.amount)
        values.put(COLUMN_BUDGET_PERIOD, budget.period)
        values.put(COLUMN_BUDGET_START_DATE, budget.startDate)
        values.put(COLUMN_BUDGET_END_DATE, budget.endDate)

        // Update row
        val result = db.update(
            TABLE_BUDGETS,
            values,
            "$COLUMN_BUDGET_ID = ?",
            arrayOf(budget.id.toString())
        )
        db.close()
        return result
    }

    fun getBudgetByPeriod(userId: Long, period: String): Budget? {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_BUDGETS,
            null,
            "$COLUMN_BUDGET_USER_ID = ? AND $COLUMN_BUDGET_PERIOD = ?",
            arrayOf(userId.toString(), period),
            null, null, null
        )

        var budget: Budget? = null
        if (cursor.moveToFirst()) {
            budget = Budget(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_ID)),
                amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_AMOUNT)),
                period = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_PERIOD)),
                startDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_START_DATE)),
                endDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BUDGET_END_DATE)),
                userId = userId
            )
        }
        cursor.close()
        db.close()
        return budget
    }

    // Statistics and summary methods
    fun getTotalExpensesByCategory(userId: Long): List<CategoryExpenseSummary> {
        val summaries = mutableListOf<CategoryExpenseSummary>()
        val db = this.readableDatabase

        val query = """
            SELECT c.$COLUMN_CATEGORY_ID, c.$COLUMN_CATEGORY_NAME, c.$COLUMN_CATEGORY_COLOR, 
                   c.$COLUMN_CATEGORY_BUDGET, SUM(e.$COLUMN_EXPENSE_AMOUNT) as total_spent
            FROM $TABLE_CATEGORIES c
            LEFT JOIN $TABLE_EXPENSES e ON c.$COLUMN_CATEGORY_ID = e.$COLUMN_EXPENSE_CATEGORY_ID
            WHERE c.$COLUMN_CATEGORY_USER_ID = ?
            GROUP BY c.$COLUMN_CATEGORY_ID
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString()))

        if (cursor.moveToFirst()) {
            do {
                val summary = CategoryExpenseSummary(
                    categoryId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                    categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                    categoryColor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_COLOR)),
                    budget = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_BUDGET)),
                    totalSpent = cursor.getDouble(cursor.getColumnIndexOrThrow("total_spent"))
                )
                summaries.add(summary)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return summaries
    }

    fun getTotalExpensesByPeriod(userId: Long, startDate: String, endDate: String): Double {
        val db = this.readableDatabase
        var total = 0.0

        val query = """
            SELECT SUM($COLUMN_EXPENSE_AMOUNT) as total
            FROM $TABLE_EXPENSES
            WHERE $COLUMN_EXPENSE_USER_ID = ?
            AND $COLUMN_EXPENSE_DATE BETWEEN ? AND ?
        """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(userId.toString(), startDate, endDate))

        if (cursor.moveToFirst()) {
            total = cursor.getDouble(cursor.getColumnIndexOrThrow("total"))
        }
        cursor.close()
        db.close()
        return total
    }
}