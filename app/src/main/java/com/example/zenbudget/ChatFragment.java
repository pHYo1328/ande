package com.example.zenbudget;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatFragment extends Fragment implements View.OnClickListener, BaseActivity.KeyboardVisibilityListener, ChatAdapter.SelectionModeListener {

    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private EditText messageInput;
    private RecyclerView recyclerView;
    private BottomNavigationView navigationPlaceholder;
    private ChatViewModel chatViewModel;
    private String copyContent;

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.chat_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatAdapter = new ChatAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(chatAdapter);

        messageInput = view.findViewById(R.id.message_input);
        navigationPlaceholder = requireActivity().findViewById(R.id.bottomNavigationView);

        View sendButton = view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);

        View copyButton = view.findViewById(R.id.copy_button);
        copyButton.setOnClickListener(this);

        View closeButton = view.findViewById(R.id.exit_selection_mode);
        closeButton.setOnClickListener(this);

        View suggestionButton = view.findViewById(R.id.suggestion_button);
        suggestionButton.setOnClickListener((View v)->{
            messageInput.setText(R.string.button_suggestion);
            sendMessage();
            suggestionButton.setVisibility(View.GONE);
        });

        // Initialize ViewModel yes
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        // Observe chat messages LiveData i think
        chatViewModel.getChatMessages().observe(getViewLifecycleOwner(), chatMessages -> {
            chatAdapter.setChatMessages(chatMessages); // Update adapter's data otherwise it die
            if (chatMessages.size() > 0) {
                suggestionButton.setVisibility(View.GONE);
            }
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatMessages.size() - 1);
        });

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe LiveData
        chatViewModel.getChatMessages().observe(getViewLifecycleOwner(), newMessages -> {
            chatMessages = newMessages;
            chatAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(chatMessages.size() - 1);
        });
    }

    @Override
    public void onSelectionModeChanged(int selectedItemCount, List<ChatMessage> selectedMessages) {
        ImageButton exitButton = requireActivity().findViewById(R.id.exit_selection_mode);
        TextView selectionCount = requireActivity().findViewById(R.id.selection_count);
        ImageButton copyButton = requireActivity().findViewById(R.id.copy_button);
        TextView toolbarTitle = requireActivity().findViewById(R.id.toolbar_title);
        copyContent = "";
        if (selectedMessages != null) {
            StringBuilder sb = new StringBuilder();
            for (ChatMessage message : selectedMessages) {
                sb.append(message.getMessage()).append("\n");
            }
            copyContent = sb.toString();
        }
        if (selectedItemCount > 0) {
            // In selection mode
            exitButton.setVisibility(View.VISIBLE);
            selectionCount.setVisibility(View.VISIBLE);
            copyButton.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.GONE);
            String selectionCountText = selectedItemCount + " Selected";
            selectionCount.setText(selectionCountText);
        } else {
            // Not in selection mode
            copyContent = "";
            exitButton.setVisibility(View.GONE);
            selectionCount.setVisibility(View.GONE);
            copyButton.setVisibility(View.GONE);
            toolbarTitle.setVisibility(View.VISIBLE);
        }
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

    private static void addExample(JsonArray examplesArray, String inputContent, String outputContent) {
        JsonObject example = new JsonObject();
        JsonObject input = new JsonObject();
        JsonObject output = new JsonObject();

        input.addProperty("author", "user");
        input.addProperty("content", inputContent);
        output.addProperty("author", "bot");
        output.addProperty("content", outputContent);

        example.add("input", input);
        example.add("output", output);
        examplesArray.add(example);
    }

    private static JsonObject createRequestJson(List<ChatMessage> chatMessages) {
        JsonObject requestJson = new JsonObject();
        JsonArray instancesArray = new JsonArray();
        JsonObject instance = new JsonObject();

        // Adding context
        instance.addProperty("context", "You are a financial advisor chat assistant within the ZenBudget application based in Singapore.\n" +
                "Your primary objective is to provide real-time financial advice based on the user\\'s current budget status and financial goals. You have access to the user\\'s historical transaction data, categorized spending, income streams, and savings balances. You should prioritize personalized, actionable, and timely recommendations.\n" +
                "\n" +
                "A conversation history will be provided'\n" +
                "\n" +
                "You are to refrain from generating other content not related to a financial advisor and kindly reject the request\n" +
                "Important: Please format your responses as short paragraphs and break them down into multiple messages, similar to a WhatsApp chat.\n" +
                "Please also verify that the JSON array is in the correct format\n" +
                "Output Format: JSON array with each message as a separate object with only the message key with no markdown or HTML formatting");

        // Adding examples in the api
        JsonArray examplesArray = new JsonArray();
        addExample(examplesArray, "Hello", "[{\"message\": \"Hi there! I'm happy to lend a hand. Tell me about your financial goals for 2023.\"}]");
        addExample(examplesArray, "I want to buy a house", "[{\"message\": \"Great! Let's start by looking at your current budget.\"}]");
        instance.add("examples", examplesArray);

        List<ChatMessage> combinedMessages = new ArrayList<>();
        StringBuilder recipientMessages = new StringBuilder();

        // Combine recipient messages that are from the same sender
        for (ChatMessage chatMsg : chatMessages) {
            if (chatMsg.getIsIgnored()) {
                continue;
            }
            if (chatMsg.getType() == ChatMessage.TYPE_RECIPIENT) {
                if (recipientMessages.length() > 0) {
                    recipientMessages.append("\n");
                }
                recipientMessages.append(chatMsg.getMessage());
            } else {
                if (recipientMessages.length() > 0) {
                    combinedMessages.add(new ChatMessage(recipientMessages.toString(), "Now", ChatMessage.TYPE_RECIPIENT));
                    recipientMessages = new StringBuilder(); // reset this shiet
                }
                combinedMessages.add(chatMsg);
            }
        }

        // Add any remaining recipient messages
        if (recipientMessages.length() > 0) {
            combinedMessages.add(new ChatMessage(recipientMessages.toString(), "Now", ChatMessage.TYPE_RECIPIENT));
        }

        // Select the last 9 messages mus t be odd number otherwise message history ends
        //with a user message and api doesn't like 2 consecutive user messages
        int start = Math.max(0, combinedMessages.size() - 9);
        List<ChatMessage> lastEightMessages = combinedMessages.subList(start, combinedMessages.size());

        // Add to the real messagesArray final
        JsonArray messagesArray = new JsonArray();
        for (ChatMessage chatMsg : lastEightMessages) {
            JsonObject messageObj = new JsonObject();
            messageObj.addProperty("author", chatMsg.getType() == ChatMessage.TYPE_SENDER ? "user" : "bot");
            messageObj.addProperty("content", chatMsg.getMessage());
            messagesArray.add(messageObj);
        }
        instance.add("messages", messagesArray);

        instancesArray.add(instance);
        requestJson.add("instances", instancesArray);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("maxOutputTokens", 1024);
        parameters.addProperty("temperature", 0.9);
        parameters.addProperty("topP", 1);
        requestJson.add("parameters", parameters);

        return requestJson;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.send_button) {
            sendMessage();
        } else if (view.getId() == R.id.copy_button) {
            ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", copyContent);
            assert clipboard != null;
            clipboard.setPrimaryClip(clip);
            chatAdapter.exitSelectionMode();
            copyContent = "";
            Toast.makeText(requireActivity(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.exit_selection_mode) {
            // Exit selection mode
            copyContent = "";
            chatAdapter.exitSelectionMode();
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString();
        if (!message.isEmpty()) {
            View sendButton = requireActivity().findViewById(R.id.send_button);
            CircularProgressIndicator loadingSpinner = requireActivity().findViewById(R.id.loading_spinner);
            sendButton.setVisibility(View.GONE);
            loadingSpinner.setVisibility(View.VISIBLE);

            //remove trailing white spaces so no send extra space
            message = message.trim();
            ChatMessage chatMessage = new ChatMessage(message, "Now", ChatMessage.TYPE_SENDER);
            chatViewModel.addChatMessage(chatMessage);
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            recyclerView.scrollToPosition(chatMessages.size() - 1);
            messageInput.setText("");


            new Thread(() -> {
                try {
                    InputStream inputStream = getResources().openRawResource(R.raw.key);
                    GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                            .createScoped("https://www.googleapis.com/auth/cloud-platform");
                    credentials.refreshIfExpired();
                    credentials.refreshAccessToken();

                    OkHttpClient client = new OkHttpClient.Builder()
                            .connectTimeout(20, TimeUnit.SECONDS)
                            .readTimeout(20, TimeUnit.SECONDS)
                            .writeTimeout(20, TimeUnit.SECONDS)
                            .build();


                    JsonObject requestBody = createRequestJson(chatMessages);
                    String url = "https://asia-southeast1-aiplatform.googleapis.com/v1/projects/booming-landing-410605/locations/asia-southeast1/publishers/google/models/chat-bison-32k:predict";
                    RequestBody body = RequestBody.create(requestBody.toString(), MediaType.parse("application/json"));

                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .addHeader("Authorization", "Bearer " + credentials.getAccessToken().getTokenValue())
                            .addHeader("Content-Type", "application/json")
                            .build();

                    try (Response response = client.newCall(request).execute()) {
                        if (response.isSuccessful() && response.body() != null) {
                            String responseBody = response.body().string();
                            JsonArray responseArray = JsonParser.parseString(responseBody).getAsJsonObject().get("predictions").getAsJsonArray();

                            for (JsonElement responseElement : responseArray) {
                                JsonObject responseObject = responseElement.getAsJsonObject();

                                // Process candidate in the response obj
                                JsonArray candidates = responseObject.getAsJsonArray("candidates");
                                for (JsonElement candidateElement : candidates) {
                                    JsonObject candidate = candidateElement.getAsJsonObject();
                                    String candidateContent = candidate.get("content").getAsString();

                                    // Parse candidate content and update the chat messages
                                    try {
                                        JsonArray arr = JsonParser.parseString(candidateContent).getAsJsonArray();
                                        for (JsonElement objElement : arr) {
                                            JsonObject obj = objElement.getAsJsonObject();
                                            ChatMessage newMessage = new ChatMessage(obj.get("message").getAsString(), "Now", ChatMessage.TYPE_RECIPIENT);

                                            // Ensure UI updates are run on the main thread
                                            requireActivity().runOnUiThread(() -> {
                                                chatViewModel.addChatMessage(newMessage);
                                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                                recyclerView.scrollToPosition(chatMessages.size() - 1);
                                            });
                                        }
                                    } catch (Exception e) {
                                        ChatMessage newMessage = new ChatMessage(candidateContent, "Now", ChatMessage.TYPE_RECIPIENT);
                                        // Ensure UI update on ui thread otherwise cmi
                                        requireActivity().runOnUiThread(() -> {
                                            chatViewModel.addChatMessage(newMessage);
                                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                                        });
                                    }
                                }
                            }

                        } else {
                            // Handle error response
                            System.out.println("Response code: " + response.code());
                            System.out.println("Response message: " + response.message());
                            chatMessage.setIsIgnored(true);
                            requireActivity().runOnUiThread(() -> {
                                ChatMessage errMessage = new ChatMessage("An error occurred. Please try again later.", "Now", ChatMessage.TYPE_RECIPIENT);
                                errMessage.setIsIgnored(true);
                                chatViewModel.addChatMessage(errMessage);
                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                                recyclerView.scrollToPosition(chatMessages.size() - 1);
                            });
                            throw new IOException("Unexpected code " + response);

                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                requireActivity().runOnUiThread(() -> {
                    sendButton.setVisibility(View.VISIBLE);
                    loadingSpinner.setProgressCompat(100, true);
                    loadingSpinner.setVisibility(View.GONE);

                });

            }).start();


        }
    }
}
