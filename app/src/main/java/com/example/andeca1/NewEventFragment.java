package com.example.andeca1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class NewEventFragment extends Fragment {
    private Button addSubEventButton;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_event, container, false);

        addSubEventButton = view.findViewById(R.id.btnAddSubEvent);
        addSubEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewSubEventFragment newSubEventFragment = new NewSubEventFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.content_frame, newSubEventFragment);
                transaction.commit();
            }
        });
        return view;
    }
}
