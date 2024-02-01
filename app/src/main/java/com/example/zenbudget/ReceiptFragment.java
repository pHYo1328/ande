package com.example.zenbudget;

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
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
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

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReceiptFragment extends Fragment implements BaseActivity.KeyboardVisibilityListener {
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
        //Loop through scrollview
        ScrollView scrollView = requireActivity().findViewById(R.id.scrollViewItems);
        LinearLayout linearLayout = (LinearLayout) scrollView.getChildAt(0);
        double totalAmount = 0;
        StringBuilder finalDescription = new StringBuilder();

        for (int i = 0; i < linearLayout.getChildCount(); i++) {

            View view = linearLayout.getChildAt(i);
            CheckBox checkBoxItem = view.findViewById(R.id.checkBox);
            if (!checkBoxItem.isChecked()) {
                continue;
            }
            TextInputEditText editTextProduct = view.findViewById(R.id.editTextProduct);
            TextInputEditText editTextAmount = view.findViewById(R.id.editTextAmount);
            TextInputEditText editTextQuantity = view.findViewById(R.id.editTextQuantity);
            TextInputEditText editTextTotal = view.findViewById(R.id.editTextTotal);

            String productName = Objects.requireNonNull(editTextProduct.getText()).toString();
            String amount = Objects.requireNonNull(editTextAmount.getText()).toString();
            String quantity = Objects.requireNonNull(editTextQuantity.getText()).toString();
            String total = Objects.requireNonNull(editTextTotal.getText()).toString();
            //add to totalAmount
            //if quantity or total is not a double, skip
            if (quantity.matches("\\d+(\\.\\d+)?") && total.matches("\\d+(\\.\\d+)?")) {
                totalAmount += Double.parseDouble(amount) * Double.parseDouble(quantity);
            }
            //add to finalDescription
            finalDescription.append(productName).append("   |  $").append(amount).append(" x ").append(quantity).append(" = $").append(total).append("\n");
        }
        ExpenseFragment expenseFragment = ExpenseFragment.newInstance(String.valueOf(totalAmount), finalDescription.toString());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.content_frame, expenseFragment)
                .commit();

    }

    public void selectAll() {
        CheckBox checkBox = requireActivity().findViewById(R.id.checkBoxSelectAll);
        //Loop through scrollview
        ScrollView scrollView = requireActivity().findViewById(R.id.scrollViewItems);
        LinearLayout linearLayout = (LinearLayout) scrollView.getChildAt(0);
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View view = linearLayout.getChildAt(i);
            CheckBox checkBoxItem = view.findViewById(R.id.checkBox);
            checkBoxItem.setChecked(checkBox.isChecked());
        }
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
        new Thread(() -> {
            try {
                InputStream inputStream = getResources().openRawResource(R.raw.key);
                GoogleCredentials credentials = GoogleCredentials.fromStream(inputStream)
                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
                credentials.refreshIfExpired();
                credentials.refreshAccessToken();

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build();

                // Convert Bitmap to Base64 for inline data
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String encodedImage = Base64.encodeToString(byteArray, Base64.NO_WRAP);

                // Construct JSON for the request body
                JsonObject requestBodyJson = new JsonObject();

                // Create the contents array with inlineData
                JsonArray contentsArray = new JsonArray();
                JsonObject content = new JsonObject();
                content.addProperty("role", "USER"); // Assuming role is USER, adjust if necessary

                JsonArray partsArray = new JsonArray();
                JsonObject part = new JsonObject();

                // Add inlineData
                JsonObject inlineData = new JsonObject();
                inlineData.addProperty("mimeType", "image/jpeg");
                inlineData.addProperty("data", encodedImage);
                part.add("inlineData", inlineData);


                partsArray.add(part);
                JsonObject contextPart = new JsonObject();
                contextPart.addProperty("text", "Analyze the receipt in the image above and create a JSON response with the following information:\n" +
                        "\n" +
                        "- Product name\n" +
                        "- Amount (price per item)\n" +
                        "- Quantity\n" +
                        "- Total (price for each item line)" +
                        "\n" +
                        "Analyze the receipt in the image above and create a JSON array response with the following information:\n" +
                        "\n" +
                        "- Product name\n" +
                        "- Amount (price per item)\n" +
                        "- Quantity\n" +
                        "- Total (price for each item line)\n" +
                        "\n" +
                        "If there are no items in the receipt, return an empty array.\n" +
                        "Example of the desired JSON output:\n" +
                        "[\n" +
                        "  {\n" +
                        "    \"product_name\": \"Banana\",\n" +
                        "    \"amount\": 0.99,\n" +
                        "    \"quantity\": 2,\n" +
                        "    \"total\": 1.98\n" +
                        "  },\n" +
                        "  {\n" +
                        "    \"product_name\": \"Milk\",\n" +
                        "    \"amount\": 3.49,\n" +
                        "    \"quantity\": 1,\n" +
                        "    \"total\": 3.49\n" +
                        "  }\n" +
                        "]");
                partsArray.add(contextPart);
                content.add("parts", partsArray);
                contentsArray.add(content);
                requestBodyJson.add("contents", contentsArray);

                // Generation config
                JsonObject generationConfig = new JsonObject();
                generationConfig.addProperty("maxOutputTokens", 2048);
                generationConfig.addProperty("temperature", 0.4);
                generationConfig.addProperty("topP", 1);
                generationConfig.addProperty("topK", 32);

                requestBodyJson.add("generationConfig", generationConfig);

                // Convert the JSON to a String for the request body
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(requestBodyJson.toString(), JSON);
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonElement je = JsonParser.parseString(requestBodyJson.toString());
                String prettyJsonString = gson.toJson(je);
                System.out.println(prettyJsonString);
                // Create the request
                String url = "https://asia-southeast1-aiplatform.googleapis.com/v1/projects/booming-landing-410605/locations/asia-southeast1/publishers/google/models/gemini-pro-vision:streamGenerateContent";
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer " + credentials.getAccessToken().getTokenValue())
                        .post(body)
                        .build();

                // Execute the request
                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseBody = response.body().string();
                        // Handle the successful response here
                        JsonElement jsonElement = JsonParser.parseString(responseBody);
                        JsonArray arr = jsonElement.getAsJsonArray();
                        StringBuilder sb = new StringBuilder();
                        for (JsonElement element : arr) {
                            JsonArray candidatesArray = element.getAsJsonObject().get("candidates").getAsJsonArray();

                            StringBuilder combinedTextBuilder = new StringBuilder();
                            for (JsonElement candidateElement : candidatesArray) {
                                JsonObject candidateObject = candidateElement.getAsJsonObject();
                                JsonObject contentObject = candidateObject.getAsJsonObject("content");
                                JsonArray partArray = contentObject.getAsJsonArray("parts");
                                for (JsonElement partElement : partArray) {
                                    String text = partElement.getAsJsonObject().get("text").getAsString();
                                    combinedTextBuilder.append(text);
                                }
                            }


                            String combinedText = combinedTextBuilder.toString();
                            sb.append(combinedText);
                        }
                        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
                        reader.setLenient(true);

                        JsonElement je2 = JsonParser.parseReader(reader);

                        JsonArray jsonArray = je2.getAsJsonArray();
                        requireActivity().runOnUiThread(() -> {
                            ScrollView scrollView = requireActivity().findViewById(R.id.scrollViewItems);
                            LinearLayout linearLayout = new LinearLayout(requireActivity());
                            linearLayout.setOrientation(LinearLayout.VERTICAL);

                            for (JsonElement element : jsonArray) {
                                // Parse the JSON and create ReceiptItem objects
                                JsonObject obj = element.getAsJsonObject();
                                String productName = obj.get("product_name").getAsString();
                                String amount = obj.get("amount").getAsString();
                                String quantity = obj.get("quantity").getAsString();
                                String total = obj.get("total").getAsString();

                                // Inflate the receipt_item_row layout and set its data
                                View itemView = LayoutInflater.from(requireActivity()).inflate(R.layout.receipt_item_row, linearLayout, false);

                                TextInputEditText editTextProduct = itemView.findViewById(R.id.editTextProduct);
                                TextInputEditText editTextAmount = itemView.findViewById(R.id.editTextAmount);
                                TextInputEditText editTextQuantity = itemView.findViewById(R.id.editTextQuantity);
                                TextInputEditText editTextTotal = itemView.findViewById(R.id.editTextTotal);

                                // Set the data to the views
                                editTextProduct.setText(productName);
                                editTextAmount.setText(amount);
                                editTextQuantity.setText(quantity);
                                editTextTotal.setText(total);

                                // Add the view to the linear layout
                                linearLayout.addView(itemView);
                            }
                            scrollView.addView(linearLayout);

                            View addToExpenses = requireActivity().findViewById(R.id.add_to_expenses);
                            addToExpenses.setVisibility(View.VISIBLE);
                            addToExpenses.setEnabled(true);
                            imageViewLoading.setProgressCompat(100, true);
                            imageViewLoading.setVisibility(View.GONE);
                        });


                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageViewLoading.setVisibility(View.GONE);
                View TextViewError = requireActivity().findViewById(R.id.textViewError);
                TextViewError.setVisibility(View.VISIBLE);
            }


        }
        ).start();
    }
}


