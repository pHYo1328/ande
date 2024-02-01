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
import com.example.zenbudget.classes.User;
import com.example.zenbudget.utils.Provider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private RecyclerView recyclerView;
    private HomeViewAdapter adapter;
    private List<HomeRecyclerItem> items;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView txtWelcome = view.findViewById(R.id.txtWelcome);
        FirebaseUser currentUser = mAuth.getCurrentUser();
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

        db.collection("events")
                .whereEqualTo("userID", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<HomeRecyclerItem> current = new ArrayList<>();
                        ArrayList<HomeRecyclerItem> upcoming = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            LocalDate startDate = LocalDate.parse(event.getStartDate());
                            LocalDate endDate = LocalDate.parse(event.getEndDate());
                            LocalDate today = LocalDate.now();

                            if (today.isAfter(startDate) && today.isBefore(endDate)) {
                                current.add(new HomeRecyclerItem(event, document.getId()));
                            } else {
                                upcoming.add(new HomeRecyclerItem(event, document.getId()));
                            }
                        }

                        items.add(new HomeRecyclerItem(HomeRecyclerItem.TYPE_CURRENT));
                        items.addAll(current);
                        items.add(new HomeRecyclerItem(HomeRecyclerItem.TYPE_UPCOMING));
                        items.addAll(upcoming);

                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getActivity(), "Error getting events: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });


        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        adapter = new HomeViewAdapter(items, requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(adapter);

        return view;
    }
}
