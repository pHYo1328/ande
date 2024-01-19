package com.example.andeca1;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
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
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ReceiptFragment extends Fragment {

    // Factory method to create a new instance of the fragment using the provided parameters
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


        // Check if we got a URI or Bitmap
        if (getArguments() != null && getArguments().containsKey("image_uri")) {
            Uri imageUri = Uri.parse(getArguments().getString("image_uri"));
            try {
                // Open an input stream to the image URI using ContentResolver
                InputStream imageStream = requireContext().getContentResolver().openInputStream(imageUri);
                // Decode the input stream to a Bitmap
                Bitmap imageBitmap = BitmapFactory.decodeStream(imageStream);
                // Set the Bitmap to ImageView
                imageViewReceipt.setImageBitmap(imageBitmap);
                // Send the Bitmap with your request
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

    public void sendRequestWithImage(Bitmap imageBitmap) {
        ImageView imageViewLoading = requireView().findViewById(R.id.imageViewLoading);
        new Thread(() -> {
            try {
                requireActivity().runOnUiThread(() -> {

                    Glide.with(this).asGif().load(R.drawable.reciept_loading).transition(DrawableTransitionOptions.withCrossFade()).into(imageViewLoading);
                    imageViewLoading.setVisibility(View.VISIBLE);
                });


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
                        //parse bs as json returned from ai
                        JsonReader reader = new JsonReader(new StringReader(sb.toString()));
                        reader.setLenient(true);

                        JsonElement je2 = JsonParser.parseReader(reader);
                        //if malformed, JsonParser throws an error
                        //otherwise its ai hallucinating
                        JsonArray jsonArray = je2.getAsJsonArray();

                        TableLayout tableLayoutItems = requireView().findViewById(R.id.tableLayoutItems);

                        requireActivity().runOnUiThread(() -> {

                            for (JsonElement element : jsonArray) {
                                // Extract the properties of the current product
                                JsonObject obj = element.getAsJsonObject();
                                String productName = obj.get("product_name").getAsString();
                                String amount = obj.get("amount").getAsString();
                                String quantity = obj.get("quantity").getAsString();
                                String total = obj.get("total").getAsString();

                                // Create a new row to be added
                                TableRow tableRow = new TableRow(getContext());

                                //Helper fn for consistent padding also no copy pasting like an idiot
                                TextView textViewProduct = createTextViewWithWrap(productName);
                                TextView textViewAmount = createTextViewWithWrap(amount);
                                TextView textViewQuantity = createTextViewWithWrap(quantity);
                                TextView textViewTotal = createTextViewWithWrap(total);


                                //Checkbox styling...
                                //css debugging but slow
                                CheckBox checkBoxAction = new CheckBox(getContext());
                                TableRow.LayoutParams checkBoxParams = new TableRow.LayoutParams(2);
                                checkBoxAction.setLayoutParams(checkBoxParams);
                                // Add the TextViews and CheckBox to the TableRow
                                tableRow.addView(checkBoxAction);
                                tableRow.addView(textViewProduct);
                                tableRow.addView(textViewAmount);
                                tableRow.addView(textViewQuantity);
                                tableRow.addView(textViewTotal);

                                // Add the TableRow to the TableLayout
                                tableLayoutItems.addView(tableRow);
                            }

                        });

                    } else {
                        // Handle the error response here
                        System.out.println("Request was not successful: " + response);
                        assert response.body() != null;
                        System.out.println(response.body().string());
                        System.out.println(response.message());
                        System.out.println(response.code());
                        throw new Exception("Request was not successful: " + response);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    TextView textViewError = requireView().findViewById(R.id.textViewError);
                    textViewError.setVisibility(View.VISIBLE);
                });

            }
            requireActivity().runOnUiThread(() -> imageViewLoading.setVisibility(View.GONE));

        }).start();
    }

    // Utility method to create a TextView with text wrapping
    private TextView createTextViewWithWrap(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setSingleLine(false);
        textView.setMaxLines(3);

        // Set the layout parameters with weight
        TableRow.LayoutParams params = new TableRow.LayoutParams();
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);
        return textView;
    }


}
