package com.example.zenbudget;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zenbudget.classes.Event;

import java.util.List;

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

        Log.d("HomeViewAdapter", "onBindViewHolder: " + event.getEventName());

        if (type == HomeRecyclerItem.TYPE_EVENT) {
            Glide.with(currentHolder.imgEvent.getContext())
                    .load(event.getImgUrl())
                    .apply(new RequestOptions().centerCrop()
                    ).into(currentHolder.imgEvent);

            currentHolder.lblEventName.setText(event.getEventName());
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
