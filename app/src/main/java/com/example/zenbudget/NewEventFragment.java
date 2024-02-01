package com.example.zenbudget;

import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.zenbudget.utils.FirestoreUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewEventFragment extends Fragment {
    private EditText editEventTitle, editBudget;
    private TextView selectedDateStr;
    private Date startDate;
    private Date endDate;
    private static final String ACCESS_KEY = "lWdTZql7q7NmIpmsmnS7ShJb3nJuucYcAIci0u277bM";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_event, container, false);

        Button addSubEventButton = view.findViewById(R.id.btnAddSubEvent);
        Button saveButton = view.findViewById(R.id.btnSaveEvent);
        editEventTitle = view.findViewById(R.id.editTxtEventTitle);
        editBudget = view.findViewById(R.id.editTxtBudget);
        CalendarView calendarView = view.findViewById(R.id.calendarViewNewEvent);
        selectedDateStr = view.findViewById(R.id.selected_date);
        ImageButton closeButton = view.findViewById(R.id.exit_selection_mode);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat dateFormatForDB = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(android.icu.util.Calendar.YEAR), calendar.get(android.icu.util.Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        selectedDateStr.setText(dateFormat.format(calendar.getTime()));
        endDate = calendar.getTime();

        String startDateStr = null;
        if (getArguments() != null) {
            startDateStr = getArguments().getString("selectedDate");
            try {
                assert startDateStr != null;
                startDate = dateFormatForDB.parse(startDateStr);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);
            String formattedDate = dateFormat.format(selectedDate.getTime());
            endDate = selectedDate.getTime();
            selectedDateStr.setText(formattedDate);
        });
        String finalStartDateStr = startDateStr;

        addSubEventButton.setOnClickListener(view12 -> {
            String eventTitle, budget;
            eventTitle = editEventTitle.getText().toString().trim();
            budget = editBudget.getText().toString().trim();
            double budgetValue;
            try {
                budgetValue = Double.parseDouble(budget);
                if (budgetValue <= 0) {
                    throw new NumberFormatException("Amount must be greater than zero");
                }
            } catch (NumberFormatException e) {
                editBudget.setError("Please enter a valid amount");
                return;
            }

            if (eventTitle.isEmpty()) {
                editEventTitle.setError("Please enter a event title");
                return;
            }
            if (endDate.before(startDate)) {
                Toast.makeText(getContext(), "Event end date shouldn't before start date", Toast.LENGTH_LONG).show();
                return;
            }
            String endDateStr = dateFormatForDB.format(endDate.getTime());


            FirestoreUtils.createEvent(eventTitle, finalStartDateStr, endDateStr, budgetValue, new FirestoreUtils.FirestoreCallback<String>() {
                @Override
                public void onSuccess(String eventId) {
                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", eventId);
                    bundle.putString("startDate", finalStartDateStr);
                    bundle.putString("endDate", endDateStr);
                    NewSubEventFragment newSubEventFragment = new NewSubEventFragment();
                    newSubEventFragment.setArguments(bundle);
                    FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, newSubEventFragment);
                    transaction.commit();
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(getContext(), "fail to create event", Toast.LENGTH_LONG).show();
                }
            });

        });

        saveButton.setOnClickListener(view1 -> {
            String eventTitle, budget;
            eventTitle = editEventTitle.getText().toString().trim();
            budget = editBudget.getText().toString().trim();
            double budgetValue;
            try {
                budgetValue = Double.parseDouble(budget);
                if (budgetValue <= 0) {
                    throw new NumberFormatException("Amount must be greater than zero");
                }
            } catch (NumberFormatException e) {
                editBudget.setError("Please enter a valid amount");
                return;
            }

            if (eventTitle.isEmpty()) {
                editEventTitle.setError("Please enter a event title");
                return;
            }

            if (endDate.before(startDate)) {
                Toast.makeText(getContext(), "Event end date shouldn't before start date", Toast.LENGTH_LONG).show();
                return;
            }
            String endDateStr = dateFormatForDB.format(endDate.getTime());


            OkHttpClient client = new OkHttpClient();
            String baseUrl = "https://api.unsplash.com/photos/random";
            String count = "1";
            String orientation = "landscape";
            String query = "bali";

            StringBuilder apiUrlBuilder = new StringBuilder(baseUrl);
            apiUrlBuilder.append("?client_id=").append(ACCESS_KEY);
            apiUrlBuilder.append("&count=").append(count);
            apiUrlBuilder.append("&orientation=").append(orientation);
            apiUrlBuilder.append("&query=").append(query);

            String apiUrl = apiUrlBuilder.toString();

            Request request = new Request.Builder()
                    .url(apiUrl)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Gson gson = new Gson();
                        String responseBody = response.body().string();
                        JsonArray res = JsonParser.parseString(responseBody).getAsJsonArray();
                        JsonObject obj = res.get(0).getAsJsonObject();
                        JsonObject urls = obj.get("urls").getAsJsonObject();
                        String url = urls.get("regular").getAsString();

                        FirestoreUtils.createEvent(eventTitle, finalStartDateStr, endDateStr, budgetValue, url, new FirestoreUtils.FirestoreCallback<String>() {
                            @Override
                            public void onSuccess(String eventId) {
                                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                                transaction.replace(R.id.content_frame, new EventFragment());
                                transaction.commit();
                            }

                            @Override
                            public void onError(Exception e) {
                                Toast.makeText(getContext(), "fail to create event", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    e.printStackTrace();
                }
            });
        });

        closeButton.setOnClickListener(view1 -> getParentFragmentManager().beginTransaction().replace(R.id.content_frame, new EventFragment()).commit());
        return view;
    }
}
