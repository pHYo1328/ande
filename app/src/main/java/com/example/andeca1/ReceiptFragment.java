package com.example.andeca1;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReceiptFragment extends Fragment implements BaseActivity.KeyboardVisibilityListener {

    private List<ReceiptItem> receiptItems;
    private ReceiptItemAdapter adapter;

    private RecyclerView recyclerView;

    private BottomNavigationView navigationPlaceholder;

    public static ReceiptFragment newInstance(String imageUri) {
        ReceiptFragment fragment = new ReceiptFragment();
        Bundle args = new Bundle();
        args.putString("image_uri", imageUri);
        fragment.setArguments(args);
        return fragment;
    }

    public static ReceiptFragment newInstance(Bitmap imageBitmap) {
        ReceiptFragment fragment = new ReceiptFragment();
        Bundle args = new Bundle();
        args.putParcelable("image_bitmap", imageBitmap);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receipt, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView imageViewReceipt = view.findViewById(R.id.imageViewReceipt);

        navigationPlaceholder = requireActivity().findViewById(R.id.bottomNavigationView);

        //Trying another way without public void onClick
        View allCheckbox = view.findViewById(R.id.checkBoxSelectAll);
        allCheckbox.setOnClickListener(v -> selectAll());

        View closeReceipt = view.findViewById(R.id.exit_selection_mode);
        closeReceipt.setOnClickListener(v -> closeReceipt());

        View addToExpenses = view.findViewById(R.id.add_to_expenses);
        addToExpenses.setEnabled(false);
        addToExpenses.setVisibility(View.GONE);
        addToExpenses.setOnClickListener(v -> addToExpenses());

        receiptItems = new ArrayList<>();
        recyclerView = requireActivity().findViewById(R.id.recyclerViewItems);
        adapter = new ReceiptItemAdapter(receiptItems);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));


        // Check if we got a URI or Bitmap
        if (getArguments() != null && getArguments().containsKey("image_uri")) {
            Uri imageUri = Uri.parse(getArguments().getString("image_uri"));
            try {
                //Convert the URI to a Bitmap
                InputStream imageStream = requireContext().getContentResolver().openInputStream(imageUri);
                Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                imageViewReceipt.setImageBitmap(imageBitmap);
                sendRequestWithImage(imageBitmap);
            } catch (FileNotFoundException e) {
                // Handle the exception if the image file is not found
                e.printStackTrace();
            }
        } else if (getArguments() != null && getArguments().containsKey("image_bitmap")) {
            Bitmap imageBitmap = getArguments().getParcelable("image_bitmap");
            imageViewReceipt.setImageBitmap(imageBitmap);
            sendRequestWithImage(imageBitmap);
        }
    }

    public void addToExpenses() {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            ReceiptItemAdapter.ViewHolder holder = (ReceiptItemAdapter.ViewHolder)
                    recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                ReceiptItem item = holder.getCurrentData();
                if (item.isChecked()) {
                    System.out.println("Product: " + item.getProductName());
                    System.out.println("Amount: " + item.getAmount());
                    System.out.println("Quantity: " + item.getQuantity());
                    System.out.println("Total: " + item.getTotal());

                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void selectAll() {
        CheckBox checkBox = requireActivity().findViewById(R.id.checkBoxSelectAll);

        for (ReceiptItem item : receiptItems) {
            item.setChecked(checkBox.isChecked());
        }
        adapter.updateReceiptItems(receiptItems);

    }

    public void closeReceipt() {
        //Replace fragment with ExpenseFragment
        getParentFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new ExpenseFragment())
                .commit();
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

    public void sendRequestWithImage(Bitmap imageBitmap) {
        CircularProgressIndicator imageViewLoading = requireView().findViewById(R.id.imageViewLoading);
//        new Thread(() -> {
//            try {
//                InputStream inputStream = getResources().openRawResource(R.raw.key);
//                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
//                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
//                credentials.refreshIfExpired();
//                credentials.refreshAccessToken();
//
//                OkHttpClient client = new OkHttpClient.Builder()
//                        .connectTimeout(30, TimeUnit.SECONDS)
//                        .readTimeout(30, TimeUnit.SECONDS)
//                        .writeTimeout(30, TimeUnit.SECONDS)
//                        .build();
//
//                // Convert Bitmap to Base64 for inline data
//                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
//                byte[] byteArray = byteArrayOutputStream.toByteArray();
//                String encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);
//
//                // Construct JSON for the request body
//                JsonObject requestBodyJson = new JsonObject();
//
//                // Create the contents array with inlineData
//                JsonArray contentsArray = new JsonArray();
//                JsonObject content = new JsonObject();
//                content.addProperty("role", "USER"); // Assuming role is USER, adjust if necessary
//
//                JsonArray partsArray = new JsonArray();
//                JsonObject part = new JsonObject();
//
//                // Add inlineData
//                JsonObject inlineData = new JsonObject();
//                inlineData.addProperty("mimeType", "image/jpeg");
//                inlineData.addProperty("data", encodedImage);
//                part.add("inlineData", inlineData);
//
//
//                partsArray.add(part);
//                JsonObject contextPart = new JsonObject();
//                contextPart.addProperty("text", "Analyze the receipt in the image above and create a JSON response with the following information:\n" +
//                        "\n" +
//                        "- Product name\n" +
//                        "- Amount (price per item)\n" +
//                        "- Quantity\n" +
//                        "- Total (price for each item line)" +
//                        "\n" +
//                        "Analyze the receipt in the image above and create a JSON array response with the following information:\n" +
//                        "\n" +
//                        "- Product name\n" +
//                        "- Amount (price per item)\n" +
//                        "- Quantity\n" +
//                        "- Total (price for each item line)\n" +
//                        "\n" +
//                        "If there are no items in the receipt, return an empty array.\n" +
//                        "Example of the desired JSON output:\n" +
//                        "[\n" +
//                        "  {\n" +
//                        "    \"product_name\": \"Banana\",\n" +
//                        "    \"amount\": 0.99,\n" +
//                        "    \"quantity\": 2,\n" +
//                        "    \"total\": 1.98\n" +
//                        "  },\n" +
//                        "  {\n" +
//                        "    \"product_name\": \"Milk\",\n" +
//                        "    \"amount\": 3.49,\n" +
//                        "    \"quantity\": 1,\n" +
//                        "    \"total\": 3.49\n" +
//                        "  }\n" +
//                        "]");
//                partsArray.add(contextPart);
//                content.add("parts", partsArray);
//                contentsArray.add(content);
//                requestBodyJson.add("contents", contentsArray);
//
//                // Generation config
//                JsonObject generationConfig = new JsonObject();
//                generationConfig.addProperty("maxOutputTokens", 2048);
//                generationConfig.addProperty("temperature", 0.4);
//                generationConfig.addProperty("topP", 1);
//                generationConfig.addProperty("topK", 32);
//
//                requestBodyJson.add("generationConfig", generationConfig);
//
//                // Convert the JSON to a String for the request body
//                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//                RequestBody body = RequestBody.create(requestBodyJson.toString(), JSON);
//                Gson gson = new GsonBuilder().setPrettyPrinting().create();
//                JsonElement je = JsonParser.parseString(requestBodyJson.toString());
//                String prettyJsonString = gson.toJson(je);
//                System.out.println(prettyJsonString);
//                // Create the request
//                String url = "https://asia-southeast1-aiplatform.googleapis.com/v1/projects/booming-landing-410605/locations/asia-southeast1/publishers/google/models/gemini-pro-vision:streamGenerateContent";
//                Request request = new Request.Builder()
//                        .url(url)
//                        .addHeader("Authorization", "Bearer " + credentials.getAccessToken().getTokenValue())
//                        .post(body)
//                        .build();
//
//                // Execute the request
//                try (Response response = client.newCall(request).execute()) {
//                    if (response.isSuccessful() && response.body() != null) {
//                        String responseBody = response.body().string();
//                        // Handle the successful response here
//                        JsonElement jsonElement = JsonParser.parseString(responseBody);
//                        JsonArray arr = jsonElement.getAsJsonArray();
//                        StringBuilder sb = new StringBuilder();
//                        for (JsonElement element : arr) {
//                            JsonArray candidatesArray = element.getAsJsonObject().get("candidates").getAsJsonArray();
//
//                            StringBuilder combinedTextBuilder = new StringBuilder();
//                            for (JsonElement candidateElement : candidatesArray) {
//                                JsonObject candidateObject = candidateElement.getAsJsonObject();
//                                JsonObject contentObject = candidateObject.getAsJsonObject("content");
//                                JsonArray partArray = contentObject.getAsJsonArray("parts");
//                                for (JsonElement partElement : partArray) {
//                                    String text = partElement.getAsJsonObject().get("text").getAsString();
//                                    combinedTextBuilder.append(text);
//                                }
//                            }
//
//
//                            String combinedText = combinedTextBuilder.toString();
//                            sb.append(combinedText);
//                        }
//                        //parse bs as json returned from ai
//                        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
//                        reader.setLenient(true);
//
//                        JsonElement je2 = JsonParser.parseReader(reader);
//                        //if malformed, JsonParser throws an error
//                        //otherwise its ai hallucinating
//                        JsonArray jsonArray = je2.getAsJsonArray();
//
//                        requireActivity().runOnUiThread(() -> {
//
//
//                            for (JsonElement element : jsonArray) {
//                                JsonObject obj = element.getAsJsonObject();
//                                String productName = obj.get("product_name").getAsString();
//                                String amount = obj.get("amount").getAsString();
//                                String quantity = obj.get("quantity").getAsString();
//                                String total = obj.get("total").getAsString();
//
//                                receiptItems.add(new ReceiptItem(productName, amount, quantity, total));
//                                adapter.notifyItemInserted(receiptItems.size() - 1);
//                            }
//                            View addToExpenses = requireActivity().findViewById(R.id.add_to_expenses);
//                            addToExpenses.setVisibility(View.VISIBLE);
//                            addToExpenses.setEnabled(true);
//
//
//                        });
//                    } else {
//                        // Handle the error response here
//                        System.out.println("Request was not successful: " + response);
//                        assert response.body() != null;
//                        System.out.println(response.body().string());
//                        System.out.println(response.message());
//                        System.out.println(response.code());
//                        throw new Exception("Request was not successful: " + response);
//
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                requireActivity().runOnUiThread(() -> {
//                    TextView textViewError = requireActivity().findViewById(R.id.textViewError);
//                    textViewError.setVisibility(View.VISIBLE);
//                });
//
//            }
//            requireActivity().runOnUiThread(() -> imageViewLoading.setProgressCompat(100, true));
//
//        }
//        ).start();
        String sb = "[\n" +
                "    {\n" +
                "        \"product_name\": \"Product 1\",\n" +
                "        \"amount\": \"$10.5\",\n" +
                "        \"quantity\": \"1\",\n" +
                "        \"total\": \"$10.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 2\",\n" +
                "        \"amount\": \"$11.0\",\n" +
                "        \"quantity\": \"2\",\n" +
                "        \"total\": \"$22.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 3\",\n" +
                "        \"amount\": \"$11.5\",\n" +
                "        \"quantity\": \"3\",\n" +
                "        \"total\": \"$34.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 4\",\n" +
                "        \"amount\": \"$12.0\",\n" +
                "        \"quantity\": \"4\",\n" +
                "        \"total\": \"$48.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 5\",\n" +
                "        \"amount\": \"$12.5\",\n" +
                "        \"quantity\": \"5\",\n" +
                "        \"total\": \"$62.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 6\",\n" +
                "        \"amount\": \"$13.0\",\n" +
                "        \"quantity\": \"6\",\n" +
                "        \"total\": \"$78.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 7\",\n" +
                "        \"amount\": \"$13.5\",\n" +
                "        \"quantity\": \"7\",\n" +
                "        \"total\": \"$94.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 8\",\n" +
                "        \"amount\": \"$14.0\",\n" +
                "        \"quantity\": \"8\",\n" +
                "        \"total\": \"$112.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 9\",\n" +
                "        \"amount\": \"$14.5\",\n" +
                "        \"quantity\": \"9\",\n" +
                "        \"total\": \"$130.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 10\",\n" +
                "        \"amount\": \"$15.0\",\n" +
                "        \"quantity\": \"10\",\n" +
                "        \"total\": \"$150.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 11\",\n" +
                "        \"amount\": \"$15.5\",\n" +
                "        \"quantity\": \"11\",\n" +
                "        \"total\": \"$170.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 12\",\n" +
                "        \"amount\": \"$16.0\",\n" +
                "        \"quantity\": \"12\",\n" +
                "        \"total\": \"$192.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 13\",\n" +
                "        \"amount\": \"$16.5\",\n" +
                "        \"quantity\": \"13\",\n" +
                "        \"total\": \"$214.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 14\",\n" +
                "        \"amount\": \"$17.0\",\n" +
                "        \"quantity\": \"14\",\n" +
                "        \"total\": \"$238.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 15\",\n" +
                "        \"amount\": \"$17.5\",\n" +
                "        \"quantity\": \"15\",\n" +
                "        \"total\": \"$262.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 16\",\n" +
                "        \"amount\": \"$18.0\",\n" +
                "        \"quantity\": \"16\",\n" +
                "        \"total\": \"$288.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 17\",\n" +
                "        \"amount\": \"$18.5\",\n" +
                "        \"quantity\": \"17\",\n" +
                "        \"total\": \"$314.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 18\",\n" +
                "        \"amount\": \"$19.0\",\n" +
                "        \"quantity\": \"18\",\n" +
                "        \"total\": \"$342.0\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 19\",\n" +
                "        \"amount\": \"$19.5\",\n" +
                "        \"quantity\": \"19\",\n" +
                "        \"total\": \"$370.5\"\n" +
                "    },\n" +
                "    {\n" +
                "        \"product_name\": \"Product 20\",\n" +
                "        \"amount\": \"$20.0\",\n" +
                "        \"quantity\": \"20\",\n" +
                "        \"total\": \"$400.0\"\n" +
                "    }\n" +
                "]\n";
        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
        reader.setLenient(true);

        JsonElement je2 = JsonParser.parseReader(reader);
        //if malformed, JsonParser throws an error
        //otherwise its ai hallucinating
        JsonArray jsonArray = je2.getAsJsonArray();
        Integer i = 0;
        for (JsonElement element : jsonArray) {
            JsonObject obj = element.getAsJsonObject();
            String productName = obj.get("product_name").getAsString();
            String amount = obj.get("amount").getAsString();
            String quantity = obj.get("quantity").getAsString();
            String total = obj.get("total").getAsString();

            receiptItems.add(new ReceiptItem(i,productName, amount, quantity, total));
            i++;
        }
        adapter.updateReceiptItems(receiptItems);
        adapter.notifyDataSetChanged();
        View addToExpenses = requireActivity().findViewById(R.id.add_to_expenses);
        addToExpenses.setVisibility(View.VISIBLE);
        addToExpenses.setEnabled(true);
    }


}
