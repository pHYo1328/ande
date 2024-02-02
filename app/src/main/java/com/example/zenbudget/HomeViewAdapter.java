package com.example.zenbudget;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zenbudget.classes.Event;
import com.example.zenbudget.utils.FirestoreUtils;

import java.util.List;
import java.util.Locale;

public class HomeViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<HomeRecyclerItem> items;
    private final FragmentManager fragmentManager;


    public HomeViewAdapter(List<HomeRecyclerItem> items, FragmentManager fragmentManager) {
        this.items = items;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_event_item, parent, false);
        return new CurrentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CurrentViewHolder currentHolder = (CurrentViewHolder) holder;
        HomeRecyclerItem item = items.get(position);
        int type = item.getType();
        Event event = item.getEvent();

        if (type == HomeRecyclerItem.TYPE_EVENT) {
            currentHolder.headerLayout.setVisibility(View.GONE);
            currentHolder.txtInfo.setVisibility(View.GONE);
            currentHolder.txtStartDate.setText(FirestoreUtils.formatDate(event.getStartDate()));
            currentHolder.txtEndDate.setText(FirestoreUtils.formatDate(event.getEndDate()));
            Glide.with(currentHolder.imgEvent.getContext())
                    .load(event.getImgUrl())
                    .apply(new RequestOptions().centerCrop()
                    ).into(currentHolder.imgEvent);

            currentHolder.lblEventName.setText(event.getEventName());
            currentHolder.cardDetails.setOnClickListener(v -> {
                EventDashboard eventDetailsFragment = new EventDashboard();
                Bundle args = new Bundle();
                args.putString("eventId", item.getEventId());
                eventDetailsFragment.setArguments(args);
                fragmentManager.beginTransaction().replace(R.id.content_frame, eventDetailsFragment).commit();
            });

            Double left = item.getTotalBudget() - item.getTotalSpent();
            currentHolder.lblAmount.setText(String.format(Locale.getDefault(), "$%.2f", left));

            if (left < item.getTotalBudget() / 2) {
                int colorResourceId = R.color.theme_danger;

                int colorInt = ContextCompat.getColor(currentHolder.progressBar.getContext(), colorResourceId);

                currentHolder.progressBar.setProgressBarColor(colorInt);
            }

            if (left < 0) {
                currentHolder.lblAmount.setText(String.format(Locale.getDefault(), "-$%.2f", Math.abs(left)));
            }

            currentHolder.progressBar.setProgress((float) (left/ item.getTotalBudget() * 100));
        } else if (type == HomeRecyclerItem.TYPE_CURRENT) {
            currentHolder.lblHeader.setText("Current Events");
            currentHolder.txtInfo.setVisibility(View.GONE);
            currentHolder.cardDetails.setVisibility(View.GONE);
        } else if (type == HomeRecyclerItem.TYPE_UPCOMING) {
            currentHolder.lblHeader.setText("Upcoming Events");
            currentHolder.txtInfo.setVisibility(View.GONE);
            currentHolder.cardDetails.setVisibility(View.GONE);
        } else if (type == HomeRecyclerItem.TYPE_INFO) {
            currentHolder.headerLayout.setVisibility(View.GONE);
            currentHolder.cardDetails.setVisibility(View.GONE);
            currentHolder.txtInfo.setText(item.getInfoMsg());
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
