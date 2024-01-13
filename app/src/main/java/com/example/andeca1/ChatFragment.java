package com.example.andeca1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseStream;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatFragment extends Fragment implements View.OnClickListener {

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeChatMessages();

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);

        messageInput = view.findViewById(R.id.message_input);
        View sendButton = view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(this); // Set the click listener

        return view;
    }

    private void initializeChatMessages() {
        chatMessages = new ArrayList<>();
        // Add two sample messages
        chatMessages.add(new ChatMessage("Hi there!", "10:15 AM", ChatMessage.TYPE_RECIPIENT));
        chatMessages.add(new ChatMessage("Hello!", "10:16 AM", ChatMessage.TYPE_SENDER));
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_button) {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                ChatMessage chatMessage = new ChatMessage(message, "Now", ChatMessage.TYPE_SENDER);
                chatMessages.add(chatMessage);
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.scrollToPosition(chatMessages.size() - 1);
                messageInput.setText("");

                view.setEnabled(false);

                try (InputStream inputStream = getResources().openRawResource(R.raw.key)) {
                    GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream).createScoped("https://www.googleapis.com/auth/cloud-platform");

                    new Thread(() -> {
                        try {
                            credentials.refreshAccessToken();

                        } catch (IOException e) {
                            e.printStackTrace();
                            requireActivity().runOnUiThread(() -> view.setEnabled(true));
                        }
                    }).start();


                    VertexAI vertexAi = new VertexAI("booming-landing-410605", "us-central1", credentials);

                    GenerationConfig generationConfig =
                            GenerationConfig.newBuilder()
                                    .setMaxOutputTokens(2048)
                                    .setTemperature(0.9F)
                                    .setTopP(1)
                                    .build();
                    GenerativeModel model = new GenerativeModel("gemini-pro", generationConfig, vertexAi);

                    List<Content> contents = new ArrayList<>();
                    contents.add(Content.newBuilder().setRole("user").addParts(Part.newBuilder().setText(message)).build());
                    //contents.add(Content.newBuilder().setRole("model").addParts(Part.newBuilder().setText("Greetings! How may I assist you today?")).build());

                    ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(contents);

                    // Do something with the response
                    responseStream.stream().forEach(reply -> {
                        StringBuilder response = new StringBuilder();
                        for (int i = 0; i < reply.getCandidates(0).getContent().getPartsCount(); i++) {
                            response.append(reply.getCandidates(0).getContent().getParts(i).getText());
                        }
                        ChatMessage replyMessage = new ChatMessage(response.toString(), "Now", ChatMessage.TYPE_RECIPIENT);
                        chatMessages.add(replyMessage);
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                        messageInput.setText("");
                    });

                    vertexAi.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    view.setEnabled(true);
                }
            }
        }
    }
}
