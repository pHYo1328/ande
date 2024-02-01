package com.example.zenbudget;

import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.RecyclerView;

public class ExpenseViewHolder extends RecyclerView.ViewHolder{
    public TextView txtExpenseTitle, txtDescription, txtAmount, txtDate, txtCategory;
    public LinearLayout layoutExpenseDetails;
    public FragmentContainerView cardDetails;

    public ImageButton btnDelete;
    public ExpenseViewHolder(@NonNull View v) {
        super(v);

        txtExpenseTitle = v.findViewById(R.id.txtExpenseTitle);
        txtDescription = v.findViewById(R.id.txtDescription);
        txtAmount = v.findViewById(R.id.txtAmount);
        txtDate = v.findViewById(R.id.txtDate);
        txtCategory = v.findViewById(R.id.txtCategory);
        layoutExpenseDetails = v.findViewById(R.id.layoutExpenseDetails);
        cardDetails = v.findViewById(R.id.container_expenses);
        btnDelete = v.findViewById(R.id.btnDeleteExpense);
    }
}
