package com.example.zenbudget;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

public class CurrentViewHolder extends RecyclerView.ViewHolder {
    public LinearLayout headerLayout, eventDetails;

    public TextView lblHeader, lblEventName, txtStartDate, txtEndDate,  lblAmount, txtInfo;
    public ImageButton action, btnEventMore;

    public CardView cardDetails;
    public ImageView imgEvent;

    CircularProgressBar progressBar;
    public CurrentViewHolder(View itemView) {
        super(itemView);

        headerLayout = itemView.findViewById(R.id.layoutEventHeader);
        lblHeader = itemView.findViewById(R.id.lblHeader);
        action = itemView.findViewById(R.id.btnHeader);

        cardDetails = itemView.findViewById(R.id.cardDetails);
        imgEvent = itemView.findViewById(R.id.imgEventBG);

        eventDetails = itemView.findViewById(R.id.layoutEventDetails);
        lblEventName = itemView.findViewById(R.id.lblEventName);
        btnEventMore = itemView.findViewById(R.id.btnEventMore);

        txtStartDate = itemView.findViewById(R.id.txtStartDate);
        txtEndDate = itemView.findViewById(R.id.txtEndDate);
        lblAmount = itemView.findViewById(R.id.lblAmount);
        progressBar = itemView.findViewById(R.id.progressBar);

        txtInfo = itemView.findViewById(R.id.txtInfo);

    }
}
