package com.example.zenbudget;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenbudget.classes.Event;
import com.example.zenbudget.classes.Expense;
import com.example.zenbudget.classes.SubEvent;
import com.example.zenbudget.classes.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class HomeFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    CountDownLatch latch;
    FirebaseUser currentUser = mAuth.getCurrentUser();


    Double totalSpent = 0.0;
    private RecyclerView recyclerView;
    private HomeViewAdapter adapter;
    private List<HomeRecyclerItem> items;

    ArrayList<HomeRecyclerItem> current = new ArrayList<>();
    ArrayList<HomeRecyclerItem> upcoming = new ArrayList<>();

    private final List<Event> myEvents = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView txtWelcome = view.findViewById(R.id.txtWelcome);
        items = new ArrayList<>();

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    String text = "Welcome back, " + user.getFirst_name();
                    SpannableString spannableString = new SpannableString(text);
                    spannableString.setSpan(new AbsoluteSizeSpan(25, true), 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new AbsoluteSizeSpan(25, true), 15, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtWelcome.setText(spannableString);
                });



        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        // events done
        adapter = new HomeViewAdapter(items, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        latch = new CountDownLatch(1);

        retrieveEvents();

        new Thread(() -> {
            try {
                latch.await();

                getActivity().runOnUiThread(() -> {
                    Log.d("HomeFragment", "Items: " + myEvents.size());
                    Log.d("HomeFragment", "Current: " + current.size());
                    Log.d("HomeFragment", "Upcoming: " + upcoming.size());

                    for (Event e : myEvents) {
                        AtomicReference<Double> totalSpent = new AtomicReference<>(0.0);
                        AtomicReference<Double> totalBudget = new AtomicReference<>(e.getBudget());

                        Log.d("HomeFragment", "Event: " + e.getEventName() + " " + e.getId());

                        db.collection("expenses")
                                .whereEqualTo("event", e.getId())
                                .get()
                                .addOnCompleteListener(expenseTask -> {
                                    if (expenseTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot expenseDoc : expenseTask.getResult()) {
                                            Expense expense = expenseDoc.toObject(Expense.class);
                                            Double amount = expense.getDoubleAmount();
                                            totalSpent.updateAndGet(v -> v + amount);
                                        }

                                        db.collection("events")
                                                .document(e.getId())
                                                .collection("subEvents")
                                                .get()
                                                .addOnCompleteListener(subEventTask -> {
                                                    if (subEventTask.isSuccessful()) {
                                                        for (QueryDocumentSnapshot subEventDoc : subEventTask.getResult()) {
                                                            SubEvent subEvent = subEventDoc.toObject(SubEvent.class);
                                                            totalBudget.updateAndGet(v -> v + subEvent.getSubEvent_budget());

                                                        }

                                                        LocalDate startDate = LocalDate.parse(e.getStartDate());
                                                        LocalDate endDate = LocalDate.parse(e.getEndDate());
                                                        LocalDate today = LocalDate.now();

                                                        Double totalS = totalSpent.get();
                                                        Double totalB = totalBudget.get();
                                                        if (today.isAfter(startDate) && today.isBefore(endDate)) {
                                                            current.add(new HomeRecyclerItem(e, e.getId(), totalS, totalB));
                                                        } else {
                                                            upcoming.add(new HomeRecyclerItem(e, e.getId(), totalS, totalB));
                                                        }

                                                        items.clear();
                                                        items.add(new HomeRecyclerItem(HomeRecyclerItem.TYPE_CURRENT));
                                                        items.addAll(current);
                                                        items.add(new HomeRecyclerItem(HomeRecyclerItem.TYPE_UPCOMING));
                                                        items.addAll(upcoming);

                                                        adapter.notifyDataSetChanged();
                                                    } else {
                                                        Toast.makeText(getActivity(), "Error getting subevents: " + subEventTask.getException(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });

                                    } else {
                                        Toast.makeText(getActivity(), "Error getting expenses: " + expenseTask.getException(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        return view;
    }

    private void retrieveEvents() {
        db.collection("events")
                .whereEqualTo("userID", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setId(document.getId());


                            myEvents.add(event);
                            latch.countDown();
                        }

                    }
                });
    }
}
