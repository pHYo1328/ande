package com.example.zenbudget;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {

    // Using MutableLiveData for chat messages
    private MutableLiveData<List<ChatMessage>> chatMessages;

    public MutableLiveData<List<ChatMessage>> getChatMessages() {
        if (chatMessages == null) {
            chatMessages = new MutableLiveData<>();
            chatMessages.setValue(new ArrayList<>()); // Initialize with empty list
        }
        return chatMessages;
    }

    // Method to add a message to the list
    public void addChatMessage(ChatMessage message) {
        List<ChatMessage> currentMessages = chatMessages.getValue();
        if (currentMessages != null) {
            currentMessages.add(message);
            chatMessages.setValue(currentMessages); // Trigger LiveData update
        }
    }
}
