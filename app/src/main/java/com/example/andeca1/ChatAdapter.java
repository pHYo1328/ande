package com.example.andeca1;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface SelectionModeListener {
        void onSelectionModeChanged(int selectedItemCount, List<ChatMessage> selectedMessages);
    }

    private final SelectionModeListener selectionModeListener;
    int selectedItemCount = 0;
    private List<ChatMessage> chatMessages;
    private final List<ChatMessage> selectedMessages = new ArrayList<>();

    public ChatAdapter(List<ChatMessage> chatMessages, SelectionModeListener listener) {

        this.chatMessages = chatMessages;
        this.selectionModeListener = listener;
    }

    private boolean isSelectionMode = false;

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == ChatMessage.TYPE_SENDER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sender, parent, false);
            return new SenderViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_recipient, parent, false);
            return new RecipientViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.getType() == ChatMessage.TYPE_SENDER) {
            ((SenderViewHolder) holder).bind(message);
        } else {
            ((RecipientViewHolder) holder).bind(message);
        }

        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                toggleSelection(position);
            }
            return true;
        });

        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(position);
                checkAndExitSelectionMode();
            }
        });

        // Update UI based on selection
        holder.itemView.setSelected(message.getIsSelected());
    }

    private void toggleSelection(int position) {
        ChatMessage message = chatMessages.get(position);
        message.setIsSelected(!message.getIsSelected());
        notifyItemChanged(position);

        if (message.getIsSelected()) {
            selectedItemCount += 1;
            selectedMessages.add(message);
        } else {
            selectedItemCount -= 1;
            selectedMessages.remove(message);
        }
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionModeChanged(selectedItemCount, selectedMessages);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void exitSelectionMode() {
        isSelectionMode = false;
        for (ChatMessage message : chatMessages) {
            message.setIsSelected(false);
        }
        selectedMessages.clear();
        selectedItemCount = 0;
        notifyDataSetChanged();
        selectionModeListener.onSelectionModeChanged(0, null);
    }

    private void checkAndExitSelectionMode() {
        if (!isAnyMessageSelected()) {
            isSelectionMode = false;
        }
    }

    private boolean isAnyMessageSelected() {
        for (ChatMessage message : chatMessages) {
            if (message.getIsSelected()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void setChatMessages(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }


    static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView body;
        TextView timestamp;

        SenderViewHolder(View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.text_message_body_sender);
            timestamp = itemView.findViewById(R.id.timestamp_sender);
        }

        void bind(ChatMessage message) {
            body.setText(message.getMessage());
            timestamp.setText(message.getTimestamp());
        }
    }

    static class RecipientViewHolder extends RecyclerView.ViewHolder {
        TextView body;
        TextView timestamp;

        RecipientViewHolder(View itemView) {
            super(itemView);
            body = itemView.findViewById(R.id.text_message_body_recipient);
            timestamp = itemView.findViewById(R.id.timestamp_recipient);
        }

        void bind(ChatMessage message) {
            body.setText(message.getMessage());
            timestamp.setText(message.getTimestamp());
        }
    }
}
