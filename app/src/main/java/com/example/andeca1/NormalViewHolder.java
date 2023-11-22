package com.example.andeca1;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class NormalViewHolder extends RecyclerView.ViewHolder {
    public TextView eventName;
    public TextView date;
    public CircularProgressBar circularProgressBar;
    public TextView amount;
    public LinearLayout wrapElement;
    public NormalViewHolder(View itemView) {
        super(itemView);
        eventName= itemView.findViewById(R.id.event_name);
        date = itemView.findViewById(R.id.date);
        circularProgressBar = itemView.findViewById(R.id.progressBar);
        amount= itemView.findViewById(R.id.amount);
        wrapElement = itemView.findViewById(R.id.wrapElement3);
    }
}
