package com.example.andeca1;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andeca1.classes.User;
import com.example.andeca1.utils.Provider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private RecyclerView recyclerView;
    private recyclerViewAdapter adapter;
    private List<recycleItem> items;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView txtWelcome = view.findViewById(R.id.txtWelcome);
        FirebaseUser currentUser = mAuth.getCurrentUser();

        Log.d("HomeFragment", "onCreateView: " + Provider.Determine());

        db.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    String text = "Welcome back, \n" + user.getFirst_name();
                    SpannableString spannableString = new SpannableString(text);
                    spannableString.setSpan(new AbsoluteSizeSpan(25, true), 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new AbsoluteSizeSpan(25, true), 15, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    txtWelcome.setText(spannableString);
                });


        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd", java.util.Locale.getDefault());
        items = new ArrayList<>();

        try {
            Date date1 = inputFormat.parse("2023-10-01");
            Date date2 = inputFormat.parse("2023-11-15");
            Date date3 = inputFormat.parse("2023-12-20");

            assert date1 != null;
            String formattedDate1 = outputFormat.format(date1);
            assert date2 != null;
            String formattedDate2 = outputFormat.format(date2);
            assert date3 != null;
            String formattedDate3 = outputFormat.format(date3);

            items.add(new recycleItem("October", 90, formattedDate1, R.drawable.oct, "$69.20 Left"));
            items.add(new recycleItem("Trip to Japan", 100, formattedDate2, R.drawable.jp, "$400.60"));
            items.add(new recycleItem("December", 100, formattedDate3, R.drawable.jp, "$400.60"));
            items.add(new recycleItem("December", 100, formattedDate3, R.drawable.jp, "$400.60"));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        adapter = new recyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
