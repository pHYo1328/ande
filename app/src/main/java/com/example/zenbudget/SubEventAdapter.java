package com.example.zenbudget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenbudget.classes.SubEvent;
import com.example.zenbudget.utils.FirestoreUtils;

import java.util.List;
import java.util.Locale;

public class SubEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    List<SubEventRecyclerItem> subEvents;
    FragmentManager fragmentManger;
    public SubEventAdapter(List<SubEventRecyclerItem> subEvents, FragmentManager fragmentManger){
        this.subEvents = subEvents;
        this.fragmentManger = fragmentManger;
    }

    @NonNull
    @Override
    public SubEventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subevent_item, parent, false);
        return new SubEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SubEventViewHolder subEventHolder = (SubEventViewHolder) holder;
        SubEventRecyclerItem item = subEvents.get(position);
        SubEvent subEvent = item.getSubEvent();

        subEventHolder.txtTitle.setText(subEvent.getSubEvent_title());
        subEventHolder.txtStartTime.setText(subEvent.getStart_time());
        subEventHolder.txtEndTime.setText(subEvent.getEnd_time());
        subEventHolder.txtDate.setText(String.format("%s,", FirestoreUtils.formatDate(subEvent.getSubEvent_date())));
        subEventHolder.txtBudgetAllocation.setText(String.format(Locale.getDefault(), "+$%.2f", subEvent.getSubEvent_budget()));
    }

    @Override
    public int getItemCount() {
        return subEvents.size();
    }
}
