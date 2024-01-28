package com.example.andeca1;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andeca1.classes.Event;
import com.example.andeca1.classes.SubEvent;

import java.util.List;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    public interface OnEventEditListener {
        void onEventEditClicked(String eventId,String startDate);
    }

    public interface OnSubEventEditListener {
        void onSubEventEditClicked(String eventId,String subEventId, String startDate, String endDate);
    }


    private final List<Event> eventList;
    private final OnEventEditListener editButtonClickListener;
    private final OnSubEventEditListener subEventEditButtonClickListener;


    // Constructor
    public EventsAdapter(List<Event> events, OnEventEditListener eventListener, OnSubEventEditListener subEventListener) {
        this.eventList = events;
        this.editButtonClickListener = eventListener;
        this.subEventEditButtonClickListener = subEventListener;
    }


    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventTitle,date,budget;
        ImageButton btnEventEdit;
        LinearLayout subEventsContainer;

        EventViewHolder(View itemView) {
            super(itemView);
            eventTitle = itemView.findViewById(R.id.eventTitle);
            date = itemView.findViewById(R.id.txtViewDate);
            budget = itemView.findViewById(R.id.txtViewBudget);
            btnEventEdit = itemView.findViewById(R.id.btnEventEdit);
            subEventsContainer = itemView.findViewById(R.id.subEventsContainer);
        }

        void bind(Event event) {
            eventTitle.setText(event.getEventName());
            date.setText(String.format("%s - %s", event.getStartDate(), event.getEndDate()));
            budget.setText(String.format("budget: $%s", event.getBudget()));
            btnEventEdit.setOnClickListener(view -> {
                if (editButtonClickListener != null) {
                    Log.d("checkData","eventId "+ event.getId());
                    editButtonClickListener.onEventEditClicked(event.getId(),event.getStartDate());
                }
            });
            subEventsContainer.removeAllViews();
            if (event.getExpanded()) {
                for (SubEvent subEvent : event.getSubEvents()) {
                    if(subEvent.getSubEvent_title() != null){
                        View subEventView = LayoutInflater.from(subEventsContainer.getContext())
                                .inflate(R.layout.sub_event_item, subEventsContainer, false);
                        TextView subEventTitle = subEventView.findViewById(R.id.txtSubEventTitle);
                        TextView time = subEventView.findViewById(R.id.txtViewTime);
                        TextView subBudget = subEventView.findViewById(R.id.txtSubBudget);
                        ImageButton btnSubEventEdit = subEventView.findViewById(R.id.btnSubEventEdit);
                        subEventTitle.setText(subEvent.getSubEvent_title());
                        time.setText(String.format("%s - %s", subEvent.getStart_time(), subEvent.getEnd_time()));
                        subBudget.setText(String.format("%s", subEvent.getSubEvent_budget()));
                        btnSubEventEdit.setOnClickListener(v->{
                            if (subEventEditButtonClickListener != null) {
                                subEventEditButtonClickListener.onSubEventEditClicked(event.getId(),subEvent.getId(),event.getStartDate(), event.getEndDate());
                            }
                        });

                        subEventsContainer.addView(subEventView);
                    }
                }
            }

            itemView.setOnClickListener(v -> {
                event.setExpanded(!event.getExpanded());
                notifyItemChanged(getAdapterPosition());
                Log.d("checkData", String.valueOf(getAdapterPosition()));
            });
        }
    }
}
