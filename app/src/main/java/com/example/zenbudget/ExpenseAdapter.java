package com.example.zenbudget;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenbudget.classes.Expense;
import com.example.zenbudget.utils.FirestoreUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.Toast;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final List<ExpenseRecyclerItem> expenses;
    FragmentManager fragmentManager;

    public ExpenseAdapter(List<ExpenseRecyclerItem> expenses, FragmentManager fragmentManager) {
        this.expenses = expenses;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout and create the ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.expense_item, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ExpenseRecyclerItem item = expenses.get(position);
        Expense expense = item.getExpense();
        ExpenseViewHolder expenseViewHolder = (ExpenseViewHolder) holder;

        if (expense.getDescription().isEmpty()) {
            expenseViewHolder.txtDescription.setVisibility(View.GONE);
        }

        expenseViewHolder.txtCategory.setText(expense.getCategory());
        expenseViewHolder.txtExpenseTitle.setText(expense.getTitle());
        expenseViewHolder.txtDescription.setText(expense.getDescription());
        expenseViewHolder.txtAmount.setText(String.format("-$%.2f", Double.valueOf(expense.getAmount())));
        expenseViewHolder.txtDate.setText(FirestoreUtils.formatDate(expense.getDate(), "dd/mm/yyyy"));


        expenseViewHolder.btnDelete.setOnClickListener(v -> {

            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Are you sure you want to delete this expense?")
                    .setMessage("This will delete your expense, " + expense.getTitle() + ". This action cannot be undone.")
                    .setNeutralButton("Cancel", (dialog, which) -> {
                        dialog.dismiss();
                    })
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("expenses")
                                .document(item.getExpenseId())
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        db.collection("expenses")
                                                .document(item.getExpenseId())
                                                .delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(v.getContext(), "Successfully deleted " + expense.getTitle() + " expense.", Toast.LENGTH_LONG).show();
                                                    dialog.dismiss();

                                                    EventDashboard fragment = new EventDashboard();
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("eventId", expense.getEvent());
                                                    fragment.setArguments(bundle);

                                                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                                                });
                                    } else {
                                        dialog.dismiss();
                                        Log.d("ExpenseAdapter", "onBindViewHolder: " + task.getException());
                                        Toast.makeText(v.getContext(), "Error deleting " + expense.getTitle() + "expense.", Toast.LENGTH_LONG).show();
                                    }
                                });
                    })
                    .show();
            Log.d("ExpenseAdapter", "onBindViewHolder: " + item.getExpenseId());
        });

    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }
}
