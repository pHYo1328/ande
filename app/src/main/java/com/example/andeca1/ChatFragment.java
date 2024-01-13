package com.example.andeca1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private List<ChatMessage> chatMessages;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeChatMessages();

        RecyclerView recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ChatAdapter chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);

        return view;
    }

    private void initializeChatMessages() {
        chatMessages = new ArrayList<>();
        // Add two sample messages
        for (int i = 0; i < 20; i++) {
            chatMessages.add(new ChatMessage("Hi there!", "10:15 AM", ChatMessage.TYPE_RECIPIENT));
            chatMessages.add(new ChatMessage("Hello!", "10:16 AM", ChatMessage.TYPE_SENDER));
        }
//        chatMessages.add(new ChatMessage("Hi there!", "10:15 AM", ChatMessage.TYPE_RECIPIENT));
//        chatMessages.add(new ChatMessage("Hello!", "10:16 AM", ChatMessage.TYPE_SENDER));
    }
}
