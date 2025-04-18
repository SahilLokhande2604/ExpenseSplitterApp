📖 Expense Splitter App (Java CLI Project)
🚀 Overview
A console-based Java application to manage group expenses, split costs among users, simplify debts, and track payment history.
Inspired by apps like Splitwise.

✨ Features
Create Users 👤

Create Groups 👥

Add Users to Groups ➕

Add Expenses with custom shares 💸

Auto Debt Simplification (Minimize number of transactions) 🔄

Make Payments between users 🧾

View Balances 💵

View Transaction Logs 📜

View All Outstanding Debts 📈

Run a Demo Test Case 🧪

📋 How It Works
Create Users

Create a Group

Add Users to Group

Add Expenses (equal or custom splits)

Debt Simplification after every expense

Make Payments to settle debts

View Balances, Logs, and Detailed Debts

📂 Project Structure
User → Represents a user (Overrides equals, hashCode, toString)

Group → Represents a group of users, manages:

Members

Balances

Debt Map

Logs

ExpenseSplitterApp → Main class with CLI Menu

🛠 Tech Stack
Language: Java

Libraries: None (Only Core Java - Collections)

📸 Screenshots
Example Menu:

sql
Copy
Edit
1. Create User
2. Create Group
3. Add User to Group
4. Add Expense
5. Make Payment
6. View Balances
7. View Logs
8. View Debts
9. Run Test Case
10. Exit
Choose option:
Example Log:

vbnet
Copy
Edit
Transaction Log for Group Trip:
- 2 will pay 400 to 0
- 0 will pay 100 to 1
- 2 will pay 100 to 1
Example Balances:

rust
Copy
Edit
Balances for user: 0 -> Net balance: 500
Balances for user: 1 -> Net balance: 200
Balances for user: 2 -> Net balance: -700
🧪 How to Run
bash
Copy
Edit
javac ExpenseSplitterApp.java
java ExpenseSplitterApp
🧠 Concepts Used
Object-Oriented Programming (OOP)

HashMap, HashSet, List, PriorityQueue

Debt Simplification Algorithm (Greedy Approach)

Java Collections Framework (JCF)

CLI-based interaction
