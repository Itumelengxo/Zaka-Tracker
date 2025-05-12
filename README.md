ITUMELENG KOLA-ST10075573
LWANDLE MSIYA-ST10406108
Zaka Tracker
Zaka Tracker is a personal finance Android application that helps users track their daily expenses, monitor spending categories, and stay within their set monthly budget. Designed with simplicity and usability in mind, Zaka Tracker offers a clear, interactive interface backed by a local SQLite database.

Features
User Authentication – Secure sign-up and login system per user.
Dashboard – Real-time summary showing total expenses and remaining budget.
Expense Management – Add, view, edit, and delete individual expenses.
Categorization – Organize expenses by category (e.g., Food, Transport, Entertainment).
Budget Settings – Define or update your monthly budget.
Offline Support – Data is stored locally using SQLite, no internet required.
Modern UI – A sleek purple-themed interface for a clean and user-friendly experience.

Screenshots
Dashboard	Add Expense	View Expenses
(Add screenshots in screenshots/ folder)		

Setup Instructions
Requirements
Android Studio (latest stable version)

Android SDK 21 or above
Emulator or physical Android device
Kotlin support enabled

Steps
Clone the repository

bash
Copy
Edit
git clone https://github.com/Itumelengxo/Zaka-Tracker.git
cd Zaka-Tracker
Open the project in Android Studio

Launch Android Studio.

Go to File > Open, then select the Zaka-Tracker directory.

Build and Sync

Let Gradle sync the project.

Install any missing dependencies if prompted.

Run the App

Select an emulator or connect a physical device.

Click the Run button to build and launch the app.

Start Using the App

Sign up with a username.

Set your monthly budget.

Begin logging and tracking your expenses.

Tech Stack
Language: Kotlin

Database: SQLite (via a custom DatabaseHelper class)

UI: XML layouts with custom drawable styles

Architecture: Activity-based structure

Libraries: Android Jetpack components
