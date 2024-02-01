package com.example.zenbudget;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SubEventsFragment extends Fragment {
    List<SubEventRecyclerItem> subEvents;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recycler, container, false);
        subEvents = (List<SubEventRecyclerItem>) getArguments().getSerializable("subEvents");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView subEventsRecycler = view.findViewById(R.id.recyclerView);
        SubEventAdapter adapter = new SubEventAdapter(subEvents, getActivity().getSupportFragmentManager());
        subEventsRecycler.setAdapter(adapter);
        subEventsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));

    }
}
