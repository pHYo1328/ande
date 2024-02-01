package com.example.zenbudget;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zenbudget.ExpenseAdapter;
import com.example.zenbudget.ExpenseRecyclerItem;

import java.util.List;

public class ExpensesFragment extends Fragment {
    List<ExpenseRecyclerItem> expenses;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler, container, false);
        expenses = (List<ExpenseRecyclerItem>) getArguments().getSerializable("expenses");

        Log.d("ExpensesFragment", "onCreateView: " + expenses.size());

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView expenseRecycler = view.findViewById(R.id.recyclerExpenses);
        ExpenseAdapter adapter = new ExpenseAdapter(expenses, getActivity().getSupportFragmentManager());
        expenseRecycler.setAdapter(adapter);
        expenseRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

    }
}
