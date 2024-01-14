package com.example.andeca1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

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
