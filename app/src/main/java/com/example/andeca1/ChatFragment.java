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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatFragment extends Fragment implements View.OnClickListener, BaseActivity.KeyboardVisibilityListener {

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private RecyclerView recyclerView;
    private BottomNavigationView navigationPlaceholder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        initializeChatMessages();

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatAdapter = new ChatAdapter(chatMessages);
        recyclerView.setAdapter(chatAdapter);

        messageInput = view.findViewById(R.id.message_input);
        navigationPlaceholder = requireActivity().findViewById(R.id.bottomNavigationView);

        View sendButton = view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(this); // Set the click listener

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity) requireActivity()).setKeyboardVisibilityListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BaseActivity) requireActivity()).setKeyboardVisibilityListener(null);
    }

    @Override
    public void onKeyboardVisibilityChanged(boolean keyboardVisible) {
        navigationPlaceholder.setVisibility(keyboardVisible ? View.GONE : View.VISIBLE);
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

    private String truncateToWordLimit(String text, int wordLimit) {
        String[] words = text.split("\\s+");
        if (words.length <= wordLimit) return text;

        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < wordLimit; i++) {
            truncated.append(words[i]).append(" ");
        }
        truncated.append("...");
        return truncated.toString();
    }

    private void initializeChatMessages() {
        chatMessages = new ArrayList<>();
        /*
 Add two sample messages
        chatMessages.add(new ChatMessage("Hi there!", "10:15 AM", ChatMessage.TYPE_RECIPIENT));
        chatMessages.add(new ChatMessage("Hello!", "10:16 AM", ChatMessage.TYPE_SENDER));
*/
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_button) {
            sendMessage();
        }
    }

    private void sendMessage() {
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
                    String url = "https://asia-southeast1-aiplatform.googleapis.com/v1/projects/booming-landing-410605/locations/asia-southeast1/publishers/google/models/gemini-pro:streamGenerateContent";

                    // Create the request body
                    JsonObject requestBody = new JsonObject();
                    JsonArray contentsArray = new JsonArray();
                    //I hate prompt engineering
                    addContent(contentsArray, "user", "Context:\n" +
                            "\n" +
                            "You are a financial advisor chat assistant within the ZenBudget application where the user is in Singapore.\n" +
                            "Your primary objective is to provide real-time financial advice based on the user's current budget status and financial goals. You have access to the user's historical transaction data, categorized spending, income streams, and savings balances. You should prioritize personalized, actionable, and timely recommendations.\n" +
                            "\n" +
                            "A conversation history will be provided below for you where you are the 'model' while the user is the 'user' \n" +
                            "\n" +
                            "You are to refrain from generating other content not related to a financial advisor and kindly reject the request\n" +
                            "Important: Please format your responses as short paragraphs and break them down into multiple messages, similar to a WhatsApp chat. \n" +
                            "Output Format: JSON array with each message as a separate object with only the message key and no markdown syntax\n" +
                            "\n" +
                            "User data starts below:\n" +
                            "\n" +
                            "Conversation history starts below:\n");
                    addContent(contentsArray, "model", "[\n" +
                            "  {\n" +
                            "    \"message\": \"Hi there! I'm happy to lend a hand. Tell me about your financial goals for 2023.\"\n" +
                            "  }\n" +
                            "]");

                    // Hack, the consequence of my actions...
                    List<ChatMessage> combinedMessages = new ArrayList<>();


                    int i = 0;
                    while (i < chatMessages.size()) {
                        ChatMessage chatMsg = chatMessages.get(i);

                        if (chatMsg.getType() == ChatMessage.TYPE_SENDER) {
                            combinedMessages.add(chatMsg);
                        } else {
                            StringBuilder modelMessages = new StringBuilder(chatMsg.getMessage());

                            // Combine subsequent model messages
                            while (i + 1 < chatMessages.size() && chatMessages.get(i + 1).getType() == ChatMessage.TYPE_RECIPIENT) {
                                i++;
                                modelMessages.append("\n").append(chatMessages.get(i).getMessage());
                            }

                            // Truncate the message to 25 words if necessary
                            String truncatedMessage = truncateToWordLimit(modelMessages.toString(), 25);
                            combinedMessages.add(new ChatMessage(truncatedMessage, "now", ChatMessage.TYPE_RECIPIENT));
                        }
                        i++;
                    }


//                    int start = Math.max(0, combinedMessages.size() - 6);
                    for (i = 0; i < combinedMessages.size(); i++) {
                        ChatMessage combinedMsg = combinedMessages.get(i);

                        if (combinedMsg.getType() == ChatMessage.TYPE_SENDER) {
                            addContent(contentsArray, "user", combinedMsg.getMessage() + "\nOutput Format: JSON array with each message as a separate object with only the message key and no markdown syntax\n");
                        } else {
                            addContent(contentsArray, "model", combinedMsg.getMessage());
                        }
                    }

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


                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful()) {
                            assert response.body() != null;
                            String responseBody = response.body().string();

                            JsonArray responseArray = JsonParser.parseString(responseBody).getAsJsonArray();
                            StringBuilder responseText = new StringBuilder();

                            for (JsonElement objElement : responseArray) {
                                JsonObject obj = objElement.getAsJsonObject();
                                JsonArray candidates = obj.getAsJsonArray("candidates"); // Corrected this line

                                for (JsonElement candidateElement : candidates) {
                                    JsonObject candidate = candidateElement.getAsJsonObject();
                                    JsonArray responseParts = candidate.getAsJsonObject("content").getAsJsonArray("parts");

                                    for (JsonElement elementPart : responseParts) {
                                        responseText.append(elementPart.getAsJsonObject().get("text").getAsString());
                                    }
                                }
                            }

                            requireActivity().runOnUiThread(() -> {
                                //Parse responseText as json array
                                ChatMessage replyMessage;
                                try {
                                    JsonArray arr = JsonParser.parseString(responseText.toString()).getAsJsonArray();
                                    for (JsonElement objElement : arr) {
                                        JsonObject obj = objElement.getAsJsonObject();
                                        replyMessage = new ChatMessage(obj.get("message").getAsString(), "Now", ChatMessage.TYPE_RECIPIENT);
                                        chatMessages.add(replyMessage);
                                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                        recyclerView.scrollToPosition(chatMessages.size() - 1);
                                        messageInput.setText("");
                                    }
                                } catch (Exception e) {
                                    replyMessage = new ChatMessage(responseText.toString(), "Now", ChatMessage.TYPE_RECIPIENT);
                                    chatMessages.add(replyMessage);
                                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                    recyclerView.scrollToPosition(chatMessages.size() - 1);
                                    messageInput.setText("");
                                }
                            });

                        } else {
                            // Handle error response
                            System.out.println(response.body().string());
                            System.out.println(response.code());
                            System.out.println(response.message());
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
