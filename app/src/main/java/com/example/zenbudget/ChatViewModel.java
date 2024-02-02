package com.example.zenbudget;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private MutableLiveData<List<ChatMessage>> chatMessages;

    public MutableLiveData<List<ChatMessage>> getChatMessages() {
        if (chatMessages == null) {
            chatMessages = new MutableLiveData<>();
            chatMessages.setValue(new ArrayList<>());
        }
        return chatMessages;
    }

    public void addChatMessage(ChatMessage message) {
        List<ChatMessage> currentMessages = chatMessages.getValue();
        if (currentMessages != null) {
            currentMessages.add(message);
            chatMessages.setValue(currentMessages);
        }
    }
}
