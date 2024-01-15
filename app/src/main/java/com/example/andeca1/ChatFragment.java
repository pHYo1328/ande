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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.IOException;
import java.io.InputStream;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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

    private void addContent(JsonArray contentsArray, String role, String text) {
        JsonObject content = new JsonObject();
        content.addProperty("role", role);
        JsonArray partsArray = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", text);
        partsArray.add(part);
        content.add("parts", partsArray);
        contentsArray.add(content);
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


                new Thread(() -> {
                    try {
                        // Load credentials
                        InputStream inputStream = getResources().openRawResource(R.raw.key); // Replace 'R.raw.key' with your actual resource
                        GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                                .createScoped("https://www.googleapis.com/auth/cloud-platform");
                        credentials.refreshIfExpired();
                        credentials.refreshAccessToken();

                        OkHttpClient client = new OkHttpClient();

                        // Build the endpoint URL
                        String url = "https://us-central1-aiplatform.googleapis.com/v1/projects/booming-landing-410605/locations/us-central1/publishers/google/models/gemini-pro:streamGenerateContent";

                        // Create the request body
                        JsonObject requestBody = new JsonObject();
                        JsonArray contentsArray = new JsonArray();

                        addContent(contentsArray, "user", message);


                        requestBody.add("contents", contentsArray);

                        // Generation config
                        JsonObject generationConfig = new JsonObject();
                        generationConfig.addProperty("maxOutputTokens", 2048);
                        generationConfig.addProperty("temperature", 0.9);
                        generationConfig.addProperty("topP", 1);
                        requestBody.add("generation_config", generationConfig);

                        RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));

                        // Create the request
                        Request request = new Request.Builder()
                                .url(url)
                                .post(body)
                                .addHeader("Authorization", "Bearer " + credentials.getAccessToken().getTokenValue())
                                .addHeader("Content-Type", "application/json")
                                .build();

                        // Execute the request and handle the response
                        try (Response response = client.newCall(request).execute()) {
                            if (response.isSuccessful()) {
                                assert response.body() != null;
                                String responseBody = response.body().string();

                                JsonArray responseArray = JsonParser.parseString(responseBody).getAsJsonArray();
                                JsonObject firstObject = responseArray.get(0).getAsJsonObject();
                                JsonArray candidates = firstObject.getAsJsonArray("candidates");
                                JsonObject candidate = candidates.get(0).getAsJsonObject();
                                JsonArray responseParts = candidate.getAsJsonObject("content").getAsJsonArray("parts");
                                StringBuilder responseText = new StringBuilder();
                                for (JsonElement elementPart : responseParts) {
                                    responseText.append(elementPart.getAsJsonObject().get("text").getAsString());
                                }

                                // Process the response
                                requireActivity().runOnUiThread(() -> {
                                    ChatMessage replyMessage = new ChatMessage(responseText.toString(), "Now", ChatMessage.TYPE_RECIPIENT);
                                    chatMessages.add(replyMessage);
                                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                                    messageInput.setText("");
                                });
                            } else {
                                // Handle error response
                                throw new IOException("Unexpected code " + response);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle exceptions (update UI, show error message, etc.)
                    }
                }).start();


            }
        }
    }
}
