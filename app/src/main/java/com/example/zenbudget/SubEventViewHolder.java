package com.example.zenbudget;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class SubEventViewHolder extends RecyclerView.ViewHolder{
    public TextView txtTitle, txtDate, txtStartTime, txtEndTime, txtBudgetAllocation;
    public SubEventViewHolder(View v) {
        super(v);

        txtTitle = v.findViewById(R.id.txtSubEventName);
        txtDate = v.findViewById(R.id.txtSubEventDate);
        txtBudgetAllocation = v.findViewById(R.id.txtBudgetAllocated);
        txtStartTime = v.findViewById(R.id.txtSubEventStart);
        txtEndTime = v.findViewById(R.id.txtSubEventEnd);

    }
}
