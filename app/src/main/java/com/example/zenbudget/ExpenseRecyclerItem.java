package com.example.zenbudget;

import com.example.zenbudget.classes.Expense;

public class ExpenseRecyclerItem {
    Expense expense;
    String expenseId;

    public ExpenseRecyclerItem(Expense expense, String ExpenseId) {
        this.expense = expense;
        this.expenseId = ExpenseId;
    }

    public Expense getExpense() {
        return expense;
    }

    public void setExpense(Expense expense) {
        this.expense = expense;
    }

    public String getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(String expenseId) {
        this.expenseId = expenseId;
    }
}
