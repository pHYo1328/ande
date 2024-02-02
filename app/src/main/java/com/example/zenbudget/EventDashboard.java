package com.example.zenbudget;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.zenbudget.classes.Event;
import com.example.zenbudget.classes.Expense;
import com.example.zenbudget.classes.SubEvent;
import com.example.zenbudget.utils.FirestoreUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class EventDashboard extends Fragment {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String event_id;
    Event event;
    List<Expense> expenses;
    List<ExpenseRecyclerItem> expenseRecyclerItems;
    List<SubEventRecyclerItem> subEventRecyclerItems;
    List<SubEvent> subEvents;
    TextView txtEventName, txtEventStartDate, txtEventEndDate, txtBudget, txtSpent, txtRemaining, txtNoExpenses, txtNoSubEvents;
    LinearLayout layoutBack;
    ImageView imgDashboard;
    PieChart mPieChart;
    FragmentContainerView containerExpenses, containerSubEvents;


    private CountDownLatch latch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        event_id = getArguments().getString("eventId");

        layoutBack = view.findViewById(R.id.layoutBack);
        layoutBack.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new HomeFragment()).commit();
        });

        txtEventName = view.findViewById(R.id.txtEventName);
        txtEventStartDate = view.findViewById(R.id.txtEventStartDate);
        txtEventEndDate = view.findViewById(R.id.txtEventEndDate);
        imgDashboard = view.findViewById(R.id.imgDashboard);

        txtBudget = view.findViewById(R.id.txtBudget);
        txtSpent = view.findViewById(R.id.txtSpent);
        txtRemaining = view.findViewById(R.id.txtRemaining);
        txtNoExpenses = view.findViewById(R.id.txtNoExpenses);
        txtNoSubEvents = view.findViewById(R.id.txtNoSubEvents);

        containerExpenses = view.findViewById(R.id.container_expenses);
        containerSubEvents = view.findViewById(R.id.container_subevents);

        mPieChart = view.findViewById(R.id.piechart);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        latch = new CountDownLatch(3);

        retrieveEvents();
        retrieveExpenses();
        retrieveSubEvents();

        // Now, wait for both tasks to complete before executing further code
        new Thread(() -> {
            try {
                latch.await(); // This will block until latch count reaches zero


                getActivity().runOnUiThread(() -> {
                    if (expenses.size() > 0) {
                        txtNoExpenses.setVisibility(View.GONE);

                        ExpensesFragment f = new ExpensesFragment();
                        Bundle expenseArgs = new Bundle();
                        expenseArgs.putSerializable("expenses", (ArrayList<ExpenseRecyclerItem>) this.expenseRecyclerItems);
                        f.setArguments(expenseArgs);
                        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                        transaction.replace(R.id.container_expenses, f);
                        transaction.commit();
                    } else {
                        txtNoExpenses.setText("You have no expenses.");
                        containerExpenses.setVisibility(View.GONE);
                    }

                    if (subEvents.size() > 0) {
                        txtNoSubEvents.setVisibility(View.GONE);

                        SubEventsFragment f = new SubEventsFragment();
                        Bundle subEventArgs = new Bundle();
                        subEventArgs.putSerializable("subEvents", (ArrayList<SubEventRecyclerItem>) this.subEventRecyclerItems);
                        f.setArguments(subEventArgs);
                        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                        transaction.replace(R.id.container_subevents, f);
                        transaction.commit();
                    } else {
                        txtNoSubEvents.setText("You have no sub events.");
                        containerSubEvents.setVisibility(View.GONE);
                    }


                    // Event Details
                    Glide.with(this)
                            .load(event.getImgUrl())
                            .apply(new RequestOptions().centerCrop()
                            ).into(imgDashboard);

                    txtEventName.setText(event.getEventName());
                    txtEventStartDate.setText(FirestoreUtils.formatDate(event.getStartDate()));
                    txtEventEndDate.setText(FirestoreUtils.formatDate(event.getEndDate()));

                    // Dashboard
                    double budget = event.getBudget();
                    for (SubEvent se : subEvents) {
                        budget += se.getSubEvent_budget();
                    }
                    txtBudget.setText(String.format("%.2f", budget));


                    double spent = 0;
                    for (Expense e : expenses) {
                        spent += e.getAmountDouble();
                    }


                    if (spent > budget) {
                        txtRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.theme_danger));
                    } else {
                        txtRemaining.setTextColor(ContextCompat.getColor(requireContext(), R.color.theme_success));
                    }

                    txtSpent.setText(String.format("%.2f", spent));
                    txtRemaining.setText(String.format("%.2f", budget - spent));


                    Map<String, Double> expenseMap = new HashMap<>();
                    for (Expense e : expenses) {
                        if (e.getEvent() != null) {
                            Double accExpense = expenseMap.get(e.getCategory());
                            if (accExpense != null)
                                expenseMap.put(e.getCategory(), accExpense + e.getAmountDouble());
                            else {
                                expenseMap.put(e.getCategory(), e.getAmountDouble());
                            }
                        }

                    }

                    generatePieChart(expenseMap);

                    Log.d("EventDashboard", "onViewCreated: " + expenseMap);

                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void retrieveSubEvents() {
        subEvents = new ArrayList<>();
        subEventRecyclerItems = new ArrayList<>();
        db.collection("events")
                .document(event_id)
                .collection("subEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            SubEvent se = document.toObject(SubEvent.class);
                            subEvents.add(se);
                            subEventRecyclerItems.add(new SubEventRecyclerItem(se, document.getId()));
                        }

                        latch.countDown();
                    } else {
                        Log.d("EventDashboard", "onViewCreated: " + task.getException());
                    }
                });
    }

    private void retrieveExpenses() {
        expenses = new ArrayList<>();
        expenseRecyclerItems = new ArrayList<>();
        db.collection("expenses")
                .whereEqualTo("event", event_id)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Expense e = document.toObject(Expense.class);
                            Log.d("EventDashboard", "onViewCreated: " + e.getEvent());
                            expenses.add(e);
                            expenseRecyclerItems.add(new ExpenseRecyclerItem(e, document.getId()));
                        }

                        latch.countDown();
                    } else {
                        Log.d("EventDashboard", "onViewCreated: " + task.getException());
                    }
                });
    }

    private void retrieveEvents() {
        db.collection("events")
                .document(event_id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    event = documentSnapshot.toObject(Event.class);

                    latch.countDown();
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load event", Toast.LENGTH_SHORT).show();
                    layoutBack.performClick();
                })
        ;
    }

    private void generatePieChart(Map<String, Double> expenseByCategoryMap) {
        // Create a list from the entries of the map
        List<Map.Entry<String, Double>> entryList = new ArrayList<>(expenseByCategoryMap.entrySet());

        TextView txtNoData = getActivity().findViewById(R.id.txtNoData);
        if (expenseByCategoryMap.isEmpty()) {
            LinearLayout layoutPieChart = getActivity().findViewById(R.id.layoutChart);
            layoutPieChart.setVisibility(View.GONE);
            txtNoData.setText("No data available.");
        } else {
            txtNoData.setVisibility(View.GONE);
        }

        // Sort the list in descending order based on values
        entryList.sort(Map.Entry.<String, Double>comparingByValue().reversed());

        Map<String, Double> others = new HashMap<>();
        if (entryList.size() > 3) {
            for (int i = 3; i < entryList.size(); i++) {
                Map.Entry<String, Double> entry = entryList.get(i);

                Double accOthers = others.get("Others");
                if (accOthers != null)
                    others.put("Others", accOthers + entry.getValue());
                else {
                    others.put("Others", entry.getValue());
                }
            }
            entryList.subList(3, entryList.size()).clear();
            entryList.add(others.entrySet().iterator().next());
        }


        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(requireContext(), R.color.pie_cat_1));
        colors.add(ContextCompat.getColor(requireContext(), R.color.pie_cat_2));
        colors.add(ContextCompat.getColor(requireContext(), R.color.pie_cat_3));
        colors.add(ContextCompat.getColor(requireContext(), R.color.pie_cat_others));

        ArrayList<TextView> chartLabels = new ArrayList<>();
        chartLabels.add(getActivity().findViewById(R.id.txtCatOne));
        chartLabels.add(getActivity().findViewById(R.id.txtCatTwo));
        chartLabels.add(getActivity().findViewById(R.id.txtCatThree));
        chartLabels.add(getActivity().findViewById(R.id.txtCatOthers));

        ArrayList<LinearLayout> labelLayouts = new ArrayList<>();
        labelLayouts.add(getActivity().findViewById(R.id.layoutCatOne));
        labelLayouts.add(getActivity().findViewById(R.id.layoutCatTwo));
        labelLayouts.add(getActivity().findViewById(R.id.layoutCatThree));
        labelLayouts.add(getActivity().findViewById(R.id.layoutCatOthers));

        for (int i = 0; i < labelLayouts.size(); i++) {
            labelLayouts.get(i).setVisibility(View.GONE);
        }


        for (Map.Entry<String, Double> entry : entryList) {
            labelLayouts.get(entryList.indexOf(entry)).setVisibility(View.VISIBLE);
            chartLabels.get(entryList.indexOf(entry)).setText(entry.getKey());
            mPieChart.addPieSlice(
                    new PieModel(
                            entry.getKey(),
                            (int) Math.round(entry.getValue()),
                            Color.parseColor("#" + Integer.toHexString(colors.get(entryList.indexOf(entry))).substring(2))));
        }


        mPieChart.startAnimation();


    }

}
