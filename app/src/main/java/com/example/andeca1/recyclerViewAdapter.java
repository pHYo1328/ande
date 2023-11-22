package com.example.andeca1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class recyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private List<recycleItem> items;
    private static final int TYPE_CURRENT = 0;
    private static final int TYPE_UPCOMING = 1;
    private static final int TYPE_NORMAL =2;

    public recyclerViewAdapter(List<recycleItem> items) {
        this.items = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_CURRENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.current_event_item, parent, false);
            return new CurrentViewHolder(view);
        } else if(viewType == TYPE_UPCOMING) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.coming_event_item, parent, false);
            return new UpcomingViewHolder(view);
        }
        else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.normal_item, parent, false);
            return new NormalViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_CURRENT) {
            CurrentViewHolder currentHolder = (CurrentViewHolder) holder;
            recycleItem item = items.get(position);
            currentHolder.eventName.setText(item.getEventTitle());
            currentHolder.date.setText(item.getDate().toString()+ "->"+ item.getDate().toString());
            currentHolder.circularProgressBar.setProgress(item.getProgress());
            currentHolder.amount.setText(item.getAmount());
            currentHolder.wrapElement.setBackgroundResource(item.getPhotoID());
        } else if(getItemViewType(position)==TYPE_UPCOMING){
            UpcomingViewHolder upcomingHolder = (UpcomingViewHolder) holder;
            recycleItem item = items.get(position);
            upcomingHolder.eventName.setText(item.getEventTitle());
            upcomingHolder.date.setText(item.getDate().toString()+ "->"+ item.getDate().toString());
            upcomingHolder.circularProgressBar.setProgress(item.getProgress());
            upcomingHolder.amount.setText(item.getAmount());
            upcomingHolder.wrapElement.setBackgroundResource(item.getPhotoID());
        }
        else{
            NormalViewHolder normalHolder = (NormalViewHolder) holder;
            recycleItem item = items.get(position);
            normalHolder.eventName.setText(item.getEventTitle());
            normalHolder.date.setText(item.getDate().toString()+ "->"+ item.getDate().toString());
            normalHolder.circularProgressBar.setProgress(item.getProgress());
            normalHolder.amount.setText(item.getAmount());
            normalHolder.wrapElement.setBackgroundResource(item.getPhotoID());
        }

    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_CURRENT;
        } else if(position==1) {
            return TYPE_UPCOMING;
        }else return TYPE_NORMAL;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
