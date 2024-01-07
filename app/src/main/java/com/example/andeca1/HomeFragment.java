package com.example.andeca1;

import android.os.Bundle;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private recyclerViewAdapter adapter;
    private List<recycleItem> items;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView textView = view.findViewById(R.id.textView2);
        String text = "Welcome Back,\nJohn";

        SpannableString spannableString = new SpannableString(text);
        spannableString.setSpan(new AbsoluteSizeSpan(35, true), 0, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new AbsoluteSizeSpan(25, true), 15, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd");
        items = new ArrayList<>();

        try {
            Date date1 = inputFormat.parse("2023-10-01");
            Date date2 = inputFormat.parse("2023-11-15");
            Date date3 = inputFormat.parse("2023-12-20");

            String formattedDate1 = outputFormat.format(date1);
            String formattedDate2 = outputFormat.format(date2);
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
